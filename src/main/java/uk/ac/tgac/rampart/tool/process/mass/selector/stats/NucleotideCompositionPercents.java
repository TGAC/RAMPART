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
 * Time: 09:03
 */
public class NucleotideCompositionPercents {

    private double a;
    private double c;
    private double g;
    private double t;
    private double n;

    public NucleotideCompositionPercents() {
        this(0.0, 0.0, 0.0, 0.0, 0.0);
    }

    public NucleotideCompositionPercents(double a, double c, double g, double t, double n) {
        this.a = a;
        this.c = c;
        this.g = g;
        this.t = t;
        this.n = n;
    }

    public NucleotideCompositionPercents(String[] parts) {

        if (parts.length != 5)
            throw new IllegalArgumentException("Array length is not 5. Invalid number of nucleotide types.");

        this.a = Double.parseDouble(parts[0]);
        this.c = Double.parseDouble(parts[1]);
        this.g = Double.parseDouble(parts[2]);
        this.t = Double.parseDouble(parts[3]);
        this.n = Double.parseDouble(parts[4]);
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getC() {
        return c;
    }

    public void setC(double c) {
        this.c = c;
    }

    public double getG() {
        return g;
    }

    public void setG(double g) {
        this.g = g;
    }

    public double getT() {
        return t;
    }

    public void setT(double t) {
        this.t = t;
    }

    public double getN() {
        return n;
    }

    public void setN(double n) {
        this.n = n;
    }

    public static String getStatsFileHeaderPercents() {
        return "a.pc|c.pc|g.pc|t.pc|n.pc";
    }

    @Override
    public String toString() {
        return this.toString("|");
    }


    public String toString(String separator) {
        StringJoiner sj = new StringJoiner(separator);

        sj.add(Double.isNaN(a) ? 0.0 : a);
        sj.add(Double.isNaN(c) ? 0.0 : c);
        sj.add(Double.isNaN(g) ? 0.0 : g);
        sj.add(Double.isNaN(t) ? 0.0 : t);
        sj.add(Double.isNaN(n) ? 0.0 : n);

        return  sj.toString();
    }
}
