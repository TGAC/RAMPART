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
package uk.ac.tgac.rampart.conan.tool.external.sspace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.tool.args.ScaffolderArgs;
import uk.ac.tgac.rampart.conan.conanx.process.ProcessArgs;
import uk.ac.tgac.rampart.core.data.Library;

public class SSpaceBasicV2Args implements ScaffolderArgs {

	// SSPACE vars
	private File libraryFile;
	private File inputContigFile;
	private Integer extend;
	private Integer bowtieThreads;
	private String baseName;
	
	// Vars to support ScaffolderArgs interface
	private File outputScaffoldFile;
	private Set<Library> libs;
		
	public SSpaceBasicV2Args() {
		this.libraryFile = null;
		this.inputContigFile = null;
		this.extend = null;
		this.bowtieThreads = null;
		this.baseName = null;
		
		this.outputScaffoldFile = null;
		this.libs = new HashSet<Library>();
	}
	
	public File getLibraryFile() {
		return libraryFile;
	}

	public void setLibraryFile(File libraryFile) {
		this.libraryFile = libraryFile;
	}

	@Override
	public File getInputContigFile() {
		return inputContigFile;
	}

	@Override
	public void setInputContigFile(File inputContigFile) {
		this.inputContigFile = inputContigFile;
	}

	public int getExtend() {
		return extend;
	}

	public void setExtend(int extend) {
		this.extend = extend;
	}

	public Integer getBowtieThreads() {
		return bowtieThreads;
	}

	public void setBowtieThreads(Integer bowtieThreads) {
		this.bowtieThreads = bowtieThreads;
	}

	public String getBaseName() {
		return baseName;
	}

	public void setBaseName(String baseName) {
		this.baseName = baseName;
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

		/*if (this.libraryFile != null)
			pvp.put(SSpaceBasicV2Param.LIBRARY_FILE.getConanParameter(),
					this.libraryFile.getPath());
		else {
			throw new IllegalArgumentException("Must have a library file specified.  If working from a Set<Library> you can call setLibraryFile(Set<Library> libs, File outputLibFile) to automatically generate a SSPACE library file and set the class variable.");
		}

		if (this.inputContigFile != null)
			pvp.put(SSpaceBasicV2Param.CONTIG_FILE.getConanParameter(),
					this.inputContigFile.getPath());

		if (this.extend != null)
			pvp.put(SSpaceBasicV2Param.EXTEND.getConanParameter(),
					this.extend.toString());

		if (this.bowtieThreads != null && this.bowtieThreads > 1)
			pvp.put(SSpaceBasicV2Param.BOWTIE_THREADS.getConanParameter(),
					this.bowtieThreads.toString());

		if (this.baseName != null)
			pvp.put(SSpaceBasicV2Param.BASE_NAME.getConanParameter(),
					this.baseName);
		          */
		return pvp;
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
	public int getThreads() {		
		return getBowtieThreads() == null ? 1 : getBowtieThreads();
	}

	@Override
	public void setThreads(int threads) {
		this.setBowtieThreads(new Integer(threads));
	}

	@Override
	public File getOutputScaffoldFile() {
		return this.outputScaffoldFile;
	}

	@Override
	public void setOutputScaffoldFile(File outputScaffoldFile) {		
		this.outputScaffoldFile = outputScaffoldFile;
	}

}
