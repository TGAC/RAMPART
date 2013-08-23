/**
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2013  Daniel Mapleson - TGAC
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
 **/
package uk.ac.tgac.rampart.tool.process.mass.selector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.SchedulerArgs;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.process.asm.stats.AscV10Args;
import uk.ac.tgac.conan.process.asm.stats.AscV10Process;
import uk.ac.tgac.rampart.tool.process.mass.selector.stats.AssemblyStats;
import uk.ac.tgac.rampart.tool.process.mass.selector.stats.AssemblyStatsMatrix;
import uk.ac.tgac.rampart.tool.process.mass.selector.stats.AssemblyStatsMatrixRow;
import uk.ac.tgac.rampart.tool.process.mass.selector.stats.AssemblyStatsTable;
import uk.ac.tgac.rampart.util.FileHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: maplesod
 * Date: 01/02/13
 * Time: 12:39
 */
public class MassSelectorProcess extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(MassSelectorProcess.class);


    public MassSelectorProcess() {
        this(new MassSelectorArgs());
    }

    public MassSelectorProcess(MassSelectorArgs args) {
        super("", args, new MassSelectorParams());
    }

    @Override
    public String getName() {
        return "MASS Selector";
    }

    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        MassSelectorArgs args = (MassSelectorArgs)this.getProcessArgs();

        try {
            // Load all analyser files into tables
            List<AssemblyStatsTable> tables = loadStats(args.getStatsFiles());

            // Merge tables
            AssemblyStatsTable merged = new AssemblyStatsTable(tables);

            // Normalise merged table
            AssemblyStatsMatrix matrix = merged.generateStatsMatrix();
            matrix.normalise(args.getApproxGenomeSize());

            // Apply weightings and calculate final scores
            matrix.weight(loadWeightings(args.getWeightings()));
            double[] scores = matrix.calcScores();

            // Save merged matrix with added scores
            merged.addScores(scores);

            log.debug("Merged scores are: " + ArrayUtils.toString(scores));

            // Save final scores file to disk
            File finalFile = new File(args.getOutputDir(), "scores.tab");
            merged.save(finalFile);

            // Generate plots (Can run in its own time)
            this.executePlots(
                    finalFile,
                    new File(finalFile.getParentFile(), finalFile.getName() + ".pdf"),
                    executionContext.usingScheduler() ? executionContext.getScheduler().getArgs().getJobName() + "-asc_plots" : "asc_plots",
                    executionContext);

            // Get best
            AssemblyStats best = merged.getBest();
            File outputAssembly = new File(args.getOutputDir(), "best.fa");

            log.debug("Best assembly stats: " + best.toString());
            log.debug("Output assembly path: " + outputAssembly.getAbsolutePath());

            // Create link to "best" assembly in stats dir
            FileHelper.createSymbolicLink(this.conanProcessService, new File(best.getFilePath()), outputAssembly, executionContext.getLocality());

        } catch (IOException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        }

        return true;
    }

    protected void plot() {

    }

    protected AssemblyStatsMatrixRow loadWeightings(File weightingsFile) throws IOException {

        List<String> lines = FileUtils.readLines(weightingsFile);
        String weightLine = lines.get(1);

        String[] parts = weightLine.split("\\|");

        double[] weights = new double[parts.length];

        for(int i = 0; i < parts.length; i++) {
            weights[i] = Double.parseDouble(parts[i]);
        }

        AssemblyStatsMatrixRow wMxRow = new AssemblyStatsMatrixRow(weights);

        return wMxRow;
    }

    protected List<AssemblyStatsTable> loadStats(List<File> statsFiles) throws IOException {
        List<AssemblyStatsTable> tables = new ArrayList<AssemblyStatsTable>();
        for (File file : statsFiles) {
            try {
                tables.add(new AssemblyStatsTable(file));
            }
            catch (IOException ioe) {
                log.warn("Could not open stats file: " + file.getAbsolutePath() + ".  This implies the MASS run for this dataset did not complete successfully.  Continuing...");
            }
        }

        return tables;
    }

    protected void executePlots(File inputFile, File outputFile, String jobName, ExecutionContext executionContext)
            throws InterruptedException {

        ExecutionContext executionContextCopy = executionContext.copy();

        if (executionContextCopy.usingScheduler()) {

            SchedulerArgs schedulerArgs = executionContextCopy.getScheduler().getArgs();

            schedulerArgs.setJobName(jobName);
            schedulerArgs.setMonitorFile(new File(inputFile.getParentFile(), jobName + ".log"));
        }

        AscV10Args ascArgs = new AscV10Args();
        ascArgs.setInput(inputFile);
        ascArgs.setOutput(outputFile);
        ascArgs.setMode("PLOTS");

        AscV10Process ascProcess = new AscV10Process(ascArgs);

        try {
            this.conanProcessService.execute(ascProcess, executionContextCopy);
        }
        catch(ProcessExecutionException pee) {
            // If an error occurs here it isn't critical so just log the error and continue
            log.error(pee.getMessage(), pee);
        }
    }

}