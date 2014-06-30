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
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.process.asm.stats.CegmaV2_4Args;
import uk.ac.tgac.conan.process.asm.stats.CegmaV2_4Process;
import uk.ac.tgac.rampart.tool.process.analyse.asm.AnalyseAssembliesArgs;
import uk.ac.tgac.rampart.tool.process.analyse.asm.AnalyseMassAssemblies;
import uk.ac.tgac.rampart.tool.process.analyse.asm.stats.AssemblyStatsTable;
import uk.ac.tgac.rampart.tool.process.mass.MassJob;

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
    public List<Integer> execute(List<File> assemblies, File outputDir, String jobPrefix, AnalyseAssembliesArgs args, ConanExecutorService ces)
            throws InterruptedException, ProcessExecutionException, ConanParameterException, IOException {

        // Add quast job id to list
        List<Integer> jobIds = new ArrayList<>();

        if (outputDir.exists()) {
            FileUtils.deleteDirectory(outputDir);
        }
        outputDir.mkdirs();

        int i = 1;
        for(File f : assemblies) {

            String cegmaJobName = jobPrefix + "-" + i++;

            File cegOutputDir = new File(outputDir, f.getName());
            if (cegOutputDir.exists()) {
                FileUtils.deleteDirectory(cegOutputDir);
            }
            cegOutputDir.mkdirs();

            CegmaV2_4Process cegmaProc = this.makeCegmaProcess(f, cegOutputDir, args.getThreadsPerProcess());
            ExecutionResult result = ces.executeProcess(
                    cegmaProc,
                    cegOutputDir,
                    cegmaJobName,
                    args.getThreadsPerProcess(),
                    0,
                    args.isRunParallel());

            jobIds.add(result.getJobId());

            // Create symbolic links to completeness_reports
            File sourceFile = new File(((CegmaV2_4Args)cegmaProc.getProcessArgs()).getOutputPrefix().getAbsolutePath() +
                    ".completeness_report");
            File destFile = new File(cegOutputDir, f.getName() + ".cegma");

            ces.getConanProcessService().createLocalSymbolicLink(sourceFile, destFile);
        }

        return jobIds;
    }

    @Override
    public void updateTable(AssemblyStatsTable table, List<File> assemblies, File reportDir, String subGroup) throws IOException {


        Collection<File> cegmaFiles = FileUtils.listFiles(reportDir, new String[]{"cegma"}, false);

        for(File asm : assemblies) {

            File c = null;

            for(File cf : cegmaFiles) {
                if (FilenameUtils.getBaseName(asm.getName()).equals(FilenameUtils.getBaseName(cf.getName()))) {
                    c = cf;
                    break;
                }
            }

            if (c == null || !c.exists())
                throw new IllegalStateException("Could not find cegma output file");

            if (!asm.exists())
                throw new IllegalStateException("Could not find assembly associated with cegma file: " + asm.getAbsolutePath());

            table.mergeWithCegmaResults(c, FilenameUtils.getBaseName(asm.getName()), subGroup);
        }

    }

    @Override
    public boolean isFast() {
        return false;
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
