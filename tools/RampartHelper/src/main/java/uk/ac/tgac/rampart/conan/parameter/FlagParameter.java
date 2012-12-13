package uk.ac.tgac.rampart.conan.parameter;

import uk.ac.ebi.fgpt.conan.model.AbstractConanParameter;

/**
 * @author Rob Davey
 */
public class FlagParameter extends AbstractConanParameter implements
		Optionable, Transientable {

	private static final long serialVersionUID = -3206576211691167926L;
	private boolean optional = false;
	private boolean t = false;

	public FlagParameter(String name) {
		super(name);
	}

	public FlagParameter(String name, boolean isBoolean) {
		super(name, isBoolean);
	}

	public FlagParameter(String name, String description) {
		super(name, description);
	}

	public FlagParameter(String name, String description, boolean isBoolean) {
		super(name, description, isBoolean);
	}

	@Override
	public boolean validateParameterValue(String value) {
		return true;
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
