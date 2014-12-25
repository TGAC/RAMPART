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
 * Created by maplesod on 25/12/14.
 */
public abstract class MetricMatrix {

    protected int nbMetrics;
    protected int nbEntries;

    protected double[][] matrix;

    protected MetricMatrix(int nbMetrics, int nbEntries) {

        if (nbMetrics < 1 || nbEntries < 1) {
            throw new IllegalArgumentException("Can't have zero sized matrix");
        }

        this.nbMetrics = nbMetrics;
        this.nbEntries = nbEntries;

        // Add one to metrics to include score
        this.matrix = new double[nbMetrics + 1][nbEntries];
    }

    public abstract void normalise();

    public void weight(double[] weights) {

        if (weights.length != this.nbMetrics) {
            throw new IllegalArgumentException("Not the same number of weightings as their are metrics in this matrix");
        }

        for (int j = 0; j < this.nbMetrics; j++) {
            Scaling.weight(this.matrix[j], weights[j]);
        }
    }

    public double[] calcScores() {

        for (int i = 0; i < this.nbEntries; i++) {
            for (int j = 0; j < this.nbMetrics; j++) {
                this.matrix[this.nbMetrics][i] = this.matrix[j][i];
            }
        }

        return this.matrix[this.nbMetrics];
    }

    public abstract double[] parseWeightings(List<Pair<String, Double>> list);
}