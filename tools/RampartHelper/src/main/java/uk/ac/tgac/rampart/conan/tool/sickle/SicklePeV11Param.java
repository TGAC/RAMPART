package uk.ac.tgac.rampart.conan.tool.sickle;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.parameter.FlagParameter;
import uk.ac.tgac.rampart.conan.parameter.NumericParameter;
import uk.ac.tgac.rampart.conan.parameter.PathParameter;
import uk.ac.tgac.rampart.conan.parameter.ToolParameter;

public enum SicklePeV11Param implements ToolParameter {
	
	PE_FILE_1 {

		@Override
		public ConanParameter getConanParameter() {
			
			return new PathParameter(
					"pe-file1", 
					"Input paired-end fastq file 1 (required, must have same number of records as pe2)", 
					false);
		}
		
	},
	PE_FILE_2 {

		@Override
		public ConanParameter getConanParameter() {
			
			return new PathParameter(
					"pe-file2",
					"Input paired-end fastq file 2 (required, must have same number of records as pe1)",
					false);
		}
		
	},
	OUTPUT_PE_1 {

		@Override
		public ConanParameter getConanParameter() {
			
			return new PathParameter(
					"output-pe1",
					"Output trimmed fastq file 1 (required)",
					false);
		}
		
	},
	OUTPUT_PE_2 {

		@Override
		public ConanParameter getConanParameter() {
			
			return new PathParameter(
					"output-pe2",
					"Output trimmed fastq file 2 (required)",
					false);
		}
		
	},
	SINGLES_FILE_1 {

		@Override
		public ConanParameter getConanParameter() {
			
			return new PathParameter(
					"output-single",
					"Output trimmed singles fastq file (required)",
					false);				
		}
		
	},
	QUALITY_THRESHOLD {

		@Override
		public ConanParameter getConanParameter() {
			
			return new NumericParameter(
					"qual-threshold",
					"Threshold for trimming based on average quality in a window. Default 20.",
					true);
		}
		
	},
	LENGTH_THRESHOLD {

		@Override
		public ConanParameter getConanParameter() {
			
			return new NumericParameter(
					"length-threshold",
					"Threshold to keep a read based on length after trimming. Default 20.",
					true);
		}
		
	},
	DISCARD_N {

		@Override
		public ConanParameter getConanParameter() {
			
			return new FlagParameter(
					"discard-n",
					"Discard sequences with any Ns in them.");
		}
		
	},
	QUALITY_TYPE {

		@Override
		public ConanParameter getConanParameter() {
			
			return new SicklePeV11QualityTypeParameter();
		}
		
	};

}
