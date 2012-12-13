package uk.ac.tgac.rampart.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.springframework.stereotype.Service;

import uk.ac.tgac.rampart.service.ToolLoaderService;

@Service
public class ToolLoaderServiceImpl implements ToolLoaderService {	
	
	private Properties loadToolCommands = new Properties();
	
	@Override
	public void loadPropertiesFile(String propertiesFile) throws IOException {
		
		if (new File(propertiesFile).exists()) {
			loadToolCommands.load(new InputStreamReader(new FileInputStream(propertiesFile)));
		}
		else {
			loadToolCommands.load(ClassLoader.getSystemResourceAsStream(propertiesFile));
		}
	}
	
	@Override
	public String getLoadToolCommand(String toolKey) {
		return loadToolCommands.getProperty(toolKey);
	}
}
