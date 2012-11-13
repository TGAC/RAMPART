package uk.ac.tgac.rampart.frontend.cli;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import uk.ac.tgac.rampart.service.ReportBuilderService;
import uk.ac.tgac.rampart.util.ApplicationContextProvider;
import uk.ac.tgac.rampart.util.Tools;

public class RampartHelper {
	
	private static Logger log = Logger.getLogger(RampartHelper.class.getName());
	
	private static final String OPT_JOB_DIR = "job_dir";
	private static final String OPT_PROJECT_DIR = "project_dir";
	private static final String OPT_VERBOSE = "verbose";
	private static final String OPT_HELP = "help";

	private File jobDir;
	private File projectDir;
	private boolean verbose;
	private boolean help;
	
	@Autowired
	private ReportBuilderService reportBuilderService;
	
	/*public void setReportBuilderService(ReportBuilderService reportBuilderService) {
		this.reportBuilderService = reportBuilderService;
	}*/
	
	
	public RampartHelper(File jobDir, File projectDir, boolean verbose, boolean help) {
		this.jobDir = jobDir;
		this.projectDir = projectDir;
		this.verbose = verbose;
		this.help = help;
		
		this.reportBuilderService = (ReportBuilderService) ApplicationContextProvider.getAppContext().getBean("reportBuilderServiceImpl");
	}
	
	public void process(Options options) {
		
		if (help) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("ReportGen", options);
			return;
		}

		try {
			// Analyse the rampart job directory, build the report and dump data to database
			this.reportBuilderService.buildReport(this.jobDir, this.projectDir);
		} catch (Exception ioe) {
			log.error("Problem merging template and context into latex file: " + ioe.getMessage());
			log.error(Tools.getStackTrace(ioe));
			return;
		}
	}

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

	private static RampartHelper processArgs(CommandLine line) throws ParseException {

		File jobDir;
		File projectDir;
		boolean verbose;
		boolean help;
		
		if (line.hasOption(OPT_JOB_DIR)) {
			jobDir = new File(line.getOptionValue(OPT_JOB_DIR));
		} else {
			throw new ParseException(OPT_JOB_DIR + " argument not specified.");
		}
		
		if (line.hasOption(OPT_PROJECT_DIR)) {
			projectDir = new File(line.getOptionValue(OPT_PROJECT_DIR));
		} else {
			throw new ParseException(OPT_PROJECT_DIR + " argument not specified.");
		}

		verbose = line.hasOption(OPT_VERBOSE) ? true : false;
		help = line.hasOption(OPT_HELP) ? true : false;
		
		return new RampartHelper(jobDir, projectDir, verbose, help);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		log.info("Starting RAMPART Helper");
		
		// Setup Spring application context first
		new ApplicationContextProvider().setApplicationContext(new ClassPathXmlApplicationContext("applicationContext.xml"));
		
		log.debug("Spring configured");
		
		// Create the available options
		Options options = createOptions();
		
		log.debug("Command line options created");

		// Parse the actual arguments
		CommandLineParser parser = new PosixParser();
		RampartHelper rh = null;
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			rh = processArgs(line);
		} catch (ParseException exp) {
			log.fatal("Options Parsing failed.  Reason: " + exp.getMessage());
			return;
		}		
		
		log.debug("Command line arguments processed");
		
		// Process report builder
		rh.process(options);		

		log.info("Finished RAMPART Helper");
		
		return;
	}
	
}
