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
package uk.ac.tgac.rampart.stage.analyse.asm.stats;

import uk.ac.ebi.fgpt.conan.util.StringJoiner;

/**
 * User: maplesod
 * Date: 23/08/13
 * Time: 09:02
 */
public class AssemblyStatsMatrixRow {

    public static final int IDX_NB_SEQS = 0;
    public static final int IDX_NB_SEQS_GT_1K = 1;
    public static final int IDX_NB_BASES = 2;
    public static final int IDX_NB_BASES_GT_1K = 3;
    public static final int IDX_MAX_LEN = 4;
    public static final int IDX_N_50 = 5;
    public static final int IDX_L_50 = 6;
    public static final int IDX_GC_PERC = 7;
    public static final int IDX_N_PERC = 8;
    public static final int IDX_NB_GENES = 9;
    public static final int IDX_COMPLETENESS = 10;



    private double[] stats;

    public AssemblyStatsMatrixRow() {
        this(new double[11]);
    }

    public AssemblyStatsMatrixRow(double[] stats) {
        this.stats = stats;
    }

    public AssemblyStatsMatrixRow(AssemblyStats assemblyStats) {

        // Initialise matrix
        this();

        // Add assembly stats
        this.stats[IDX_NB_SEQS] = assemblyStats.getNbSeqs();
        this.stats[IDX_NB_SEQS_GT_1K] = assemblyStats.getNbSeqsGt1K();
        this.stats[IDX_NB_BASES] = assemblyStats.getNbBases();
        this.stats[IDX_NB_BASES_GT_1K] = assemblyStats.getNbBasesGt1K();
        this.stats[IDX_MAX_LEN] = assemblyStats.getMaxLen();
        this.stats[IDX_N_50] = assemblyStats.getN50();
        this.stats[IDX_L_50] = assemblyStats.getL50();
        this.stats[IDX_GC_PERC] = assemblyStats.getGcPercentage();
        this.stats[IDX_N_PERC] = assemblyStats.getNPercentage();
        this.stats[IDX_NB_GENES] = assemblyStats.getNbGenes();
        this.stats[IDX_COMPLETENESS] = assemblyStats.getCompletenessPercentage();
    }


    public double[] getStats() {
        return stats;
    }

    public void setStats(double[] stats) {
        this.stats = stats;
    }

    public double getAt(int index) {
        return this.stats[index];
    }

    public void setAt(int index, double newVal) {
        this.stats[index] = newVal;
    }

    public void weight(AssemblyStatsMatrixRow weightings) {

        for(int i = 0; i < this.stats.length; i++) {
            this.stats[i] *= weightings.getAt(i);
        }
    }

    public double calcScore() {

        double sum = 0.0;

        for (double stat : this.stats) {
            sum += stat;
        }

        return sum; // / (double)this.stats.length;
    }

    public String toString() {

        StringJoiner sj = new StringJoiner("|");

        for (double stat : this.stats) {
            sj.add(stat);
        }

        return sj.toString();
    }
}
