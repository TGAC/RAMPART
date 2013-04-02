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
package uk.ac.tgac.rampart.pipeline.cli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.tgac.rampart.core.data.RampartJobFileStructure;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 28/02/13
 * Time: 15:57
 */
public class RampartCLITest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();


    @Test
    public void testCleanJob() {

        File jobDir = temp.newFolder("rampartJobTest");
        RampartJobFileStructure jobFileStructure = new RampartJobFileStructure(jobDir);

        jobFileStructure.getReadsDir().mkdir();
        jobFileStructure.getMassDir().mkdir();
        jobFileStructure.getImproverDir().mkdir();
        jobFileStructure.getReportDir().mkdir();

        File noDelDir = new File(jobDir, "noDel");
        noDelDir.mkdir();

        RampartCLI.main(new String[] {"--clean", jobDir.getAbsolutePath()});

        assertTrue(!jobFileStructure.getReadsDir().exists());
        assertTrue(!jobFileStructure.getMassDir().exists());
        assertTrue(!jobFileStructure.getImproverDir().exists());
        assertTrue(!jobFileStructure.getReportDir().exists());
        assertTrue(noDelDir.exists());
    }

}
