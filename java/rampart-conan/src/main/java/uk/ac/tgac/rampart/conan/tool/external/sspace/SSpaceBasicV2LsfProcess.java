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
package uk.ac.tgac.rampart.conan.tool.external.sspace;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.cli.ToolCommandLoader;

import java.util.Map;


public class SSpaceBasicV2LsfProcess extends AbstractRampartLSFProcess {
	
	private static final long serialVersionUID = -9137766834408006360L;
	
	public static final String NAME = "SSPACE_Basic_v2.0-LSF";
	public static final String COMPONENT_NAME = uk.ac.tgac.rampart.conan.cli.ToolCommandLoader.SSPACE_BASIC_2_0;
	public static final String COMMAND = "SSPACE_Basic_v2.0.pl";
	public static final String PARAM_PREFIX = "-";
	
	public SSpaceBasicV2LsfProcess() {
		super(NAME, COMPONENT_NAME, COMMAND, PARAM_PREFIX, 
				new SSpaceBasicV2Params().getConanParameters());
	}

    @Override
    protected String getCommand(Map<ConanParameter, String> parameters) throws IllegalArgumentException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public String loadToolCommand() {
		return 	ToolCommandLoader.getInstance().getLoadToolCommand(ToolCommandLoader.PERL_5_16_1) + "; " +
				ToolCommandLoader.getInstance().getLoadToolCommand(ToolCommandLoader.SSPACE_BASIC_2_0);
	}

}