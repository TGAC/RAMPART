package uk.ac.tgac.rampart.stage.analyse.asm.analysers;

import org.apache.commons.io.FileUtils;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishCountV11;
import uk.ac.tgac.conan.process.kmer.kat.KatCompV1;
import uk.ac.tgac.conan.process.kmer.kat.KatPlotSpectraCnV1;
import uk.ac.tgac.rampart.stage.analyse.asm.AnalyseAssembliesArgs;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStatsTable;
import uk.ac.tgac.rampart.util.JobOutput;
import uk.ac.tgac.rampart.util.JobOutputList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 22/01/14
 * Time: 11:19
 * To change this template use File | Settings | File Templates.
 */
@MetaInfServices(AssemblyAnalyser.class)
public class KatAsmAnalyser extends AbstractConanProcess implements AssemblyAnalyser {

    private static Logger log = LoggerFactory.getLogger(KatAsmAnalyser.class);

    @Override
    public boolean isOperational(ExecutionContext executionContext) {
        boolean jellyfish = new JellyfishCountV11(this.conanExecutorService).isOperational(executionContext);

        KatCompV1 katCompProc = new KatCompV1();
        katCompProc.setConanProcessService(this.getConanProcessService());
        boolean katComp = katCompProc.isOperational(executionContext);

        KatPlotSpectraCnV1 katPlotSpectraCn = new KatPlotSpectraCnV1();
        katPlotSpectraCn.setConanProcessService(this.getConanProcessService());
        boolean katPlot = katPlotSpectraCn.isOperational(executionContext);

        return jellyfish && katComp && katPlot;
    }

    @Override
    public List<Integer> execute(List<File> assemblies, File outputDir, String jobPrefix, AnalyseAssembliesArgs args, ConanExecutorService ces)
            throws InterruptedException, ProcessExecutionException, ConanParameterException, IOException {

        List<Integer> jobIds = new ArrayList<>();
        List<File> jellyfishHashes = new ArrayList<>();


        if (outputDir.exists()) {
            FileUtils.deleteDirectory(outputDir);
        }

        outputDir.mkdirs();

        int i = 1;
        for(File f : assemblies) {

            String jfJobName = jobPrefix + "-jfcount-" + i++;

            // Should be ok to assume that the extension is .fa as we should be working with symbolic links that we
            // generated ourselves
            File katOutputDir = new File(outputDir, f.getName().substring(0, f.getName().length() - 3));

            if (katOutputDir.exists()) {
                FileUtils.deleteDirectory(katOutputDir);
            }
            katOutputDir.mkdirs();

            String outputPrefix = katOutputDir.getAbsolutePath() + "/" + jfJobName + ".jf31";

            // Setup Jellyfish for counting the assembly
            JellyfishCountV11.Args jellyfishArgs = new JellyfishCountV11.Args();
            jellyfishArgs.setOutputPrefix(outputPrefix);
            jellyfishArgs.setLowerCount(0);  // Count everything!
            jellyfishArgs.setHashSize(args.getOrganism().getEstGenomeSize() * args.getOrganism().getPloidy() * 10);
            jellyfishArgs.setMerLength(31);       // 31 should be more than sufficient for all organisms (even wheat)
            jellyfishArgs.setBothStrands(true);
            jellyfishArgs.setThreads(args.getThreadsPerProcess());
            jellyfishArgs.setCounterLength(32); // This is probably overkill... consider changing this later (or using jellyfish
            // 2 which auto adjusts this figure)
            jellyfishArgs.setInputFile(f.getAbsolutePath());

            JellyfishCountV11 jellyfishProcess = new JellyfishCountV11(this.conanExecutorService, jellyfishArgs);

            // Execute kmer count and comparison
            ExecutionResult resultCount = ces.executeProcess(
                    jellyfishProcess,
                    new File(outputPrefix).getParentFile(),
                    jfJobName,
                    args.getThreadsPerProcess(),
                    0,  //TODO Probably should work out something sensible to put here
                    args.isRunParallel());

            jellyfishHashes.add(new File(outputPrefix + "_0"));

            // Add job id to list
            jobIds.add(resultCount.getJobId());
        }

        // If we're using a scheduler and we have been asked to run each MECQ group for each library
        // in parallel, then we should wait for all those to complete before continueing.
        if (ces.usingScheduler() && args.isRunParallel()) {
            log.debug("Analysing assemblies using kmers in parallel, waiting for completion");
            ces.executeScheduledWait(
                    jobIds,
                    args.getJobPrefix() + "-kmer-count-*",
                    ExitStatus.Type.COMPLETED_ANY,
                    args.getJobPrefix() + "-k-count-wait",
                    args.getOutputDir());
        }

        jobIds.clear();

        JobOutputList outputList = new JobOutputList();

        Collection<File> readCounts = FileUtils.listFiles(args.getAnalyseReadsDir(), new String[] {"jf31_0"}, true);

        // Loop through jellyfish hashes
        i = 1;
        for(File jfHash : jellyfishHashes) {

            // Loop through each read count for this assembly
            int j = 1;
            for(File readCount : readCounts) {

                String outputPrefix = "katcomp-" + i + "-" + j;
                String jobName = jobPrefix + "-" + outputPrefix;

                // Setup kat comp
                KatCompV1.Args katCompArgs = new KatCompV1.Args();
                katCompArgs.setJellyfishHash1(readCount);
                katCompArgs.setJellyfishHash2(jfHash);
                katCompArgs.setOutputPrefix(new File(jfHash.getParentFile(), outputPrefix).getAbsolutePath());
                katCompArgs.setThreads(args.getThreadsPerProcess());

                KatCompV1 katCompProcess = new KatCompV1(katCompArgs);

                ExecutionResult result = ces.executeProcess(
                        katCompProcess,
                        jfHash.getParentFile(),
                        jobName,
                        args.getThreadsPerProcess(),
                        0,
                        args.isRunParallel());

                outputList.add(new JobOutput(result.getJobId(), new File(katCompArgs.getOutputPrefix() + "_main.mx")));
            }
        }


        // If we're using a scheduler and we have been asked to run each MECQ group for each library
        // in parallel, then we should wait for all those to complete before continueing.
        if (ces.usingScheduler() && args.isRunParallel()) {
            log.debug("Analysing assemblies using kmers in parallel, waiting for completion");
            ces.executeScheduledWait(
                    outputList.getJobIds(),
                    args.getJobPrefix() + "-katcomp*",
                    ExitStatus.Type.COMPLETED_ANY,
                    args.getJobPrefix() + "-katcomp-wait",
                    args.getOutputDir());
        }

        // Create plots
        i = 1;
        for(File matrix : outputList.getFiles()) {
            // Setup kat comp
            KatPlotSpectraCnV1.Args katPlotSpectraCnArgs = new KatPlotSpectraCnV1.Args();
            katPlotSpectraCnArgs.setInput(matrix);
            katPlotSpectraCnArgs.setOutput(new File(matrix.getAbsolutePath() + ".png"));

            KatPlotSpectraCnV1 katPlotSpectraCnProcess = new KatPlotSpectraCnV1(katPlotSpectraCnArgs);

            ExecutionResult result = ces.executeProcess(
                    katPlotSpectraCnProcess,
                    matrix.getParentFile(),
                    args.getJobPrefix() + "-kat-plot-spectra-cn-" + i++,
                    1,
                    0,
                    args.isRunParallel());
        }

        // Just let these run.  They shouldn't take long and no processes are dependent on them downstream

        return new ArrayList<>();
    }

    @Override
    public void updateTable(AssemblyStatsTable table, List<File> assemblies, File reportDir, String subGroup) {

    }

    @Override
    public boolean isFast() {
        return false;
    }

    @Override
    public String getName() {
        return "KAT";
    }
}
