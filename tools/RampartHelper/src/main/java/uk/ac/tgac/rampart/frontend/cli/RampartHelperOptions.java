package uk.ac.tgac.rampart.frontend.cli;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class RampartHelperOptions {

	public static final String OPT_JOB_DIR = "job_dir";
	public static final String OPT_PROJECT_DIR = "project_dir";
	public static final String OPT_VERBOSE = "verbose";
	public static final String OPT_HELP = "help";
	
	private File jobDir;
	private File projectDir;
	private boolean verbose;
	private boolean help;
	
	
	
	public RampartHelperOptions(File jobDir, File projectDir, boolean verbose, boolean help) {
		
		this.jobDir = jobDir;
		this.projectDir = projectDir;
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

		verbose = cmdLine.hasOption(OPT_VERBOSE) ? true : false;
		help = cmdLine.hasOption(OPT_HELP) ? true : false;
	}
	
	

	public File getJobDir() {
		return jobDir;
	}
	
	public File getProjectDir() {
		return projectDir;
	}
	
	public boolean isVerbose() {
		return verbose;
	}
	
	public boolean isHelp() {
		return help;
	}


	@SuppressWarnings("static-access")
	public static Options createOptions() {

		// Boolean options
		Option opt_verbose = new Option("v", OPT_VERBOSE, false, "Output extra information while running.");
		Option opt_help = new Option("?", OPT_HELP, false, "Print this message.");

		// Options with arguments		
		Option opt_job_dir = OptionBuilder.withArgName("file").withLongOpt(OPT_JOB_DIR).hasArg()
				.withDescription("The job directory to analyse.").create("j");
		
		Option opt_project_dir = OptionBuilder.withArgName("file").withLongOpt(OPT_PROJECT_DIR).hasArg()
				.withDescription("The rampart project directory.").create("p");
		
		// create Options object
		Options options = new Options();

		// add t option
		options.addOption(opt_verbose);
		options.addOption(opt_help);
		options.addOption(opt_job_dir);
		options.addOption(opt_project_dir);

		return options;
	}
}
