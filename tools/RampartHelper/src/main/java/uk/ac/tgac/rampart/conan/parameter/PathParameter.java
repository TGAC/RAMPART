package uk.ac.tgac.rampart.conan.parameter;

import org.springframework.util.StringUtils;

import uk.ac.ebi.fgpt.conan.model.AbstractConanParameter;

/**
 * @author Rob Davey
 */
public class PathParameter extends AbstractConanParameter implements
		Optionable, Transientable {

	private static final long serialVersionUID = -3887080676113159103L;
	private boolean optional = false;
	private boolean t = false;

	public PathParameter(String name) {
		super(name);
	}

	public PathParameter(String name, boolean isBoolean) {
		super(name, isBoolean);
	}

	public PathParameter(String name, String description) {
		super(name, description);
	}

	public PathParameter(String name, String description, boolean isBoolean) {
		super(name, description, isBoolean);
	}

	@Override
	public boolean validateParameterValue(String value) {
		return !StringUtils.containsWhitespace(value) && !value.contains("~");
	}

	@Override
	public boolean isOptional() {
		return optional;
	}

	@Override
	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	@Override
	public boolean isTransient() {
		return t;
	}

	@Override
	public void setTransient(boolean t) {
		this.t = t;
	}
}