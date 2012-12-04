package uk.ac.tgac.rampart.data;

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
