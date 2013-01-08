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
package uk.ac.tgac.rampart.conan.tool.external.gapcloser;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.parameter.NumericParameter;
import uk.ac.tgac.rampart.conan.conanx.parameter.PathParameter;
import uk.ac.tgac.rampart.conan.tool.ToolParameter;

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
