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
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;


public class RampartCLI {

	private static Logger log = Logger.getLogger(RampartCLI.class);

	@Autowired
	private ApplicationContext context;
	
	
	private RampartOptions options;

	public RampartCLI(RampartOptions options) {
		this.options = options;
	}

	public void process() throws Exception {
		
		// Load all RampartCLI load tool commands.
		/*ToolCommandLoader.getInstance().loadPropertiesFile("load_tool_commands.properties");*/
		
		// Tell conan about the properties file
		/*File conanPropertiesFile = new File(RampartCLI.class.getResource("conan.properties").toURI());
		ConanProperties.getConanProperties().setPropertiesFile(conanPropertiesFile);*/
		
		
		// Start daemon
		/*ConanDaemonService conan = new DefaultDaemonService();		
		conan.start();*/
		
		// Create Pipeline
		
		Map<String, File> peLibs = new HashMap<String, File>();
		peLibs.put("f1", new File("f1"));
		peLibs.put("f2", new File("f2"));
		
		/*AbyssV134InputLibsArg inputLibraries = new AbyssV134InputLibsArg();
		inputLibraries.setPairedEndLibraries(peLibs);
		
		
		AbyssV134Args abyssArgs = new AbyssV134Args();
		abyssArgs.setKmer(65);
		abyssArgs.setInputlibraries(inputLibraries);
		
		AbyssV134ProcessLSF abyssProcess = new AbyssV134ProcessLSF();
		
		abyssProcess.execute(abyssArgs.getArgMap());*/

		/*List<ConanProcess> rampartProcesses = new ArrayList<ConanProcess>();
		rampartProcesses.add(abyssProcess);
		
		ConanUser rampartUser = new GuestUser("daniel.mapleson@tgac.ac.uk");
		
		DefaultConanPipeline rampartPipeline = new DefaultConanPipeline("rampartPipeline", rampartUser, false);
		rampartPipeline.setProcesses(rampartProcesses);*/

		//conan.addPipeline(rampartPipeline);
		
		
		// Create task
		
		/*Map<ConanParameter, String> rampartPipelineParameters = abyssParams.getArgMap();
		
		ConanTaskFactory conanTaskFactory = new DefaultTaskFactory();
		ConanTask<DefaultConanPipeline> rampartTask = conanTaskFactory.createTask(
				rampartPipeline,
				1, 
				rampartPipelineParameters, 
				ConanTask.Priority.HIGHEST,
				rampartUser);
		rampartTask.setId("");
		rampartTask.submit();		
		rampartTask.execute();*/
	}
	
	
	/**
	 * @param args
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws URISyntaxException {

		// Create the available options
		Options options = RampartOptions.createOptions();
		
				// Parse the actual arguments
		CommandLineParser parser = new PosixParser();
		RampartCLI rampart = null;
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			RampartOptions rampartOptions = new RampartOptions(line);
			
			// If help was requested output that and finish before starting Spring 
			if (rampartOptions.doHelp()) {
				rampartOptions.printUsage();
				System.exit(0);
			}
			
			// Create RAMPART object
			rampart = new RampartCLI(rampartOptions);
			
			Configure.configureProperties();
			
		} catch (ParseException exp) {
			log.fatal(exp.getMessage(), exp);
			System.exit(1);
		} catch (IOException e) {
			log.fatal(e.getMessage(), e);
			System.exit(2);
		}
		
		// Run RAMPART
		try {
			rampart.process();
		}
		catch(ProcessExecutionException pee) {
			System.exit(3);
		}
		catch (IOException ioe) {
			log.fatal(ioe.getMessage(), ioe);
			System.exit(4);
		}
		catch (Exception e) {
			log.fatal(e.getMessage(), e);
			System.exit(5);
		}

		log.info("Finished RAMPART");
		
		System.exit(0);
	}

}
