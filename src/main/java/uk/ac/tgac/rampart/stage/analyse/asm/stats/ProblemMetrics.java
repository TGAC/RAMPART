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
import uk.ac.ebi.fgpt.conan.util.StringJoiner;

import java.util.List;

/**
 * Created by maplesod on 24/12/14.
 */
public class ProblemMetrics extends MetricGroup {

    private static final String M_N_PERC = "N%";
    private static final String M_NB_MA_REF = "nb_ma_ref";
    private static final String M_SCORE = "problems_score";

    private double nPercentage;
    private int nbMisassembliesFromRef;

    public ProblemMetrics() {
        super();

        this.nPercentage = 0.0;
        this.nbMisassembliesFromRef = 0;
    }

    @Override
    public String[] getMetricNames() {
        return new String[] {
                M_N_PERC,
                M_NB_MA_REF,
                M_SCORE
        };
    }


    @Override
    public void parseStrings(String[] values) {

        int i = 0;
        this.nPercentage = Double.parseDouble(values[i++]);
        this.nbMisassembliesFromRef = Integer.parseInt(values[i++]);
        this.setScore(Double.parseDouble(values[i++]));
    }

    public double getnPercentage() {
        return nPercentage;
    }

    public void setnPercentage(double nPercentage) {
        this.nPercentage = nPercentage;
    }

    public int getNbMisassembliesFromRef() {
        return nbMisassembliesFromRef;
    }

    public void setNbMisassembliesFromRef(int nbMisassembliesFromRef) {
        this.nbMisassembliesFromRef = nbMisassembliesFromRef;
    }

    @Override
    public String toTabString() {
        StringJoiner sj = new StringJoiner("\t");
        sj.add(this.getnPercentage());
        sj.add(this.getNbMisassembliesFromRef());
        sj.add(this.getScore());

        return sj.toString();
    }

    @Override
    public double[] toMatrixRow() {
        return new double[] {
                this.nPercentage,
                this.nbMisassembliesFromRef
        };
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add("Assembly Problem Metrics:");
        sj.add(" - Percent gaps: " + this.nPercentage);
        sj.add(" - Number of misassemblies from reference: " + this.nbMisassembliesFromRef);
        sj.add(" = Problem score: " + this.getScore());

        return sj.toString();
    }


    public static class Matrix extends MetricMatrix {

        private static final int IDX_N_PERC = 0;
        private static final int IDX_NB_MA_REF = 1;
        private static final int IDX_SCORE = 2;

        private boolean referenceProvided;

        public Matrix(int nbEntries) {

            super(IDX_SCORE, nbEntries);

            this.referenceProvided = false;
        }


        public Matrix(AssemblyStatsTable table) {

            this(table.size());

            for(int i = 0; i < table.size(); i++) {

                ProblemMetrics m = table.get(i).getProblems();

                this.matrix[IDX_N_PERC][i] = m.getnPercentage();
                this.matrix[IDX_NB_MA_REF][i] = m.getNbMisassembliesFromRef();
                this.matrix[IDX_SCORE][i] = m.getScore();
            }
        }

        public Matrix(AssemblyStatsTable table, boolean referenceProvided) {

            this(table);

            this.referenceProvided = referenceProvided;
        }

        @Override
        public void normalise() {

            Scaling.percentageScale(this.matrix[IDX_N_PERC], true);
            Scaling.standardScale(this.matrix[IDX_NB_MA_REF], true);
        }

        @Override
        public double[] parseWeightings(List<Pair<String, Double>> list) {

            double[] weightings = new double[this.nbMetrics];

            for(Pair<String, Double> kv : list) {

                String key = kv.getKey();
                double value = kv.getValue();

                if (key.equalsIgnoreCase(M_N_PERC)) {
                    weightings[IDX_N_PERC] = value;
                }
                else if (key.equalsIgnoreCase(M_NB_MA_REF)) {
                    weightings[IDX_NB_MA_REF] = value;
                }
            }

            return weightings;
        }
    }
}
