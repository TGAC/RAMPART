package uk.ac.tgac.rampart.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;


public class ToolCommandLoader {	
	
	
	// Keys used for each tool used by rampart
	public static final String PERL_5_16_1 			= "perl-5.16.1";
	public static final String R_2_12_2 			= "r-2.12.2";
	public static final String JRE_6 				= "jre-6";
	public static final String TEXLIVE_2012 		= "texlive-2012";
	public static final String FASTX_0_0_13 		= "fastx-0.0.13";
	public static final String SICKLE_1_1 			= "sickle-1.1";
	public static final String EXONERATE_2_2_0		= "exonerate-2.2.0";
	public static final String ABYSS_1_3_4			= "abyss-1.3.4";
	public static final String SSPACE_BASIC_2_0		= "sspace-basic-2.0";
	public static final String GAPCLOSER_1_12		= "gapcloser-1.12";
	
	
	private Properties loadToolCommands = new Properties();
	
	private static ToolCommandLoader instance = new ToolCommandLoader();

	public static ToolCommandLoader getInstance() {
		return instance;
	}
	
	public void loadPropertiesFile(String propertiesFile) throws IOException {
		
		if (new File(propertiesFile).exists()) {
			loadToolCommands.load(new InputStreamReader(new FileInputStream(propertiesFile)));
		}
		else {
			loadToolCommands.load(ClassLoader.getSystemResourceAsStream(propertiesFile));
		}
	}
	
	public String getLoadToolCommand(String toolKey) {
		return loadToolCommands.getProperty(toolKey);
	}
}
