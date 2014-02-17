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

public class RampartCLIITCase {

    /*private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
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
    }  */


    // Not running this for now as a System.exit call will screw up the integration testing.
    /*@Test
    public void testRampart() throws URISyntaxException, IOException {

        File outputDir = temp.newFolder("rampartTest");

        File configFile = FileUtils.toFile(this.getClass().getResource("/config/test_rampart_config.xml"));


        RampartCLI.main(new String[]{
                "run",
                "--output",
                outputDir.getAbsolutePath(),
                configFile.getAbsolutePath()
        });

    }*/


}
