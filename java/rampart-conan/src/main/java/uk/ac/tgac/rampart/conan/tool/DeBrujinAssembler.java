package uk.ac.tgac.rampart.conan.tool;

import java.io.File;

import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.env.arch.ge.GridEngineArgs;
import uk.ac.tgac.rampart.conan.tool.args.DeBrujinAssemblerArgs;

public interface DeBrujinAssembler {

	void execute(DeBrujinAssemblerArgs args, GridEngineArgs geArgs, File workingDir)
			throws IllegalArgumentException, ProcessExecutionException, InterruptedException;
	
	boolean makesScaffolds();
}