package uk.ac.tgac.rampart.service.impl;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.tgac.rampart.data.RampartJobFileStructure;
import uk.ac.tgac.rampart.data.RampartProjectFileStructure;
import uk.ac.tgac.rampart.service.LatexService;
import uk.ac.tgac.rampart.service.RampartJobService;
import uk.ac.tgac.rampart.service.ReportBuilderService;
import uk.ac.tgac.rampart.service.VelocityMergerService;

@Service
public class ReportBuilderServiceImpl implements ReportBuilderService {

	private static Logger log = Logger.getLogger(ReportBuilderServiceImpl.class.getName());
	
	@Autowired
	private RampartJobService rampartJobService;
	
	@Autowired
	private VelocityMergerService velocityMergerService;
	
	@Autowired
	private LatexService latexService;
	
	
	@Override
	public void buildReport(File jobDir, File projectDir) throws Exception {
		
		// Gather statistics and other variables
		log.info("Gathering job context for report");
		VelocityContext vc = this.rampartJobService.buildContext(jobDir, projectDir);
		log.info("Gathered job context for report");
		
		buildReport(jobDir, projectDir, vc);
	}
	
	@Override
	public void buildReport(File jobDir, File projectDir, VelocityContext context) throws Exception {
		
		log.info("Starting report generation");
		
		RampartJobFileStructure jobFS = new RampartJobFileStructure(jobDir);
		RampartProjectFileStructure projectFS = new RampartProjectFileStructure(projectDir);
		
		// Create the report directories
		jobFS.makeReportDirs();
		log.debug("Created report directories");
		
		// Make sure the whole directory structure looks ok
		jobFS.validate(true, true);
		log.debug("Validated new job directory structure");

		// Copy template resources
		FileUtils.copyFile(projectFS.getReportTemplateFile(), jobFS.getReportTemplateFile());
		FileUtils.copyDirectory(projectFS.getDataReportImagesDir(), jobFS.getReportImagesDir());
		log.debug("Copied common resources to report directories");
		
		// Create the plot files which are to be used in the report
		this.rampartJobService.seperatePlots(jobFS.getMassPlotsFile(), jobFS.getReportImagesDir(), "Mass");
		log.debug("Seperated Mass plots into seperate files for report");
		
		// Create the plot files which are to be used in the report
		this.rampartJobService.seperatePlots(jobFS.getImproverPlotsFile(), jobFS.getReportImagesDir(), "Improver");
		log.debug("Seperated Improver plots into seperate files for report");

		// Merge the template and context
		this.velocityMergerService.merge(jobFS.getReportTemplateFile(), context, jobFS.getReportMergedFile());
		log.debug("Merged report template and context");
		
		// Compile report (If there were any errors carry on anyway, we might still be able to log the 
		// details in the database
		this.latexService.compileDocument(jobFS.getReportMergedFile());
		log.info("Report built");
	}

}
