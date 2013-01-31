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
package uk.ac.tgac.rampart.conan.tool.external.asm.abyss;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.tool.external.asm.AssemblerArgs;
import uk.ac.tgac.rampart.core.data.Library;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AbyssV134Args implements AssemblerArgs {

    private AbyssV134Params params = new AbyssV134Params();

    private AbyssV134InputLibsArg libs;
	private int nbContigPairs;
	private int kmer;
	private int threads;
	private String name;

	
	public AbyssV134Args() {
	
		this.libs = null;
		this.nbContigPairs = 10;
		this.kmer = 65;
		this.threads = 0;
		this.name = null;
	}
	
	@Override
	public Set<Library> getLibraries() {
		return libs == null ? null : libs.getLibs();
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

    @Override
	public int getThreads() {
		return threads;
	}

    @Override
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
	public Map<ConanParameter, String> getArgMap() {
		
		Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();


        if (this.libs != null) {
            pvp.put(params.getLibs(), this.libs.toString());
        }
		
		pvp.put(params.getNbContigPairs(), params.getNbContigPairs().getName() + "=" + String.valueOf(this.nbContigPairs));
		pvp.put(params.getKmer(), params.getKmer().getName() + "=" + String.valueOf(this.kmer));

        if (this.threads > 0) {
            pvp.put(params.getThreads(), params.getThreads().getName() + "=" + String.valueOf(this.threads));
        }

		if (this.name != null) {
            pvp.put(params.getName(), params.getName().getName() + "=" + this.name);
        }
		
		
		return pvp;
	}

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {
        for(Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            };

            String param = entry.getKey().getName();

            if (param.equals(this.params.getLibs().getName())) {
                //TODO this needs proper parsing
                this.libs = null; //entry.getValue();
            }
            else if (param.equals(this.params.getKmer().getName())) {
                this.kmer = Integer.parseInt(entry.getValue());
            }
            else if (param.equals(this.params.getName().getName())) {
                this.name = entry.getValue();
            }
            else if (param.equals(this.params.getNbContigPairs().getName())) {
                this.nbContigPairs = Integer.parseInt(entry.getValue());
            }
            else if (param.equals(this.params.getThreads().getName())) {
                this.threads = Integer.parseInt(entry.getValue());
            }
            else {
                throw new IllegalArgumentException("Unknown parameter found: " + param);
            }
        }
    }

    @Override
	public AssemblerArgs copy() {
		
		AbyssV134Args copy = new AbyssV134Args();
		copy.setName(this.getName());
		copy.setKmer(this.getKmer());
		copy.setThreads(this.getThreads());
		copy.setNbContigPairs(this.getNbContigPairs());
		copy.setLibraries(this.getLibraries());  // Not really copying this!!
		
		return copy;
	}
	
}
