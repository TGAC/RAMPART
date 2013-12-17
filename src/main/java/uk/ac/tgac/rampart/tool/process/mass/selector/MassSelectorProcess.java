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
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.tool.RampartExecutor;
import uk.ac.tgac.rampart.tool.RampartExecutorImpl;
import uk.ac.tgac.rampart.tool.process.mass.selector.stats.AssemblyStats;
import uk.ac.tgac.rampart.tool.process.mass.selector.stats.AssemblyStatsMatrix;
import uk.ac.tgac.rampart.tool.process.mass.selector.stats.AssemblyStatsMatrixRow;
import uk.ac.tgac.rampart.tool.process.mass.selector.stats.AssemblyStatsTable;

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

    private RampartExecutor executor;

    public MassSelectorProcess(ConanProcessService conanProcessService) {
        this(new MassSelectorArgs(), conanProcessService);
    }

    public MassSelectorProcess(MassSelectorArgs args, ConanProcessService conanProcessService) {
        super("", args, new MassSelectorParams());
        this.executor = new RampartExecutorImpl();
        this.conanProcessService = conanProcessService;
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

            // Initialise the executor
            this.executor.initialise(this.conanProcessService, executionContext);

            // Get results from whatever source is available (prefer pre-merged table over dingle merged file over files)
            AssemblyStatsTable merged = args.getMergedTable() != null ?
                    args.getMergedTable() :
                    args.getMergedFile() != null ?
                        new AssemblyStatsTable(args.getMergedFile()) :
                        this.mergeFiles(args);

            log.debug("Collected merged assembly stats.  Merged table contains " + merged.size() + " entries");

            // Saving merged results to disk
            /*File mergedFile = new File(args.getOutputDir(), "merged.tab");
            merged.save(mergedFile);
            log.debug("Saved merged results to disk at: " + mergedFile.getAbsolutePath());*/

            // Normalise merged table
            AssemblyStatsMatrix matrix = merged.generateStatsMatrix();
            matrix.normalise(args.getEstimatedGenomeSize(), args.getEstimatedGcPercentage());
            log.debug("Normalised merged stats table");

            // Load weightings
            AssemblyStatsMatrixRow weightings = loadWeightings(args.getWeightings());
            log.debug("Weightings loaded: " + ArrayUtils.toString(weightings.getStats()));

            // Apply weightings and calculate final scores
            matrix.weight(weightings);
            double[] scores = matrix.calcScores();
            log.debug("Weightings applied to normalised stats table.  Final scores calculated.");

            // Save merged matrix with added scores
            merged.addScores(scores);
            log.debug("Weighted and normalised scores are: " + ArrayUtils.toString(scores));

            // Save final scores file to disk
            File finalFile = new File(args.getOutputDir(), "scores.tab");
            merged.save(finalFile);
            log.debug("Saved final results to disk at: " + finalFile.getAbsolutePath());

            // Generate plots (Can run in its own time, i.e. they have no dependencies so we just fire them off and they
            // finish when they finish)
            /*this.executePlots(
                    finalFile,
                    new File(finalFile.getParentFile(), finalFile.getName() + ".pdf"),
                    executionContext.usingScheduler() ? executionContext.getScheduler().getArgs().getJobName() + "-asc_plots" : "asc_plots",
                    executionContext);*/

            // Get best
            AssemblyStats best = merged.getBest();
            File outputAssembly = new File(args.getOutputDir(), "best.fa");

            log.debug("Best assembly stats: " + best.toString());
            log.info("Best assembly path: " + best.getFilePath());

            // Create link to "best" assembly in stats dir
            this.conanProcessService.createLocalSymbolicLink(new File(best.getFilePath()), outputAssembly);

        } catch (IOException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        }

        return true;
    }

    private AssemblyStatsTable mergeFiles(MassSelectorArgs args) throws IOException {

        log.debug("Loading " + args.getStatsFiles().size() + " stats files");

        // Load all analyser files into tables
        List<AssemblyStatsTable> tables = loadStats(args.getStatsFiles());

        log.debug("Loaded " + tables.size() + " tables from stats files");

        // Merge tables
        return new AssemblyStatsTable(tables);

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
        List<AssemblyStatsTable> tables = new ArrayList<>();
        for (File file : statsFiles) {

            // Notify system, we are attempting to load a file from disk
            log.debug("Loading stats from: " + file.getAbsolutePath());

            try {
                tables.add(new AssemblyStatsTable(file));
            }
            catch (IOException ioe) {
                log.warn("Could not open stats file: " + file.getAbsolutePath() + ".  This implies the MASS run for this dataset did not complete successfully.  Continuing...");
            }
        }

        return tables;
    }


    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        return true;
    }

}