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
package uk.ac.tgac.rampart.conan.tool.external.sickle;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.cli.ToolCommandLoader;
import uk.ac.tgac.rampart.conan.tool.AbstractRampartLSFProcess;

import java.util.Map;

public class SicklePeV11LsfProcess extends AbstractRampartLSFProcess {

	private static final long serialVersionUID = 7102159667412634823L;

	public static final String NAME = "Sickle_V1.1-LSF";
	public static final String COMPONENT_NAME = ToolCommandLoader.SICKLE_1_1;
	public static final String COMMAND = "sickle pe";
	public static final String PARAM_PREFIX = "--";
	
	public SicklePeV11LsfProcess() {
		super(NAME, COMPONENT_NAME, COMMAND, PARAM_PREFIX, 
				new SicklePeV11Params().getConanParameters());
	}

    @Override
    protected String getCommand(Map<ConanParameter, String> parameters) throws IllegalArgumentException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
