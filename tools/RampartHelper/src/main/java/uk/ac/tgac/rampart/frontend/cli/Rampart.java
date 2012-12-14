package uk.ac.tgac.rampart.frontend.cli;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
import uk.ac.tgac.rampart.conan.parameter.tool.AbyssV134Params;
import uk.ac.tgac.rampart.conan.parameter.tool.AbyssV134Params.AbyssInputLibrariesParam;
import uk.ac.tgac.rampart.conan.process.lsf.tool.LsfAbyssV134Process;
import uk.ac.tgac.rampart.util.ApplicationContextLoader;
import uk.ac.tgac.rampart.util.ToolCommandLoader;

public class Rampart {

	private static Logger log = Logger.getLogger(Rampart.class);

	private RampartOptions options;

	public Rampart(RampartOptions options) {
		this.options = options;
	}

	public void process() throws Exception {
		
		// Load all Rampart load tool commands.  
		ToolCommandLoader.getInstance().loadPropertiesFile("load_tool_commands.properties");
		
		// Tell conan about the properties file
		File conanPropertiesFile = new File(ClassLoader.getSystemResource("conan.properties").toURI());
		ConanProperties.getConanProperties().setPropertiesFile(conanPropertiesFile);
		
		
		// Start daemon
		/*ConanDaemonService conan = new DefaultDaemonService();		
		conan.start();*/
		
		// Create Pipeline
		
		Map<String, File> peLibs = new HashMap<String, File>();
		peLibs.put("f1", new File("f1"));
		peLibs.put("f2", new File("f2"));
		
		AbyssInputLibrariesParam inputLibraries = new AbyssInputLibrariesParam();
		inputLibraries.setPairedEndLibraries(peLibs);
		
		
		AbyssV134Params abyssParams = new AbyssV134Params();
		abyssParams.setKmer(65);
		abyssParams.setInputlibraries(inputLibraries);
		
		LsfAbyssV134Process abyssProcess = new LsfAbyssV134Process(abyssParams);
		
		abyssProcess.execute(abyssParams.getParameterValuePairs());

		/*List<ConanProcess> rampartProcesses = new ArrayList<ConanProcess>();
		rampartProcesses.add(abyssProcess);
		
		ConanUser rampartUser = new GuestUser("daniel.mapleson@tgac.ac.uk");
		
		DefaultConanPipeline rampartPipeline = new DefaultConanPipeline("rampartPipeline", rampartUser, false);
		rampartPipeline.setProcesses(rampartProcesses);*/

		//conan.addPipeline(rampartPipeline);
		
		
		// Create task
		
		/*Map<ConanParameter, String> rampartPipelineParameters = abyssParams.getParameterValuePairs();
		
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
	 */
	public static void main(String[] args) {

		BasicConfigurator.configure();
		
		log.info("Starting RAMPART Helper");
		
		// Create the available options
		Options options = RampartOptions.createOptions();
		
		log.debug("Command line options created");

		// Parse the actual arguments
		CommandLineParser parser = new PosixParser();
		Rampart rampart = null;
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			RampartOptions rampartOptions = new RampartOptions(line);
			log.debug("Command line arguments processed");
			
			// If help was requested output that and finish before starting Spring 
			if (rampartOptions.doHelp()) {
				rampartOptions.printUsage();
				System.exit(0);
			}
			
			// Create RAMPART object
			rampart = new Rampart(rampartOptions);
			
			// Load Spring
			new ApplicationContextLoader().load(rampart, "applicationContext.xml");
			log.debug("Spring configured -- dependencies injected");
			
			
			
		} catch (ParseException exp) {
			log.fatal(exp.getMessage(), exp);
			System.exit(1);
		}
		
		// Run RAMPART
		try {
			rampart.process();
		}
		catch (IOException ioe) {
			log.fatal(ioe.getMessage(), ioe);
			System.exit(2);
		}
		catch (Exception e) {
			log.fatal(e.getMessage(), e);
			System.exit(3);
		}

		log.info("Finished RAMPART");
		
		System.exit(0);
	}

}
