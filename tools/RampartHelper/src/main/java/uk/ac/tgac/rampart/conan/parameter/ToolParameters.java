package uk.ac.tgac.rampart.conan.parameter;

import java.util.Collection;
import java.util.Map;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;

public interface ToolParameters {

	Collection<ConanParameter> getParameters();
	
	Map<ConanParameter,String> getParameterValuePairs();
}
