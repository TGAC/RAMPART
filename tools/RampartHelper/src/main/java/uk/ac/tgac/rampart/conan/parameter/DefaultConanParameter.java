package uk.ac.tgac.rampart.conan.parameter;

import uk.ac.ebi.fgpt.conan.model.AbstractConanParameter;

/**
 * @author Dan Mapleson
 */
public class DefaultConanParameter extends AbstractConanParameter implements
		Optionable, Transientable {

	private static final long serialVersionUID = 1454972239759100291L;

	private boolean optional;
	private boolean t;

	public DefaultConanParameter(String name) {
		this(name, name, false);
	}
	
	public DefaultConanParameter(String name, String description, boolean isBoolean) {
		this(name, description, isBoolean, false);
	}
	
	public DefaultConanParameter(String name, String description, boolean isBoolean,
			boolean isOptional) {
		
		this(name, description, isBoolean, isOptional, false);
	}
	
	public DefaultConanParameter(String name, String description, boolean isBoolean,
			boolean isOptional, boolean isTransient) {
		super(name, description, isBoolean);
		this.optional = isOptional;
		this.t = isTransient;
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
