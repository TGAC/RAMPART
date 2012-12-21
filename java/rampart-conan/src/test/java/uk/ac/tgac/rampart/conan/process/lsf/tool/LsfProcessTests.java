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
package uk.ac.tgac.rampart.conan.process.lsf.tool;

import org.junit.Before;
import org.junit.Test;
import uk.ac.tgac.rampart.conan.cli.Configure;
import uk.ac.tgac.rampart.conan.tool.abyss.AbyssV134Args;
import uk.ac.tgac.rampart.conan.tool.sspace.SSpaceBasicV2Args;
import uk.ac.tgac.rampart.core.data.Library;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

public class LsfProcessTests {

	@Before
	public void setup() throws URISyntaxException, IOException {
		
		Configure.configureProperties();
	}
	
	@Test
	public void testAbyssV134() {
		
		Set<Library> libs = new HashSet<Library>();
		
		AbyssV134Args args = new AbyssV134Args();
		args.setLibraries(libs);
		args.setKmer(61);
		args.setName("OUTPUT_FILE");
		args.setThreads(16);
		
		/*AbstractRampartLsfProcess process = new AbyssV134LsfProcess();
		process.setMemoryRequired(150000);
		process.setProjectName("TEST");
		process.setJobName("TEST_ABYSS");
		process.setOpenmpi(true);
		process.setQueueName("lsf_testing");
		
		try {
			process.execute(args.getParameterValuePairs());
		}
		catch(Exception e) {
			e.printStackTrace();
			fail();
		}               */
	}
	
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
			process.execute(args.getParameterValuePairs());
		}
		catch(Exception e) {
			e.printStackTrace();
			fail();
		}      */
	}

}
