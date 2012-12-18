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
package uk.ac.tgac.rampart.conan.tool.abyss;

import java.util.HashMap;
import java.util.Map;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.parameter.ToolArgs;

public class AbyssV134Args implements ToolArgs {

	private AbyssV134InputLibsArg inputlibraries;
	private Integer nbContigPairs;
	private Integer kmer;
	private Integer threads;
	private String name;

	
	public AbyssV134Args() {
	
		this.inputlibraries = null;
		this.nbContigPairs = null;
		this.kmer = null;
		this.threads = null;
		this.name = null;
	}
	

	public AbyssV134InputLibsArg getInputlibraries() {
		return inputlibraries;
	}

	public void setInputlibraries(AbyssV134InputLibsArg inputlibraries) {
		this.inputlibraries = inputlibraries;
	}

	public int getNbContigPairs() {
		return nbContigPairs;
	}

	public void setNbContigPairs(int AbyssV134InputLibsParameter) {
		this.nbContigPairs = AbyssV134InputLibsParameter;
	}

	public Integer getKmer() {
		return kmer;
	}

	public void setKmer(Integer kmer) {
		this.kmer = kmer;
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public Map<ConanParameter, String> getParameterValuePairs() {
		
		Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();
		
		if (this.inputlibraries != null)
			pvp.put(AbyssV134Param.LIBRARIES.getConanParameter(), this.inputlibraries.toString());
		
		if (this.nbContigPairs != null)
			pvp.put(AbyssV134Param.NB_CONTIG_PAIRS.getConanParameter(), this.nbContigPairs.toString());
		
		if (this.kmer != null)
			pvp.put(AbyssV134Param.KMER.getConanParameter(), this.kmer.toString());
		
		if (this.threads != null) 
			pvp.put(AbyssV134Param.THREADS.getConanParameter(), this.threads.toString());
		
		if (this.name != null)
			pvp.put(AbyssV134Param.NAME.getConanParameter(), this.name.toString());
		
		
		return pvp;
	}
}
