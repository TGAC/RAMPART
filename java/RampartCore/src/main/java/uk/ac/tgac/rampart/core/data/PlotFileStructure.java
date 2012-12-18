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

public class PlotFileStructure {

	private static final String FILENAME_SUFFIX_NB_CONTIGS 	= "_NBC.pdf";	
	private static final String FILENAME_SUFFIX_NB_BASES 	= "_TB.pdf";
	private static final String FILENAME_SUFFIX_N_PERC 		= "_N.pdf";
	private static final String FILENAME_SUFFIX_AVG_LEN 	= "_AL.pdf";
	private static final String FILENAME_SUFFIX_MAX_LEN 	= "_ML.pdf";
	private static final String FILENAME_SUFFIX_N50 		= "_N50.pdf";
	private static final String FILENAME_SUFFIX_SCORES		= "_Scores.pdf";
	
	private File plots;
	private File outputDir;
	private String filenamePrefix;
	
	private File nbContigsFile;
	private File nbBasesFile;
	private File nPcFile;
	private File avgLenFile;
	private File maxLenFile;
	private File n50File;
	private File scoreFile;
	
	public PlotFileStructure(File plots, File outputDir, String filenamePrefix) {
		this.plots = plots;
		this.outputDir = outputDir;
		this.filenamePrefix = filenamePrefix;
		
		setupFiles();
	}
	
	protected void setupFiles() {
		final String outDir = this.outputDir.getPath() + "/" + this.filenamePrefix;
		
		this.nbContigsFile = new File(outDir + FILENAME_SUFFIX_NB_CONTIGS);
		this.nbBasesFile = new File(outDir + FILENAME_SUFFIX_NB_BASES);
		this.nPcFile = new File(outDir + FILENAME_SUFFIX_N_PERC);
		this.avgLenFile = new File(outDir + FILENAME_SUFFIX_AVG_LEN);
		this.maxLenFile = new File(outDir + FILENAME_SUFFIX_MAX_LEN);
		this.n50File = new File(outDir + FILENAME_SUFFIX_N50);
		this.scoreFile = new File(outDir + FILENAME_SUFFIX_SCORES);
	}

	public File getPlots() {
		return plots;
	}
	
	public File getOutputDir() {
		return outputDir;
	}

	public File getNbContigsFile() {
		return nbContigsFile;
	}

	public File getNbBasesFile() {
		return nbBasesFile;
	}

	public File getNPcFile() {
		return nPcFile;
	}

	public File getAvgLenFile() {
		return avgLenFile;
	}

	public File getMaxLenFile() {
		return maxLenFile;
	}

	public File getN50File() {
		return n50File;
	}
	
	public File getScoreFile() {
		return scoreFile;
	}
}
