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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.tgac.rampart.RampartCLI;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URISyntaxException;

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 28/02/13
 * Time: 15:57
 */
public class RampartCLITest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();


    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
        System.setErr(null);
    }


    @Test
    public void testHelp() throws URISyntaxException {

        RampartCLI.main(new String[]{
                "--help"
        });
    }



    /*@Test
    public void testAccessInternalResources() throws IOException {

        File resDir = temp.newFolder("rampartResources");

        // Copy resources to external system
        File internalScripts = FileUtils.toFile(RampartCLI.class.getResource("/scripts"));
        File internalData = FileUtils.toFile(RampartCLI.class.getResource("/data"));
        File internalReport = FileUtils.toFile(RampartCLI.class.getResource("/data/report"));

        File externalScriptsDir = new File(resDir, "scripts");
        File externalDataDir = new File(resDir, "data");
        File externalReportDir = new File(resDir, "data/report");

        FileUtils.copyDirectory(internalScripts, externalScriptsDir);
        FileUtils.copyDirectory(internalData, externalDataDir);
        FileUtils.copyDirectory(internalReport, externalReportDir);

        assertTrue(externalScriptsDir.exists());
        assertTrue(externalDataDir.exists());
        assertTrue(externalReportDir.exists());
    }   */

    @Test
    public void testFile() {
        File pwd = new File(".").getAbsoluteFile().getParentFile();

        assertTrue(pwd.exists());
    }

}
