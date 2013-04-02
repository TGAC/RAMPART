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
package uk.ac.tgac.rampart.pipeline.cli;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExternalProcessConfiguration;
import uk.ac.ebi.fgpt.conan.core.context.locality.Local;
import uk.ac.ebi.fgpt.conan.core.context.locality.LocalityFactory;
import uk.ac.ebi.fgpt.conan.core.context.scheduler.SchedulerFactory;
import uk.ac.ebi.fgpt.conan.core.user.GuestUser;
import uk.ac.ebi.fgpt.conan.factory.DefaultTaskFactory;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExternalProcessConfiguration;
import uk.ac.ebi.fgpt.conan.model.context.Locality;
import uk.ac.ebi.fgpt.conan.model.context.Scheduler;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
import uk.ac.ebi.fgpt.conan.service.exception.TaskExecutionException;
import uk.ac.tgac.rampart.core.data.RampartJobFileStructure;
import uk.ac.tgac.rampart.pipeline.spring.RampartAppContext;
import uk.ac.tgac.rampart.pipeline.tool.pipeline.rampart.RampartArgs;
import uk.ac.tgac.rampart.pipeline.tool.pipeline.rampart.RampartPipeline;

import java.io.File;
import java.io.IOException;


public class RampartCLI {

    private static Logger log = LoggerFactory.getLogger(RampartCLI.class);


    private static RampartOptions processCmdLine(String[] args) throws ParseException {

        // Create the available options
        Options options = RampartOptions.createOptions();

        // Parse the actual arguments
        CommandLineParser parser = new PosixParser();

        // parse the command line arguments
        CommandLine line = parser.parse(options, args);
        RampartOptions rampartOptions = new RampartOptions(line);


        return rampartOptions;
    }

    private static void configureSystem(RampartOptions rampartOptions) throws IOException {

        // Environment specific configuration options are set in the user's home directory
        final String rampartSettingsDir = System.getProperty("user.home") + "/.rampart/";

        // Setup logging first
        File loggingProperties = new File(rampartSettingsDir, "log4j.properties");

        // If logging file exists use settings from that, otherwise use basic settings.
        if (loggingProperties.exists()) {
            PropertyConfigurator.configure(loggingProperties.getAbsolutePath());
        }
        else {
            BasicConfigurator.configure();
        }

        // Load spring
        RampartAppContext.INSTANCE.load("applicationContext.xml");

        // Load Conan properties
        final File conanPropsFile = new File(rampartSettingsDir + "conan.properties");
        if (conanPropsFile.exists()) {
            ConanProperties.getConanProperties().setPropertiesFile(conanPropsFile);
        }
    }

    private static ExecutionContext buildExecutionContext() throws IOException {

        // Get external process pre-commands
        ExternalProcessConfiguration externalProcessConfiguration = new DefaultExternalProcessConfiguration();
        if (ConanProperties.containsKey("externalProcessConfigFile")) {
            externalProcessConfiguration.setProcessConfigFilePath(ConanProperties.getProperty("externalProcessConfigFile"));
            externalProcessConfiguration.load();
        }

        // Get execution context
        Locality locality = ConanProperties.containsKey("executionContext.locality") ?
                LocalityFactory.createLocality(ConanProperties.getProperty("executionContext.locality")) :
                new Local();

        Scheduler scheduler = ConanProperties.containsKey("executionContext.scheduler") ?
                SchedulerFactory.createScheduler(ConanProperties.getProperty("executionContext.scheduler")) :
                null;

        if (scheduler != null && ConanProperties.containsKey("executionContext.scheduler.queue")) {
            scheduler.getArgs().setQueueName(ConanProperties.getProperty("executionContext.scheduler.queue"));
        }

        if (scheduler != null && ConanProperties.containsKey("executionContext.scheduler.extraArgs")) {
            scheduler.getArgs().setExtraArgs(ConanProperties.getProperty("executionContext.scheduler.extraArgs"));
        }


        ExecutionContext executionContext = new DefaultExecutionContext(locality, scheduler, externalProcessConfiguration, true);

        return executionContext;
    }

    private static void cleanJob(File jobDir) throws IOException {

        // If no job directory is specified assume we want to clean the current directory
        if (jobDir == null) {
            jobDir = new File(".");
        }

        RampartJobFileStructure jobFs = new RampartJobFileStructure(jobDir);

        FileUtils.deleteDirectory(jobFs.getReadsDir());
        FileUtils.deleteDirectory(jobFs.getMassDir());
        FileUtils.deleteDirectory(jobFs.getImproverDir());
        FileUtils.deleteDirectory(jobFs.getReportDir());
        FileUtils.deleteDirectory(jobFs.getLogDir());
    }

    private static void runRampart(RampartArgs rampartArgs, ExecutionContext executionContext)
            throws InterruptedException, TaskExecutionException, IOException {

        // Create Rampart Pipeline (use Spring for autowiring)
        RampartPipeline rampartPipeline = (RampartPipeline)RampartAppContext.INSTANCE.getApplicationContext().getBean("rampartPipeline");
        rampartPipeline.configureProcesses(rampartArgs);

        // Ensure the output directory exists before we start
        if (!rampartArgs.getOutputDir().exists()) {
            rampartArgs.getOutputDir().mkdirs();
        }

        // Create a guest user
        ConanUser rampartUser = new GuestUser("daniel.mapleson@tgac.ac.uk");

        // Create the RAMPART task
        ConanTask<RampartPipeline> rampartTask = new DefaultTaskFactory().createTask(
                rampartPipeline,
                0,
                rampartPipeline.getArgs().getArgMap(),
                ConanTask.Priority.HIGHEST,
                rampartUser);
        rampartTask.setId("rampart");
        rampartTask.submit();
        rampartTask.execute(executionContext);
    }


    /**
     * @param args
     */
    public static void main(String[] args) {

        try {

            // Process the command line
            RampartOptions rampartOptions = processCmdLine(args);

            // If help was requested output that and finish before starting Spring
            if (rampartOptions.doHelp()) {
                rampartOptions.printUsage();
            }
            // Otherwise if clean option was selected then clean the specified job dir
            else if (rampartOptions.getClean() != null) {
                cleanJob(rampartOptions.getClean());
            }
            // Otherwise run RAMPART proper
            else {

                // Configure RAMPART system
                configureSystem(rampartOptions);
                log.info("RAMPART: System configured");

                // Build execution context
                ExecutionContext executionContext = buildExecutionContext();
                log.info("RAMPART: Built execution context");

                // Build Rampart args from the apache command line handler
                RampartArgs rampartArgs = rampartOptions.convert();

                // Run RAMPART
                log.info("RAMPART: Started");
                runRampart(rampartArgs, executionContext);
                log.info("RAMPART: Finished");
            }
        }
        catch (IOException ioe) {
            log.error(ioe.getMessage(), ioe);
            System.exit(2);
        }
        catch (ParseException exp) {
            System.err.println(exp.getMessage());
            System.err.println(StringUtils.join(exp.getStackTrace(), "\n"));
            System.exit(3);
        }
        catch (InterruptedException ie) {
            log.error(ie.getMessage(), ie);
            System.exit(4);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            System.exit(6);
        }
    }



}
