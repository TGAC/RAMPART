package uk.ac.tgac.rampart.conan.parameter;

/**
 * @author Rob Davey
 */
public interface Optionable {
	
	boolean isOptional();

	void setOptional(boolean optional);
}