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
package uk.ac.tgac.rampart.core.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.ebi.fgpt.conan.utils.ProcessRunner;
import uk.ac.tgac.rampart.core.service.LatexService;

import java.io.File;
import java.io.IOException;


@Service
public class LatexServiceImpl implements LatexService {
	
	private static Logger log = LoggerFactory.getLogger(LatexServiceImpl.class.getName());
	
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
			
			// EBI Conan Way
			ProcessRunner runner = new ProcessRunner();
			runner.redirectStderr(true);
			String[] output = runner.runCommmand(command);
			
			if (output.length > 0) {
				log.debug("Response from command [" + command + "]: " +
                        output.length + " lines, first line was " + output[0]);
			}
			
			/* UEA sRNA Workbench way */
			/* Assumes we are in the correct directory and output should go here too.
			Process task = Runtime.getRuntime().exec(command);
			ProcessStreamManager psm = new ProcessStreamManager(task, "PDFLATEX");
	
			int code = psm.runInForeground(false);
	
			if (code != 0) {
				throw new IOException("PDFLATEX returned code " + code + " on run: " + i);
			}*/
		}
		
		log.debug("LaTeX document compiled successfully");
	}
}
