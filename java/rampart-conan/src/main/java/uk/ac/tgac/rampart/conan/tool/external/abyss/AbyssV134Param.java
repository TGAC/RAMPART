/**
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2013  Daniel Mapleson - TGAC
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package uk.ac.tgac.rampart.conan.tool.external.abyss;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.parameter.DefaultConanParameter;
import uk.ac.tgac.rampart.conan.parameter.NumericParameter;
import uk.ac.tgac.rampart.conan.tool.ToolParameter;

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
