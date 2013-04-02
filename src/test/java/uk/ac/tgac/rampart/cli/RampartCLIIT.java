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
package uk.ac.tgac.rampart.cli;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class RampartCLIIT {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void testRampart() throws URISyntaxException {

        File outputDir = temp.newFolder("rampartTest");

        File configFile = FileUtils.toFile(this.getClass().getResource("/tools/test_rampart_1.cfg"));


        RampartCLI.main(new String[]{
                "--config",
                configFile.getAbsolutePath(),
                "--output",
                outputDir.getAbsolutePath()
        });

    }

    @Test
    public void testHelp() throws URISyntaxException {

        RampartCLI.main(new String[]{
                "--help"
        });
    }

    @Test
    public void testClean() throws URISyntaxException {

        File outputDir = temp.newFolder("rampartCleanTest");

        new File(outputDir, "mass").mkdir();
        new File(outputDir, "reads").mkdir();
        new File(outputDir, "amp").mkdir();
        new File(outputDir, "random").mkdir();

        RampartCLI.main(new String[]{
                "--clean",
                outputDir.getAbsolutePath()
        });

        File[] outputDirContents = outputDir.listFiles();

        assertTrue(outputDirContents.length == 1);
        assertTrue(new File(outputDir, "random").exists());
    }

}
