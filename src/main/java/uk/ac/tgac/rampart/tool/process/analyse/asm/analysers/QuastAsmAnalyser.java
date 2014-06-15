package uk.ac.tgac.rampart.tool.process.analyse.asm.analysers;

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
import uk.ac.tgac.conan.process.asm.stats.QuastV2_2Args;
import uk.ac.tgac.conan.process.asm.stats.QuastV2_2Process;
import uk.ac.tgac.rampart.tool.process.analyse.asm.AnalyseAssemblies;
import uk.ac.tgac.rampart.tool.process.analyse.asm.stats.AssemblyStatsTable;
import uk.ac.tgac.rampart.tool.process.mass.MassJob;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 22/01/14
 * Time: 11:25
 * To change this template use File | Settings | File Templates.
 */
@MetaInfServices(AssemblyAnalyser.class)
public class QuastAsmAnalyser extends AbstractConanProcess implements AssemblyAnalyser {

    private static Logger log = LoggerFactory.getLogger(QuastAsmAnalyser.class);

    public static final String QUAST_DIR_NAME = "quast";
    public static final String QUAST_REPORT_NAME = "report.txt";

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        QuastV2_2Process proc = new QuastV2_2Process();
        proc.setConanProcessService(this.getConanProcessService());
        return proc.isOperational(executionContext);
    }

    @Override
    public boolean execute(AnalyseAssemblies.Args args, ConanExecutorService ces)
            throws InterruptedException, ProcessExecutionException, ConanParameterException, IOException {

        // Add quast job id to list
        List<Integer> jobIds = new ArrayList<>();

        // Loop through MASS groups
        for(MassJob.Args singleMassArgs : args.getMassGroups()) {

            String massGroup = singleMassArgs.getName();

            File inputDir = new File(args.getMassDir(), massGroup);

            if (!inputDir.exists()) {
                throw new ProcessExecutionException(-1, "Could not find output from mass group: " + massGroup + "; at: " + inputDir.getAbsolutePath());
            }

            // Loop through output levels
            File unitigsDir = new File(inputDir, "unitigs");
            File contigsDir = new File(inputDir, "contigs");
            File scaffoldsDir = new File(inputDir, "scaffolds");

            File outputDir = new File(args.getOutputDir(), massGroup + "/" + QUAST_DIR_NAME);
            String quastPrefix = args.getJobPrefix() + "-" + QUAST_DIR_NAME + "-" + massGroup;

            if (unitigsDir.exists()) {

                QuastV2_2Process quastProcess = this.makeQuast(
                        unitigsDir,
                        new File(outputDir, "unitigs"),
                        args.getOrganism().getEstGenomeSize(),
                        args.getThreadsPerProcess(), false);

                ExecutionResult result = ces.executeProcess(
                        quastProcess,
                        outputDir,
                        quastPrefix + "-unitigs",
                        args.getThreadsPerProcess(),
                        0,
                        args.isRunParallel());

                jobIds.add(result.getJobId());
            }

            if (contigsDir.exists()) {

                QuastV2_2Process quastProcess = this.makeQuast(contigsDir, new File(outputDir, "contigs"),
                        args.getOrganism().getEstGenomeSize(),
                        args.getThreadsPerProcess(), false);

                ExecutionResult result = ces.executeProcess(
                        quastProcess,
                        outputDir,
                        quastPrefix + "-contigs",
                        args.getThreadsPerProcess(),
                        0,
                        args.isRunParallel());

                jobIds.add(result.getJobId());
            }

            if (scaffoldsDir.exists()) {

                QuastV2_2Process quastProcess = this.makeQuast(scaffoldsDir, new File(outputDir, "scaffolds"),
                        args.getOrganism().getEstGenomeSize(),
                        args.getThreadsPerProcess(), true);

                ExecutionResult result = ces.executeProcess(
                        quastProcess,
                        outputDir,
                        quastPrefix + "-scaffolds",
                        args.getThreadsPerProcess(),
                        0,
                        args.isRunParallel());

                jobIds.add(result.getJobId());
            }

        }

        // If we're using a scheduler and we have been asked to run each MECQ group for each library
        // in parallel, then we should wait for all those to complete before continueing.
        if (ces.usingScheduler() && args.isRunParallel()) {
            log.debug("Analysing assemblies for contiguity in parallel, waiting for completion");
            ces.executeScheduledWait(
                    jobIds,
                    args.getJobPrefix() + "-" + QUAST_DIR_NAME + "-*",
                    ExitStatus.Type.COMPLETED_ANY,
                    args.getJobPrefix() + "-cont-wait",
                    args.getOutputDir());
        }

        return true;
    }

    @Override
    public void getStats(AssemblyStatsTable table, AnalyseAssemblies.Args args) throws IOException {

        // Loop through MASS groups
        for(MassJob.Args singleMassArgs : args.getMassGroups()) {

            String massGroup = singleMassArgs.getName();

            File asmDirs = new File(args.getMassDir(), massGroup);
            File aUnitigsDir = new File(asmDirs, "unitigs");
            File aContigsDir = new File(asmDirs, "contigs");
            File aScaffoldsDir = new File(asmDirs, "scaffolds");

            File quastDirs = new File(args.getOutputDir(), massGroup + "/" + QUAST_DIR_NAME);
            File qUnitigsDir = new File(quastDirs, "unitigs");
            File qContigsDir = new File(quastDirs, "contigs");
            File qScaffoldsDir = new File(quastDirs, "scaffolds");

            File asmDir = null;
            File quastReportFile = null;

            if (aScaffoldsDir.exists() && qScaffoldsDir.exists()) {
                asmDir = aScaffoldsDir;
                quastReportFile = new File(qScaffoldsDir, QUAST_REPORT_NAME);
            }
            else if (aContigsDir.exists() && qContigsDir.exists()) {
                asmDir = aContigsDir;
                quastReportFile = new File(qContigsDir, QUAST_REPORT_NAME);
            }
            else if (aUnitigsDir.exists() && qUnitigsDir.exists()) {
                asmDir = aUnitigsDir;
                quastReportFile = new File(qUnitigsDir, QUAST_REPORT_NAME);
            }
            else {
                throw new IllegalStateException("Could not find any quast output to gather");
            }

            if (quastReportFile.exists()) {
                table.mergeWithQuastResults(quastReportFile, asmDir, massGroup);
            }
            else {
                log.warn("Could not find Quast report file at: " + quastReportFile.getAbsolutePath() + "; possibly one of the assemblies does not contain valid contigs.  Skipping quast result integration for this MASS group.");
            }
        }
    }

    @Override
    public String getName() {
        return "QUAST";
    }


    protected QuastV2_2Process makeQuast(File inputDir, File outputDir, long genomeSize, int threads, boolean scaffolds) {
        QuastV2_2Args quastArgs = new QuastV2_2Args();
        quastArgs.setInputFiles(AnalyseAssemblies.assembliesFromDir(inputDir));
        quastArgs.setOutputDir(outputDir);   // No need to create this directory first... quast will take care of that
        quastArgs.setEstimatedGenomeSize(genomeSize);
        quastArgs.setThreads(threads);
        quastArgs.setScaffolds(scaffolds);

        return new QuastV2_2Process(quastArgs);
    }
}
