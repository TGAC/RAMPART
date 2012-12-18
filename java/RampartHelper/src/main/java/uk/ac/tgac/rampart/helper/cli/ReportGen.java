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
import org.apache.log4j.Logger;

import uk.ac.tgac.rampart.core.service.impl.VelocityMergerServiceImpl;


public class ReportGen {
	
	private static Logger log = Logger.getLogger(RampartHelper.class.getName());
		
	private static final String OPT_TEMPLATE 	= "template";
	private static final String OPT_CONTEXT 	= "context";
	private static final String OPT_OUTPUT 		= "output";
	private static final String OPT_VERBOSE 	= "verbose";
	private static final String OPT_HELP 		= "help";
	
	private static File template_path;
	private static File context_path;
	private static File output_path;
	private static boolean verbose;
	private static boolean help;
	
	

	@SuppressWarnings("static-access")
	private static Options createOptions() {
		
		// Boolean options
		Option verbose 	= new Option( "v", OPT_VERBOSE, false,	"Output extra information while running.");
		Option help 	= new Option( "?", OPT_HELP,	false, 	"Print this message." );
		
		// Options with arguments
		Option template = OptionBuilder.withArgName("file")
									   .withLongOpt(OPT_TEMPLATE)
				   					   .hasArg()
				   					   .withDescription("The template for the RAMPART report.")
				   					   .create("t");
		
		Option context 	= OptionBuilder.withArgName("file")
									   .withLongOpt(OPT_CONTEXT)
									   .hasArg()
									   .withDescription("The context for the RAMPART report.")
									   .create("c");
		
		Option output 	= OptionBuilder.withArgName("file")
									   .withLongOpt(OPT_OUTPUT)
									   .hasArg()
									   .withDescription("The location the merged and compiled RAMPART report should be placed.")
									   .create("o");
		
					
		// create Options object
		Options options = new Options();

		// add t option
		options.addOption(verbose);
		options.addOption(help);
		options.addOption(template);
		options.addOption(context);
		options.addOption(output);
		
		return options;
	}
	
	
	private static void processArgs(CommandLine line) throws ParseException {
		
		if( line.hasOption(OPT_TEMPLATE) ) {
			template_path = new File(line.getOptionValue(OPT_TEMPLATE));
		}
		else {
			throw new ParseException(OPT_TEMPLATE + " argument not specified.");
		}
		
		if( line.hasOption(OPT_CONTEXT) ) {
			context_path = new File(line.getOptionValue(OPT_CONTEXT));
		}
		else {
			throw new ParseException(OPT_CONTEXT + " argument not specified.");
		}
		
		output_path = line.hasOption(OPT_OUTPUT) ? new File(line.getOptionValue(OPT_OUTPUT)) : new File("report.pdf");
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
	        CommandLine line = parser.parse( options, args );
	        processArgs(line);
	    }
	    catch( ParseException exp ) {
	        // oops, something went wrong
	        log.error("Options Parsing failed.  Reason: " + exp.getMessage() );
	        return;
	    }
		
	    if (help) {
	    	HelpFormatter formatter = new HelpFormatter();
	    	formatter.printHelp( "ReportGen", options );
	    	return;
	    }
	    
	    try {
	    	// Merge the template and output the result
			new VelocityMergerServiceImpl().merge(template_path, context_path, output_path);
	    }
	    catch(IOException ioe) {
	    	log.error( "Problem merging template and context into latex file: " + ioe.getMessage() );
	    	return;
	    }
	    
	    if (verbose) {
	    	System.out.println("Finished");
	    }

	    return;
	}

}
