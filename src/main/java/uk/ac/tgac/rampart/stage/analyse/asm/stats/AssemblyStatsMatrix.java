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

import java.util.ArrayList;

/**
 * User: maplesod
 * Date: 25/04/13
 * Time: 18:37
 */
public class AssemblyStatsMatrix extends ArrayList<AssemblyStatsMatrixRow> {

    public AssemblyStatsMatrix() {
        super();
    }

    public AssemblyStatsMatrix(AssemblyStatsTable table) {

        super(table.size());

        for(AssemblyStats stats : table) {
            this.add(new AssemblyStatsMatrixRow(stats));
        }
    }

    public double getMin(int index) {

        double min = 0.0;

        for(AssemblyStatsMatrixRow row : this) {
            min = Math.min(row.getAt(index), min);
        }

        return min;
    }

    public double getMax(int index) {

        double max = 0.0;

        for(AssemblyStatsMatrixRow row : this) {
            max = Math.max(row.getAt(index), max);
        }

        return max;
    }

    public void standardScale(int index, boolean invert) {

        double min = this.getMin(index);
        double max = this.getMax(index);

        for (AssemblyStatsMatrixRow assemblyStatsMatrixRow : this) {

            double delta = assemblyStatsMatrixRow.getAt(index) - min;

            double diff = max - min;

            double norm = delta / diff;

            double newVal = diff == 0.0 ? 0.5 : norm;

            assemblyStatsMatrixRow.setAt(index, invert ? 1.0 - newVal : newVal);
        }
    }

    public void deviationScale(int index, double mean) {

        double max = 0.0;

        for (AssemblyStatsMatrixRow assemblyStatsMatrixRow : this) {

            double dev = Math.abs(assemblyStatsMatrixRow.getAt(index) - mean);

            max = Math.max(max, dev);
        }

        for (AssemblyStatsMatrixRow assemblyStatsMatrixRow : this) {

            double dev = Math.abs(assemblyStatsMatrixRow.getAt(index) - mean);

            double norm = 1.0 - (dev / max);

            assemblyStatsMatrixRow.setAt(index, 1.0 - norm);
        }
    }

    public void percentageScale(int index, boolean invert) {

        for (AssemblyStatsMatrixRow assemblyStatsMatrixRow : this) {

            double norm = assemblyStatsMatrixRow.getAt(index) / 100.0;

            assemblyStatsMatrixRow.setAt(index,invert ? 1.0 - norm : norm);
        }
    }


    public void normalise(long estimatedGenomeSize, double estimatedGCPercentage) {

        // Standard variables
        standardScale(AssemblyStatsMatrixRow.IDX_NB_SEQS, true);
        standardScale(AssemblyStatsMatrixRow.IDX_NB_SEQS_GT_1K, true);
        standardScale(AssemblyStatsMatrixRow.IDX_MAX_LEN, false);
        standardScale(AssemblyStatsMatrixRow.IDX_N_50, false);
        standardScale(AssemblyStatsMatrixRow.IDX_NA_50, false);
        standardScale(AssemblyStatsMatrixRow.IDX_L_50, true);
        standardScale(AssemblyStatsMatrixRow.IDX_NB_GENES, false);
        standardScale(AssemblyStatsMatrixRow.IDX_NB_MA_REF, true);

        // Percentage variables
        percentageScale(AssemblyStatsMatrixRow.IDX_COMPLETENESS, false);
        percentageScale(AssemblyStatsMatrixRow.IDX_N_PERC, true);

        // Deviation variables
        if (estimatedGCPercentage != 0.0) {
            deviationScale(AssemblyStatsMatrixRow.IDX_GC_PERC, estimatedGCPercentage);
        }
        else {
            setColumn(AssemblyStatsMatrixRow.IDX_GC_PERC, 0.0);
        }

        if (estimatedGenomeSize != 0L) {
            deviationScale(AssemblyStatsMatrixRow.IDX_NB_BASES, (double) estimatedGenomeSize);
            deviationScale(AssemblyStatsMatrixRow.IDX_NB_BASES_GT_1K, (double) estimatedGenomeSize);
        }
        else {
            setColumn(AssemblyStatsMatrixRow.IDX_NB_BASES, 0.0);
            setColumn(AssemblyStatsMatrixRow.IDX_NB_BASES_GT_1K, 0.0);
        }
    }

    private void setColumn(int idxNbBases, double v) {
        for(AssemblyStatsMatrixRow row : this) {
            row.setAt(idxNbBases, v);
        }
    }

    public void weight(AssemblyStatsMatrixRow weights) {
        for(AssemblyStatsMatrixRow row : this) {
            row.weight(weights);
        }
    }

    public double[] calcScores() {
        double[] scores = new double[this.size()];

        for(int i = 0; i < this.size(); i++) {
            scores[i] = this.get(i).calcScore();
        }

        return scores;
    }
}
