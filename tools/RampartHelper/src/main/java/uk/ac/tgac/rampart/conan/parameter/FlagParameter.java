package uk.ac.tgac.rampart.conan.parameter;


/**
 * @author Rob Davey
 */
public class FlagParameter extends DefaultConanParameter {

	private static final long serialVersionUID = -3206576211691167926L;


	public FlagParameter(String name) {
		this(name, name);
	}
	
	public FlagParameter(String name, String description) {
		super(name, description, true, true, false);
	}

	@Override
	public boolean validateParameterValue(String value) {
		return true;
	}
}
