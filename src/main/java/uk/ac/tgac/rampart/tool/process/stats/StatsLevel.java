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

import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.tgac.conan.process.asm.stats.CegmaV2_4Process;
import uk.ac.tgac.conan.process.asm.stats.QuastV2_2Process;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishCountV11Process;
import uk.ac.tgac.conan.process.kmer.kat.KatCompV1Process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: maplesod
 * Date: 23/08/13
 * Time: 11:34
 */
public enum StatsLevel {

    CONTIGUITY {
        @Override
        public boolean isOperational(ExecutionContext executionContext, ConanProcessService conanProcessService) {

            QuastV2_2Process proc = new QuastV2_2Process();
            proc.setConanProcessService(conanProcessService);
            return proc.isOperational(executionContext);
        }
    },
    COMPLETENESS {
        @Override
        public boolean isOperational(ExecutionContext executionContext, ConanProcessService conanProcessService) {
            CegmaV2_4Process proc = new CegmaV2_4Process();
            proc.setConanProcessService(conanProcessService);
            return proc.isOperational(executionContext);
        }
    },
    KMER {
        @Override
        public boolean isOperational(ExecutionContext executionContext, ConanProcessService conanProcessService) {
            JellyfishCountV11Process proc = new JellyfishCountV11Process();
            proc.setConanProcessService(conanProcessService);
            boolean jellyfish = proc.isOperational(executionContext);

            KatCompV1Process katProc = new KatCompV1Process();
            katProc.setConanProcessService(conanProcessService);
            boolean kat = katProc.isOperational(executionContext);

            return jellyfish && kat;
        }
    };

    public abstract boolean isOperational(ExecutionContext executionContext, ConanProcessService conanProcessService);


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
