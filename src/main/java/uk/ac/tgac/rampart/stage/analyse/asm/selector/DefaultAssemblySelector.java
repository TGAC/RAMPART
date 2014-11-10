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
package uk.ac.tgac.rampart.stage.analyse.asm.selector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStats;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStatsMatrix;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStatsMatrixRow;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStatsTable;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * User: maplesod
 * Date: 01/02/13
 * Time: 12:39
 */
public class DefaultAssemblySelector implements AssemblySelector {

    private static Logger log = LoggerFactory.getLogger(DefaultAssemblySelector.class);

    private AssemblyStatsMatrixRow weightings;

    public DefaultAssemblySelector(File weightingsFile) throws IOException {
         this.weightings = loadWeightings(weightingsFile);
    }

    @Override
    public AssemblyStats selectAssembly(AssemblyStatsTable table,
                               long estimatedGenomeSize,
                               double estimatedGcPercentage) {

        // Normalise merged table
        AssemblyStatsMatrix matrix = table.generateStatsMatrix();
        matrix.normalise(estimatedGenomeSize, estimatedGcPercentage);
        log.debug("Normalised merged stats table");

        // Apply weightings and calculate final scores
        matrix.weight(weightings);
        double[] scores = matrix.calcScores();
        log.debug("Weightings applied to normalised stats table.  Final scores calculated.");

        // Save merged matrix with added scores
        table.addScores(scores);
        log.debug("Weighted and normalised scores are: " + ArrayUtils.toString(scores));

        // Report best assembly stats
        log.info("Best assembly stats: " + table.getBest().toString());

        return table.getBest();
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

}