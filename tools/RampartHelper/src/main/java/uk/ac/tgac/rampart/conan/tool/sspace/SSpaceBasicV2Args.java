package uk.ac.tgac.rampart.conan.tool.sspace;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.parameter.ToolArgs;

public class SSpaceBasicV2Args implements ToolArgs {

	private File libraryFile;
	private File contigFile;
	private Integer extend;
	private Integer bowtieThreads;
	private String baseName;
	
	public SSpaceBasicV2Args() {
		this.libraryFile = null;
		this.contigFile = null;
		this.extend = null;
		this.bowtieThreads = null;
		this.baseName = null;
	}

	public File getLibraryFile() {
		return libraryFile;
	}

	public void setLibraryFile(File libraryFile) {
		this.libraryFile = libraryFile;
	}

	public File getContigFile() {
		return contigFile;
	}

	public void setContigFile(File contigFile) {
		this.contigFile = contigFile;
	}

	public int getExtend() {
		return extend;
	}

	public void setExtend(int extend) {
		this.extend = extend;
	}

	public int getBowtieThreads() {
		return bowtieThreads;
	}

	public void setBowtieThreads(int bowtieThreads) {
		this.bowtieThreads = bowtieThreads;
	}

	public String getBaseName() {
		return baseName;
	}

	public void setBaseName(String baseName) {
		this.baseName = baseName;
	}

	@Override
	public Map<ConanParameter, String> getParameterValuePairs() {
		Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();

		if (this.libraryFile != null)
			pvp.put(SSpaceBasicV2Param.LIBRARY_FILE  .getConanParameter(),
					this.libraryFile.getPath());

		if (this.contigFile != null)
			pvp.put(SSpaceBasicV2Param.CONTIG_FILE.getConanParameter(),
					this.contigFile.getPath());

		if (this.extend != null)
			pvp.put(SSpaceBasicV2Param.EXTEND.getConanParameter(),
					this.extend.toString());

		if (this.bowtieThreads != null)
			pvp.put(SSpaceBasicV2Param.BOWTIE_THREADS.getConanParameter(),
					this.bowtieThreads.toString());

		if (this.baseName != null)
			pvp.put(SSpaceBasicV2Param.BASE_NAME.getConanParameter(),
					this.baseName);
		
		return pvp;
	}

}
