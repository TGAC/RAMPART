package uk.ac.tgac.rampart.conan.tool.abyss;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.parameter.DefaultConanParameter;
import uk.ac.tgac.rampart.conan.parameter.NumericParameter;
import uk.ac.tgac.rampart.conan.parameter.ToolParameter;

public enum AbyssV134Param implements ToolParameter {
	
	LIBRARIES {

		@Override
		public ConanParameter getConanParameter() {
			return new AbyssV134InputLibsParameter();
		}
		
	},
	NB_CONTIG_PAIRS {

		@Override
		public ConanParameter getConanParameter() {
			return new NumericParameter(
					"n",
					"minimum  number  of  pairs  (default: 10). The optimal value for this parameter depends on  coverage, but 10 is a reasonable default.",
					true);
		}
		
	},
	KMER {
		@Override
		public ConanParameter getConanParameter() {
			return new NumericParameter(
					"k",
					"k-mer size",
					false);
		}
	},
	THREADS {
		@Override
		public ConanParameter getConanParameter() {
			return new NumericParameter(
					"np",
					"the number of processes of an MPI assembly",
					false);
		}
	},
	NAME {
		@Override
		public ConanParameter getConanParameter() {
			return new DefaultConanParameter(
					"name",
					"The name of this assembly. The resulting contigs will be stored in ${name}-contigs.fa",
					false, false, false);
		}
	};

}
