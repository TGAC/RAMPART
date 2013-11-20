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
package uk.ac.tgac.rampart.tool.process.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: maplesod
 * Date: 23/08/13
 * Time: 11:34
 */
public enum StatsLevel {

    CONTIGUITY,
    COMPLETENESS,
    KMER;


    public static List<StatsLevel> parseList(String statsLevelsString) {

        List<StatsLevel> allStats = new ArrayList<>();

        String[] statsLevels = statsLevelsString.split(",");

        for(String level : statsLevels) {
            allStats.add(StatsLevel.valueOf(level.trim().toUpperCase()));
        }

        return allStats;
    }

    public static List<StatsLevel> createAll() {

        List<StatsLevel> allStats = new ArrayList<>();

        Collections.addAll(allStats, StatsLevel.values());

        return allStats;
    }
}
