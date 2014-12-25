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

import org.apache.commons.lang.ArrayUtils;
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

    private ContiguityMetrics contiguity;
    private ProblemMetrics problems;
    private ConservationMetrics conservation;

    private double finalScore;

    public AssemblyStats()
    {
        this.index = 0;
        this.desc = "";
        this.dataset = "";
        this.filePath = "";
        this.bubblePath = "";
        this.contiguity = new ContiguityMetrics();
        this.problems = new ProblemMetrics();
        this.conservation = new ConservationMetrics();
        this.finalScore = 0.0;
    }

    public AssemblyStats(String[] stats) {
        this();

        int i = 0;
        this.index = Integer.parseInt(stats[i++]);
        this.desc = stats[i++];
        this.dataset = stats[i++];
        this.filePath = stats[i++];
        this.bubblePath = stats[i++];
        this.contiguity.parseStrings((String[])ArrayUtils.subarray(stats, i, i + this.contiguity.getNbMetrics()));      i += this.contiguity.getNbMetrics();
        this.problems.parseStrings((String[])ArrayUtils.subarray(stats, i, i + this.problems.getNbMetrics()));          i += this.problems.getNbMetrics();
        this.conservation.parseStrings((String[])ArrayUtils.subarray(stats, i, i + this.conservation.getNbMetrics()));  i += this.conservation.getNbMetrics();
        this.finalScore = Double.parseDouble(stats[i++]);
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

    public ContiguityMetrics getContiguity() {
        return contiguity;
    }

    public void setContiguity(ContiguityMetrics contiguity) {
        this.contiguity = contiguity;
    }

    public ProblemMetrics getProblems() {
        return problems;
    }

    public void setProblems(ProblemMetrics problems) {
        this.problems = problems;
    }

    public ConservationMetrics getConservation() {
        return conservation;
    }

    public void setConservation(ConservationMetrics conservation) {
        this.conservation = conservation;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    public String getStatsFileHeader() {
        return "index\tdesc\tdataset\tasm_path\tbubble_path\t" +
                this.contiguity.getTabHeader() + "\t" +
                this.problems.getTabHeader() + "\t" +
                this.conservation.getTabHeader() + "\t" +
                "final_score";
    }


    public String toTabString() {

        StringJoiner sj = new StringJoiner("\t");

        sj.add(this.getIndex());
        sj.add(this.getDesc());
        sj.add(this.getDataset());
        sj.add(this.getFilePath());
        sj.add(this.getBubblePath() == null || this.getBubblePath().trim().isEmpty() ? "NA" : this.getBubblePath());
        sj.add(this.contiguity.toTabString());
        sj.add(this.problems.toTabString());
        sj.add(this.conservation.toTabString());
        sj.add(this.getFinalScore());

        return sj.toString();
    }

    @Override
    public String toString() {

        StringJoiner sj = new StringJoiner("\n");
        sj.add("Assembly #: " + this.index);
        sj.add("Description: " + this.desc);
        sj.add("Dataset: " + this.dataset);
        sj.add("Path to Assembly: " + this.filePath);
        sj.add("Path to Bubble file (if present): " + (this.getBubblePath() == null || this.getBubblePath().trim().isEmpty() ? "NA" : this.getBubblePath()));
        sj.add(this.contiguity.toString());
        sj.add(this.problems.toString());
        sj.add(this.conservation.toString());
        sj.add("--------------");
        sj.add("Final Score: " + this.getFinalScore());

        return sj.toString();
    }

    @Override
    public int compareTo(AssemblyStats o) {

        Double thisScore = this.finalScore;
        Double thatScore = o.getFinalScore();

        return thisScore.compareTo(thatScore);
    }

    public static class IndexComparator implements Comparator<AssemblyStats> {

        @Override
        public int compare(AssemblyStats o1, AssemblyStats o2) {
            return o1.getIndex() < o2.getIndex() ? -1 : o1.getIndex() == o2.getIndex() ? 0 : 1;
        }
    }

}
