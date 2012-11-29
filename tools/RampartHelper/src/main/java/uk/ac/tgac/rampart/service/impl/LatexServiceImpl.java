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
		
		// Assumes latex is installed and pdflatex is on the path
		File workingDir = new File(texFile.getParent());
		String command = "pdflatex -interaction=nonstopmode " + texFile.getName();
		
		//log.debug("Executing " + command + " in " + workingDir.getPath());
		log.debug("Executing \"" + command + "\"");

		//Process process = Runtime.getRuntime().exec(command, new String[]{}, workingDir);
		Process process = Runtime.getRuntime().exec(command);
		ProcessStreamManager psm = new ProcessStreamManager(process, "PDFLATEX");

		int code = psm.runInForeground(false);

		if (code != 0) {
			throw new IOException("PDFLATEX returned code " + code);
		}
		
		log.debug("LaTeX document compiled successfully");
	}
}
