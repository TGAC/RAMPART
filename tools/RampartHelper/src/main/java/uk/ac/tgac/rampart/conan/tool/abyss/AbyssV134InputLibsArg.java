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
