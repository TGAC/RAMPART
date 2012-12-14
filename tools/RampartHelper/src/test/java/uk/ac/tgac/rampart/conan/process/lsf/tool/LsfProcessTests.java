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
import uk.ac.tgac.rampart.conan.parameter.tool.AbyssV134Params;
import uk.ac.tgac.rampart.conan.parameter.tool.AbyssV134Params.AbyssInputLibrariesParam;
import uk.ac.tgac.rampart.conan.process.lsf.AbstractRampartLsfProcess;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext.xml"})
public class LsfProcessTests {

	@Before
	public void setup() throws URISyntaxException, IOException {
		
		// Setup logging
		BasicConfigurator.configure();
		
		// Tell conan about the properties file
		File conanPropertiesFile = new File(ClassLoader.getSystemResource("conan.properties").toURI());
		ConanProperties.getConanProperties().setPropertiesFile(conanPropertiesFile);
	}
	
	@Test
	public void testAbyssV134() {
		
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
		
		try {
			process.execute(params.getParameterValuePairs());
		}
		catch(Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testSSpaceBasicV2() {
		
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
		
		try {
			boolean bool = process.execute(params.getParameterValuePairs());
		}
		catch(Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
