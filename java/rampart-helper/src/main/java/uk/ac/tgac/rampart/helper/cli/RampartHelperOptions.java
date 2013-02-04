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
package uk.ac.tgac.rampart.helper.cli;

import org.apache.commons.cli.*;

import java.io.File;

public class RampartHelperOptions {

	public static final String OPT_JOB_DIR = "job_dir";
	public static final String OPT_PROJECT_DIR = "project_dir";
	public static final String OPT_PERSIST = "persist";
	public static final String OPT_REPORT = "report";
	public static final String OPT_VERBOSE = "verbose";
	public static final String OPT_HELP = "help";
	
	private File jobDir;
	private File projectDir;
	private boolean persist;
	private boolean report;
	private boolean verbose;
	private boolean help;
	
	
	
	public RampartHelperOptions(File jobDir, File projectDir, boolean persist, boolean report, boolean verbose, boolean help) {
		
		this.jobDir = jobDir;
		this.projectDir = projectDir;
		this.report = report;
		this.persist = persist;
		this.verbose = verbose;
		this.help = help;
	}
	
	public RampartHelperOptions(CommandLine cmdLine) throws ParseException {
		
		if (cmdLine.hasOption(OPT_JOB_DIR)) {
			jobDir = new File(cmdLine.getOptionValue(OPT_JOB_DIR));
		} else {
			throw new ParseException(OPT_JOB_DIR + " argument not specified.");
		}
		
		if (cmdLine.hasOption(OPT_PROJECT_DIR)) {
			projectDir = new File(cmdLine.getOptionValue(OPT_PROJECT_DIR));
		} else {
			throw new ParseException(OPT_PROJECT_DIR + " argument not specified.");
		}

		persist = cmdLine.hasOption(OPT_PERSIST);
		report = cmdLine.hasOption(OPT_REPORT);
		verbose = cmdLine.hasOption(OPT_VERBOSE);
		help = cmdLine.hasOption(OPT_HELP);
	}
	
	

	public File getJobDir() {
		return jobDir;
	}
	
	public File getProjectDir() {
		return projectDir;
	}
	
	public boolean doPersist() {
		return persist;
	}

	public boolean doReport() {
		return report;
	}

	public boolean doVerbose() {
		return verbose;
	}
	
	public boolean doHelp() {
		return help;
	}


	@SuppressWarnings("static-access")
	public static Options createOptions() {

		// Boolean options
		Option opt_persist = new Option("pst", OPT_PERSIST, false, "Outputs job data to database");
		Option opt_report = new Option("scripts.r", OPT_REPORT, false, "Creates a report from job data");
		Option opt_verbose = new Option("v", OPT_VERBOSE, false, "Output extra information while running.");
		Option opt_help = new Option("?", OPT_HELP, false, "Print this message.");

		// Options with arguments		
		Option opt_job_dir = OptionBuilder.withArgName("file").withLongOpt(OPT_JOB_DIR).hasArg()
				.withDescription("The job directory to analyseReads.").create("j");
		
		Option opt_project_dir = OptionBuilder.withArgName("file").withLongOpt(OPT_PROJECT_DIR).hasArg()
				.withDescription("The rampart project directory.").create("prj");
		
		// create Options object
		Options options = new Options();

		// add t option
		options.addOption(opt_verbose);
		options.addOption(opt_help);
		options.addOption(opt_persist);
		options.addOption(opt_report);
		options.addOption(opt_job_dir);
		options.addOption(opt_project_dir);

		return options;
	}
}
