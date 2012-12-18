package uk.ac.tgac.rampart.conan.process.lsf.tool;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
import uk.ac.tgac.rampart.conan.process.lsf.AbstractRampartLsfProcess;
import uk.ac.tgac.rampart.conan.tool.abyss.AbyssV134Args;
import uk.ac.tgac.rampart.conan.tool.abyss.AbyssV134InputLibsArg;
import uk.ac.tgac.rampart.conan.tool.abyss.AbyssV134LsfProcess;
import uk.ac.tgac.rampart.conan.tool.sspace.SSpaceBasicV2Args;
import uk.ac.tgac.rampart.conan.tool.sspace.SSpaceBasicV2LsfProcess;
import uk.ac.tgac.rampart.util.ToolCommandLoader;
import uk.ac.tgac.rampart.util.Tools;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext.xml"})
public class LsfProcessTests {

	@Before
	public void setup() throws URISyntaxException, IOException {
		
		Tools.configureProperties();
	}
	
	@Test
	public void testAbyssV134() {
		
		Map<String, File> peLibs = new HashMap<String,File>();
		
		AbyssV134InputLibsArg inputLibraries = new AbyssV134InputLibsArg();
		inputLibraries.setPairedEndLibraries(peLibs);
		
		AbyssV134Args args = new AbyssV134Args();
		args.setInputlibraries(inputLibraries);
		args.setKmer(61);
		args.setName("OUTPUT_FILE");
		args.setThreads(16);
		
		AbstractRampartLsfProcess process = new AbyssV134LsfProcess();
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
		}
	}
	
	@Test
	public void testSSpaceBasicV2() {
		
		SSpaceBasicV2Args args = new SSpaceBasicV2Args();
		args.setLibraryFile(new File("testlib.lib"));
		args.setBowtieThreads(8);
		args.setContigFile(new File("contigs.fa"));
		
		AbstractRampartLsfProcess process = new SSpaceBasicV2LsfProcess();
		process.setProjectName("TEST");
		process.setJobName("TEST_SSPACE");
		process.setQueueName("lsf_testing");
		
		try {
			process.execute(args.getParameterValuePairs());
		}
		catch(Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
