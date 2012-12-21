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
package uk.ac.tgac.rampart.conan.tool.sspace;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.parameter.DefaultConanParameter;
import uk.ac.tgac.rampart.conan.parameter.NumericParameter;
import uk.ac.tgac.rampart.conan.parameter.PathParameter;
import uk.ac.tgac.rampart.conan.parameter.tools.ToolParameter;

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
