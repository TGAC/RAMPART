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
import java.util.Map;

public class SicklePeV11Args extends SickleV11Args {

    private SicklePeV11Params params = new SicklePeV11Params();

    private FilePair inputFilePair;
	private FilePair outputFilePair;
	private File singlesOut;

		
	public SicklePeV11Args() {
		this.inputFilePair = null;
		this.outputFilePair = null;
		this.singlesOut = null;
	}

    @Override
    public boolean isSingleEndArgs() {
        return false;
    }
	
	@Override
    public FilePair getPairedEndInputFiles() {
		return inputFilePair;
	}

    @Override
	public void setPairedEndInputFiles(FilePair pairedEndInputFiles) {
		this.inputFilePair = pairedEndInputFiles;
	}

	@Override
    public FilePair getPairedEndOutputFiles() {
		return outputFilePair;
	}
	
	@Override
    public void setPairedEndOutputFiles(FilePair pairedEndOutputFiles) {
		this.outputFilePair = pairedEndOutputFiles;
	}
	
	@Override
    public File getSingleEndOutputFile() {
		return singlesOut;
	}

    @Override
	public void setSingleEndOutputFile(File singleEndOutputFile) {
		this.singlesOut = singleEndOutputFile;
	}

    @Override
    public File getSingleEndInputFile() {
        return null;
    }

    @Override
    public void setSingleEndInputFile(File singleEndInputFile) {
        // do nothing
    }

	@Override
	public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = super.getArgMap();

        if (this.inputFilePair != null) {
			pvp.put(params.getPeFile1(), "--" + params.getPeFile1().getName() + " " + this.inputFilePair.getFile1().getPath());
			pvp.put(params.getPeFile2(), "--" + params.getPeFile2().getName() + " " + this.inputFilePair.getFile2().getPath());
		}
		
		if (this.outputFilePair != null) {
			pvp.put(params.getOutputPe1(), "--" + params.getOutputPe1().getName() + " " + this.outputFilePair.getFile1().getPath());
			pvp.put(params.getOutputPe2(), "--" + params.getOutputPe2().getName() + " " + this.outputFilePair.getFile2().getPath());
		}
		
		if (this.singlesOut != null)
			pvp.put(params.getOutputSingles(), "--" + params.getOutputSingles().getName() + " " + this.singlesOut.getPath());
		
		return pvp;
	}

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {
        for(Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            };

            String param = entry.getKey().getName();

            this.inputFilePair = new FilePair();
            this.outputFilePair = new FilePair();

            if (param.equals(this.params.getPeFile1().getName())) {
                this.inputFilePair.setFile1(new File(entry.getValue()));
            }
            else if (param.equals(this.params.getPeFile2().getName())) {
                this.inputFilePair.setFile2(new File(entry.getValue()));
            }
            else if (param.equals(this.params.getOutputPe1().getName())) {
                this.outputFilePair.setFile1(new File(entry.getValue()));
            }
            else if (param.equals(this.params.getOutputPe2().getName())) {
                this.outputFilePair.setFile2(new File(entry.getValue()));
            }
            else if (param.equals(this.params.getOutputSingles().getName())) {
                this.singlesOut = new File(entry.getValue());
            }
            else {
                throw new IllegalArgumentException("Unknown parameter found: " + param);
            }
        }
    }

}
