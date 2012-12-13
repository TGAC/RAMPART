package uk.ac.tgac.rampart.conan.parameter.tool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.parameter.DefaultProcessParameter;
import uk.ac.tgac.rampart.conan.parameter.PathParameter;
import uk.ac.tgac.rampart.conan.parameter.ToolParameters;

public class GapCloserV112Params implements ToolParameters {

	private final Collection<ConanParameter> parameters;
	
	private final PathParameter inputScaffolds;
	private final PathParameter configFile;
	private final PathParameter outputScaffolds;
	private final DefaultProcessParameter threads;
	private final DefaultProcessParameter kmer;
	
	public GapCloserV112Params() {
		this.configFile = new PathParameter("b");
		this.inputScaffolds = new PathParameter("a");
		this.outputScaffolds = new PathParameter("o");
		this.threads = new DefaultProcessParameter("t");
		this.threads.setOptional(true);
		this.kmer = new DefaultProcessParameter("p");
		this.kmer.setOptional(true);

		this.parameters = new ArrayList<ConanParameter>();
		this.parameters.add(this.configFile);
		this.parameters.add(this.inputScaffolds);
		this.parameters.add(this.outputScaffolds);
		this.parameters.add(this.threads);
		this.parameters.add(this.kmer);		
	}

	@Override
	public Collection<ConanParameter> getParameters() {
		return parameters;
	}

	public PathParameter getInputScaffolds() {
		return inputScaffolds;
	}

	public PathParameter getConfigFile() {
		return configFile;
	}

	public PathParameter getOutputScaffolds() {
		return outputScaffolds;
	}

	public DefaultProcessParameter getThreads() {
		return threads;
	}

	public DefaultProcessParameter getKmer() {
		return kmer;
	}

	@Override
	public Map<ConanParameter, String> getParameterValuePairs() {
		// TODO Auto-generated method stub
		return null;
	}
}
