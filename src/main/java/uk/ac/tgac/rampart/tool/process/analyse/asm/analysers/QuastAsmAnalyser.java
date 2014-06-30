package uk.ac.tgac.rampart.tool.process.analyse.asm.analysers;

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
import uk.ac.tgac.conan.process.asm.stats.QuastV2_2Args;
import uk.ac.tgac.conan.process.asm.stats.QuastV2_2Process;
import uk.ac.tgac.rampart.tool.process.analyse.asm.AnalyseAssembliesArgs;
import uk.ac.tgac.rampart.tool.process.analyse.asm.AnalyseMassAssemblies;
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
    public List<Integer> execute(List<File> assemblies, File outputDir, String jobPrefix, AnalyseAssembliesArgs args, ConanExecutorService ces)
            throws InterruptedException, ProcessExecutionException, ConanParameterException, IOException {

        if (outputDir.exists()) {
            FileUtils.deleteDirectory(outputDir);
        }

        outputDir.mkdirs();


        // Add quast job id to list
        List<Integer> jobIds = new ArrayList<>();

        QuastV2_2Process quastProcess = this.makeQuast(
                assemblies,
                outputDir,
                args.getOrganism().getEstGenomeSize(),
                args.getThreadsPerProcess(),
                false // Assume all sequences are not scaffolds... I don't like this options much in Quast.
        );

        ExecutionResult result = ces.executeProcess(
                quastProcess,
                outputDir,
                jobPrefix,
                args.getThreadsPerProcess(),
                0,
                args.isRunParallel());

        jobIds.add(result.getJobId());

        return jobIds;
    }

    @Override
    public void updateTable(AssemblyStatsTable table, List<File> assemblies, File reportDir, String subGroup) throws IOException {

        File quastReportFile = new File(reportDir, QUAST_REPORT_NAME);
        if (quastReportFile.exists()) {
            table.mergeWithQuastResults(quastReportFile, subGroup);
        }
        else {
            log.warn("Could not find Quast report file at: " + quastReportFile.getAbsolutePath() + "; possibly one of the assemblies does not contain valid contigs.  Skipping quast result integration for this group.");
        }
    }

    @Override
    public boolean isFast() {
        return true;
    }

    @Override
    public String getName() {
        return "QUAST";
    }


    protected QuastV2_2Process makeQuast(List<File> assemblies, File outputDir, long genomeSize, int threads, boolean scaffolds) {
        QuastV2_2Args quastArgs = new QuastV2_2Args();
        quastArgs.setInputFiles(assemblies);
        quastArgs.setOutputDir(outputDir);   // No need to create this directory first... quast will take care of that
        quastArgs.setEstimatedGenomeSize(genomeSize);
        quastArgs.setThreads(threads);
        quastArgs.setScaffolds(scaffolds);

        return new QuastV2_2Process(quastArgs);
    }
}
