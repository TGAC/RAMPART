package uk.ac.tgac.rampart.service.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.VelocityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.tgac.rampart.data.Job;
import uk.ac.tgac.rampart.data.RampartJobFileStructure;
import uk.ac.tgac.rampart.data.RampartProjectFileStructure;
import uk.ac.tgac.rampart.data.SequenceFileStats;
import uk.ac.tgac.rampart.service.LatexService;
import uk.ac.tgac.rampart.service.MassPlotService;
import uk.ac.tgac.rampart.service.ReportBuilderService;
import uk.ac.tgac.rampart.service.VelocityMergerService;
import uk.ac.tgac.rampart.service.seq.FastQStatCounter;
import uk.ac.tgac.rampart.util.ProcessStreamManager;

@Service
public class ReportBuilderServiceImpl implements ReportBuilderService {

	private MassPlotService massPlotService;
	private VelocityMergerService velocityMergerService;
	private LatexService latexService;
	
	@Autowired
	public void setMassPlotService(MassPlotService massPlotService) {
		this.massPlotService = massPlotService;
	}
	
	@Autowired
	public void setVelocityMergerService(VelocityMergerService velocityMergerService) {
		this.velocityMergerService = velocityMergerService;
	}
	
	@Autowired
	public void setLatexService(LatexService latexService) {
		this.latexService = latexService;
	}

	

	protected Job gatherDetails(RampartJobFileStructure jobDir) throws IOException {

		// Get Info from MISO
		// Can't do this yet... need to talk to Rob and Xingdong
		//JobDetails jobDetails = getMISODetails();

		// Get Info from Reads
		List<SequenceFileStats> seqFileStats = getReadStats(jobDir);

		// Get Info from Assemblies (MASS) ---- is this necessary?

		// Get Info from Best Assembly

		// Get Info from Improver

		// Get final scaffold locations

		return null;
	}
	

	protected Job getJobDetails(int misoId) {
		
		// Ok so we should be using the MISO API to get these details
		
		
		return null;
	}

	protected List<SequenceFileStats> getReadStats(RampartJobFileStructure jobDir) throws IOException {
		List<SequenceFileStats> seqFileStats = new ArrayList<SequenceFileStats>();

		File[] files = jobDir.getReadsDir().listFiles(new FilenameFilter() {
			public boolean accept(File directory, String fileName) {
				return fileName.endsWith(".fastq") || fileName.endsWith(".fq");
			}
		});

		for (File f : files) {
			seqFileStats.add(FastQStatCounter.analyse(f));
		}

		return seqFileStats;
	}

	
	@Override
	public void buildReport(File job_dir, File project_dir) throws Exception {
		
		RampartJobFileStructure jobDir = new RampartJobFileStructure(job_dir);
		RampartProjectFileStructure projectDir = new RampartProjectFileStructure(project_dir);
		
		// Create the report directories
		jobDir.makeReportDirs();

		// Make sure the whole directory structure looks ok
		jobDir.validate(true, true);

		// Copy template resources
		FileUtils.copyFile(projectDir.getReportTemplateFile(), jobDir.getReportTemplateFile());
		FileUtils.copyDirectory(projectDir.getDataReportImagesDir(), jobDir.getReportImagesDir());

		// Create the plot files which are to be used in the report
		this.massPlotService.seperatePlots(jobDir.getMassPlotsFile(), jobDir.getReportImagesDir());

		// Gather statistics and other variables
		Job projectDetails = gatherDetails(jobDir);

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
		this.velocityMergerService.merge(jobDir.getReportTemplateFile(), vc, jobDir.getReportMergedFile());

		// Compile report (If there were any errors carry on anyway, we might still be able to log the details in the
		// database
		this.latexService.compileDocument(jobDir.getReportMergedFile(), jobDir.getReportDir());
		
		// Log details in database (Is this really part of building the report?)
		
	}

}
