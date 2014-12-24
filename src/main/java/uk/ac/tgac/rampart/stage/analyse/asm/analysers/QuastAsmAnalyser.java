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
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.process.asm.stats.QuastV23;
import uk.ac.tgac.rampart.stage.analyse.asm.AnalyseAssembliesArgs;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStats;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStatsTable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private AnalyseAssembliesArgs.ToolArgs args;

    private Map<String, File> assemblies;

    public static final String QUAST_DIR_NAME = "quast";
    public static final String QUAST_REPORT_NAME = "report.txt";

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        return new QuastV23(this.conanExecutorService).isOperational(executionContext);
    }

    @Override
    public void setArgs(AnalyseAssembliesArgs.ToolArgs args) {
         this.args = args;
    }

    @Override
    public List<ExecutionResult> execute(List<File> assemblies, File outputDir, String jobPrefix, ConanExecutorService ces)
            throws InterruptedException, ProcessExecutionException, ConanParameterException, IOException {

        if (outputDir.exists()) {
            FileUtils.deleteDirectory(outputDir);
        }

        outputDir.mkdirs();

        // Create mapping between quast assembly name and actual file path
        this.assemblies = new HashMap<>();

        for(File assembly : assemblies) {
            this.assemblies.put(assembly.getName().substring(0, assembly.getName().length() - 3), assembly.getCanonicalFile());
        }

        // Add quast job id to list
        List<ExecutionResult> jobResults = new ArrayList<>();


        QuastV23 quastProcess = this.makeQuast(
                assemblies,
                outputDir,
                args.getOrganism().getEstGenomeSize(),
                args.getOrganism().getPloidy() > 1,
                args.getOrganism().getReference().getPath(),
                args.getThreads(),
                false // Assume all sequences are not scaffolds... I don't like this options much in Quast.
        );

        ExecutionResult result = ces.executeProcess(
                quastProcess,
                outputDir,
                jobPrefix,
                args.getThreads(),
                args.getMemory(),
                false);

        jobResults.add(result);

        return jobResults;
    }

    @Override
    public void updateTable(AssemblyStatsTable table, File reportDir) throws IOException {

        File quastReportFile = new File(reportDir, QUAST_REPORT_NAME);

        if (quastReportFile.exists()) {
            QuastV23.Report quastReport = new QuastV23.Report(quastReportFile);
            for(QuastV23.AssemblyStats qStats : quastReport.getStatList()) {

                if (!qStats.getName().endsWith("broken")) {

                    AssemblyStats stats = table.findStatsByFilename(qStats.getName());

                    // If not found then create a new entry
                    if (stats == null) {
                        throw new IOException("Couldn't find assembly stats entry for " + qStats.getName());
                    }

                    // Override attributes
                    stats.setN50(qStats.getN50());
                    stats.setL50(qStats.getL50());
                    stats.setMaxLen(qStats.getLargestContig());
                    stats.setGcPercentage(qStats.getGcPc());
                    stats.setNbSeqs(qStats.getNbContigsGt0());
                    stats.setNbSeqsGt1K(qStats.getNbContigsGt1k());
                    stats.setNbBases(qStats.getTotalLengthGt0());
                    stats.setNbBasesGt1K(qStats.getTotalLengthGt1k());
                    stats.setNPercentage(qStats.getNsPer100k() / 1000.0);
                    stats.setNbGenes(qStats.getNbGenes());
                    stats.setNA50(qStats.getNA50());
                    stats.setNbMisassembliesFromRef(qStats.getNbMisassemblies());
                }
            }
        }
        else {
            log.warn("Could not find Quast report file at: " + quastReportFile.getCanonicalPath() + "; possibly one of the assemblies does not contain valid contigs.  Skipping quast result integration for this group.");
        }
    }

    @Override
    public boolean isFast() {
        return true;
    }

    @Override
    public void setConanExecutorService(ConanExecutorService ces) {
        this.conanExecutorService = ces;
    }

    @Override
    public String getName() {
        return "QUAST";
    }


    protected QuastV23 makeQuast(List<File> assemblies, File outputDir, long genomeSize, boolean eukaryote, File reference, int threads, boolean scaffolds) {

        QuastV23.Args quastArgs = new QuastV23.Args();
        quastArgs.setInputFiles(assemblies);
        quastArgs.setOutputDir(outputDir);   // No need to create this directory first... quast will take care of that
        quastArgs.setEstimatedGenomeSize(genomeSize);
        quastArgs.setReference(reference);
        quastArgs.setFindGenes(true);
        quastArgs.setEukaryote(eukaryote);
        quastArgs.setThreads(threads);
        quastArgs.setScaffolds(scaffolds);

        return new QuastV23(this.conanExecutorService, quastArgs);
    }
}
