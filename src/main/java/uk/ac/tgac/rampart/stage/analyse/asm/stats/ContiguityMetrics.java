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
public class ContiguityMetrics extends MetricGroup {

    private static final String M_NB_SEQS = "nb_seqs";
    private static final String M_NB_SEQS_GT_1KB = "nb_seqs_gt_1k";
    private static final String M_MAX_LEN = "max_len";
    private static final String M_N50 = "N50";
    private static final String M_NA50 = "NA50";
    private static final String M_L50 = "L50";
    private static final String M_SCORE = "contiguity_score";

    private long nbSeqs;
    private long nbSeqsGt1K;
    private long maxLen;
    private long n50;
    private long na50;
    private long l50;

    public ContiguityMetrics() {
        super();

        this.nbSeqs = 0;
        this.nbSeqsGt1K = 0;
        this.maxLen = 0L;
        this.n50 = 0L;
        this.na50 = 0L;
        this.l50 = 0L;
    }

    @Override
    public String[] getMetricNames() {
        return new String[] {
                M_NB_SEQS,
                M_NB_SEQS_GT_1KB,
                M_MAX_LEN,
                M_N50,
                M_NA50,
                M_L50,
                M_SCORE
        };
    }


    @Override
    public void parseStrings(String[] values) {

        int i = 0;
        this.nbSeqs = Long.parseLong(values[i++]);
        this.nbSeqsGt1K = Long.parseLong(values[i++]);
        this.maxLen = Long.parseLong(values[i++]);
        this.n50 = Long.parseLong(values[i++]);
        this.na50 = Long.parseLong(values[i++]);
        this.l50 = Long.parseLong(values[i++]);
        this.setScore(Double.parseDouble(values[i++]));
    }

    public long getNbSeqs() {
        return nbSeqs;
    }

    public void setNbSeqs(long nbSeqs) {
        this.nbSeqs = nbSeqs;
    }

    public long getNbSeqsGt1K() {
        return nbSeqsGt1K;
    }

    public void setNbSeqsGt1K(long nbSeqsGt1K) {
        this.nbSeqsGt1K = nbSeqsGt1K;
    }

    public long getMaxLen() {
        return maxLen;
    }

    public void setMaxLen(long maxLen) {
        this.maxLen = maxLen;
    }

    public long getN50() {
        return n50;
    }

    public void setN50(long n50) {
        this.n50 = n50;
    }

    public long getNA50() {
        return na50;
    }

    public void setNA50(long na50) {
        this.na50 = na50;
    }

    public long getL50() {
        return l50;
    }

    public void setL50(long l50) {
        this.l50 = l50;
    }


    @Override
    public String toTabString() {
        StringJoiner sj = new StringJoiner("\t");
        sj.add(this.getNbSeqs());
        sj.add(this.getNbSeqsGt1K());
        sj.add(this.getMaxLen());
        sj.add(this.getN50());
        sj.add(this.getNA50());
        sj.add(this.getL50());
        sj.add(this.getScore());

        return sj.toString();
    }

    @Override
    public double[] toMatrixRow() {
        return new double[] {
                this.nbSeqs,
                this.nbSeqsGt1K,
                this.maxLen,
                this.n50,
                this.na50,
                this.l50
        };
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add("Contiguity Metrics:");
        sj.add(" - Number of sequences: " + this.getNbSeqs());
        sj.add(" - Number of sequences > 1kb: " + this.getNbSeqsGt1K());
        sj.add(" - Length of longest sequence: " + this.getMaxLen());
        sj.add(" - N50: " + this.getN50());
        sj.add(" - NA50: " + this.getNA50());
        sj.add(" - L50: " + this.getL50());
        sj.add(" = Contiguity score: " + this.getScore());

        return sj.toString();
    }

    public static class Matrix extends MetricMatrix {

        private static final int IDX_NB_SEQS = 0;
        private static final int IDX_NB_SEQS_GT_1KB = 1;
        private static final int IDX_MAX_LEN = 2;
        private static final int IDX_N50 = 3;
        private static final int IDX_NA50 = 4;
        private static final int IDX_L50 = 5;
        private static final int IDX_SCORE = 6;

        private boolean referenceProvided;

        public Matrix(int nbEntries) {

            super(IDX_SCORE, nbEntries);

            this.referenceProvided = false;
        }


        public Matrix(AssemblyStatsTable table) {

            this(table.size());

            for(int i = 0; i < table.size(); i++) {

                ContiguityMetrics m = table.get(i).getContiguity();

                this.matrix[IDX_NB_SEQS][i] = m.getNbSeqs();
                this.matrix[IDX_NB_SEQS_GT_1KB][i] = m.getNbSeqsGt1K();
                this.matrix[IDX_MAX_LEN][i] = m.getMaxLen();
                this.matrix[IDX_N50][i] = m.getN50();
                this.matrix[IDX_NA50][i] = m.getNA50();
                this.matrix[IDX_L50][i] = m.getL50();
                this.matrix[IDX_SCORE][i] = m.getScore();
            }
        }

        public Matrix(AssemblyStatsTable table, boolean referenceProvided) {

            this(table);

            this.referenceProvided = referenceProvided;
        }

        @Override
        public void normalise() {

            Scaling.standardScale(this.matrix[IDX_NB_SEQS], true);
            Scaling.standardScale(this.matrix[IDX_NB_SEQS_GT_1KB], true);
            Scaling.standardScale(this.matrix[IDX_MAX_LEN], false);
            Scaling.standardScale(this.matrix[IDX_N50], false);
            Scaling.standardScale(this.matrix[IDX_NA50], false);
            Scaling.standardScale(this.matrix[IDX_L50], true);
        }


        @Override
        public double[] parseWeightings(List<Pair<String, Double>> list) {

            double[] weightings = new double[this.nbMetrics];

            for(Pair<String, Double> kv : list) {

                String key = kv.getKey();
                double value = kv.getValue();

                if (key.equalsIgnoreCase(M_NB_SEQS)) {
                    weightings[IDX_NB_SEQS] = value;
                }
                else if (key.equalsIgnoreCase(M_NB_SEQS_GT_1KB)) {
                    weightings[IDX_NB_SEQS_GT_1KB] = value;
                }
                else if (key.equalsIgnoreCase(M_MAX_LEN)) {
                    weightings[IDX_MAX_LEN] = value;
                }
                else if (key.equalsIgnoreCase(M_N50)) {
                    weightings[IDX_N50] = value;
                }
                else if (key.equalsIgnoreCase(M_NA50)) {
                    weightings[IDX_NA50] = value;
                }
                else if (key.equalsIgnoreCase(M_L50)) {
                    weightings[IDX_L50] = value;
                }
            }

            return weightings;
        }


    }
}
