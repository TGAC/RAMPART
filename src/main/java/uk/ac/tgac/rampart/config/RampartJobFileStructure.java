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
package uk.ac.tgac.rampart.config;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class RampartJobFileStructure {

	// Root directory
	private File jobDir;
	
	// Directories
	private File meqcDir;
    private File meqcConfigDir;
	private File massDir;
	private File massStatsDir;
	private File ampDir;
	private File ampAssembliesDir;
	private File reportDir;
	private File reportImagesDir;
	private File logDir;
	
	// Important Files
	private File configFile;
    private File qtLogFile;
	private File massPlotsFile;
	private File massStatsFile;
	private File massLogFile;
    private File massOutFile;
	private File ampPlotsFile;
	private File ampStatsFile;
	private File ampLogFile;
	private File reportTemplateFile;
	private File reportMergedFile;
	private File settingsFile;
	
	
	public RampartJobFileStructure(File jobDir) {
		this.jobDir = jobDir;
		
		setupFileStructure();
	}


	public void setupFileStructure() {
		
		// Record all important directories and make sure they exist
		this.meqcDir = new File(jobDir, "mecq");
        this.meqcConfigDir = new File(meqcDir, "configs");
		this.massDir = new File(jobDir, "mass");
		this.massStatsDir = new File(massDir, "stats");
		this.ampDir = new File(jobDir, "amp");
		this.ampAssembliesDir = new File(ampDir, "assemblies");
		this.reportDir = new File(jobDir, "report");
		this.reportImagesDir = new File(reportDir, "images");
		this.logDir = new File(jobDir, "log");
		
		this.configFile = new File(jobDir, "rampart.cfg");
		this.qtLogFile = new File(this.meqcDir + "/qt.log");
		this.massPlotsFile = new File(this.massStatsDir.getPath() + "/plots.pdf");
		this.massStatsFile = new File(this.massStatsDir.getPath() + "/score.tab");
		this.massLogFile = new File(this.massDir.getPath() + "/mass.settings");
        this.massOutFile = new File(this.massStatsDir.getPath() + "/best.fa");
		this.ampPlotsFile = new File(this.ampAssembliesDir.getPath() + "/analyser.pdf");
		this.ampStatsFile = new File(this.ampAssembliesDir.getPath() + "/analyser.txt");
		this.ampLogFile = new File(this.ampDir.getPath() + "/amp.log");
		this.reportTemplateFile = new File(this.reportDir.getPath() + "/template.tex");
		this.reportMergedFile = new File(this.reportDir.getPath() + "/report.tex");
		this.settingsFile = new File(jobDir.getPath() + "/rampart_settings.log");


	}
	
	
	public void validate(boolean includeReport, boolean includeFiles) throws IOException {
		
		if (!this.meqcDir.exists() || !this.massDir.exists() || !this.massStatsDir.exists() || !this.ampDir.exists()) {
			throw new IOException("RAMPART job directory structure is not valid.");
		}
		
		if (includeReport && (!this.reportDir.exists() || !this.reportImagesDir.exists())) {
			throw new IOException("RAMPART report directory structure is not valid.");
		}
		
		/*if (includeFiles && (!this.massPlotsFile.exists() || !this.ampPlotsFile.exists())) {
			throw new IOException("Not all RAMPART files are present");
		} */
	}
	
	public void makeReportDirs() throws IOException {
		
		// Clean the report dir first...
        FileUtils.deleteDirectory(this.reportDir);
		
		// ... then rebuild
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


	public File getMeqcDir() {
		return meqcDir;
	}

    public File getMeqcConfigDir() {
        return meqcConfigDir;
    }

    public File getMassDir() {
		return massDir;
	}


	public File getMassStatsDir() {
		return massStatsDir;
	}


	public File getAmpDir() {
		return ampDir;
	}


	public File getReportDir() {
		return reportDir;
	}


	public File getReportImagesDir() {
		return reportImagesDir;
	}
	
	public File getLogDir() {
		return logDir;
	}
	
	
	// ****** Files ******

	public File getConfigFile() {
		return configFile;
	}
	
	public File getSettingsFile() {
		return settingsFile;
	}

	public File getQtLogFile() {
		return qtLogFile;
	}

	public File getMassPlotsFile() {
		return massPlotsFile;
	}

	public File getMassStatsFile() {
		return massStatsFile;
	}

	public File getMassLogFile() {
		return massLogFile;
	}

    public File getMassOutFile() {
        return massOutFile;
    }

    public File getReportTemplateFile() {
		return reportTemplateFile;
	}
	
	public File getReportMergedFile() {
		return reportMergedFile;
	}
	
	public File getAmpPlotsFile() {
		return ampPlotsFile;
	}

	public File getAmpAssembliesDir() {
		return ampAssembliesDir;
	}

	public File getAmpStatsFile() {
		return ampStatsFile;
	}

	public File getAmpLogFile() {
		return ampLogFile;
	}

    public File[] getMecqConfigFiles() {
        return this.meqcConfigDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                if (lowercaseName.endsWith(".cfg")) {
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

}
