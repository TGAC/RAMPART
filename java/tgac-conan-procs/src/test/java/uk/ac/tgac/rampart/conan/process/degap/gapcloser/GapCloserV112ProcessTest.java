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
package uk.ac.tgac.rampart.conan.process.degap.gapcloser;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 28/02/13
 * Time: 16:40
 */
public class GapCloserV112ProcessTest {

    private String pwd;

    @Before
    public void setup() {

        String pwdFull = new File(".").getAbsolutePath();
        this.pwd = pwdFull.substring(0, pwdFull.length() - 1);
    }

    @Test
    public void testGapCloserV112() throws InterruptedException, ProcessExecutionException, IOException, CommandExecutionException {

        GapCloserV112Args args = new GapCloserV112Args();
        args.setOutput(new File("output.fa"));
        args.setInput(new File("input.fa"));
        args.setThreads(8);
        args.setOverlap(29);
        args.setLibraryFile(new File("libFile.cfg"));

        GapCloserV112Process gc = new GapCloserV112Process(args);

        String command = gc.getCommand();
        String correct = "GapCloser " +
                "-a " + pwd + "input.fa " +
                "-b " + pwd + "libFile.cfg " +
                "-o " + pwd + "output.fa " +
                "-p 29 -t 8";

        assertTrue(command != null && !command.isEmpty());
        assertTrue(correct != null && !correct.isEmpty());
        assertTrue(command.length() == correct.length());
        assertTrue(command.equals(correct));
    }
}
