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

        List<String> lines = new ArrayList<String>();

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
}
