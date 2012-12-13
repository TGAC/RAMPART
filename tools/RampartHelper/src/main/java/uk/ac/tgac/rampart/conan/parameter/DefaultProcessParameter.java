package uk.ac.tgac.rampart.conan.parameter;

import uk.ac.ebi.fgpt.conan.model.AbstractConanParameter;

/**
 * @author Rob Davey
 */
public class DefaultProcessParameter extends AbstractConanParameter implements
		Optionable, Transientable {

	private static final long serialVersionUID = 1454972239759100291L;

	private boolean optional = false;
	private boolean t = false;

	public DefaultProcessParameter(String name) {
		super(name);
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
