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
package uk.ac.tgac.rampart.conan.tool.r;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.parameter.DefaultConanParameter;
import uk.ac.tgac.rampart.conan.parameter.PathParameter;
import uk.ac.tgac.rampart.conan.parameter.tools.ToolParameter;

public enum RV2122Param implements ToolParameter {
	
	ARGS {

		@Override
		public ConanParameter getConanParameter() {
			
			return new DefaultConanParameter(
					"args", 
					"Any arguments that should be provided to the script", 
					false,
					false,
					false);
		}
		
	},
	SCRIPT {

		@Override
		public ConanParameter getConanParameter() {
			
			return new PathParameter(
					"script",
					"The R script to execute",
					false);
		}
		
	},
	OUTPUT {

		@Override
		public ConanParameter getConanParameter() {
			
			return new PathParameter(
					"output",
					"The location to store output from R",
					false);
		}
		
	};

}
