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
package uk.ac.tgac.rampart.conan.tool.external.abyss;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.tool.args.DeBrujinAssemblerArgs;
import uk.ac.tgac.rampart.core.data.Library;

public class AbyssV134Args implements DeBrujinAssemblerArgs {

	private AbyssV134InputLibsArg libs;
	private Integer nbContigPairs;
	private int kmer;
	private Integer threads;
	private String name;

	
	public AbyssV134Args() {
	
		this.libs = null;
		this.nbContigPairs = null;
		this.kmer = 65;
		this.threads = null;
		this.name = null;
	}
	
	@Override
	public Set<Library> getLibraries() {
		return libs.getLibs();
	}

	@Override
	public void setLibraries(Set<Library> libs) {		
		this.libs = new AbyssV134InputLibsArg(libs);
	}

	public int getNbContigPairs() {
		return nbContigPairs;
	}

	public void setNbContigPairs(int nbContigPairs) {
		this.nbContigPairs = nbContigPairs;
	}

	@Override
	public int getKmer() {
		return kmer;
	}

	@Override
	public void setKmer(int kmer) {
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
		
		if (this.libs != null)
			pvp.put(AbyssV134Param.LIBRARIES.getConanParameter(), this.libs.toString());
		
		if (this.nbContigPairs != null)
			pvp.put(AbyssV134Param.NB_CONTIG_PAIRS.getConanParameter(), this.nbContigPairs.toString());
		
		pvp.put(AbyssV134Param.KMER.getConanParameter(), String.valueOf(this.kmer));
		
		if (this.threads != null) 
			pvp.put(AbyssV134Param.THREADS.getConanParameter(), this.threads.toString());
		
		if (this.name != null)
			pvp.put(AbyssV134Param.NAME.getConanParameter(), this.name.toString());
		
		
		return pvp;
	}

	@Override
	public DeBrujinAssemblerArgs copy() {
		
		AbyssV134Args copy = new AbyssV134Args();
		copy.setName(this.getName());
		copy.setKmer(this.getKmer());
		copy.setThreads(this.getThreads());
		copy.setNbContigPairs(this.getNbContigPairs());
		copy.setLibraries(this.getLibraries());
		
		return copy;
	}
	
}
