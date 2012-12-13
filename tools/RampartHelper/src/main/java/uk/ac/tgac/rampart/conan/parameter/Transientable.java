package uk.ac.tgac.rampart.conan.parameter;

/**
 * @author Rob Davey
 */
public interface Transientable {
	
	boolean isTransient();

	void setTransient(boolean t);
}
