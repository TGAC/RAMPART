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
package uk.ac.tgac.rampart.conan.conanx.env;

import org.junit.Test;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.env.arch.scheduler.LSF;
import uk.ac.tgac.rampart.conan.conanx.env.arch.scheduler.LSFArgs;
import uk.ac.tgac.rampart.conan.conanx.env.locality.Local;
import uk.ac.tgac.rampart.conan.service.ProcessExecutionService;
import uk.ac.tgac.rampart.conan.service.impl.DefaultProcessExecutionService;
import uk.ac.tgac.rampart.conan.tool.external.asm.abyss.AbyssV134Args;
import uk.ac.tgac.rampart.conan.tool.external.asm.abyss.AbyssV134Process;
import uk.ac.tgac.rampart.conan.tool.external.scaffold.sspace.SSpaceBasicV2Args;
import uk.ac.tgac.rampart.conan.util.PETestLibrary;
import uk.ac.tgac.rampart.core.data.Library;

import java.io.File;
import java.net.ConnectException;
import java.util.Set;

public class LSFEnvironmentTests {


	@Test
	public void testAbyssV134() throws InterruptedException, ProcessExecutionException, ConnectException {
		
		Set<Library> libs = new PETestLibrary().createPETestLibrary();
		
		AbyssV134Args args = new AbyssV134Args();
		args.setLibraries(libs);
		args.setKmer(61);
		args.setName("OUTPUT_FILE");
		args.setThreads(16);

        AbyssV134Process abyss = new AbyssV134Process(args);
        abyss.addPreCommand("source abyss_cb-1.3.4");

        LSFArgs envArgs = new LSFArgs();
        envArgs.setCmdLineOutputFile(new File("~/test/rampart-conan/output.log"));
        envArgs.setMemoryMB(60000);
        envArgs.setThreads(16);
        envArgs.setJobName("testAbyssV134");
        envArgs.setOpenmpi(true);
        envArgs.setProjectName("test");
        envArgs.setExtraLsfOptions("-Rselect[hname!='n57142.tgaccluster']"); // Abyss doesn't like this node

        Environment env = new DefaultEnvironment(
                new Local(),
                new LSF(),
                envArgs);

        ProcessExecutionService exec = new DefaultProcessExecutionService();

        exec.execute(abyss, env);
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
