/*
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
 */

package uk.ac.tgac.rampart.stage.analyse.asm.analysers;

import org.junit.Test;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStats;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStatsTable;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class CegmaAsmAnalyserTest {

    //@Test
    public void testUpdate() throws IOException {

        CegmaAsmAnalyser c = new CegmaAsmAnalyser();

        AssemblyStatsTable t = new AssemblyStatsTable();

        AssemblyStats s = new AssemblyStats();
        s.setDataset("amp");
        s.setIndex(0);
        s.setDesc("stage-4");
        t.add(s);

        File reportDir = new File("/home/maplesod/mnt/cluster_new_scratch/rampart_tests/arabidopsis/8-amp-analyses/cegma");

        c.updateTable(t, reportDir);

    }

}