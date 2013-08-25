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
package uk.ac.tgac.rampart;

import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
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
import uk.ac.ebi.fgpt.conan.service.DefaultProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.TaskExecutionException;
import uk.ac.tgac.conan.TgacConanConfigure;
import uk.ac.tgac.conan.core.util.JarUtils;
import uk.ac.tgac.rampart.tool.RampartConfiguration;
import uk.ac.tgac.rampart.tool.pipeline.rampart.RampartArgs;
import uk.ac.tgac.rampart.tool.pipeline.rampart.RampartPipeline;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

/**
 * Main entry point for RAMPART: a automated de novo assembly pipeline.
 */
public class RampartCLI {

    private static Logger log = LoggerFactory.getLogger(RampartCLI.class);

    // Environment specific configuration options and resource files are set in the user's home directory
    public static final File SETTINGS_DIR = new File(System.getProperty("user.home") + "/.tgac/rampart/");

    public static final File DATA_DIR = new File(SETTINGS_DIR, "data");
    public static final File REPORT_DIR = new File(DATA_DIR, "report");
    public static final File SCRIPTS_DIR = new File(SETTINGS_DIR, "scripts");

    public static final File DEFAULT_ENV_CONFIG = new File(SETTINGS_DIR, "conan.properties");
    public static final File DEFAULT_LOG_CONFIG = new File(SETTINGS_DIR, "log4j.properties");

    /**
     * Configures the RAMPART system.  Specifically, this means initialising logging, initialising conan, and copying any
     * required resources to a known location if they are not there already.
     * @param rampartOptions Information provided by the user to the command line
     * @throws IOException Thrown if there are any issues dealing with the environment or logging configuration files,
     * or if resources couldn't be copied to a suitable location
     */
    private static void configureSystem(RampartOptions rampartOptions) throws IOException {

        if (!SETTINGS_DIR.exists()) {
            if (!SETTINGS_DIR.mkdirs()) {
                throw new IOException("Could not create RAMPART settings directory in: " + SETTINGS_DIR.getAbsolutePath());
            };
        }

        // Setup logging first
        File loggingProperties = rampartOptions.getLogConfig();

        // If logging file exists use settings from that, otherwise use basic settings.
        if (loggingProperties.exists()) {
            PropertyConfigurator.configure(loggingProperties.getAbsolutePath());
            log.debug("Using user specified logging properties: " + loggingProperties.getAbsolutePath());
        }
        else {
            BasicConfigurator.configure();
            log.debug("No log4j properties file found.  Using default logging properties.");
        }

        // Load spring
        //RampartAppContext.INSTANCE.load("/applicationContext.xml");

        // Load Conan properties
        final File conanPropsFile = rampartOptions.getEnvironmentConfig();
        if (conanPropsFile.exists()) {
            ConanProperties.getConanProperties().setPropertiesFile(conanPropsFile);
        }

        // Copy resources to external system

        File internalScripts = FileUtils.toFile(RampartCLI.class.getResource("/scripts"));
        File internalData = FileUtils.toFile(RampartCLI.class.getResource("/data"));

        File externalScriptsDir = new File(SETTINGS_DIR, "scripts");
        File externalDataDir = new File(SETTINGS_DIR, "data");

        JarFile thisJar = JarUtils.jarForClass(RampartCLI.class, null);

        if (thisJar == null) {

            log.debug("Copying resources to settings directory.");
            FileUtils.copyDirectory(internalScripts, externalScriptsDir);
            FileUtils.copyDirectory(internalData, externalDataDir);
        }
        else {

            log.debug("Executing from JAR.  Copying resources to settings directory.");
            JarUtils.copyResourcesToDirectory(thisJar, "scripts", externalScriptsDir.getAbsolutePath());
            JarUtils.copyResourcesToDirectory(thisJar, "data", externalDataDir.getAbsolutePath());
        }

        // Intialise TGAC Conan
        TgacConanConfigure.initialise();
    }

    /**
     * Returns the current working directory as an absolute file
     * @return
     */
    public static File currentWorkingDir() {
        return new File(".").getAbsoluteFile().getParentFile();
    }


    /**
     * Constructs an execution context from details discovered from the environment configuration file.
     * @return
     * @throws IOException
     */
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


        return new DefaultExecutionContext(locality, scheduler, externalProcessConfiguration, true);
    }

    /**
     * Cleans a RAMPART job directory of any known temporary information.  This will not delete any information not in
     * the MECQ, MASS, AMP or REPORT directories.
     * @param jobDir The RAMPART job directory to clean
     * @throws IOException Thrown if there were an issues cleaning the directory
     */
    private static void cleanJob(File jobDir) throws IOException {

        // If no job directory is specified assume we want to clean the current directory
        jobDir = jobDir == null ? currentWorkingDir() : jobDir;

        RampartConfiguration jobFs = new RampartConfiguration(jobDir);

        FileUtils.deleteDirectory(jobFs.getMeqcDir());
        FileUtils.deleteDirectory(jobFs.getMassDir());
        FileUtils.deleteDirectory(jobFs.getAmpDir());
        FileUtils.deleteDirectory(jobFs.getReportDir());
    }

    /**
     * Constructs and configures a RAMPART pipeline using information provided by the user in the job configuration and
     * the environment configuration.  The pipeline is then added to a conan task and executed.
     * @param rampartArgs Details of the RAMPART job
     * @param executionContext How and where to run the RAMPART job
     * @throws InterruptedException
     * @throws TaskExecutionException
     * @throws IOException
     */
    private static void runRampart(RampartArgs rampartArgs, ExecutionContext executionContext)
            throws InterruptedException, TaskExecutionException, IOException {

        // Create Rampart Pipeline (use Spring for autowiring)
        RampartPipeline rampartPipeline = new RampartPipeline();
        rampartPipeline.setConanProcessService(new DefaultProcessService());
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
     * The main entry point for RAMPART
     * @param args Command line arguments
     */
    public static void main(String[] args) {

        try {

            // Process the command line
            RampartOptions rampartOptions = new RampartOptions(new PosixParser().parse(RampartOptions.createOptions(), args));

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
