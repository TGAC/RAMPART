package uk.ac.tgac.rampart.conan.tool.sickle;

import uk.ac.tgac.rampart.conan.parameter.DefaultConanParameter;

public class SicklePeV11QualityTypeParameter extends DefaultConanParameter {

	private static final long serialVersionUID = 3065149558945750682L;

	public static enum SickleQualityTypeOptions {
		
		ILLUMINA,
		PHRED,
		SANGER;
	}
	
	public SicklePeV11QualityTypeParameter() {
		super("qual-type", "Type of quality values (illumina, phred, sanger) (required)", false, false, false);
	}
	
	@Override
	public boolean validateParameterValue(String value) {
		try {
			SickleQualityTypeOptions.valueOf(value.toUpperCase());
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}
}
