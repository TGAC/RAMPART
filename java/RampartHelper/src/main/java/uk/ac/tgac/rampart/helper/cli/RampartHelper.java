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

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import uk.ac.tgac.rampart.core.service.RampartJobService;
import uk.ac.tgac.rampart.core.service.ReportBuilderService;
import uk.ac.tgac.rampart.helper.util.ApplicationContextLoader;

public class RampartHelper {
	
	private static Logger log = Logger.getLogger(RampartHelper.class.getName());
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private ReportBuilderService reportBuilderService;
	
	@Autowired
	private RampartJobService rampartJobService;
	
	
	private RampartHelperOptions rhOptions;
	
	public RampartHelper(RampartHelperOptions options) {
		this.rhOptions = options;
	}
	
	public void process(Options options) throws Exception {
		
		if (this.rhOptions.doHelp()) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("RampartHelper", options);
			return;
		}

		
		// Analyse the rampart job directory and build the job context object 
		VelocityContext context = this.rampartJobService.buildContext(this.rhOptions.getJobDir(), this.rhOptions.getProjectDir());
		
		// Build the report
		if (this.rhOptions.doReport()) {
			this.reportBuilderService.buildReport(this.rhOptions.getJobDir(), this.rhOptions.getProjectDir(), context);
		}
		
		// Persist the context to the database
		if (this.rhOptions.doPersist()) {
			this.rampartJobService.persistContext(context);
		}
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		log.info("Starting RAMPART Helper");
		
		// Create the available options
		Options options = RampartHelperOptions.createOptions();
		
		log.debug("Command line options created");

		// Parse the actual arguments
		CommandLineParser parser = new PosixParser();
		RampartHelper rampartHelper = null;
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			RampartHelperOptions rhOptions = new RampartHelperOptions(line);
			rampartHelper = new RampartHelper(rhOptions);
			log.debug("Command line arguments processed");
			
			new ApplicationContextLoader().load(rampartHelper, "applicationContext.xml");
			log.debug("Spring configured -- dependencies injected");
			
		} catch (ParseException exp) {
			log.fatal(exp.getMessage(), exp);
			System.exit(1);
		}
		
		// Process report builder
		try {
			rampartHelper.process(options);
		}
		catch (IOException ioe) {
			log.fatal(ioe.getMessage(), ioe);
			System.exit(2);
		}
		catch (Exception e) {
			log.fatal(e.getMessage(), e);
			System.exit(3);
		}

		log.info("Finished RAMPART Helper");
		
		return;
	}
	
}
