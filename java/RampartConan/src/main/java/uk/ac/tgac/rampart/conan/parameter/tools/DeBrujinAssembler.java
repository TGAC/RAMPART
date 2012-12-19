package uk.ac.tgac.rampart.conan.parameter.tools;

import java.io.File;

import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.parameter.GEArgs;

public interface DeBrujinAssembler {

	void execute(DeBrujinAssemblerArgs args, GEArgs geArgs, File workingDir) 
			throws IllegalArgumentException, ProcessExecutionException, InterruptedException;
	
	boolean makesScaffolds();
}