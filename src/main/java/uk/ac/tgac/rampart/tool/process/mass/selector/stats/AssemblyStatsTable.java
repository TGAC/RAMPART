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

import org.apache.commons.io.FileUtils;
import uk.ac.tgac.conan.process.asm.stats.CegmaV2_4Report;
import uk.ac.tgac.conan.process.asm.stats.QuastV2_2Report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.io.FileUtils.readLines;

/**
 * User: maplesod
 * Date: 01/02/13
 * Time: 13:47
 */
public class AssemblyStatsTable extends ArrayList<AssemblyStats> {

    public AssemblyStatsTable() {
        super();
    }

    public AssemblyStatsTable(File statsFile) throws IOException {
        super();
        this.load(statsFile);
    }

    public AssemblyStatsTable(List<AssemblyStatsTable> tables) {
        super();
        this.mergeAll(tables);
    }

    public void mergeAll(List<AssemblyStatsTable> tables) {

        this.clear();

        for(AssemblyStatsTable table : tables) {
            this.addAll(table);
        }
    }

    protected final void load(File statsFile) throws IOException {

        if (statsFile == null || !statsFile.exists())
            throw new IOException("Stats File doesn't exist");

        List<String> lines = readLines(statsFile);

        for(String line : lines.subList(1, lines.size())) {

            String trimmedLine = line.trim();

            if (!trimmedLine.isEmpty()) {
                String[] parts = trimmedLine.split("\\|");

                AssemblyStats stats = new AssemblyStats(parts);

                this.add(stats);
            }
        }
    }

    public void save(File outputFile) throws IOException {

        List<String> lines = new ArrayList<>();

        // Add the header line
        lines.add(AssemblyStats.getStatsFileHeader());

        // Add the data
        for(AssemblyStats stats : this) {
            lines.add(stats.toString());
        }

        // Write data to disk
        FileUtils.writeLines(outputFile, lines);
    }

    public AssemblyStatsMatrix generateStatsMatrix() {

        AssemblyStatsMatrix matrix = new AssemblyStatsMatrix(this);

        return matrix;
    }

    public void addScores(double[] scores) {

        int i = 0;
        for(AssemblyStats stats : this) {
            stats.setScore(scores[i++]);
        }
    }

    public AssemblyStats getBest() {

        double max = 0.0;
        AssemblyStats best = null;

        for(AssemblyStats stats : this) {

            if (stats.getScore() > max) {
                best = stats;
                max = stats.getScore();
            }
        }

        return best;
    }

    public AssemblyStats findStatsByDescription(String description) {

        for(AssemblyStats stats : this) {
            if (stats.getDesc().equalsIgnoreCase(description)) {
                return stats;
            }
        }

        return null;
    }

    /**
     * Overrides any existing entries with results from Quast.  If quast results contain unknown entries then they are
     * created
     * @param quastReportFile
     * @param assemblyDir
     * @throws IOException
     */
    public void mergeWithQuastResults(File quastReportFile, File assemblyDir, String massGroup) throws IOException {

        QuastV2_2Report quastReport = new QuastV2_2Report(quastReportFile);

        for(QuastV2_2Report.QuastV2_2AssemblyStats qStats : quastReport.getStatList()) {

            if (!qStats.getName().endsWith("broken")) {

                AssemblyStats stats = this.findStatsByDescription(qStats.getName());

                // If not found then create a new entry
                if (stats == null) {
                    stats = new AssemblyStats();
                    stats.setDesc(qStats.getName());
                    stats.setFilePath(new File(assemblyDir, qStats.getName() + ".fa").getAbsolutePath());
                    stats.setDataset(massGroup);
                    this.add(stats);
                }

                // Override attributes
                stats.setN50(qStats.getN50());
                stats.setL50(qStats.getL50());
                stats.setMaxLen(qStats.getLargestContig());
                stats.setGcPercentage(qStats.getGcPc());
                stats.setNbSeqs(qStats.getNbContigsGt0());
                stats.setNbSeqsGt1K(qStats.getNbContigsGt1k());
                stats.setNbBases(qStats.getTotalLengthGt0());
                stats.setNbBasesGt1K(qStats.getTotalLengthGt1k());
                stats.setNPercentage(qStats.getNsPer100k() / 1000.0);
            }
        }
    }

    public void mergeWithCegmaResults(File cegmaFile, File assemblyFile, String description, String massGroup) throws IOException {

        CegmaV2_4Report cegmaReport = new CegmaV2_4Report(cegmaFile);

        AssemblyStats stats = this.findStatsByDescription(description);

        if (stats == null) {
            stats = new AssemblyStats();
            stats.setDesc(description);
            stats.setFilePath(assemblyFile.getAbsolutePath());
            stats.setDataset(massGroup);
            this.add(stats);
        }

        stats.setCompletenessPercentage(cegmaReport.getPcComplete());
    }
}
