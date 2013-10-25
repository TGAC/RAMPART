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

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
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
import uk.ac.tgac.rampart.tool.pipeline.RampartStage;
import uk.ac.tgac.rampart.tool.pipeline.rampart.RampartArgs;
import uk.ac.tgac.rampart.tool.pipeline.rampart.RampartPipeline;
import uk.ac.tgac.rampart.util.CommandLineHelper;

import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.JarFile;

/**
 * This class handles execution of Rampart in run mode.
 */
public class Rampart {

    private static Logger log = LoggerFactory.getLogger(Rampart.class);

    // Environment specific configuration options and resource files are set in the user's home directory
    public static final File SETTINGS_DIR = new File(System.getProperty("user.home") + "/.tgac/rampart/");

    public static final File DATA_DIR = new File(SETTINGS_DIR, "data");
    public static final File REPORT_DIR = new File(DATA_DIR, "report");
    public static final File SCRIPTS_DIR = new File(SETTINGS_DIR, "scripts");



    // **** Option parameter names ****
    public static final String OPT_ENV_CONFIG = "env_config";
    public static final String OPT_LOG_CONFIG = "log_config";
    public static final String OPT_WEIGHTINGS = "weightings";
    public static final String OPT_OUTPUT_DIR = "output";
    public static final String OPT_JOB_PREFIX = "job_prefix";
    public static final String OPT_STAGES = "stages";
    public static final String OPT_VERBOSE = "verbose";
    public static final String OPT_HELP = "help";

    // **** Defaults ****
    public static final File    DEFAULT_ENV_CONFIG      = new File(SETTINGS_DIR, "conan.properties");
    public static final File    DEFAULT_LOG_CONFIG      = new File(SETTINGS_DIR, "log4j.properties");
    public static final File    DEFAULT_WEIGHTINGS_FILE = new File(DATA_DIR, "weightings.tab");
    public static final File    DEFAULT_OUTPUT_DIR      = new File("");
    public static final String  DEFAULT_JOB_PREFIX      = "rampart-" + createTimestamp();
    public static final String  DEFAULT_STAGES          = null;


    // **** Options ****
    private File jobConfig;
    private File environmentConfig;
    private File logConfig;
    private File weightingsFile;
    private File outputDir;
    private String jobPrefix;
    private String stages;

    private boolean verbose;
    private boolean help;

    /**
     * Creates a new RAMPART instance with default arguments
     */
    public Rampart() {
        this.jobConfig          = null;
        this.environmentConfig  = DEFAULT_ENV_CONFIG;
        this.logConfig          = DEFAULT_LOG_CONFIG;
        this.weightingsFile     = DEFAULT_WEIGHTINGS_FILE;
        this.outputDir          = DEFAULT_OUTPUT_DIR;
        this.jobPrefix          = DEFAULT_JOB_PREFIX;
        this.stages             = DEFAULT_STAGES;

        this.verbose            = false;
        this.help               = false;
    }

    /**
     * Creates a new RAMPART instance based on command line arguments
     * @param args List of command line arguments containing information to setup RAMPART
     * @throws ParseException Thrown if an invalid command line was encountered
     */
    public Rampart(String[] args) throws ParseException {

        // Parse the command line arguments
        CommandLine cmdLine = new PosixParser().parse(createOptions(), args, true);

        // Extract optional boolean flags
        this.help = cmdLine.hasOption(OPT_HELP);
        this.verbose = cmdLine.hasOption(OPT_VERBOSE);

        // Extract options
        this.environmentConfig = cmdLine.hasOption(OPT_ENV_CONFIG) ?
                new File(cmdLine.getOptionValue(OPT_ENV_CONFIG)) :
                DEFAULT_ENV_CONFIG;

        this.logConfig = cmdLine.hasOption(OPT_LOG_CONFIG) ?
                new File(cmdLine.getOptionValue(OPT_LOG_CONFIG)) :
                DEFAULT_LOG_CONFIG;

        this.weightingsFile = cmdLine.hasOption(OPT_WEIGHTINGS) ?
                new File(cmdLine.getOptionValue(OPT_WEIGHTINGS)) :
                DEFAULT_WEIGHTINGS_FILE;

        this.outputDir = cmdLine.hasOption(OPT_OUTPUT_DIR) ?
                new File(cmdLine.getOptionValue(OPT_OUTPUT_DIR)) :
                DEFAULT_OUTPUT_DIR;

        this.jobPrefix = cmdLine.hasOption(OPT_JOB_PREFIX) ?
                cmdLine.getOptionValue(OPT_JOB_PREFIX) :
                DEFAULT_JOB_PREFIX;

        this.stages = cmdLine.hasOption(OPT_STAGES) ?
                cmdLine.getOptionValue(OPT_STAGES) :
                DEFAULT_STAGES;

    }


    private Options createOptions() {

        // create Options object
        Options options = new Options();

        // add t option
        options.addOption(new Option("v", OPT_VERBOSE, false, "Output extra information while running."));
        options.addOption(new Option("?", OPT_HELP, false, "Print this message."));

        options.addOption(OptionBuilder.withArgName("file").withLongOpt(OPT_ENV_CONFIG).hasArg()
                .withDescription("The rampart environment configuration file.  Default: " + Rampart.DEFAULT_ENV_CONFIG.getAbsolutePath()).create("e"));

        options.addOption(OptionBuilder.withArgName("file").withLongOpt(OPT_LOG_CONFIG).hasArg()
                .withDescription("The rampart logging configuration file.  Default: " + Rampart.DEFAULT_LOG_CONFIG.getAbsolutePath()).create("l"));

        options.addOption(OptionBuilder.withArgName("file").withLongOpt(OPT_WEIGHTINGS).hasArg()
                .withDescription("The rampart logging configuration file.  Default: " + Rampart.DEFAULT_WEIGHTINGS_FILE.getAbsolutePath()).create("l"));

        options.addOption(OptionBuilder.withArgName("file").withLongOpt(OPT_OUTPUT_DIR).hasArg()
                .withDescription("The directory to put output from this job.").create("o"));

        options.addOption(OptionBuilder.withArgName("string").withLongOpt(OPT_JOB_PREFIX).hasArg()
                .withDescription("The job prefix descriptor to use when scheduling.  " +
                        "WARNING: Be careful what you set this to.  It's possible that if you run multiple jobs in succession " +
                        "with the same job prefix, the scheduler may confuse old recently completed jobs with the same name " +
                        "with this one.  This can cause some job's wait conditions to never be fulfilled.  Ideally, include " +
                        "some kind of unique identifier with this option such as a timestamp.  To help with this RAMPART will automatically " +
                        "replace any instances of \"TIMESTAMP\" in this argument with an actual timestamp.  Default: rampart-<timestamp>").create("p"));

        options.addOption(OptionBuilder.withArgName("string").withLongOpt(OPT_STAGES).hasArg()
                .withDescription("The RAMPART stages to execute: " + RampartStage.getFullListAsString() + ", ALL.  Default: ALL.").create("s"));

        return options;
    }

    /**
     * Configures the RAMPART system.  Specifically, this means initialising logging, initialising conan, and copying any
     * required resources to a known location if they are not there already.
     * @throws IOException Thrown if there are any issues dealing with the environment or logging configuration files,
     * or if resources couldn't be copied to a suitable location
     */
    private void configureSystem() throws IOException {

        // Create the settings directory in user.home if it doesn't already exist
        if (!SETTINGS_DIR.exists()) {
            if (!SETTINGS_DIR.mkdirs()) {
                throw new IOException("Could not create RAMPART settings directory in: " + SETTINGS_DIR.getAbsolutePath());
            }
        }

        // Setup logging first
        File loggingProperties = this.logConfig;

        // If logging file exists use settings from that, otherwise use basic settings.
        if (loggingProperties.exists()) {
            PropertyConfigurator.configure(loggingProperties.getAbsolutePath());
            log.debug("Using user specified logging properties: " + loggingProperties.getAbsolutePath());
        }
        else {
            BasicConfigurator.configure();
            log.debug("No log4j properties file found.  Using default logging properties.");
        }

        // Load Conan properties
        final File conanPropsFile = this.environmentConfig;
        if (conanPropsFile.exists()) {
            ConanProperties.getConanProperties().setPropertiesFile(conanPropsFile);
        }

        // Copy resources to external system

        File internalScripts = FileUtils.toFile(RampartCLI.class.getResource("/scripts"));
        File internalData = FileUtils.toFile(RampartCLI.class.getResource("/data"));
        File internalConfig = FileUtils.toFile(RampartCLI.class.getResource("/config"));


        File externalScriptsDir = new File(SETTINGS_DIR, "scripts");
        File externalDataDir = new File(SETTINGS_DIR, "data");
        File externalConfigDir = new File(SETTINGS_DIR, "config");

        JarFile thisJar = JarUtils.jarForClass(RampartCLI.class, null);

        if (thisJar == null) {

            log.debug("Copying resources to settings directory.");
            FileUtils.copyDirectory(internalScripts, externalScriptsDir);
            FileUtils.copyDirectory(internalData, externalDataDir);
            FileUtils.copyDirectory(internalConfig, externalConfigDir);
        }
        else {

            log.debug("Executing from JAR.  Copying resources to settings directory.");
            JarUtils.copyResourcesToDirectory(thisJar, "scripts", externalScriptsDir.getAbsolutePath());
            JarUtils.copyResourcesToDirectory(thisJar, "data", externalDataDir.getAbsolutePath());
            JarUtils.copyResourcesToDirectory(thisJar, "config", externalConfigDir.getAbsolutePath());
        }

        // Intialise TGAC Conan
        TgacConanConfigure.initialise();
    }

    /**
     * Constructs an execution context from details discovered from the environment configuration file.
     * @return An execution content build from the environment configuration file
     * @throws IOException
     */
    private ExecutionContext buildExecutionContext() throws IOException {

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

        String localityName = locality == null ? "" : locality.toString();
        log.debug("RAMPART: ENV: Locality: " + localityName);

        Scheduler scheduler = ConanProperties.containsKey("executionContext.scheduler") ?
                SchedulerFactory.createScheduler(ConanProperties.getProperty("executionContext.scheduler")) :
                null;

        String schedulerName = scheduler == null ? "" : scheduler.getName();
        log.debug("RAMPART: ENV: Scheduler: " + schedulerName);

        if (scheduler != null && ConanProperties.containsKey("executionContext.scheduler.queue")) {
            scheduler.getArgs().setQueueName(ConanProperties.getProperty("executionContext.scheduler.queue"));
        }

        if (scheduler != null && ConanProperties.containsKey("executionContext.scheduler.extraArgs")) {
            scheduler.getArgs().setExtraArgs(ConanProperties.getProperty("executionContext.scheduler.extraArgs"));
        }


        return new DefaultExecutionContext(locality, scheduler, externalProcessConfiguration, true);
    }

    /**
     * Constructs and configures a RAMPART pipeline using information provided by the user in the job configuration and
     * the environment configuration.  The pipeline is then added to a conan task and executed.
     * @param rampartArgs Details of the RAMPART job
     * @param executionContext How and where to run the RAMPART job
     * @throws InterruptedException
     * @throws uk.ac.ebi.fgpt.conan.service.exception.TaskExecutionException
     * @throws IOException
     */
    private void runRampart(RampartArgs rampartArgs, ExecutionContext executionContext)
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
     * Creates a timestamp as a String that can be sorted into chronological order based on the numbers in the String
     * @return
     */
    protected static final String createTimestamp() {
        Format formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return formatter.format(new Date());
    }


    private void printHelp() {
        CommandLineHelper.printHelp(
                System.err,
                RampartCLI.START_COMMAND_LINE + " run <job_config_file>",
                "RAMPART pipeline runner\n\n" +
                "This tool executes the RAMPART pipeline according to the rules and requests described by the job configuration " +
                "file, which should be the last argument provided on the command line.\n\n" +
                "The pipeline can be loosely described in 4 separate steps.  The first step involves error correction and " +
                "or quality trimming.  The second involves assembling the input data, multiple assemblies can be created " +
                "using different assemblers and settings.  Each assembly can be compared and analysed in order to determine " +
                "the best candidate.  The third stage involves trying to improve the selected assembly further by using " +
                "tools such as scaffolders and gap closers.\n\n",
                createOptions()
        );
    }

    /**
     * Using all the properties that have been defined, this method executes the RAMPART pipeline.
     * @throws IOException Thrown if there were any problems accessing files
     * @throws TaskExecutionException Thrown if there was a problem during execution.
     * @throws InterruptedException Thrown if RAMPART was halted or interrupted by the user.
     */
    public void execute()
            throws IOException, TaskExecutionException, InterruptedException {

        if (this.help) {
            printHelp();
        }
        else {
            // Configure RAMPART system
            configureSystem();
            log.info("RAMPART: System configured");

            // Build execution context
            ExecutionContext executionContext = buildExecutionContext();
            log.info("RAMPART: Built execution context");

            // Build Rampart args from the apache command line handler
            RampartArgs rampartArgs = new RampartArgs();
            rampartArgs.setConfig(this.jobConfig);
            rampartArgs.setOutputDir(this.outputDir);
            rampartArgs.setJobPrefix(this.jobPrefix.replaceAll("TIMESTAMP", createTimestamp()));
            rampartArgs.setStages(RampartStage.parse(this.stages));


            log.debug("RAMPART: Arguments: \n" + rampartArgs.toString());

            // Run RAMPART
            log.info("RAMPART: Started");
            runRampart(rampartArgs, executionContext);
            log.info("RAMPART: Finished");
        }
    }

}
