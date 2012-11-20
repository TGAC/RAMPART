package uk.ac.tgac.rampart.service;

import java.io.File;

import org.apache.velocity.VelocityContext;

public interface ReportBuilderService {
	
	
	void buildReport(File job_dir, File project_dir) throws Exception;
	
	void buildReport(File job_dir, File project_dir, VelocityContext context) throws Exception;
}
