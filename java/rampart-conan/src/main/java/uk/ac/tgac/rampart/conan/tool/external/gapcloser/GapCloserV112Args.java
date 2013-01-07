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
package uk.ac.tgac.rampart.conan.tool.external.gapcloser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.tool.args.DegapArgs;
import uk.ac.tgac.rampart.core.data.Library;

public class GapCloserV112Args implements DegapArgs {

	// GapCloser vars
	private File inputScaffoldFile;
	private File libraryFile;
	private File outputScaffoldFile;
	private Integer maxReadLength;
	private Integer overlap;
	private int threads;
	
	// DegapArgs vars
	private Set<Library> libs;
	
	
	
	public GapCloserV112Args() {
		this.inputScaffoldFile = null;
		this.libraryFile = null;
		this.outputScaffoldFile = null;
		this.maxReadLength = null;
		this.overlap = null;
		this.threads = 1;
	}
	
	@Override
	public File getInputScaffoldFile() {
		return inputScaffoldFile;
	}
	
	@Override
	public void setInputScaffoldFile(File inputScaffoldFile) {
		this.inputScaffoldFile = inputScaffoldFile;
	}
	
	public File getLibraryFile() {
		return libraryFile;
	}
	
	public void setLibraryFile(File libraryFile) {
		this.libraryFile = libraryFile;
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
	
	@Override
	public int getThreads() {
		return threads;
	}
	
	@Override
	public void setThreads(int threads) {
		this.threads = threads;
	}
	
	@Override
	public Set<Library> getLibraries() {
		return libs;
	}

	@Override
	public void setLibraries(Set<Library> libraries) {
		this.libs = libraries;
	}

	@Override
	public File getOutputScaffoldFile() {
		return this.outputScaffoldFile;
	}

	@Override
	public void setOutputScaffoldFile(File outputScaffoldFile) {
		this.outputScaffoldFile = outputScaffoldFile;
	}
	
	
	public void setLibraryFile(Set<Library> libs, File outputLibFile) throws IOException {
		
		List<String> lines = new ArrayList<String>();
		
		for(Library lib : libs) {
			
			if (lib.getUsage() == Library.Usage.ASSEMBLY_AND_SCAFFOLDING || 
					lib.getUsage() == Library.Usage.SCAFFOLDING_ONLY) {
				
				String[] parts = new String[] {
					lib.getName(),
					lib.getFilePaired1().getFilePath(),
					lib.getFilePaired2().getFilePath(),
					lib.getAverageInsertSize().toString(),
					lib.getInsertErrorTolerance().toString(),
					lib.getSeqOrientation().toString()
				};
				
				lines.add(StringUtils.join(parts, " "));
			}
			
		}
		
		FileUtils.writeLines(outputLibFile, lines);
		
		this.libs = libs;
		this.libraryFile = outputLibFile;
	}
	

	@Override
	public Map<ConanParameter, String> getParameterValuePairs() {
		
		Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();
		
		if (this.inputScaffoldFile != null)
			pvp.put(GapCloserV112Param.INPUT_SCAFFOLD_FILE.getConanParameter(), this.inputScaffoldFile.getPath());
		
		if (this.libraryFile != null)
			pvp.put(GapCloserV112Param.LIBRARY_FILE.getConanParameter(), this.inputScaffoldFile.getPath());
		
		if (this.outputScaffoldFile != null)
			pvp.put(GapCloserV112Param.OUTPUT_FILE.getConanParameter(), this.outputScaffoldFile.getPath());
		
		if (this.maxReadLength != null)
			pvp.put(GapCloserV112Param.MAX_READ_LENGTH.getConanParameter(), this.maxReadLength.toString());
		
		if (this.overlap != null)
			pvp.put(GapCloserV112Param.OVERLAP.getConanParameter(), this.overlap.toString());
		
		if (this.threads > 1)
			pvp.put(GapCloserV112Param.THREADS.getConanParameter(), String.valueOf(this.threads));
		
		return pvp;		
	}
	
}
