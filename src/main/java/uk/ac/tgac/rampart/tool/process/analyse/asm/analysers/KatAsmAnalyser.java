package uk.ac.tgac.rampart.tool.process.analyse.asm.analysers;

import org.apache.commons.io.FileUtils;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishCountV11;
import uk.ac.tgac.conan.process.kmer.kat.KatCompV1;
import uk.ac.tgac.conan.process.kmer.kat.KatPlotSpectraCnV1;
import uk.ac.tgac.rampart.tool.process.analyse.asm.AnalyseAsmsArgs;
import uk.ac.tgac.rampart.tool.process.analyse.asm.AnalyseAsmsExecutor;
import uk.ac.tgac.rampart.tool.process.analyse.asm.AnalyseAsmsProcess;
import uk.ac.tgac.rampart.tool.process.analyse.asm.stats.AssemblyStats;
import uk.ac.tgac.rampart.tool.process.analyse.asm.stats.AssemblyStatsTable;
import uk.ac.tgac.rampart.tool.process.mass.single.SingleMassArgs;
import uk.ac.tgac.rampart.util.JobOutput;
import uk.ac.tgac.rampart.util.JobOutputList;
import uk.ac.tgac.rampart.util.JobOutputMap;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
        JellyfishCountV11 proc = new JellyfishCountV11();
        proc.setConanProcessService(this.conanProcessService);
        boolean jellyfish = proc.isOperational(executionContext);

        KatCompV1 katCompProc = new KatCompV1();
        katCompProc.setConanProcessService(this.conanProcessService);
        boolean katComp = katCompProc.isOperational(executionContext);

        KatPlotSpectraCnV1 katPlotSpectraCn = new KatPlotSpectraCnV1();
        katPlotSpectraCn.setConanProcessService(this.conanProcessService);
        boolean katPlot = katPlotSpectraCn.isOperational(executionContext);

        return jellyfish && katComp && katPlot;
    }

    @Override
    public boolean execute(AnalyseAsmsArgs args, ExecutionContext executionContext, AnalyseAsmsExecutor analyseAsmsExecutor) throws InterruptedException, ProcessExecutionException, ConanParameterException, IOException {
        List<Integer> jobIds = new ArrayList<>();
        JobOutputMap assemblyCounts = new JobOutputMap();

        // Loop through MASS groups
        for(SingleMassArgs singleMassArgs : args.getMassGroups()) {

            String massGroup = singleMassArgs.getName();

            File inputDir = new File(args.getMassDir(), massGroup);

            if (!inputDir.exists()) {
                throw new ProcessExecutionException(-1, "Could not find output from mass group: " + massGroup + "; at: " + inputDir.getAbsolutePath());
            }

            // Loop through output levels
            File unitigsDir = new File(inputDir, "unitigs");
            File contigsDir = new File(inputDir, "contigs");
            File scaffoldsDir = new File(inputDir, "scaffolds");

            File seqDir = null;

            // We only do the longest sequence types... KAT will take a while to run through all permuations and
            // we won't get any useful info by running through all sequence types
            if (scaffoldsDir.exists()) {
                seqDir = scaffoldsDir;
            }
            else if (contigsDir.exists()) {
                seqDir = contigsDir;
            }
            else if (unitigsDir.exists()) {
                seqDir = unitigsDir;
            }
            else {
                throw new ProcessExecutionException(-2, "Could not find any output sequences for this mass group: " + massGroup);
            }

            List<File> assemblies = AnalyseAsmsProcess.assembliesFromDir(seqDir);

            File groupOutputDir = new File(args.getOutputDir(), massGroup);
            if (groupOutputDir.exists()) {
                FileUtils.deleteDirectory(groupOutputDir);
            }

            File kmerOutputDir = new File(groupOutputDir, "kmer");
            kmerOutputDir.mkdirs();

            int i = 1;
            for(File f : assemblies) {

                String kmerJobName = args.getJobPrefix() + "-kmer-count-" + massGroup + "-" + i++;

                File outputDir = new File(kmerOutputDir, f.getName().substring(0, f.getName().length() - 3));
                if (outputDir.exists()) {
                    FileUtils.deleteDirectory(outputDir);
                }
                outputDir.mkdir();

                String outputPrefix = outputDir.getAbsolutePath() + "/" + kmerJobName + ".jf31";

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

                JellyfishCountV11 jellyfishProcess = new JellyfishCountV11(jellyfishArgs);

                // Execute kmer count and comparison
                ExecutionResult resultCount = analyseAsmsExecutor.executeKmerCount(
                        jellyfishProcess, executionContext, kmerJobName, args.isRunParallel());

                assemblyCounts.updateTracker(massGroup, new File(outputPrefix + "_0"));

                // Add job id to list
                jobIds.add(resultCount.getJobId());
            }
        }

        // If we're using a scheduler and we have been asked to run each MECQ group for each library
        // in parallel, then we should wait for all those to complete before continueing.
        if (executionContext.usingScheduler() && args.isRunParallel()) {
            log.debug("Analysing assemblies using kmers in parallel, waiting for completion");
            analyseAsmsExecutor.executeScheduledWait(
                    jobIds,
                    args.getJobPrefix() + "-kmer-count-*",
                    ExitStatus.Type.COMPLETED_ANY,
                    args.getJobPrefix() + "-k-count-wait",
                    args.getOutputDir());
        }

        jobIds.clear();

        JobOutputList outputList = new JobOutputList();

        Collection<File> readCounts = FileUtils.listFiles(args.getAnalyseReadsDir(), new String[] {"jf31_0"}, true);

        // Loop through MASS groups
        for(Map.Entry<String,Set<File>> entry : assemblyCounts.entrySet()) {

            String massGroup = entry.getKey();

            File kmerOutputDir = new File(args.getOutputDir(), massGroup + "/kmer");

            // Loop through each assembly in this MASS group
            for(File assemblyCount : entry.getValue()) {

                // Loop through each read count for this assembly
                for(File readCount : readCounts) {

                    String outputPrefix = "kat-comp-" + readCount.getName();
                    String jobName = args.getJobPrefix() + "kmer-compare-" + outputPrefix;

                    // Setup kat comp
                    KatCompV1.Args katCompArgs = new KatCompV1.Args();
                    katCompArgs.setJellyfishHash1(readCount);
                    katCompArgs.setJellyfishHash2(assemblyCount);
                    katCompArgs.setOutputPrefix(new File(assemblyCount.getParentFile(), outputPrefix).getAbsolutePath());
                    katCompArgs.setThreads(args.getThreadsPerProcess());

                    KatCompV1 katCompProcess = new KatCompV1(katCompArgs);

                    ExecutionResult result = analyseAsmsExecutor.executeKatComp(
                            katCompProcess, executionContext, jobName, args.isRunParallel());

                    outputList.add(new JobOutput(result.getJobId(), new File(katCompArgs.getOutputPrefix() + "_main.mx")));
                }
            }
        }


        // If we're using a scheduler and we have been asked to run each MECQ group for each library
        // in parallel, then we should wait for all those to complete before continueing.
        if (executionContext.usingScheduler() && args.isRunParallel()) {
            log.debug("Analysing assemblies using kmers in parallel, waiting for completion");
            analyseAsmsExecutor.executeScheduledWait(
                    outputList.getJobIds(),
                    args.getJobPrefix() + "-kmer-compare*",
                    ExitStatus.Type.COMPLETED_ANY,
                    args.getJobPrefix() + "-k-comp-wait",
                    args.getOutputDir());
        }

        // Create plots
        int i = 1;
        for(File matrix : outputList.getFiles()) {
            // Setup kat comp
            KatPlotSpectraCnV1.Args katPlotSpectraCnArgs = new KatPlotSpectraCnV1.Args();
            katPlotSpectraCnArgs.setInput(matrix);
            katPlotSpectraCnArgs.setOutput(new File(matrix.getAbsolutePath() + ".png"));

            KatPlotSpectraCnV1 katPlotSpectraCnProcess = new KatPlotSpectraCnV1(katPlotSpectraCnArgs);

            ExecutionResult result = analyseAsmsExecutor.executeKatPlotSpectraCn(katPlotSpectraCnProcess,
                    executionContext, (args.getJobPrefix() + "kmer-plot-spectra-cn-" + i++), args.isRunParallel());
        }

        // Just let these run.  They shouldn't take long and no processes are dependent on them downstream

        return true;
    }

    @Override
    public void getStats(AssemblyStatsTable table, AnalyseAsmsArgs args) {

    }

    @Override
    public String getName() {
        return "KAT";
    }
}
