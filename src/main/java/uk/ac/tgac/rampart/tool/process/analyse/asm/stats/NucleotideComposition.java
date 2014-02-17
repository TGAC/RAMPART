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
package uk.ac.tgac.rampart.tool.process.analyse.asm.stats;

import uk.ac.ebi.fgpt.conan.util.StringJoiner;

/**
 * User: maplesod
 * Date: 23/08/13
 * Time: 09:05
 */
public class NucleotideComposition {


    private long nbA;
    private long nbC;
    private long nbG;
    private long nbT;
    private long nbN;
    private long nbUnknown;

    public NucleotideComposition() {
        this(0, 0, 0, 0, 0);
    }

    public NucleotideComposition(long nbA, long nbC, long nbG, long nbT, long nbN) {
        this.nbA = nbA;
        this.nbC = nbC;
        this.nbG = nbG;
        this.nbT = nbT;
        this.nbN = nbN;
        this.nbUnknown = 0;
    }

    public NucleotideComposition(String[] parts) {

        if (parts.length != 5)
            throw new IllegalArgumentException("Array length is not 5.  Invalid number of nucleotide types.");

        this.nbA = Long.parseLong(parts[0]);
        this.nbC = Long.parseLong(parts[1]);
        this.nbG = Long.parseLong(parts[2]);
        this.nbT = Long.parseLong(parts[3]);
        this.nbN = Long.parseLong(parts[4]);
    }

    public NucleotideComposition(String seq) {
        processSeq(seq);
    }

    public long getNbA() {
        return nbA;
    }

    public long getNbC() {
        return nbC;
    }

    public long getNbG() {
        return nbG;
    }

    public long getNbT() {
        return nbT;
    }

    public long getNbN() {
        return nbN;
    }

    public long getNbUnknown() {
        return nbUnknown;
    }

    public long getTotal() {
        return nbA + nbC + nbG + nbT + nbN + nbUnknown;
    }

    public double getPercentA() {
        return (double)nbA / (double)getTotal();
    }

    public double getPercentC() {
        return (double)nbC / (double)getTotal();
    }

    public double getPercentG() {
        return (double)nbG / (double)getTotal();
    }

    public double getPercentT() {
        return (double)nbT / (double)getTotal();
    }

    public double getPercentN() {
        return (double)nbN / (double)getTotal();
    }

    public double getPercentUnknown() {
        return (double)nbUnknown / (double)getTotal();
    }

    protected final void processSeq(String seq) {

        String ucSeq = seq.toUpperCase();

        for(int i = 0; i < ucSeq.length(); i++) {
            char c = ucSeq.charAt(i);

            switch (c) {
                case 'A':
                    this.nbA++;
                    break;
                case 'C':
                    this.nbC++;
                    break;
                case 'G':
                    this.nbG++;
                    break;
                case 'T':
                    this.nbT++;
                    break;
                case 'N':
                    this.nbN++;
                    break;

                default:
                    this.nbUnknown++;
            }
        }
    }

    public void add(String seq) {
        NucleotideComposition nc = new NucleotideComposition(seq);

        this.nbA += nc.getNbA();
        this.nbC += nc.getNbC();
        this.nbG += nc.getNbG();
        this.nbT += nc.getNbT();
        this.nbN += nc.getNbN();
        this.nbUnknown += this.getNbUnknown();
    }

    public static String getStatsFileHeaderCounts() {
        return "a|c|g|t|n";
    }

    public static String getStatsFileHeaderPercents() {
        return "a.pc|c.pc|g.pc|t.pc|n.pc";
    }

    @Override
    public String toString() {
        return this.toString(Format.PERCENTAGES);
    }

    public String toString(Format format) {
        return format.output(this, "|");
    }

    public enum Format {

        COUNTS {
            @Override
            public String output(NucleotideComposition nc, String separator) {
                StringJoiner sj = new StringJoiner(separator);

                sj.add(nc.getNbA());
                sj.add(nc.getNbC());
                sj.add(nc.getNbG());
                sj.add(nc.getNbT());
                sj.add(nc.getNbN());

                return  sj.toString();
            }
        },
        PERCENTAGES {
            @Override
            public String output(NucleotideComposition nc, String separator) {
                StringJoiner sj = new StringJoiner(separator);

                sj.add(nc.getPercentA());
                sj.add(nc.getPercentC());
                sj.add(nc.getPercentG());
                sj.add(nc.getPercentT());
                sj.add(nc.getPercentN());

                return  sj.toString();
            }
        };

        public abstract String output(NucleotideComposition nc, String separator);
    }


    public NucleotideCompositionPercents convert() {

        return new NucleotideCompositionPercents(this.getPercentA(), this.getPercentC(), this.getPercentG(), this.getPercentT(), this.getPercentN());
    }
}
