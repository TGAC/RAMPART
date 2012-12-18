package uk.ac.tgac.rampart.conan.tool.abyss;

import uk.ac.tgac.rampart.conan.parameter.DefaultConanParameter;

public class AbyssV134InputLibsParameter extends DefaultConanParameter {
	
	private static final long serialVersionUID = 4497529578973609010L;

	public AbyssV134InputLibsParameter() {
		super(
				"lib", 
				"Required.  The input libraries to assemble with abyss.  Can include paired end and single end.  Will run paired end assemblies in parallel.", 
				false, false, false);
	}
	
	@Override
	public boolean validateParameterValue(String value) {
		
		// Pretty tricky to validate this in String form.  Just let Abyss do the validation for the time being...
		return true;
	}
}
