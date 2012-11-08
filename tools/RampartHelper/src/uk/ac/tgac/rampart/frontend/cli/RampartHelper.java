package uk.ac.tgac.rampart.frontend.cli;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import uk.ac.tgac.rampart.service.ReportBuilder;

public class RampartHelper {
	private static final String OPT_JOB_DIR = "job_dir";
	private static final String OPT_PROJECT_DIR = "project_dir";
	private static final String OPT_VERBOSE = "verbose";
	private static final String OPT_HELP = "help";

	private static File job_dir;
	private static File project_dir;
	private static boolean verbose;
	private static boolean help;

	private static Options createOptions() {

		// Boolean options
		Option opt_verbose = new Option("v", OPT_VERBOSE, false, "Output extra information while running.");
		Option opt_help = new Option("?", OPT_HELP, false, "Print this message.");

		// Options with arguments
		Option opt_job_dir = OptionBuilder.withArgName("file").withLongOpt(OPT_JOB_DIR).hasArg()
				.withDescription("The job directory to analyse.").create("j");
		
		// Options with arguments
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

	private static void processArgs(CommandLine line) throws ParseException {

		if (line.hasOption(OPT_JOB_DIR)) {
			job_dir = new File(line.getOptionValue(OPT_JOB_DIR));
		} else {
			throw new ParseException(OPT_JOB_DIR + " argument not specified.");
		}
		
		if (line.hasOption(OPT_PROJECT_DIR)) {
			project_dir = new File(line.getOptionValue(OPT_PROJECT_DIR));
		} else {
			throw new ParseException(OPT_PROJECT_DIR + " argument not specified.");
		}

		verbose = line.hasOption(OPT_VERBOSE) ? true : false;
		help = line.hasOption(OPT_HELP) ? true : false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// Create the available options
		Options options = createOptions();

		// Parse the actual arguments
		CommandLineParser parser = new PosixParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			processArgs(line);
		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Options Parsing failed.  Reason: " + exp.getMessage());
			return;
		}

		if (help) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("ReportGen", options);
			return;
		}

		try {
			// Analyse the rampart job directory, build the report and dump data to database
			ReportBuilder.process(job_dir, project_dir);
		} catch (IOException ioe) {
			System.err.println("Problem merging template and context into latex file: " + ioe.getMessage());
			return;
		}

		return;
	}
}
