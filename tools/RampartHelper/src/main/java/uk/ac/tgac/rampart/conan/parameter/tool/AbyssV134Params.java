package uk.ac.tgac.rampart.conan.parameter.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.parameter.DefaultProcessParameter;
import uk.ac.tgac.rampart.conan.parameter.ToolParameters;

public class AbyssV134Params implements ToolParameters {
	
	public static class AbyssInputLibrariesParam {
		private Map<String, File> pairedEndLibraries;
		private List<File> singleEndLibraries;
		
		public AbyssInputLibrariesParam() {
			this(new HashMap<String,File>(), new ArrayList<File>());
		}

		public AbyssInputLibrariesParam(
				Map<String, File> pairedEndLibraries,
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
			for(Map.Entry<String,File> pp : this.pairedEndLibraries.entrySet()) {
				sb.append(pp.getKey());
			}
			sb.append("'");
			sb.append(" ");
			for(Map.Entry<String,File> pp : this.pairedEndLibraries.entrySet()) {
				sb.append(pp.getKey());
				sb.append("='");
				sb.append(pp.getValue());
				sb.append("' ");
			}
			
			if (this.singleEndLibraries != null && this.singleEndLibraries.size() > 0) {
				sb.append("se='");			
				int i = 0;
				for(File f : this.singleEndLibraries) {
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
	
	protected enum AbyssV134Param {
		LIB ("lib", true),
		NB_CONTIGS ("n", false) ,
		NB_SCAFFOLDS ("N", false),
		KMER ("k", true),
		THREADS ("np", false),
		OUTPUT_PREFIX ("name", false);
		
		private String name;
		private boolean optional;
		
		private AbyssV134Param(String name, boolean optional) {
			this.name = name;
			this.optional = optional;
		}
		
		public String getName() {
			return name;
		}

		public boolean isOptional() {
			return optional;
		}

		public ConanParameter createConanParameter() {
			DefaultProcessParameter p = new DefaultProcessParameter(this.name);	
			p.setOptional(this.optional);
			return p;
		}
	}
	
	
	private AbyssInputLibrariesParam inputlibraries;
	private int nbPairsForContigs;
	private int nbPairsForScaffolds;
	private Integer kmer;
	private int threads;
	private String outputBaseName;

	
	public AbyssV134Params() {
	
		this.inputlibraries = null;
		this.nbPairsForContigs = 10;
		this.nbPairsForScaffolds = 10;
		this.kmer = null;
		this.threads = 1;
		this.outputBaseName = "abyss-pe";
	}
	
	@Override
	public Collection<ConanParameter> getParameters() {
		
		Collection<ConanParameter> parameters = new ArrayList<ConanParameter>();
		for(AbyssV134Param p : AbyssV134Param.values()) {
			parameters.add(p.createConanParameter());
		}
		return parameters;
	}

	@Override
	public Map<ConanParameter, String> getParameterValuePairs() {
		
		Map<ConanParameter, String> paramValuePair = new HashMap<ConanParameter, String>();
		
		paramValuePair.put(AbyssV134Param.KMER.createConanParameter(), this.kmer.toString());
		
		if (this.nbPairsForContigs != 10) {
			paramValuePair.put(AbyssV134Param.NB_CONTIGS.createConanParameter(), Integer.toString(this.nbPairsForContigs));
		}
		
		if (this.nbPairsForScaffolds != 10) {
			paramValuePair.put(AbyssV134Param.NB_SCAFFOLDS.createConanParameter(), Integer.toString(this.nbPairsForScaffolds));
		}
		
		if (this.threads > 1) {
			paramValuePair.put(AbyssV134Param.THREADS.createConanParameter(), Integer.toString(this.threads));
		}
		
		paramValuePair.put(AbyssV134Param.LIB.createConanParameter(), this.inputlibraries.toString());
		
		return paramValuePair;
	}

	public AbyssInputLibrariesParam getInputlibraries() {
		return inputlibraries;
	}

	public void setInputlibraries(AbyssInputLibrariesParam inputlibraries) {
		this.inputlibraries = inputlibraries;
	}

	public int getNbPairsForContigs() {
		return nbPairsForContigs;
	}

	public void setNbPairsForContigs(int nbPairsForContigs) {
		this.nbPairsForContigs = nbPairsForContigs;
	}

	public int getNbPairsForScaffolds() {
		return nbPairsForScaffolds;
	}

	public void setNbPairsForScaffolds(int nbPairsForScaffolds) {
		this.nbPairsForScaffolds = nbPairsForScaffolds;
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

	public String getOutputBaseName() {
		return outputBaseName;
	}

	public void setOutputBaseName(String outputBaseName) {
		this.outputBaseName = outputBaseName;
	}
	
}
