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
package uk.ac.tgac.rampart.tool.process.mass.selector.stats;

import uk.ac.ebi.fgpt.conan.util.StringJoiner;

/**
 * User: maplesod
 * Date: 23/08/13
 * Time: 09:02
 */
public class AssemblyStatsMatrixRow {

    public static final int IDX_NB_SEQS = 0;
    public static final int IDX_NB_BASES = 1;
    public static final int IDX_N_PERC = 2;
    public static final int IDX_MAX_LEN = 3;
    public static final int IDX_N_50 = 4;
    public static final int IDX_L_50 = 5;
    public static final int IDX_GC = 6;
    public static final int IDX_COMPLETENESS = 7;



    private double[] stats;

    public AssemblyStatsMatrixRow() {
        this(new double[8]);
    }

    public AssemblyStatsMatrixRow(double[] stats) {
        this.stats = stats;
    }

    public AssemblyStatsMatrixRow(AssemblyStats assemblyStats) {

        // Initialise matrix
        this();

        // Add assembly stats
        this.stats[IDX_NB_SEQS] = new Double(assemblyStats.getNbSeqs()).doubleValue();
        this.stats[IDX_NB_BASES] = new Double(assemblyStats.getNbBases()).doubleValue();
        this.stats[IDX_N_PERC] = new Double(assemblyStats.getNcPercents().getN());
        this.stats[IDX_MAX_LEN] = new Double(assemblyStats.getMaxLen()).doubleValue();
        this.stats[IDX_N_50] = new Double(assemblyStats.getN50()).doubleValue();
        this.stats[IDX_L_50] = new Double(assemblyStats.getL50()).doubleValue();
        this.stats[IDX_GC] = new Double(assemblyStats.getGcPercentage()).doubleValue();
        this.stats[IDX_COMPLETENESS] = new Double(assemblyStats.getCompletenessPercentage()).doubleValue();
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
