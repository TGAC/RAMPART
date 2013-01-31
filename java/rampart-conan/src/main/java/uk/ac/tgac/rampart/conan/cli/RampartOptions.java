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
package uk.ac.tgac.rampart.conan.cli;

import org.apache.commons.cli.*;

import java.io.File;

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
