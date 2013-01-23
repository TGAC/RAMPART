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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.parameter.FilePair;
import uk.ac.tgac.rampart.conan.tool.external.qt.QualityTrimmerArgs;
import uk.ac.tgac.rampart.conan.conanx.process.ProcessArgs;
import uk.ac.tgac.rampart.conan.tool.external.qt.sickle.SickleV11QualityTypeParameter.SickleQualityTypeOptions;

public class SicklePeV11Args extends SickleV11Args {
	
	private FilePair inputFilePair;
	private FilePair outputFilePair;
	private File singlesOut;

		
	public SicklePeV11Args() {
		this.inputFilePair = null;
		this.outputFilePair = null;
		this.singlesOut = null;
	}
	
	public FilePair getInputFilePair() {
		return inputFilePair;
	}
	
	public void setInputFilePair(FilePair inputFilePair) {
		this.inputFilePair = inputFilePair;
	}

	public FilePair getOutputFilePair() {
		return outputFilePair;
	}
	
	public void setOutputFilePair(FilePair outputFilePair) {
		this.outputFilePair = outputFilePair;
	}
	
	public File getOutputSingleEndFile() {
		return singlesOut;
	}

	public void setOutputSingleEndFile(File outputSingleEndFile) {
		this.singlesOut = outputSingleEndFile;
	}
	


	@Override
	public Map<ConanParameter, String> getParameterValuePairs() {

        Map<ConanParameter, String> pvp = super.getParameterValuePairs();

        SicklePeV11Params params = new SicklePeV11Params();

		if (this.inputFilePair != null) {
			pvp.put(params.getPeFile1(), this.inputFilePair.getFile1().getPath());
			pvp.put(params.getPeFile2(), this.inputFilePair.getFile2().getPath());
		}
		
		if (this.outputFilePair != null) {
			pvp.put(params.getOutputPe1(), this.outputFilePair.getFile1().getPath());
			pvp.put(params.getOutputPe2(), this.outputFilePair.getFile2().getPath());
		}
		
		if (this.singlesOut != null)
			pvp.put(params.getOutputSingles(), this.singlesOut.getPath());
		
		return pvp;
	}
	
}
