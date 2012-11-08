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
	private File reportDir;
	private File reportImagesDir;
	
	// Important Files
	private File massPlotsFile;
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
		this.reportDir = new File(jobDir.getPath() + "/report");
		this.reportImagesDir = new File(reportDir.getPath() + "/images");		
		
		this.massPlotsFile = new File(this.massStatsDir.getPath() + "/plots.pdf");
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


	public File getMassPlotsFile() {
		return massPlotsFile;
	}
	
	public File getReportTemplateFile() {
		return reportTemplateFile;
	}
	
	public File getReportMergedFile() {
		return reportMergedFile;
	}
}
