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

import uk.ac.ebi.fgpt.conan.util.StringJoiner;

/**
 * Created by maplesod on 24/12/14.
 */
public abstract class MetricGroup implements Comparable<MetricGroup> {

    private double score;

    protected MetricGroup() {
        this.score = 0.0;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getWeightedScore(double weighting) {
        return this.score * weighting;
    }

    @Override
    public int compareTo(MetricGroup o) {
        Double thisScore = this.score;
        Double thatScore = o.getScore();

        return thisScore.compareTo(thatScore);
    }

    public String getTabHeader() {
        StringJoiner sj = new StringJoiner("\t");
        for(String s : this.getMetricNames()) {
            sj.add(s);
        }

        return sj.toString();
    }

    public int getNbMetrics() {
        return this.getMetricNames().length;
    }

    public abstract String[] getMetricNames();

    public abstract void parseStrings(String[] values);

    public abstract String toTabString();

    public abstract double[] toMatrixRow();
}
