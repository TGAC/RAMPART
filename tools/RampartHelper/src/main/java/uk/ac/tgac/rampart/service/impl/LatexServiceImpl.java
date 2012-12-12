package uk.ac.tgac.rampart.service.impl;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import uk.ac.tgac.rampart.service.LatexService;
import uk.ac.tgac.rampart.util.ProcessStreamManager;

@Service
public class LatexServiceImpl implements LatexService {
	
	private static Logger log = Logger.getLogger(LatexServiceImpl.class.getName());
	
	
	@Override
	public void compileDocument(File texFile) throws Exception {
		
		log.debug("Starting LaTeX document compilation procedure.");
		
		final String texFileName = texFile.getName();
		final String workingDir = new File(".").getAbsolutePath();
		
		// Check if report file exists first
		if (!texFile.exists())
			throw new IOException("Could not find: \"" + texFileName + "\" in : \"" + workingDir + "\"");
		
		// Assumes latex is installed and pdflatex is on the path
		String command = "pdflatex -interaction=nonstopmode " + texFile.getPath();		
		log.debug("Executing: \"" + command + "\" in: " + workingDir + "\" 3 times");

		// We have to run Latex 3 times to ensure the document is fully compiled.
		for(int i = 1; i <= 3; i++) {
			// Assumes we are in the correct directory and output should go here too.
			Process process = Runtime.getRuntime().exec(command);
			ProcessStreamManager psm = new ProcessStreamManager(process, "PDFLATEX");
	
			int code = psm.runInForeground(false);
	
			if (code != 0) {
				throw new IOException("PDFLATEX returned code " + code + " on run: " + i);
			}
		}
		
		log.debug("LaTeX document compiled successfully");
	}
}
