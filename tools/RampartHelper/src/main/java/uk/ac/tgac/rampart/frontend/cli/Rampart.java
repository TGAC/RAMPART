package uk.ac.tgac.rampart.frontend.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;
import org.springframework.context.ApplicationContext;

import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.tool.abyss.AbyssV134Args;
import uk.ac.tgac.rampart.conan.tool.abyss.AbyssV134InputLibsArg;
import uk.ac.tgac.rampart.conan.tool.abyss.AbyssV134LsfProcess;
import uk.ac.tgac.rampart.util.ApplicationContextLoader;
import uk.ac.tgac.rampart.util.ToolCommandLoader;
import uk.ac.tgac.rampart.util.Tools;

public class Rampart {

	private static Logger log = Logger.getLogger(Rampart.class);

	@Autowired
	private ApplicationContext context;
	
	
	private RampartOptions options;

	public Rampart(RampartOptions options) {
		this.options = options;
	}

	public void process() throws Exception {
		
		// Load all Rampart load tool commands.  
		/*ToolCommandLoader.getInstance().loadPropertiesFile("load_tool_commands.properties");*/
		
		// Tell conan about the properties file
		/*File conanPropertiesFile = new File(Rampart.class.getResource("conan.properties").toURI());
		ConanProperties.getConanProperties().setPropertiesFile(conanPropertiesFile);*/
		
		
		// Start daemon
		/*ConanDaemonService conan = new DefaultDaemonService();		
		conan.start();*/
		
		// Create Pipeline
		
		Map<String, File> peLibs = new HashMap<String, File>();
		peLibs.put("f1", new File("f1"));
		peLibs.put("f2", new File("f2"));
		
		AbyssV134InputLibsArg inputLibraries = new AbyssV134InputLibsArg();
		inputLibraries.setPairedEndLibraries(peLibs);
		
		
		AbyssV134Args abyssArgs = new AbyssV134Args();
		abyssArgs.setKmer(65);
		abyssArgs.setInputlibraries(inputLibraries);
		
		AbyssV134LsfProcess abyssProcess = new AbyssV134LsfProcess();
		
		abyssProcess.execute(abyssArgs.getParameterValuePairs());

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
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws URISyntaxException {

		
		
		// Create the available options
		Options options = RampartOptions.createOptions();
		
				// Parse the actual arguments
		CommandLineParser parser = new PosixParser();
		Rampart rampart = null;
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
			rampart = new Rampart(rampartOptions);
			
			// Load Spring
			new ApplicationContextLoader().load(rampart, "applicationContext.xml");
			
			Tools.configureProperties();
			
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
