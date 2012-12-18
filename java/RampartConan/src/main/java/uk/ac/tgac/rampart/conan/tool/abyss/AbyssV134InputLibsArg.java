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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbyssV134InputLibsArg {
	
	private Map<String, File> pairedEndLibraries;
	private List<File> singleEndLibraries;

	public AbyssV134InputLibsArg() {
		this(new HashMap<String, File>(), new ArrayList<File>());
	}

	public AbyssV134InputLibsArg(Map<String, File> pairedEndLibraries,
			List<File> singleEndLibraries) {
		this.pairedEndLibraries = pairedEndLibraries;
		this.singleEndLibraries = singleEndLibraries;
	}

	public Map<String, File> getPairedEndLibraries() {
		return pairedEndLibraries;
	}

	public void setPairedEndLibraries(Map<String, File> pairedEndLibraries) {
		this.pairedEndLibraries = pairedEndLibraries;
	}

	public List<File> getSingleEndLibraries() {
		return singleEndLibraries;
	}

	public void setSingleEndLibraries(List<File> singleEndLibraries) {
		this.singleEndLibraries = singleEndLibraries;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("'");
		for (Map.Entry<String, File> pp : this.pairedEndLibraries.entrySet()) {
			sb.append(pp.getKey());
			sb.append(" ");
		}
		sb.append("' -j");
		sb.append(this.pairedEndLibraries.size());
		sb.append(" ");
		for (Map.Entry<String, File> pp : this.pairedEndLibraries.entrySet()) {
			sb.append(pp.getKey());
			sb.append("='");
			sb.append(pp.getValue().getPath());
			sb.append("' ");
		}

		if (this.singleEndLibraries != null
				&& this.singleEndLibraries.size() > 0) {
			sb.append("se='");
			int i = 0;
			for (File f : this.singleEndLibraries) {
				sb.append(f.getPath());
				if (i != this.singleEndLibraries.size() - 1) {
					sb.append(" ");
				}
			}
			sb.append("'");
		}

		return sb.toString().trim();
	}

}
