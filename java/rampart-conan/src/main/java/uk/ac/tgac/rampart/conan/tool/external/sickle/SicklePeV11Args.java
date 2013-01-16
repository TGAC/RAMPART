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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.parameter.FilePair;
import uk.ac.tgac.rampart.conan.tool.args.QualityTrimmingArgs;
import uk.ac.tgac.rampart.conan.conanx.process.ProcessArgs;
import uk.ac.tgac.rampart.conan.tool.external.sickle.SicklePeV11QualityTypeParameter.SickleQualityTypeOptions;

public class SicklePeV11Args implements ProcessArgs, QualityTrimmingArgs {
	
	private FilePair inputFilePair;
	private FilePair outputFilePair;
	private File singlesOut;
	private Integer qualThreshold;
	private Integer lengthThreshold;
	private Boolean discardN;
	private SickleQualityTypeOptions qualType;
		
	public SicklePeV11Args() {
		this.inputFilePair = null;
		this.outputFilePair = null;
		this.singlesOut = null;
		this.qualThreshold = 20;
		this.lengthThreshold = 20;
		this.discardN = false;
		this.qualType = null;
	}
	
	@Override
	public FilePair getInputFilePair() {
		return inputFilePair;
	}
	
	@Override
	public void setInputFilePair(FilePair inputFilePair) {
		this.inputFilePair = inputFilePair;
	}

	@Override
	public FilePair getOutputFilePair() {
		return outputFilePair;
	}
	
	@Override
	public void setOutputFilePair(FilePair outputFilePair) {
		this.outputFilePair = outputFilePair;
	}
	
	@Override
	public File getOutputSingleEndFile() {
		return singlesOut;
	}

	@Override
	public void setOutputSingleEndFile(File outputSingleEndFile) {
		this.singlesOut = outputSingleEndFile;
	}
	
	public Integer getQualThreshold() {
		return qualThreshold;
	}

	public Integer getLengthThreshold() {
		return lengthThreshold;
	}

	public Boolean isDiscardN() {
		return discardN;
	}

	public void setQualThreshold(Integer qualThreshold) {
		this.qualThreshold = qualThreshold;
	}

	public void setLengthThreshold(Integer lengthThreshold) {
		this.lengthThreshold = lengthThreshold;
	}

	public void setDiscardN(Boolean discardN) {
		this.discardN = discardN;
	}

	public void setQualType(SickleQualityTypeOptions qualType) {
		this.qualType = qualType;
	}

	public SickleQualityTypeOptions getQualType() {
		return qualType;
	}

	@Override
	public Map<ConanParameter, String> getParameterValuePairs() {
		
		Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();
		
		/*if (this.inputFilePair != null) {
			pvp.put(SicklePeV11Params.PE_FILE_1.getConanParameter(), this.inputFilePair.getFile1().getPath());
			pvp.put(SicklePeV11Params.PE_FILE_2.getConanParameter(), this.inputFilePair.getFile2().getPath());
		}
		
		if (this.outputFilePair != null) {
			pvp.put(SicklePeV11Params.OUTPUT_PE_1.getConanParameter(), this.outputFilePair.getFile1().getPath());
			pvp.put(SicklePeV11Params.OUTPUT_PE_2.getConanParameter(), this.outputFilePair.getFile2().getPath());
		}
		
		if (this.singlesOut != null)
			pvp.put(SicklePeV11Params.SINGLES_FILE_1.getConanParameter(), this.singlesOut.getPath());
		
		if (this.qualThreshold != null)
			pvp.put(SicklePeV11Params.QUALITY_THRESHOLD.getConanParameter(), this.qualThreshold.toString());
		
		if (this.lengthThreshold != null)
			pvp.put(SicklePeV11Params.LENGTH_THRESHOLD.getConanParameter(), this.lengthThreshold.toString());
		
		if (this.discardN != null)
			pvp.put(SicklePeV11Params.DISCARD_N.getConanParameter(), this.discardN.toString());
		
		if (this.qualType != null)
			pvp.put(SicklePeV11Params.QUALITY_TYPE.getConanParameter(), this.qualType.toString().toLowerCase());
		            */
		return pvp;
	}
	
}
