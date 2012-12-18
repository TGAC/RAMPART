package uk.ac.tgac.rampart.conan.parameter;

import java.util.Map;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;

public interface ToolArgs {

	Map<ConanParameter, String> getParameterValuePairs();
}
