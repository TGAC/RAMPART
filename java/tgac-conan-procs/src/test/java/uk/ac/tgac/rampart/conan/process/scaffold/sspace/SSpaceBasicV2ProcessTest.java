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

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 28/02/13
 * Time: 11:36
 */
public class SSpaceBasicV2ProcessTest {

    @Test
    public void testSSpaceBasicV2() {

        SSpaceBasicV2Args args = new SSpaceBasicV2Args();
        args.setLibraryConfigFile(new File("testlib.lib"));
        args.setThreads(8);
        args.setInput(new File("contigs.fa"));
        args.setBaseName("Output");

        SSpaceBasicV2Process task = new SSpaceBasicV2Process(args);

        String command = task.getCommand();

        //assertTrue(command.equals("SSPACE_Basic_v2.0.pl -T 8 -b Output -s contigs.fa -l testlib.lib -x 0"));
    }
}
