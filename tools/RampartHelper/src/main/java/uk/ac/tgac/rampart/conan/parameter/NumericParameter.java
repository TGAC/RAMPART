package uk.ac.tgac.rampart.conan.parameter;

import org.apache.commons.lang.StringUtils;

public class NumericParameter extends DefaultConanParameter {

	private static final long serialVersionUID = -4971687444263920744L;

	public NumericParameter(String name) {
		this(name, name, false);
	}
	
	public NumericParameter(String name, String description, boolean isOptional) {
		super(name, description, false, isOptional, false);
	}

	@Override
	public boolean validateParameterValue(String value) {
		return StringUtils.isNumeric(value);
	}
}
