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
package uk.ac.tgac.rampart.conan.tool.external.asm.abyss;

import uk.ac.tgac.rampart.conan.conanx.parameter.DefaultConanParameter;

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
