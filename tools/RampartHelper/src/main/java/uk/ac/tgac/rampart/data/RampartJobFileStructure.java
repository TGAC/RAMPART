package uk.ac.tgac.rampart.data;

import java.io.File;
import java.io.IOException;

public class RampartJobFileStructure {

	// Root directory
	private File jobDir;
	
	// Directories
	private File readsDir;
	private File massDir;
	private File massStatsDir;
	private File improverDir;
	private File improverStatsDir;
	private File reportDir;
	private File reportImagesDir;
	
	// Important Files
	private File configFile;
	private File configRawFile;
	private File configQtFile;
	private File massPlotsFile;
	private File massStatsFile;
	private File improverStatsFile;
	private File reportTemplateFile;
	private File reportMergedFile;
	
	
	public RampartJobFileStructure(File jobDir) {
		this.jobDir = jobDir;
		
		setupFileStructure();
	}


	public void setupFileStructure() {
		
		// Record all important directories and make sure they exist
		this.readsDir = new File(jobDir.getPath() + "/reads");
		this.massDir = new File(jobDir.getPath() + "/mass");
		this.massStatsDir = new File(massDir.getPath() + "/stats");
		this.improverDir = new File(jobDir.getPath() + "/improver");
		this.improverStatsDir = new File(improverDir.getPath() + "/stats");
		this.reportDir = new File(jobDir.getPath() + "/report");
		this.reportImagesDir = new File(reportDir.getPath() + "/images");		
		
		this.configFile = new File(jobDir.getPath() + "/rampart.cfg");
		this.configRawFile = new File(this.readsDir.getPath() + "/raw.cfg");
		this.configQtFile = new File(this.readsDir.getPath() + "/qt.cfg");
		this.massPlotsFile = new File(this.massStatsDir.getPath() + "/plots.pdf");
		this.massStatsFile = new File(this.massStatsDir.getPath() + "/merged.tab");
		this.improverStatsFile = new File(this.improverStatsDir.getPath() + "/stats.txt");
		this.reportTemplateFile = new File(this.reportDir.getPath() + "/template.tex");
		this.reportMergedFile = new File(this.reportDir.getPath() + "/report.tex");
	}
	
	
	public void validate(boolean includeReport, boolean includeFiles) throws IOException {
		
		if (!this.readsDir.exists() || !this.massDir.exists() || !this.massStatsDir.exists() || !this.improverDir.exists()) {
			throw new IOException("RAMPART job directory structure is not valid.");
		}
		
		if (includeReport && (!this.reportDir.exists() || !this.reportImagesDir.exists())) {
			throw new IOException("RAMPART report directory structure is not valid.");
		}
		
		if (includeFiles && (!this.massPlotsFile.exists())) {
			throw new IOException("Not all RAMPART files are present");
		}
	}
	
	public void makeReportDirs() throws IOException {
		this.reportDir.mkdir();
		this.reportImagesDir.mkdir();
		
		if (!this.reportDir.exists() || !this.reportImagesDir.exists()) {
			throw new IOException("Could not create reports directories.");
		}
	}


	// ****** Dirs ******
	
	public File getJobDir() {
		return jobDir;
	}


	public File getReadsDir() {
		return readsDir;
	}


	public File getMassDir() {
		return massDir;
	}


	public File getMassStatsDir() {
		return massStatsDir;
	}


	public File getImproverDir() {
		return improverDir;
	}


	public File getReportDir() {
		return reportDir;
	}


	public File getReportImagesDir() {
		return reportImagesDir;
	}
	
	
	// ****** Files ******

	public File getConfigFile() {
		return configFile;
	}
	
	public File getConfigRawFile() {
		return configRawFile;
	}
	
	public File getConfigQtFile() {
		return configQtFile;
	}
	
	public File getMassPlotsFile() {
		return massPlotsFile;
	}

	public File getMassStatsFile() {
		return massStatsFile;
	}
	
	public File getReportTemplateFile() {
		return reportTemplateFile;
	}
	
	public File getReportMergedFile() {
		return reportMergedFile;
	}

	public File getImproverStatsDir() {
		return improverStatsDir;
	}

	public File getImproverStatsFile() {
		return improverStatsFile;
	}
	
	
}
