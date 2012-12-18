package uk.ac.tgac.rampart.conan.tool.gapcloser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.parameter.ToolArgs;

public class GapCloserV112Args implements ToolArgs {

	private File inputScaffoldFile;
	private File libraryFile;
	private File outputFile;
	private Integer maxReadLength;
	private Integer overlap;
	private Integer threads;
	
	public File getInputScaffoldFile() {
		return inputScaffoldFile;
	}
	
	public void setInputScaffoldFile(File inputScaffoldFile) {
		this.inputScaffoldFile = inputScaffoldFile;
	}
	
	public File getLibraryFile() {
		return libraryFile;
	}
	
	public void setLibraryFile(File libraryFile) {
		this.libraryFile = libraryFile;
	}
	
	public File getOutputFile() {
		return outputFile;
	}
	
	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}
	
	public Integer getMaxReadLength() {
		return maxReadLength;
	}
	
	public void setMaxReadLength(Integer maxReadLength) {
		this.maxReadLength = maxReadLength;
	}
	
	public Integer getOverlap() {
		return overlap;
	}
	
	public void setOverlap(Integer overlap) {
		this.overlap = overlap;
	}
	
	public Integer getThreads() {
		return threads;
	}
	
	public void setThreads(Integer threads) {
		this.threads = threads;
	}

	@Override
	public Map<ConanParameter, String> getParameterValuePairs() {
		
		Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();
		
		if (this.inputScaffoldFile != null)
			pvp.put(GapCloserV112Param.INPUT_SCAFFOLD_FILE.getConanParameter(), this.inputScaffoldFile.getPath());
		
		if (this.libraryFile != null)
			pvp.put(GapCloserV112Param.LIBRARY_FILE.getConanParameter(), this.inputScaffoldFile.getPath());
		
		if (this.outputFile != null)
			pvp.put(GapCloserV112Param.OUTPUT_FILE.getConanParameter(), this.outputFile.getPath());
		
		if (this.maxReadLength != null)
			pvp.put(GapCloserV112Param.MAX_READ_LENGTH.getConanParameter(), this.maxReadLength.toString());
		
		if (this.overlap != null)
			pvp.put(GapCloserV112Param.OVERLAP.getConanParameter(), this.overlap.toString());
		
		if (this.threads != null)
			pvp.put(GapCloserV112Param.THREADS.getConanParameter(), this.threads.toString());
		
		return pvp;
		
	}
	
	
}
