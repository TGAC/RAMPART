package uk.ac.tgac.rampart.data;

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


	
}
