package uk.ac.tgac.rampart.conan.parameter.tool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.parameter.DefaultProcessParameter;
import uk.ac.tgac.rampart.conan.parameter.PathParameter;
import uk.ac.tgac.rampart.conan.parameter.ToolParameters;

public class SSpaceBasicV2Params implements ToolParameters {

	private final Collection<ConanParameter> parameters;
	private final PathParameter libraryConfig;
	private final PathParameter inputAssembly;
	private final DefaultProcessParameter extend;
	private final DefaultProcessParameter threads;
	private final DefaultProcessParameter outputBaseName;
	
	
	public SSpaceBasicV2Params() {
		this.libraryConfig = new PathParameter("l");
		this.inputAssembly = new PathParameter("s");		
		this.threads = new DefaultProcessParameter("T");
		this.threads.setOptional(true);
		this.outputBaseName = new DefaultProcessParameter("b");
		this.outputBaseName.setOptional(true);
		this.extend = new DefaultProcessParameter("x");
		this.extend.setOptional(true);

		this.parameters = new ArrayList<ConanParameter>();
		this.parameters.add(this.libraryConfig);
		this.parameters.add(this.inputAssembly);
		this.parameters.add(this.threads);
		this.parameters.add(this.outputBaseName);		
	}
	
	@Override
	public Collection<ConanParameter> getParameters() {
		return parameters;
	}

	public PathParameter getLibraryConfig() {
		return libraryConfig;
	}

	public PathParameter getInputAssembly() {
		return inputAssembly;
	}

	public DefaultProcessParameter getExtend() {
		return extend;
	}

	public DefaultProcessParameter getThreads() {
		return threads;
	}

	public DefaultProcessParameter getOutputBaseName() {
		return outputBaseName;
	}

	@Override
	public Map<ConanParameter, String> getParameterValuePairs() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
