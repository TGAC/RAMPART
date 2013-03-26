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
package uk.ac.tgac.rampart.conan.process.scaffold.sspace;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 28/02/13
 * Time: 11:36
 */
public class SSpaceBasicV2ProcessTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private String pwd;

    private File outputDir = temp.newFolder("sspaceTest");
    private static String correctCommand;
    private static String correctFullCommand;

    @Before
    public void setup() {

        String pwdFull = new File(".").getAbsolutePath();
        this.pwd = pwdFull.substring(0, pwdFull.length() - 1);

        correctCommand = "SSPACE_Basic_v2.0.pl -l " + pwd + "testlib.lib -s " + pwd + "contigs.fa -x 0 -T 8 -b Output";

        correctFullCommand = "cd " + pwd + "sspaceTest; " + correctCommand + " 2>&1; cd " + pwd;
    }

    private SSpaceBasicV2Process createSspaceProc() {

        File libFile = new File("testlib.lib");
        File contigsFile = new File("contigs.fa");

        SSpaceBasicV2Args args = new SSpaceBasicV2Args();
        args.setLibraryConfigFile(libFile);
        args.setThreads(8);
        args.setInputFile(contigsFile);
        args.setOutputDir(outputDir);
        args.setBaseName("Output");

        return new SSpaceBasicV2Process(args);
    }

    @Test
    public void testSSpaceBasicV2Command() {

        SSpaceBasicV2Process task = createSspaceProc();

        String command = task.getCommand();

        assertTrue(command != null && !command.isEmpty());
        assertTrue(correctCommand != null && !correctCommand.isEmpty());
        assertTrue(command.length() == correctCommand.length());
        assertTrue(command.equals(correctCommand));
    }

    @Test
    public void testSSpaceBasicV2FullCommand() {

        SSpaceBasicV2Process task = createSspaceProc();

        String command = task.getFullCommand();

        assertTrue(command != null && !command.isEmpty());
        assertTrue(correctFullCommand != null && !correctFullCommand.isEmpty());
        assertTrue(command.length() == correctFullCommand.length());
        assertTrue(command.equals(correctFullCommand));
    }
}
