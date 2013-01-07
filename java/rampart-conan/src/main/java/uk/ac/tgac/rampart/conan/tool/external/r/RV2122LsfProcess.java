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
package uk.ac.tgac.rampart.conan.tool.external.r;

import uk.ac.tgac.rampart.conan.process.lsf.DefaultRampartLsfProcess;
import uk.ac.tgac.rampart.conan.cli.ToolCommandLoader;


public class RV2122LsfProcess extends DefaultRampartLsfProcess  {

	private static final long serialVersionUID = 7102159667412634823L;

	public static final String NAME = "Sickle_V1.1-LSF";
	public static final String COMPONENT_NAME = ToolCommandLoader.R_2_12_2;
	public static final String COMMAND = "R CMD BATCH";
	public static final String PARAM_PREFIX = "--";
	
	public RV2122LsfProcess() {
		super(NAME, COMPONENT_NAME, COMMAND, PARAM_PREFIX, 
				RV2122Param.values());
	}

}
