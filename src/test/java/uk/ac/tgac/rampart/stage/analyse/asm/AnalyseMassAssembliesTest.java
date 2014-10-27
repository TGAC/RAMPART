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

package uk.ac.tgac.rampart.stage.analyse.asm;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.tgac.conan.process.asm.KmerRange;
import uk.ac.tgac.rampart.MockedConanProcess;
import uk.ac.tgac.rampart.stage.MassJob;
import uk.ac.tgac.rampart.stage.util.CoverageRange;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class AnalyseMassAssembliesTest extends MockedConanProcess {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void createTableTest() throws IOException {

        /// Fake the output directories
        File massDir = temp.newFolder("mass");
        File scaffoldsDir = new File(massDir, "mj1/scaffolds");
        scaffoldsDir.mkdirs();

        MassJob.Args mjArgs = new MassJob.Args();
        mjArgs.setThreads(4);
        mjArgs.setTool("ABYSS_V1.5");
        mjArgs.setName("mj1");
        mjArgs.setKmerRange(new KmerRange(31, 51, 10));
        mjArgs.setCoverageRange(new CoverageRange("-1"));
        mjArgs.initialise();

        List<MassJob.Args> massJobs = new ArrayList<>();
        massJobs.add(mjArgs);

        AnalyseMassAssemblies.Args args = new AnalyseMassAssemblies.Args();
        args.setMassJobs(massJobs);
        args.setMassDir(massDir);

        //AnalyseMassAssemblies ama = new AnalyseMassAssemblies(this.conanExecutorService, args);

        //AssemblyStatsTable table = ama.createTable();

        assertTrue(true);
    }

}