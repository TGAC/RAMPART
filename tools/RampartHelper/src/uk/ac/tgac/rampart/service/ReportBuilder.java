package uk.ac.tgac.rampart.service;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.VelocityContext;

import uk.ac.tgac.rampart.data.JobDetails;
import uk.ac.tgac.rampart.data.RampartJobFileStructure;
import uk.ac.tgac.rampart.data.RampartProjectFileStructure;
import uk.ac.tgac.rampart.data.SequenceFileStats;
import uk.ac.tgac.rampart.service.seq.FastQStatCounter;
import uk.ac.tgac.rampart.util.ProcessStreamManager;

public class ReportBuilder {

	private RampartJobFileStructure jobDir;
	private RampartProjectFileStructure projectDir;

	public ReportBuilder(RampartJobFileStructure jobDir, RampartProjectFileStructure projectDir) {
		super();
		this.jobDir = jobDir;
		this.projectDir = projectDir;
	}

	public void process() throws IOException {

		// Create the report directories
		this.jobDir.makeReportDirs();

		// Make sure the whole directory structure looks ok
		this.jobDir.validate(true, true);

		// Copy template resources
		FileUtils.copyFile(this.projectDir.getReportTemplateFile(), this.jobDir.getReportTemplateFile());
		FileUtils.copyDirectory(this.projectDir.getDataReportImagesDir(), this.jobDir.getReportImagesDir());

		// Create the plot files which are to be used in the report
		MassPlotSeparator.seperatePlots(this.jobDir.getMassPlotsFile(), this.jobDir.getReportImagesDir());

		// Gather statistics and other variables
		JobDetails projectDetails = gatherDetails();

		// Build context
		VelocityContext vc = new VelocityContext();
		vc.put("project", projectDetails);
		vc.put("library_list", null);
		vc.put("qt_params", null);
		vc.put("assembly_params", null);
		vc.put("assembly_stats", null);
		vc.put("best_assembly", null);
		vc.put("file_locations", null);

		// Merge the template and context
		VelocityMerger.process(this.jobDir.getReportTemplateFile(), vc, this.jobDir.getReportMergedFile());

		// Compile report (If there were any errors carry on anyway, we might still be able to log the details in the
		// database
		try {
			compileReport();
		} catch (Exception e) {
			System.err.println("Could not generate report.  Error occured during LaTeX compilation: " + e.getMessage());
		}

		// Log details in database (Is this really part of building the report?)

	}

	public RampartJobFileStructure getJobDir() {
		return jobDir;
	}

	public RampartProjectFileStructure getProjectDir() {
		return projectDir;
	}

	public JobDetails gatherDetails() throws IOException {

		// Get Info from MISO
		// Can't do this yet... need to talk to Rob and Xingdong
		//JobDetails jobDetails = getMISODetails();

		// Get Info from Reads
		List<SequenceFileStats> seqFileStats = getReadStats();

		// Get Info from Assemblies (MASS) ---- is this necessary?

		// Get Info from Best Assembly

		// Get Info from Improver

		// Get final scaffold locations

		return null;
	}
	

	protected JobDetails getJobDetails(int misoId) {
		
		// Ok so we should be using the MISO API to get these details
		
		
		return null;
	}

	protected List<SequenceFileStats> getReadStats() throws IOException {
		List<SequenceFileStats> seqFileStats = new ArrayList<SequenceFileStats>();

		File[] files = this.jobDir.getReadsDir().listFiles(new FilenameFilter() {
			public boolean accept(File directory, String fileName) {
				return fileName.endsWith(".fastq") || fileName.endsWith(".fq");
			}
		});

		for (File f : files) {
			seqFileStats.add(FastQStatCounter.analyse(f));
		}

		return seqFileStats;
	}

	protected void compileReport() throws Exception {

		// Assumes latex is installed and pdflatex is on the path
		String command = "pdflatex -interaction=nonstopmode -output-directory=" + this.jobDir.getReportDir() + " "
				+ this.jobDir.getReportMergedFile();

		Process process = Runtime.getRuntime().exec(command);
		ProcessStreamManager psm = new ProcessStreamManager(process, "PDFLATEX");

		int code = psm.runInForeground(false);

		if (code != 0) {
			throw new IOException("PDFLATEX returned code " + code);
		}
	}
	
	public static void process(File job_dir, File project_dir) throws IOException {
		
		ReportBuilder rb = new ReportBuilder(new RampartJobFileStructure(job_dir), new RampartProjectFileStructure(project_dir));
		rb.process();		
	}

}
