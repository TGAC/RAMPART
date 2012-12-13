package uk.ac.tgac.rampart.conan.parameter;

import uk.ac.ebi.fgpt.conan.model.AbstractConanParameter;

/**
 * @author Dan Mapleson
 */
public class PairedPathParameter extends AbstractConanParameter implements
		Optionable, Transientable {

	private static final long serialVersionUID = -3887080676113159103L;
	private boolean optional = false;
	private boolean t = false;

	public PairedPathParameter(String name) {
		super(name);
	}

	public PairedPathParameter(String name, boolean isBoolean) {
		super(name, isBoolean);
	}

	public PairedPathParameter(String name, String description) {
		super(name, description);
	}

	public PairedPathParameter(String name, String description, boolean isBoolean) {
		super(name, description, isBoolean);
	}

	@Override
	public boolean validateParameterValue(String value) {
		return !value.contains("~") && !value.startsWith("'") && !value.endsWith("'");
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