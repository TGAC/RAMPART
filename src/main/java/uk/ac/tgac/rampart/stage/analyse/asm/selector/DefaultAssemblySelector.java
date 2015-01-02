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
package uk.ac.tgac.rampart.stage.analyse.asm.selector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.process.asm.stats.QuastV23;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: maplesod
 * Date: 01/02/13
 * Time: 12:39
 */
public class DefaultAssemblySelector implements AssemblySelector {

    private static Logger log = LoggerFactory.getLogger(DefaultAssemblySelector.class);

    private double[] groupWeightings;
    private double[] contiguityWeightings;
    private double[] problemWeightings;
    private double[] conservationWeightings;

    public DefaultAssemblySelector(File weightingsFile) throws IOException {

        // Initially set to defaults
        this.groupWeightings = null;
        this.contiguityWeightings = null;
        this.problemWeightings = null;
        this.conservationWeightings = null;

        // Override with loaded values
        this.loadWeightings(weightingsFile);
    }

    @Override
    public AssemblyStats selectAssembly(AssemblyStatsTable table,
                               Organism organism,
                               QuastV23.AssemblyStats refStats,
                               boolean cegmaEnabled) {

        boolean referenceProvided = refStats != null;
        boolean estimatesProvided = organism.getEstimated() != null;

        // Acquire metric groups
        ContiguityMetrics.Matrix contiguity = new ContiguityMetrics.Matrix(table, referenceProvided);
        ProblemMetrics.Matrix problems = new ProblemMetrics.Matrix(table, referenceProvided);
        ConservationMetrics.Matrix conservation = new ConservationMetrics.Matrix(table,
                referenceProvided ? refStats.getTotalLengthGt0() : estimatesProvided ? organism.getEstimated().getEstGenomeSize() : 0,
                referenceProvided ? refStats.getGcPc() : estimatesProvided ? organism.getEstimated().getEstGcPercentage() : 0.0,
                referenceProvided ? refStats.getNbGenes() : estimatesProvided ? organism.getEstimated().getEstNbGenes() : 0,
                cegmaEnabled);
        log.info("Acquired metrics");

        log.debug("Contiguity");
        log.debug(contiguity.toString());

        log.debug("Problems");
        log.debug(problems.toString());

        log.debug("Conservation");
        log.debug(conservation.toString());

        // Normalise matrices
        contiguity.normalise();
        problems.normalise();
        conservation.normalise();
        log.info("Scaled metrics");

        log.debug("Contiguity");
        log.debug(contiguity.toString());

        log.debug("Problems");
        log.debug(problems.toString());

        log.debug("Conservation");
        log.debug(conservation.toString());


        // Apply weightings
        contiguity.weight(contiguityWeightings);
        problems.weight(problemWeightings);
        conservation.weight(conservationWeightings);
        log.info("Applied weightings");

        log.debug("Contiguity");
        log.debug(contiguity.toString());

        log.debug("Problems");
        log.debug(problems.toString());

        log.debug("Conservation");
        log.debug(conservation.toString());

        // Calc scores for groups
        double[] contiguityScores = contiguity.calcScores();
        double[] problemScores = problems.calcScores();
        double[] conservationsScores = conservation.calcScores();
        log.info("Calculated scores for each metric group");

        log.debug("Contiguity: " + Arrays.toString(contiguityScores));
        log.debug("Problems: " + Arrays.toString(problemScores));
        log.debug("Conservation: " + Arrays.toString(conservationsScores));

        table.addGroupScores(contiguityScores, problemScores, conservationsScores);

        AssemblyGroupStats.Matrix finalScores = new AssemblyGroupStats.Matrix(contiguityScores, problemScores, conservationsScores);
        finalScores.weight(groupWeightings);
        double[] scores = finalScores.calcScores();
        log.info("Weightings applied to group scores.  Final scores calculated.");

        // Save merged matrix with added scores
        table.addScores(scores);
        log.debug("Final scores are: " + ArrayUtils.toString(scores));

        // Report best assembly stats
        log.info("Best assembly stats: " + table.getBest().toString());

        return table.getBest();
    }


    protected void loadWeightings(File weightingsFile) throws IOException {

        List<String> lines = FileUtils.readLines(weightingsFile);

        List<Pair<String,Double>> kvps = new ArrayList<>();

        for (String line : lines) {
            String tl = line.trim();

            if (!tl.isEmpty() && !tl.startsWith("#")) {

                String[] parts = tl.split("=");

                String key = parts[0].trim();
                String value = parts[1].trim();

                if (value.contains("#")) {
                    value = value.substring(0, value.indexOf("#") - 1).trim();
                }

                double dv = Double.parseDouble(value);

                kvps.add(new ImmutablePair<>(key, dv));
            }
        }

        this.contiguityWeightings = new ContiguityMetrics.Matrix(1).parseWeightings(kvps);
        this.problemWeightings = new ProblemMetrics.Matrix(1).parseWeightings(kvps);
        this.conservationWeightings = new ConservationMetrics.Matrix(1).parseWeightings(kvps);
        this.groupWeightings = new AssemblyGroupStats.Matrix(1).parseWeightings(kvps);

        log.debug("Loaded weightings from: " + weightingsFile.getAbsolutePath());
        log.debug("Contiguity weightings: " + Arrays.toString(this.contiguityWeightings));
        log.debug("Problem weightings: " + Arrays.toString(this.problemWeightings));
        log.debug("Conservation weightings: " + Arrays.toString(this.conservationWeightings));
        log.debug("Group weightings: " + Arrays.toString(this.groupWeightings));
    }

}