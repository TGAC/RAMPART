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
package uk.ac.tgac.rampart.core.stats;

/**
 * User: maplesod
 * Date: 30/01/13
 * Time: 15:16
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

    protected final void processSeq(String seq) {

        for(int i = 0; i < seq.length(); i++) {
            char c = seq.charAt(i);

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
        this.nbUnknown += this.getNbUnknown();
    }
}
