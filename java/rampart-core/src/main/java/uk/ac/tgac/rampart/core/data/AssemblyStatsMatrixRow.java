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
package uk.ac.tgac.rampart.core.data;

import uk.ac.tgac.rampart.core.utils.StringJoiner;

/**
 * User: maplesod
 * Date: 01/02/13
 * Time: 15:25
 */
public class AssemblyStatsMatrixRow {

    public static final int IDX_NB_CONTIGS = 0;
    public static final int IDX_NB_BASES = 1;
    public static final int IDX_A_PERC = 2;
    public static final int IDX_C_PERC = 3;
    public static final int IDX_G_PERC = 4;
    public static final int IDX_T_PERC = 5;
    public static final int IDX_N_PERC = 6;
    public static final int IDX_MIN_LEN = 7;
    public static final int IDX_AVG_LEN = 8;
    public static final int IDX_MAX_LEN = 9;
    public static final int IDX_N_80 = 10;
    public static final int IDX_N_50 = 11;
    public static final int IDX_N_20 = 12;
    public static final int IDX_L_50 = 13;


    private double[] stats;

    public AssemblyStatsMatrixRow() {
        this(new double[14]);
    }

    public AssemblyStatsMatrixRow(double[] stats) {
        this.stats = stats;
    }

    public AssemblyStatsMatrixRow(AssemblyStats assemblyStats) {
        this.stats = new double[14];
        this.stats[IDX_NB_CONTIGS] = new Double(assemblyStats.getNbContigs()).doubleValue();
        this.stats[IDX_NB_BASES] = new Double(assemblyStats.getNbBases()).doubleValue();
        this.stats[IDX_A_PERC] = new Double(assemblyStats.getaPerc()).doubleValue();
        this.stats[IDX_C_PERC] = new Double(assemblyStats.getcPerc()).doubleValue();
        this.stats[IDX_G_PERC] = new Double(assemblyStats.getgPerc()).doubleValue();
        this.stats[IDX_T_PERC] = new Double(assemblyStats.gettPerc()).doubleValue();
        this.stats[IDX_N_PERC] = new Double(assemblyStats.getnPerc()).doubleValue();
        this.stats[IDX_MIN_LEN] = new Double(assemblyStats.getMinLen()).doubleValue();
        this.stats[IDX_AVG_LEN] = new Double(assemblyStats.getAvgLen()).doubleValue();
        this.stats[IDX_MAX_LEN] = new Double(assemblyStats.getMaxLen()).doubleValue();
        this.stats[IDX_N_80] = new Double(assemblyStats.getN80()).doubleValue();
        this.stats[IDX_N_50] = new Double(assemblyStats.getN50()).doubleValue();
        this.stats[IDX_N_20] = new Double(assemblyStats.getN20()).doubleValue();
        this.stats[IDX_L_50] = new Double(assemblyStats.getL50()).doubleValue();
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

        return sum / (double)this.stats.length;
    }

    public String toString() {

        StringJoiner sj = new StringJoiner("|");

        for (double stat : this.stats) {
            sj.add(stat);
        }

        return sj.toString();
    }
}
