package uk.ac.tgac.rampart.conan.tool.gapcloser;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.parameter.NumericParameter;
import uk.ac.tgac.rampart.conan.parameter.PathParameter;
import uk.ac.tgac.rampart.conan.parameter.ToolParameter;

public enum GapCloserV112Param implements ToolParameter {

	INPUT_SCAFFOLD_FILE {

		@Override
		public ConanParameter getConanParameter() {
			return new PathParameter(
					"a",
					"input scaffold file name",
					false);
		}
		
	},	
	LIBRARY_FILE {

		@Override
		public ConanParameter getConanParameter() {
			return new PathParameter(
					"b",
					"input library info file name",
					false);
		}
		
	},
	OUTPUT_FILE {

		@Override
		public ConanParameter getConanParameter() {
			return new PathParameter(
					"o",
					"output file name",
					false);
		}
		
	},
	MAX_READ_LENGTH {

		@Override
		public ConanParameter getConanParameter() {
			return new NumericParameter(
					"l",
					"maximal read length (<=155), default=100",
					true);
		}
		
	},
	OVERLAP {

		@Override
		public ConanParameter getConanParameter() {
			return new NumericParameter(
					"p",
					"overlap param - kmer (<=31), default=25",
					true);
		}
		
	},
	THREADS {

		@Override
		public ConanParameter getConanParameter() {
			return new NumericParameter(
					"t",
					"thread number, default=1",
					true);
		}
		
	};
	
}
