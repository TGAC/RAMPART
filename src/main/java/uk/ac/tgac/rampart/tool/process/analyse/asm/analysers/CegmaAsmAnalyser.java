package uk.ac.tgac.rampart.tool.process.analyse.asm.analysers;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.process.asm.stats.CegmaV2_4Args;
import uk.ac.tgac.conan.process.asm.stats.CegmaV2_4Process;
import uk.ac.tgac.rampart.tool.process.analyse.asm.AnalyseAsmsArgs;
import uk.ac.tgac.rampart.tool.process.analyse.asm.AnalyseAsmsExecutor;
import uk.ac.tgac.rampart.tool.process.analyse.asm.AnalyseAsmsProcess;
import uk.ac.tgac.rampart.tool.process.analyse.asm.stats.AssemblyStatsTable;
import uk.ac.tgac.rampart.tool.process.mass.single.SingleMassArgs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 22/01/14
 * Time: 11:32
 * To change this template use File | Settings | File Templates.
 */
@MetaInfServices(AssemblyAnalyser.class)
public class CegmaAsmAnalyser extends AbstractConanProcess implements AssemblyAnalyser {

    private static Logger log = LoggerFactory.getLogger(CegmaAsmAnalyser.class);

    public static final String CEGMA_DIR_NAME = "cegma";

    @Override
    public boolean isOperational(ExecutionContext executionContext) {
        CegmaV2_4Process proc = new CegmaV2_4Process();
        proc.setConanProcessService(getConanProcessService());
        return proc.isOperational(executionContext);
    }

    @Override
    public boolean execute(AnalyseAsmsArgs args, ExecutionContext executionContext, AnalyseAsmsExecutor analyseAsmsExecutor)
            throws InterruptedException, ProcessExecutionException, ConanParameterException, IOException {

        // Add quast job id to list
        List<Integer> jobIds = new ArrayList<>();

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

            // We only do the longest sequence types... CEGMA takes ages to run and we won't get any useful info by running
            // through all sequence types
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

            List<File> inputs = AnalyseAsmsProcess.assembliesFromDir(seqDir);

            File groupOutputDir = new File(args.getOutputDir(), massGroup);

            File cegmaOutputDir = new File(groupOutputDir, CEGMA_DIR_NAME);
            if (cegmaOutputDir.exists()) {
                FileUtils.deleteDirectory(cegmaOutputDir);
            }
            cegmaOutputDir.mkdirs();

            int i = 1;
            for(File f : inputs) {

                String cegmaJobName = args.getJobPrefix() + "-" + CEGMA_DIR_NAME + "-" + massGroup + "-" + i++;

                File outputDir = new File(cegmaOutputDir, f.getName());
                if (outputDir.exists()) {
                    FileUtils.deleteDirectory(outputDir);
                }
                outputDir.mkdirs();

                CegmaV2_4Process cegmaProc = this.makeCegmaProcess(f, outputDir, args.getThreadsPerProcess());
                ExecutionResult result = analyseAsmsExecutor.executeCegma(cegmaProc, executionContext,
                        cegmaJobName, args.isRunParallel());
                jobIds.add(result.getJobId());

                // Create symbolic links to completeness_reports
                File sourceFile = new File(((CegmaV2_4Args)cegmaProc.getProcessArgs()).getOutputPrefix().getAbsolutePath() +
                        ".completeness_report");
                File destFile = new File(cegmaOutputDir, f.getName() + ".cegma");

                analyseAsmsExecutor.createSymbolicLink(sourceFile, destFile);
            }

        }

        // If we're using a scheduler and we have been asked to run each MECQ group for each library
        // in parallel, then we should wait for all those to complete before continueing.
        if (executionContext.usingScheduler() && args.isRunParallel()) {
            log.debug("Analysing assemblies for contiguity in parallel, waiting for completion");
            analyseAsmsExecutor.executeScheduledWait(
                    jobIds,
                    args.getJobPrefix() + "-" + CEGMA_DIR_NAME + "-*",
                    ExitStatus.Type.COMPLETED_ANY,
                    args.getJobPrefix() + "-comp-wait",
                    args.getOutputDir());
        }

        return true;
    }

    @Override
    public void getStats(AssemblyStatsTable table, AnalyseAsmsArgs args) throws IOException {

        // Loop through MASS groups
        for(SingleMassArgs singleMassArgs : args.getMassGroups()) {

            String massGroup = singleMassArgs.getName();

            File asmDirs = new File(args.getMassDir(), massGroup);
            File aUnitigsDir = new File(asmDirs, "unitigs");
            File aContigsDir = new File(asmDirs, "contigs");
            File aScaffoldsDir = new File(asmDirs, "scaffolds");

            File asmDir = null;
            if (aScaffoldsDir.exists()) {
                asmDir = aScaffoldsDir;
            }
            else if (aContigsDir.exists()) {
                asmDir = aContigsDir;
            }
            else if (aUnitigsDir.exists()) {
                asmDir = aUnitigsDir;
            }
            else {
                throw new IllegalStateException("Could not find any output sequences for this mass group: " + massGroup);
            }


            File cegmaDir = new File(args.getOutputDir(), massGroup + "/" + CEGMA_DIR_NAME);

            Collection<File> cegmaFiles = FileUtils.listFiles(cegmaDir, new String[]{"cegma"}, false);

            for(File c : cegmaFiles) {

                File asm = new File(asmDir, FilenameUtils.getBaseName(c.getName()));

                if (!asm.exists())
                    throw new IllegalStateException("Could not find assembly associated with cegma file: " + asm.getAbsolutePath());

                table.mergeWithCegmaResults(c, asm, FilenameUtils.getBaseName(asm.getName()), massGroup);
            }
        }
    }

    @Override
    public String getName() {
        return "CEGMA";
    }

    protected CegmaV2_4Process makeCegmaProcess(File input, File outputDir, int threads) throws IOException {

        // Setup CEGMA
        CegmaV2_4Args cegmaArgs = new CegmaV2_4Args();
        cegmaArgs.setGenomeFile(input);
        cegmaArgs.setOutputPrefix(new File(outputDir, input.getName()));
        cegmaArgs.setThreads(threads);

        CegmaV2_4Process cegmaProcess = new CegmaV2_4Process(cegmaArgs);

        // Creates output and temp directories
        // Also creates a modified genome file that's BLAST tolerant.
        cegmaProcess.initialise();

        return cegmaProcess;
    }
}
