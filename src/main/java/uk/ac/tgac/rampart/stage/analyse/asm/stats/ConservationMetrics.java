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
public class ConservationMetrics extends MetricGroup {

    private static final String M_NB_BASES = "nb_bases";
    private static final String M_NB_BASES_GT_1KB = "nb_bases_gt_1k";
    private static final String M_GC_PERC = "GC%";
    private static final String M_NB_GENES = "nb_genes";
    private static final String M_CEG_COMPLETE = "ceg_completeness";
    private static final String M_SCORE = "conservation_score";

    private long nbBases;
    private long nbBasesGt1K;
    private double gcPercentage;
    private int nbGenes;
    private double cegComplete;

    public ConservationMetrics() {
        super();

        this.nbBases = 0L;
        this.nbBasesGt1K = 0L;
        this.gcPercentage = 0.0;
        this.nbGenes = 0;
        this.cegComplete = 0.0;
    }


    @Override
    public String[] getMetricNames() {
        return new String[] {
                M_NB_BASES,
                M_NB_BASES_GT_1KB,
                M_GC_PERC,
                M_NB_GENES,
                M_CEG_COMPLETE,
                M_SCORE
        };
    }

    @Override
    public void parseStrings(String[] values) {

        int i = 0;
        this.nbBases = Long.parseLong(values[i++]);
        this.nbBasesGt1K = Long.parseLong(values[i++]);
        this.gcPercentage = Double.parseDouble(values[i++]);
        this.nbGenes = Integer.parseInt(values[i++]);
        this.cegComplete = Double.parseDouble(values[i++]);
        this.setScore(Double.parseDouble(values[i++]));
    }

    public long getNbBases() {
        return nbBases;
    }

    public void setNbBases(long nbBases) {
        this.nbBases = nbBases;
    }

    public long getNbBasesGt1K() {
        return nbBasesGt1K;
    }

    public void setNbBasesGt1K(long nbBasesGt1K) {
        this.nbBasesGt1K = nbBasesGt1K;
    }

    public double getGcPercentage() {
        return gcPercentage;
    }

    public void setGcPercentage(double gcPercentage) {
        this.gcPercentage = gcPercentage;
    }

    public int getNbGenes() {
        return nbGenes;
    }

    public void setNbGenes(int nbGenes) {
        this.nbGenes = nbGenes;
    }

    public double getCegComplete() {
        return cegComplete;
    }

    public void setCegComplete(double cegComplete) {
        this.cegComplete = cegComplete;
    }

    @Override
    public String toTabString() {
        StringJoiner sj = new StringJoiner("\t");
        sj.add(this.getNbBases());
        sj.add(this.getNbBasesGt1K());
        sj.add(this.getGcPercentage());
        sj.add(this.getNbGenes());
        sj.add(this.getCegComplete());
        sj.add(this.getScore());

        return sj.toString();
    }

    @Override
    public double[] toMatrixRow() {
        return new double[] {
                this.nbBases,
                this.nbBasesGt1K,
                this.gcPercentage,
                this.nbGenes,
                this.cegComplete
        };
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add("Conservation Metrics:");
        sj.add(" - Total assembly size: " + this.nbBases);
        sj.add(" - Assembly size from sequences > 1KB: " + this.nbBasesGt1K);
        sj.add(" - GC%: " + this.gcPercentage);
        sj.add(" - Number of genes: " + this.nbGenes);
        sj.add(" - CEG Completeness %: " + this.cegComplete);
        sj.add(" = Conservation score: " + this.getScore());

        return sj.toString();
    }


    public static class Matrix extends MetricMatrix {

        private static final int IDX_NB_BASES = 0;
        private static final int IDX_NB_BASES_GT_1KB = 1;
        private static final int IDX_GC_PERC = 2;
        private static final int IDX_NB_GENES = 3;
        private static final int IDX_CEG_COMPLETE = 4;
        private static final int IDX_SCORE = 5;

        private long estimatedGenomeSize;
        private double estimatedGCPercentage;
        private int estimatedNbGenes;
        private boolean cegmaEnabled;

        public Matrix(int nbEntries) {

            super(IDX_SCORE, nbEntries);

            this.estimatedGenomeSize = 0L;
            this.estimatedGCPercentage = 0.0;
            this.estimatedNbGenes = 0;
            this.cegmaEnabled = false;
        }


        public Matrix(AssemblyStatsTable table) {

            this(table.size());

            for(int i = 0; i < table.size(); i++) {

                ConservationMetrics m = table.get(i).getConservation();

                this.matrix[IDX_NB_BASES][i] = m.getNbBases();
                this.matrix[IDX_NB_BASES_GT_1KB][i] = m.getNbBasesGt1K();
                this.matrix[IDX_GC_PERC][i] = m.getGcPercentage();
                this.matrix[IDX_NB_GENES][i] = m.getNbGenes();
                this.matrix[IDX_CEG_COMPLETE][i] = m.getCegComplete();
                this.matrix[IDX_SCORE][i] = m.getScore();
            }
        }

        public Matrix(AssemblyStatsTable table, long estimatedGenomeSize, double estimatedGCPercentage, int estimatedNbGenes, boolean cegmaEnabled) {

            this(table);

            this.estimatedGenomeSize = estimatedGenomeSize;
            this.estimatedGCPercentage = estimatedGCPercentage;
            this.estimatedNbGenes = estimatedNbGenes;
            this.cegmaEnabled = cegmaEnabled;
        }

        @Override
        public void normalise() {

            if (estimatedGenomeSize != 0L) {
                Scaling.deviationScale(this.matrix[IDX_NB_BASES], (double) estimatedGenomeSize);
                Scaling.deviationScale(this.matrix[IDX_NB_BASES_GT_1KB], (double) estimatedGenomeSize);
            }
            else {
                Scaling.clear(this.matrix[IDX_NB_BASES]);
                Scaling.clear(this.matrix[IDX_NB_BASES_GT_1KB]);
            }

            if (estimatedGCPercentage != 0.0) {
                Scaling.deviationScale(this.matrix[IDX_GC_PERC], estimatedGCPercentage);
            }
            else {
                Scaling.clear(this.matrix[IDX_GC_PERC]);
            }

            if (estimatedNbGenes != 0) {
                Scaling.deviationScale(this.matrix[IDX_NB_GENES], estimatedNbGenes);
            }
            else {
                Scaling.clear(this.matrix[IDX_NB_GENES]);
            }

            if (cegmaEnabled) {
                Scaling.percentageScale(this.matrix[IDX_CEG_COMPLETE], false);
            }
            else {
                Scaling.clear(this.matrix[IDX_CEG_COMPLETE]);
            }
        }

        @Override
        public double[] parseWeightings(List<Pair<String, Double>> list) {

            double[] weightings = new double[this.nbMetrics];

            for(Pair<String, Double> kv : list) {

                String key = kv.getKey();
                double value = kv.getValue();

                if (key.equalsIgnoreCase(M_NB_BASES)) {
                    weightings[IDX_NB_BASES] = value;
                }
                else if (key.equalsIgnoreCase(M_NB_BASES_GT_1KB)) {
                    weightings[IDX_NB_BASES_GT_1KB] = value;
                }
                else if (key.equalsIgnoreCase(M_GC_PERC)) {
                    weightings[IDX_GC_PERC] = value;
                }
                else if (key.equalsIgnoreCase(M_NB_GENES)) {
                    weightings[IDX_NB_GENES] = value;
                }
                else if (key.equalsIgnoreCase(M_CEG_COMPLETE)) {
                    weightings[IDX_CEG_COMPLETE] = value;
                }
            }

            return weightings;
        }
    }
}
