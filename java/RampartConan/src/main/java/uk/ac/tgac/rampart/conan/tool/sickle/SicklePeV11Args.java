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
package uk.ac.tgac.rampart.conan.tool.sickle;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.parameter.ToolArgs;
import uk.ac.tgac.rampart.conan.tool.sickle.SicklePeV11QualityTypeParameter.SickleQualityTypeOptions;

public class SicklePeV11Args implements ToolArgs {
	
	private File peFile1;
	private File peFile2;
	private File outputPe1;
	private File outputPe2;
	private File singlesOut;
	private Integer qualThreshold;
	private Integer lengthThreshold;
	private Boolean discardN;
	private SickleQualityTypeOptions qualType;
		
	public SicklePeV11Args() {
		this.peFile1 = null;
		this.peFile2 = null;
		this.outputPe1 = null;
		this.outputPe2 = null;
		this.singlesOut = null;
		this.qualThreshold = 20;
		this.lengthThreshold = 20;
		this.discardN = false;
		this.qualType = null;
	}
	
	public File getPeFile1() {
		return peFile1;
	}

	public File getPeFile2() {
		return peFile2;
	}

	public File getOutputPe1() {
		return outputPe1;
	}

	public File getOutputPe2() {
		return outputPe2;
	}

	public File getSinglesOut() {
		return singlesOut;
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
	
	

	public void setPeFile1(File peFile1) {
		this.peFile1 = peFile1;
	}

	public void setPeFile2(File peFile2) {
		this.peFile2 = peFile2;
	}

	public void setOutputPe1(File outputPe1) {
		this.outputPe1 = outputPe1;
	}

	public void setOutputPe2(File outputPe2) {
		this.outputPe2 = outputPe2;
	}

	public void setSinglesOut(File singlesOut) {
		this.singlesOut = singlesOut;
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
		
		if (this.peFile1 != null)
			pvp.put(SicklePeV11Param.PE_FILE_1.getConanParameter(), this.peFile1.getPath());
		
		if (this.peFile2 != null)
			pvp.put(SicklePeV11Param.PE_FILE_2.getConanParameter(), this.peFile2.getPath());
		
		if (this.outputPe1 != null)
			pvp.put(SicklePeV11Param.OUTPUT_PE_1.getConanParameter(), this.outputPe1.getPath());
		
		if (this.outputPe2 != null)
			pvp.put(SicklePeV11Param.OUTPUT_PE_1.getConanParameter(), this.outputPe2.getPath());
		
		if (this.singlesOut != null)
			pvp.put(SicklePeV11Param.SINGLES_FILE_1.getConanParameter(), this.singlesOut.getPath());
		
		if (this.qualThreshold != null)
			pvp.put(SicklePeV11Param.QUALITY_THRESHOLD.getConanParameter(), this.qualThreshold.toString());
		
		if (this.lengthThreshold != null)
			pvp.put(SicklePeV11Param.LENGTH_THRESHOLD.getConanParameter(), this.lengthThreshold.toString());
		
		if (this.discardN != null)
			pvp.put(SicklePeV11Param.DISCARD_N.getConanParameter(), this.discardN.toString());
		
		if (this.qualType != null)
			pvp.put(SicklePeV11Param.QUALITY_TYPE.getConanParameter(), this.qualType.toString().toLowerCase());
		
		return pvp;
	}
}
