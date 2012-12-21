package uk.ac.tgac.rampart.conan.tool.stats;

import java.io.File;

public class AssemblyStats {

	private File inputDir;
	private File outputDir;
	
	public AssemblyStats(File inputDir, File outputDir) {
		this.inputDir = inputDir;
		this.outputDir = outputDir;
	}

	public File getInputDir() {
		return inputDir;
	}

	public void setInputDir(File inputDir) {
		this.inputDir = inputDir;
	}

	public File getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}
	
	/*
	protected String buildCommandLine() {
		RampartProjectFileStructure fs = new RampartProjectFileStructure();
	}
	
	public void dispatch() {
		
		$MASS_GATHERER_PATH . " " . $index_arg . " " . $qst->getInput() . " > " . $stat_file;
		my $mp_cmd_line = $MASS_PLOTTER_PATH . " --output " . $qst->getOutput() . " " . $stat_file;
		
		
	}
	*/
	
}
