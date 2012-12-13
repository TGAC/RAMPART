package uk.ac.tgac.rampart.frontend.cli;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class RampartOptions {

	public static final String OPT_CONFIG = "config";
	public static final String OPT_VERBOSE = "verbose";
	public static final String OPT_HELP = "help";
	
	private File config;
	private boolean verbose;
	private boolean help;
	
	public RampartOptions(CommandLine cmdLine) throws ParseException {
		
		if (cmdLine.hasOption(OPT_CONFIG)) {
			config = new File(cmdLine.getOptionValue(OPT_CONFIG));
		} else {
			throw new ParseException(OPT_CONFIG + " argument not specified.");
		}
		
		verbose = cmdLine.hasOption(OPT_VERBOSE) ? true : false;
		help = cmdLine.hasOption(OPT_HELP) ? true : false;
	}
	
	public File getConfig() {
		return config;
	}
	public void setConfig(File config) {
		this.config = config;
	}
	public boolean isVerbose() {
		return verbose;
	}
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	public boolean doHelp() {
		return help;
	}
	public void setHelp(boolean help) {
		this.help = help;
	}
	
	public void printUsage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(this.getClass().getName(), createOptions());
	}
	
	@SuppressWarnings("static-access")
	public static Options createOptions() {

		// Boolean options
		Option opt_verbose = new Option("v", OPT_VERBOSE, false, "Output extra information while running.");
		Option opt_help = new Option("?", OPT_HELP, false, "Print this message.");

		// Options with arguments		
		Option opt_config = OptionBuilder.withArgName("file").withLongOpt(OPT_CONFIG).hasArg()
				.withDescription("The rampart configuration file.").create("c");
		
		// create Options object
		Options options = new Options();

		// add t option
		options.addOption(opt_verbose);
		options.addOption(opt_help);
		options.addOption(opt_config);

		return options;
	}
	
	
}
