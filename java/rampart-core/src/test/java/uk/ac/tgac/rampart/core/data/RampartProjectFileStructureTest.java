package uk.ac.tgac.rampart.core.data;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Test;

public class RampartProjectFileStructureTest {

	@Test
	public void testProjectRoot() throws URISyntaxException {
		
		File projectRoot = RampartProjectFileStructure.determineProjectRoot();
		
		
		File javaDir = new File(projectRoot, "java");
		File perlDir = new File(projectRoot, "scripts.perl");
		assertTrue(javaDir.exists());
		assertTrue(perlDir.exists());
		
	}

}
