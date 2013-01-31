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
package uk.ac.tgac.rampart.conan.tool.external.scaffold.sspace;

import org.junit.Test;

import java.io.File;

/**
 * User: maplesod
 * Date: 31/01/13
 * Time: 11:48
 */
public class SSpaceBasicV2ProcessTest {

    @Test
    public void testSSpaceBasicV2() {

        SSpaceBasicV2Args args = new SSpaceBasicV2Args();
        args.setLibraryFile(new File("testlib.lib"));
        args.setBowtieThreads(8);
        args.setInputContigFile(new File("contigs.fa"));

		/*AbstractRampartLsfProcess process = new SSpaceBasicV2LsfProcess();
		process.setProjectName("TEST");
		process.setJobName("TEST_SSPACE");
		process.setQueueName("lsf_testing");

		try {
			process.execute(args.getArgMap());
		}
		catch(Exception e) {
			e.printStackTrace();
			fail();
		}      */
    }
}
