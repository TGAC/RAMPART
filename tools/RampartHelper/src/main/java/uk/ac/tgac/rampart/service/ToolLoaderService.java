package uk.ac.tgac.rampart.service;

import java.io.IOException;

public interface ToolLoaderService {

	// Keys used for each tool used by rampart
	final String PERL_5_16_1 		= "perl-5.16.1";
	final String R_2_12_2 			= "r-2.12.2";
	final String JRE_6 				= "jre-6";
	final String TEXLIVE_2012 		= "texlive-2012";
	final String FASTX_0_0_13 		= "fastx-0.0.13";
	final String SICKLE_1_1 		= "sickle-1.1";
	final String EXONERATE_2_2_0	= "exonerate-2.2.0";
	final String ABYSS_1_3_4		= "abyss-1.3.4";
	final String SSPACE_BASIC_2_0	= "sspace-basic-2.0";
	final String GAPCLOSER_1_12		= "gapcloser-1.12";
	
	// Methods
	void loadPropertiesFile(String propertiesFile) throws IOException;
	
	String getLoadToolCommand(String toolKey);
}
