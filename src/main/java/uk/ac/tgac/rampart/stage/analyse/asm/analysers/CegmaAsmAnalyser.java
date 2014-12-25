/*
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2015  Daniel Mapleson - TGAC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.tgac.rampart.stage.analyse.asm.analysers;

import org.apache.commons.io.FileUtils;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.process.asm.stats.CegmaV24;
import uk.ac.tgac.rampart.stage.analyse.asm.AnalyseAssembliesArgs;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStats;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStatsTable;

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

    private AnalyseAssembliesArgs.ToolArgs args;

    public static final String CEGMA_DIR_NAME = "cegma";

    @Override
    public boolean isOperational(ExecutionContext executionContext) {
        return new CegmaV24(this.conanExecutorService).isOperational(executionContext);
    }

    @Override
    public List<ExecutionResult> execute(List<File> assemblies, File outputDir, String jobPrefix, ConanExecutorService ces)
            throws InterruptedException, ProcessExecutionException, ConanParameterException, IOException {

        // Add quast job id to list
        List<ExecutionResult> jobResults = new ArrayList<>();

        if (outputDir.exists()) {
            FileUtils.deleteDirectory(outputDir);
        }
        outputDir.mkdirs();

        int i = 1;
        for(File f : assemblies) {

            String cegmaJobName = jobPrefix + "-" + i++;

            File cegOutputDir = new File(outputDir, f.getName().substring(0, f.getName().length() - 3));
            if (cegOutputDir.exists()) {
                FileUtils.deleteDirectory(cegOutputDir);
            }
            cegOutputDir.mkdirs();

            CegmaV24 cegmaProc = this.makeCegmaProcess(f, cegOutputDir, args.getThreads());
            ExecutionResult result = ces.executeProcess(
                    cegmaProc,
                    cegOutputDir,
                    cegmaJobName,
                    args.getThreads(),
                    args.getMemory(),
                    false);

            jobResults.add(result);

            // Create symbolic links to completeness_reports
            File sourceFile = new File(((CegmaV24.Args)cegmaProc.getProcessArgs()).getOutputPrefix().getAbsolutePath() +
                    ".completeness_report");

            File destFile = new File(cegOutputDir, f.getName() + ".cegma");

            ces.getConanProcessService().createLocalSymbolicLink(sourceFile, destFile);
        }

        return jobResults;
    }

    @Override
    public void setArgs(AnalyseAssembliesArgs.ToolArgs args) {
        this.args = args;
    }

    @Override
    public void updateTable(AssemblyStatsTable table, File reportDir) throws IOException {

        log.info("Extracting stats from CEGMA runs stored in: " + reportDir.getCanonicalPath());

        Collection<File> cegmaFiles = FileUtils.listFiles(reportDir, new String[]{"completeness_report"}, true);

        for(File cf : cegmaFiles) {

            String asmName = cf.getName().substring(0, cf.getName().length() - 23);

            log.info("Extracting CEGMA report from: " + cf.getCanonicalPath() + "; using assembly name: " + asmName);

            AssemblyStats stats = table.findStatsByFilename(asmName);

            CegmaV24.Report cegmaReport = new CegmaV24.Report(cf);

            stats.getConservation().setCegComplete(cegmaReport.getPcComplete());
        }
    }

    @Override
    public boolean isFast() {
        return false;
    }

    @Override
    public void setConanExecutorService(ConanExecutorService ces) {
        this.conanExecutorService = ces;
    }

    @Override
    public String getName() {
        return "CEGMA";
    }

    protected CegmaV24 makeCegmaProcess(File input, File outputDir, int threads) throws IOException {

        // Setup CEGMA
        CegmaV24.Args cegmaArgs = new CegmaV24.Args();
        cegmaArgs.setGenomeFile(input);
        cegmaArgs.setOutputPrefix(new File(outputDir, input.getName()));
        cegmaArgs.setThreads(threads);

        CegmaV24 cegmaProcess = new CegmaV24(this.conanExecutorService, cegmaArgs);

        // Creates output and temp directories
        // Also creates a modified genome file that's BLAST tolerant.
        cegmaProcess.initialise();

        return cegmaProcess;
    }
}
