package uk.ac.tgac.rampart.conan.process.lsf.tool;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.tgac.rampart.conan.parameter.tool.AbyssV134Params;
import uk.ac.tgac.rampart.conan.parameter.tool.AbyssV134Params.AbyssInputLibrariesParam;
import uk.ac.tgac.rampart.conan.process.lsf.AbstractRampartLsfProcess;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext.xml"})
public class LsfAbyssV134ProcessTest {

	@Test
	public void testGetCommand() {
		
		Map<String, File> peLibs = new HashMap<String,File>();
		
		AbyssInputLibrariesParam inputLibraries = new AbyssInputLibrariesParam();
		inputLibraries.setPairedEndLibraries(peLibs);
		
		AbyssV134Params params = new AbyssV134Params();
		params.setInputlibraries(inputLibraries);
		params.setKmer(61);
		params.setNbPairsForContigs(12);
		params.setNbPairsForScaffolds(11);
		params.setOutputBaseName("OUTPUT_FILE");
		params.setThreads(16);
		
		AbstractRampartLsfProcess process = new LsfAbyssV134Process(params);
		process.setMemoryRequired(150000);
		process.setProjectName("TEST");
		process.setJobName("TEST_ABYSS");
		process.setOpenmpi(true);
		process.setQueueName("lsf_testing");
		
		String command = process.getCommand(params.getParameterValuePairs());
		
		String correctCommand = "";
		
		assertTrue(command.equals(correctCommand));
	}

}
