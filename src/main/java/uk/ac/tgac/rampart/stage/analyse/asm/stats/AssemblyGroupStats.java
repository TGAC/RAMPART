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

package uk.ac.tgac.rampart.stage.analyse.asm.stats;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by maplesod on 24/12/14.
 */
public class AssemblyGroupStats {

    private static final double DEFAULT_VAL = 1.0 / 3.0;

    private double contiguity;
    private double problems;
    private double conservation;

    public AssemblyGroupStats() {
        this(DEFAULT_VAL, DEFAULT_VAL, DEFAULT_VAL);
    }

    public AssemblyGroupStats(double contiguity, double problems, double conservation) {
        this.contiguity = contiguity;
        this.problems = problems;
        this.conservation = conservation;
    }

    public double getContiguity() {
        return contiguity;
    }

    public void setContiguity(double contiguity) {
        this.contiguity = contiguity;
    }

    public double getProblems() {
        return problems;
    }

    public void setProblems(double problems) {
        this.problems = problems;
    }

    public double getConservation() {
        return conservation;
    }

    public void setConservation(double conservation) {
        this.conservation = conservation;
    }


    public static class Matrix extends MetricMatrix {

        private static final int IDX_CONTIGUITY = 0;
        private static final int IDX_PROBLEMS = 1;
        private static final int IDX_CONSERVATION = 2;


        public Matrix(int nbEntries) {

            super(3, nbEntries);
        }


        public Matrix(double[] contiguity, double[] problems, double[] conservation) {

            this(contiguity.length);

            if (contiguity.length != problems.length || problems.length != conservation.length) {
                throw new IllegalArgumentException("Array lengths are not the same");
            }

            this.matrix[IDX_CONTIGUITY] = contiguity;
            this.matrix[IDX_PROBLEMS] = problems;
            this.matrix[IDX_CONSERVATION] = conservation;
        }


        @Override
        public void normalise() {

        }

        @Override
        public double[] parseWeightings(List<Pair<String, Double>> list) {

            double[] weightings = new double[this.nbMetrics];

            for(Pair<String, Double> kv : list) {

                String key = kv.getKey();
                double value = kv.getValue();

                if (key.equalsIgnoreCase("contiguity")) {
                    weightings[IDX_CONTIGUITY] = value;
                }
                else if (key.equalsIgnoreCase("problems")) {
                    weightings[IDX_PROBLEMS] = value;
                }
                else if (key.equalsIgnoreCase("conservation")) {
                    weightings[IDX_CONSERVATION] = value;
                }
            }

            return weightings;
        }
    }
}
