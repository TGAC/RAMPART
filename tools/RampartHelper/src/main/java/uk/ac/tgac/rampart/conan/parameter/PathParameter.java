package uk.ac.tgac.rampart.conan.parameter;

import org.springframework.util.StringUtils;

/**
 * @author Dan Mapleson
 */
public class PathParameter extends DefaultConanParameter {

	private static final long serialVersionUID = -7472585120471067656L;

	public PathParameter(String name) {
		this(name, name, false);
	}

	public PathParameter(String name, String description, boolean isOptional) {
		super(name, description, false, isOptional, false);
	}

	@Override
	public boolean validateParameterValue(String value) {
		return !StringUtils.containsWhitespace(value) && !value.contains("~");
	}
}