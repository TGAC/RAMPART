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

import org.apache.commons.lang3.ArrayUtils;
import uk.ac.ebi.fgpt.conan.util.StringJoiner;

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
 * Date: 22/04/13
 * Time: 15:04
 */
public class AssemblyStats implements Comparable<AssemblyStats> {

    private int index;
    private String desc;
    private String dataset;
    private String filePath;
    private long nbSeqs;
    private long nbBases;
    private NucleotideCompositionPercents ncPercents;
    private long minLen;
    private double avgLen;
    private long maxLen;
    private long n80;
    private long n50;
    private long n20;
    private long l50;
    private double gcPercentage;
    private double completenessPercentage;
    private double score;

    public AssemblyStats()
    {
        this.index = 0;
        this.desc = "";
        this.dataset = "";
        this.filePath = "";
        this.nbSeqs = 0;
        this.ncPercents = new NucleotideCompositionPercents();
        this.nbBases = 0L;
        this.minLen = 0L;
        this.avgLen = 0.0;
        this.maxLen = 0L;
        this.n80 = 0L;
        this.n50 = 0L;
        this.n20 = 0L;
        this.l50 = 0L;
        this.gcPercentage = 0.0;
        this.completenessPercentage = 0.0;
        this.score = 0.0;
    }

    public AssemblyStats(String[] stats) {
        this.index = Integer.parseInt(stats[0]);
        this.desc = stats[1];
        this.dataset = stats[2];
        this.filePath = stats[3];
        this.nbSeqs = Long.parseLong(stats[4]);
        this.nbBases = Long.parseLong(stats[5]);
        this.ncPercents = new NucleotideCompositionPercents(ArrayUtils.subarray(stats, 6, 11));
        this.minLen = Long.parseLong(stats[11]);
        this.avgLen = Double.parseDouble(stats[12]);
        this.maxLen = Long.parseLong(stats[13]);
        this.n80 = Long.parseLong(stats[14]);
        this.n50 = Long.parseLong(stats[15]);
        this.n20 = Long.parseLong(stats[16]);
        this.l50 = Long.parseLong(stats[17]);
        this.gcPercentage = Double.parseDouble(stats[18]);
        this.completenessPercentage = Double.parseDouble(stats[19]);
        this.score = Double.parseDouble(stats[19]);
    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getNbSeqs() {
        return nbSeqs;
    }

    public void setNbSeqs(long nbSeqs) {
        this.nbSeqs = nbSeqs;
    }

    public long getNbBases() {
        return nbBases;
    }

    public void setNbBases(long nbBases) {
        this.nbBases = nbBases;
    }

    public NucleotideCompositionPercents getNcPercents() {
        return ncPercents;
    }

    public void setNcPercents(NucleotideCompositionPercents nucleotideComposition) {
        this.ncPercents = nucleotideComposition;
    }

    public long getMinLen() {
        return minLen;
    }

    public void setMinLen(long minLen) {
        this.minLen = minLen;
    }

    public double getAvgLen() {
        return avgLen;
    }

    public void setAvgLen(double avgLen) {
        this.avgLen = avgLen;
    }

    public long getMaxLen() {
        return maxLen;
    }

    public void setMaxLen(long maxLen) {
        this.maxLen = maxLen;
    }

    public long getN80() {
        return n80;
    }

    public void setN80(long n80) {
        this.n80 = n80;
    }

    public long getN50() {
        return n50;
    }

    public void setN50(long n50) {
        this.n50 = n50;
    }

    public long getN20() {
        return n20;
    }

    public void setN20(long n20) {
        this.n20 = n20;
    }

    public long getL50() {
        return l50;
    }

    public void setL50(long l50) {
        this.l50 = l50;
    }

    public double getGcPercentage() {
        return gcPercentage;
    }

    public void setGcPercentage(double gcPercentage) {
        this.gcPercentage = gcPercentage;
    }

    public double getCompletenessPercentage() {
        return completenessPercentage;
    }

    public void setCompletenessPercentage(double completenessPercentage) {
        this.completenessPercentage = completenessPercentage;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public static String getStatsFileHeader() {
        return "index|desc|dataset|file|nb_seqs|nb_bases|" + NucleotideCompositionPercents.getStatsFileHeaderPercents() +"|min_len|avg_len|max_len|n80|n50|n20|l50|gc|completeness|score";
    }

    public void setFromFileName(String filename) {
        String[] parts = filename.split("-");

        if (parts.length < 4)
            throw new IllegalArgumentException("Assembly file name does not conform to expected format");

        this.dataset = parts[0];
        this.desc = parts[1];
        this.index = Integer.parseInt(parts[2]);
    }

    @Override
    public String toString() {

        StringJoiner sj = new StringJoiner("|");

        sj.add(this.getIndex());
        sj.add(this.getDesc());
        sj.add(this.getDataset());
        sj.add(this.getFilePath());
        sj.add(this.getNbSeqs());
        sj.add(this.getNbBases());
        sj.add(this.getNcPercents().toString());
        sj.add(this.getMinLen());
        sj.add(this.getAvgLen());
        sj.add(this.getMaxLen());
        sj.add(this.getN80());
        sj.add(this.getN50());
        sj.add(this.getN20());
        sj.add(this.getL50());
        sj.add(this.getGcPercentage());
        sj.add(this.getCompletenessPercentage());
        sj.add(this.getScore());

        return sj.toString();
    }

    @Override
    public int compareTo(AssemblyStats o) {

        Double thisScore = new Double(this.score);
        Double thatScore = new Double(o.getScore());

        return thisScore.compareTo(thatScore);
    }

    public static class IndexComparator implements Comparator<AssemblyStats> {

        @Override
        public int compare(AssemblyStats o1, AssemblyStats o2) {
            return o1.getIndex() < o2.getIndex() ? -1 : o1.getIndex() == o2.getIndex() ? 0 : 1;
        }
    }
}
