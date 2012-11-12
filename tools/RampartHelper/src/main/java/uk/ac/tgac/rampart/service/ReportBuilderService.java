package uk.ac.tgac.rampart.service;

import java.io.File;

public interface ReportBuilderService {
	
	void buildReport(File job_dir, File project_dir) throws Exception;
}
