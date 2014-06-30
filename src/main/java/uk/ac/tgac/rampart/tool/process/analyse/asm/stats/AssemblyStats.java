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

import java.util.Comparator;

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
    private String bubblePath;
    private long nbSeqs;
    private long nbSeqsGt1K;
    private long nbBases;
    private long nbBasesGt1K;
    private long maxLen;
    private long n50;
    private long l50;
    private double gcPercentage;
    private double nPercentage;
    private double completenessPercentage;
    private double score;

    public AssemblyStats()
    {
        this.index = 0;
        this.desc = "";
        this.dataset = "";
        this.filePath = "";
        this.bubblePath = "";
        this.nbSeqs = 0;
        this.nbSeqsGt1K = 0;
        this.nbBases = 0L;
        this.nbBasesGt1K = 0L;
        this.maxLen = 0L;
        this.n50 = 0L;
        this.l50 = 0L;
        this.gcPercentage = 0.0;
        this.nPercentage = 0.0;
        this.completenessPercentage = 0.0;
        this.score = 0.0;
    }

    public AssemblyStats(String[] stats) {
        int i = 0;
        this.index = Integer.parseInt(stats[i++]);
        this.desc = stats[i++];
        this.dataset = stats[i++];
        this.filePath = stats[i++];
        this.bubblePath = stats[i++];
        this.nbSeqs = Long.parseLong(stats[i++]);
        this.nbSeqsGt1K = Long.parseLong(stats[i++]);
        this.nbBases = Long.parseLong(stats[i++]);
        this.nbBasesGt1K = Long.parseLong(stats[i++]);
        this.maxLen = Long.parseLong(stats[i++]);
        this.n50 = Long.parseLong(stats[i++]);
        this.l50 = Long.parseLong(stats[i++]);
        this.gcPercentage = Double.parseDouble(stats[i++]);
        this.nPercentage = Double.parseDouble(stats[i++]);
        this.completenessPercentage = Double.parseDouble(stats[i++]);
        this.score = Double.parseDouble(stats[i++]);
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

    public String getBubblePath() {
        return bubblePath;
    }

    public void setBubblePath(String bubblePath) {
        this.bubblePath = bubblePath;
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

    public long getNbSeqsGt1K() {
        return nbSeqsGt1K;
    }

    public void setNbSeqsGt1K(long nbSeqsGt1K) {
        this.nbSeqsGt1K = nbSeqsGt1K;
    }

    public long getNbBasesGt1K() {
        return nbBasesGt1K;
    }

    public void setNbBasesGt1K(long nbBasesGt1K) {
        this.nbBasesGt1K = nbBasesGt1K;
    }

    public double getnPercentage() {
        return nPercentage;
    }

    public void setnPercentage(double nPercentage) {
        this.nPercentage = nPercentage;
    }

    public double getNPercentage() {
        return nPercentage;
    }

    public void setNPercentage(double nPercentage) {
        this.nPercentage = nPercentage;
    }

    public long getMaxLen() {
        return maxLen;
    }

    public void setMaxLen(long maxLen) {
        this.maxLen = maxLen;
    }

    public long getN50() {
        return n50;
    }

    public void setN50(long n50) {
        this.n50 = n50;
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
        return "index|desc|dataset|asm_path|bubble_path|nb_seqs|nb_seqs_gt_1k|nb_bases|nb_bases_gt_1k|max_len|n50|l50|gc%|n%|completeness|score";
    }


    @Override
    public String toString() {

        StringJoiner sj = new StringJoiner("|");

        sj.add(this.getIndex());
        sj.add(this.getDesc());
        sj.add(this.getDataset());
        sj.add(this.getFilePath());
        sj.add(this.getBubblePath());
        sj.add(this.getNbSeqs());
        sj.add(this.getNbSeqsGt1K());
        sj.add(this.getNbBases());
        sj.add(this.getNbBasesGt1K());
        sj.add(this.getMaxLen());
        sj.add(this.getN50());
        sj.add(this.getL50());
        sj.add(this.getGcPercentage());
        sj.add(this.getNPercentage());
        sj.add(this.getCompletenessPercentage());
        sj.add(this.getScore());

        return sj.toString();
    }

    @Override
    public int compareTo(AssemblyStats o) {

        Double thisScore = this.score;
        Double thatScore = o.getScore();

        return thisScore.compareTo(thatScore);
    }

    public static class IndexComparator implements Comparator<AssemblyStats> {

        @Override
        public int compare(AssemblyStats o1, AssemblyStats o2) {
            return o1.getIndex() < o2.getIndex() ? -1 : o1.getIndex() == o2.getIndex() ? 0 : 1;
        }
    }
}
