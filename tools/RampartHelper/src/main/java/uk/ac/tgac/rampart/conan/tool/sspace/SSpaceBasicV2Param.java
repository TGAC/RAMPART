package uk.ac.tgac.rampart.conan.tool.sspace;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.parameter.DefaultConanParameter;
import uk.ac.tgac.rampart.conan.parameter.NumericParameter;
import uk.ac.tgac.rampart.conan.parameter.PathParameter;
import uk.ac.tgac.rampart.conan.parameter.ToolParameter;

public enum SSpaceBasicV2Param implements ToolParameter {

	LIBRARY_FILE {

		@Override
		public ConanParameter getConanParameter() {
			
			return new PathParameter(
					"l",
					"Library file containing two mate pair files with insert size, error and either mate pair or paired end indication.",
					false);
		}		
		
	},
	CONTIG_FILE {

		@Override
		public ConanParameter getConanParameter() {
			
			return new PathParameter(
					"s",
					"Fasta file containing contig sequences used for extension. Inserted pairs are mapped to extended and non-extended contigs (REQUIRED)",
					false);
		}
		
	},
	EXTEND {

		@Override
		public ConanParameter getConanParameter() {
			
			return new NumericParameter(
					"x",
					"Indicate whether to extend the contigs of -s using paired reads in -l. (-x 1=extension, -x 0=no extension, default -x 0)",
					true);
		}
		
	},
	BOWTIE_THREADS {

		@Override
		public ConanParameter getConanParameter() {
			return new NumericParameter(
					"T",
					"Specify the number of threads in Bowtie. Corresponds to the -p/--threads option in Bowtie (default -T 1, optional)",
					true);
		}
		
	},
	BASE_NAME {

		@Override
		public ConanParameter getConanParameter() {
			return new DefaultConanParameter(
					"b",
					"Base name for your output files (optional)",
					false,
					true,
					false);
		}
		
	};
	
}
