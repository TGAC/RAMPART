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

import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.AssemblyStats;
import uk.ac.tgac.conan.core.data.AssemblyStatsMatrix;
import uk.ac.tgac.conan.core.data.AssemblyStatsTable;

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
            matrix.weight(args.getWeightings());
            double[] scores = matrix.calcScores();

            // Save merged matrix with added scores
            merged.addScores(scores);
            merged.save(new File(args.getOutputDir(), "scores.tab"));

            // Get best
            AssemblyStats best = merged.getBest();
            File outputAssembly = new File(args.getOutputDir(), "best.fa");

            ExecutionContext linkingExecutionContext = new DefaultExecutionContext(executionContext.getLocality(), null, null, true);

            this.conanProcessService.execute("ln -s -f " + best.getFilePath() + " " + outputAssembly.getAbsolutePath(), linkingExecutionContext);

        } catch (IOException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        }

        return true;
    }

    protected List<AssemblyStatsTable> loadStats(List<File> statsFiles) throws IOException {
        List<AssemblyStatsTable> tables = new ArrayList<AssemblyStatsTable>();
        for (File file : statsFiles) {
            tables.add(new AssemblyStatsTable(file));
        }

        return tables;
    }


}
