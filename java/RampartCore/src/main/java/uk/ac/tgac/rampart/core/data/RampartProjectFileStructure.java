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
package uk.ac.tgac.rampart.core.data;

import java.io.File;
import java.io.IOException;

public class RampartProjectFileStructure {

	// Root directory
	private File rampartDir;
	
	// Directories
	private File toolsDir;
	private File dataDir;
	private File dataReportDir;
	private File dataReportImagesDir;
	
	// Important Files
	private File reportTemplateFile;
	private File weightingsFile;
	
	
	public RampartProjectFileStructure(File rampartDir) {
		this.rampartDir = rampartDir;
		
		setupFileStructure();
	}


	public void setupFileStructure() {
		
		// Record all important directories and make sure they exist
		this.toolsDir = new File(rampartDir.getPath() + "/tools");
		this.dataDir = new File(rampartDir.getPath() + "/data");
		this.dataReportDir = new File(dataDir.getPath() + "/report_template");
		this.dataReportImagesDir = new File(dataReportDir.getPath() + "/images");
		
		this.reportTemplateFile = new File(this.dataReportDir.getPath() + "/template.tex");
		this.weightingsFile = new File(rampartDir.getPath() + "/weightings.tab");
	}
	
	
	public void validate(boolean includeFiles) throws IOException {
		
		if (!this.toolsDir.exists() || !this.dataDir.exists() || !this.dataReportDir.exists() || !this.dataReportImagesDir.exists()) {
			throw new IOException("RAMPART project directory structure is not valid.");
		}
		
		if (includeFiles && (!this.reportTemplateFile.exists())) {
			throw new IOException("Not all RAMPART files are present");
		}
	}
	

	public File getRampartDir() {
		return rampartDir;
	}


	public File getToolsDir() {
		return toolsDir;
	}


	public File getDataDir() {
		return dataDir;
	}


	public File getDataReportDir() {
		return dataReportDir;
	}


	public File getDataReportImagesDir() {
		return dataReportImagesDir;
	}


	public File getReportTemplateFile() {
		return reportTemplateFile;
	}

	public File getWeightingsFile() {
		return weightingsFile;
	}

	
}
