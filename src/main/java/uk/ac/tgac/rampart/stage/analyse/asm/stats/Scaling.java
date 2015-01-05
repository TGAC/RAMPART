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

/**
 * Created by maplesod on 25/12/14.
 */
public class Scaling {

    public static double min(double[] a) {

        double min = 0.0;

        for(double v : a) {
            min = Math.min(v, min);
        }

        return min;
    }

    public static double max(double[] a) {

        double max = 0.0;

        for(double v : a) {
            max = Math.max(v, max);
        }

        return max;
    }

    public static void clear(double[] a) {
        for(int i = 0; i < a.length; i++) {
            a[i] = 0;
        }
    }

    public static void weight(double[] a, double weight) {
        for(int i = 0; i < a.length; i++) {
            a[i] *= weight;
        }
    }

    public static void standardScale(double[] a, boolean invert) {

        double min = min(a);
        double max = max(a);

        for (int i = 0; i < a.length; i++) {

            double delta = a[i] - min;

            double diff = max - min;

            double norm = delta / diff;

            double newVal = diff == 0.0 ? 0.5 : norm;

            a[i] = invert ? 1.0 - newVal : newVal;
        }
    }

    public static void deviationScale(double[] a, double mean) {

        double max = 0.0;

        for (int i = 0; i < a.length; i++) {

            double dev = Math.abs(a[i] - mean);

            max = Math.max(max, dev);
        }

        for (int i = 0; i < a.length; i++) {

            double dev = Math.abs(a[i] - mean);

            double norm = 1.0 - (dev / max);

            a[i] = 1.0 - norm;
        }
    }

    public static void percentageScale(double[] a, boolean invert) {

        for (int i = 0; i < a.length; i++) {

            double norm = a[i] / 100.0;

            a[i] = invert ? 1.0 - norm : norm;
        }
    }
}
