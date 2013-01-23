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
package uk.ac.tgac.rampart.conan.tool.external.qt.sickle;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.parameter.FilePair;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SickleSeV11Args extends SickleV11Args {

	private File inputFile;
	private File outputFile;


	public SickleSeV11Args() {
		this.inputFile = null;
		this.outputFile = null;
	}

    public File getInputFile() {
        return inputFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

    @Override
	public Map<ConanParameter, String> getParameterValuePairs() {

        Map<ConanParameter, String> pvp = super.getParameterValuePairs();

        SicklePeV11Params params = new SicklePeV11Params();

        if (this.inputFile != null) {
			pvp.put(params.getPeFile1(), this.inputFile.getPath());
		}
		
		if (this.outputFile != null) {
			pvp.put(params.getOutputPe1(), this.outputFile.getPath());
		}

		return pvp;
	}
	
}
