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
package uk.ac.tgac.rampart.conan.process.r;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 28/02/13
 * Time: 17:27
 */
public class RV2122ProcessTest {

    @Test
    public void testRV2122() {

        List<String> rArgs = new ArrayList<String>();
        rArgs.add("arg1");
        rArgs.add("arg2");

        RV2122Args args = new RV2122Args();
        args.setArgs(rArgs);
        args.setOutput(new File("output.txt"));
        args.setScript(new File("script.R"));

        RV2122Process task = new RV2122Process(args);

        String command = task.getCommand();
        String correct = "Rscript  script.R  arg1 arg2  > output.txt";

        assertTrue(command != null && !command.isEmpty());
        assertTrue(correct != null && !correct.isEmpty());
        assertTrue(command.length() == correct.length());
        assertTrue(command.equals(correct));
    }
}
