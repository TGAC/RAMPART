package uk.ac.tgac.rampart.frontend.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ebi.fgpt.conan.core.pipeline.DefaultConanPipeline;
import uk.ac.ebi.fgpt.conan.core.user.GuestUser;
import uk.ac.ebi.fgpt.conan.factory.ConanTaskFactory;
import uk.ac.ebi.fgpt.conan.factory.DefaultTaskFactory;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.service.ConanPipelineService;
import uk.ac.ebi.fgpt.conan.service.ConanSubmissionService;
import uk.ac.ebi.fgpt.conan.service.ConanTaskService;
import uk.ac.ebi.fgpt.conan.service.ConanUserService;
import uk.ac.ebi.fgpt.conan.service.DefaultPipelineService;
import uk.ac.ebi.fgpt.conan.service.DefaultSubmissionService;
import uk.ac.ebi.fgpt.conan.service.DefaultTaskService;
import uk.ac.ebi.fgpt.conan.service.DefaultUserService;
import uk.ac.tgac.rampart.conan.parameter.tool.AbyssV134Params;
import uk.ac.tgac.rampart.conan.parameter.tool.AbyssV134Params.AbyssInputLibrariesParam;
import uk.ac.tgac.rampart.conan.process.lsf.tool.LsfAbyssV134Process;
import uk.ac.tgac.rampart.service.ToolLoaderService;
import uk.ac.tgac.rampart.util.ApplicationContextLoader;

public class Rampart {

	private static Logger log = Logger.getLogger(Rampart.class);

	@Autowired
	private ToolLoaderService toolLoaderService;
	
	private RampartOptions options;

	public Rampart(RampartOptions options) {
		this.options = options;
	}

	public void process() throws Exception {
		
		this.toolLoaderService.loadPropertiesFile("load_tool_commands.properties");
		
		Map<String, File> peLibs = new HashMap<String, File>();
		peLibs.put("f1", new File("f1"));
		peLibs.put("f2", new File("f2"));
		
		AbyssInputLibrariesParam inputLibraries = new AbyssInputLibrariesParam();
		inputLibraries.setPairedEndLibraries(peLibs);
		
		
		AbyssV134Params abyssParams = new AbyssV134Params();
		abyssParams.setKmer(65);
		abyssParams.setInputlibraries(inputLibraries);
		
		LsfAbyssV134Process abyssProcess = new LsfAbyssV134Process(abyssParams);

		List<ConanProcess> rampartProcesses = new ArrayList<ConanProcess>();
		rampartProcesses.add(abyssProcess);
		
		Map<ConanParameter, String> rampartPipelineParameters = abyssParams.getParameterValuePairs();
		
		
		ConanUser rampartUser = new GuestUser("daniel.mapleson@tgac.ac.uk");
		
		DefaultConanPipeline rampartPipeline = new DefaultConanPipeline("rampartPipeline", rampartUser, false);
		rampartPipeline.setProcesses(rampartProcesses);
				
		ConanTaskFactory conanTaskFactory = new DefaultTaskFactory();
		ConanTask<DefaultConanPipeline> rampartTask = conanTaskFactory.createTask(
				rampartPipeline,
				1, 
				rampartPipelineParameters, 
				ConanTask.Priority.HIGHEST,
				rampartUser);
		
		rampartTask.execute();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

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
