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

import uk.ac.tgac.rampart.core.data.AssemblyStats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * User: maplesod
 * Date: 30/01/13
 * Time: 14:56
 */
public class AssemblyLengthAnalyser {

    public AssemblyStats analyse(File in) throws IOException {

        if (in == null || !in.exists()) {
             throw new IOException("Input file does not exist");
        }


        BufferedReader reader = new BufferedReader(new FileReader(in));


        long totalNbBases = 0;
        NucleotideComposition nc = new NucleotideComposition();
        List<Long> lengths = new ArrayList<Long>();

        // Ignore everything but the sequences
        // While loop handles multi-line sequences
        String line = null;
        boolean firstLine = true;
        long nbSeqBases = 0;
        while ((line = reader.readLine()) != null) {

            if (!line.isEmpty()) {
                char firstChar = line.charAt(0);


                // If we have found a header line then increment analyser for this seq (unless this is the first time here)
                if (firstChar == '>') {

                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }

                    totalNbBases += nbSeqBases;
                    lengths.add(nbSeqBases);
                    nbSeqBases = 0;
                }
                else {
                    nc.add(line);
                    nbSeqBases += line.length();
                }
            }
        }

        reader.close();


        Collections.sort(lengths, new Comparator<Long>() {
            @Override
            public int compare(Long o1, Long o2) {
                return o2.compareTo(o1);
            }
        });

        long n20 = 0;
        long n50 = 0;
        long n80 = 0;
        long l20 = 0;
        long l50 = 0;
        long l80 = 0;

        boolean foundN20 = false;
        boolean foundN50 = false;
        boolean foundN80 = false;

        long cumlativeLength = 0;
        long seqNb = 0;

        for(Long seqLength : lengths) {
            cumlativeLength += seqLength;
            seqNb++;

            if (!foundN20 && cumlativeLength >= totalNbBases * 0.2) {
                n20 = seqLength;
                l20 = seqNb;
                foundN20 = true;
            }

            if (!foundN50 && cumlativeLength >= totalNbBases * 0.5) {
                n50 = seqLength;
                l50 = seqNb;
                foundN50 = true;
            }

            if (!foundN80 && cumlativeLength >= totalNbBases * 0.8) {
                n80 = seqLength;
                l80 = seqNb;
                foundN80 = true;
            }
        }

        AssemblyStats stats = new AssemblyStats();
        stats.setNbContigs((long) lengths.size());
        stats.setNbBases(totalNbBases);
        stats.setMinLen(lengths.get(lengths.size()-1));
        stats.setAvgLen((double)totalNbBases / (double)lengths.size());
        stats.setMaxLen(lengths.get(0));
        stats.setN20(n20);
        stats.setN50(n50);
        stats.setN80(n80);
        stats.setL50(l50);
        stats.setaPerc((double)nc.getNbA() / (double)totalNbBases);
        stats.setcPerc((double)nc.getNbC() / (double)totalNbBases);
        stats.setgPerc((double)nc.getNbG() / (double)totalNbBases);
        stats.settPerc((double)nc.getNbT() / (double)totalNbBases);
        stats.setnPerc((double)nc.getNbN() / (double)totalNbBases);

        return stats;
    }
}
