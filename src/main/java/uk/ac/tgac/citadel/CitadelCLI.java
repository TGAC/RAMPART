/*
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2015  Daniel Mapleson - TGAC
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
 */
package uk.ac.tgac.citadel;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.user.GuestUser;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.TaskResult;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
import uk.ac.ebi.fgpt.conan.service.DefaultExecutorService;
import uk.ac.ebi.fgpt.conan.service.DefaultProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.TaskExecutionException;
import uk.ac.ebi.fgpt.conan.util.AbstractConanCLI;
import uk.ac.tgac.conan.core.util.JarUtils;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishCountV11;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishMergeV11;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishStatsV11;
import uk.ac.tgac.conan.process.kmer.kat.KatPlotDensityV1;
import uk.ac.tgac.conan.process.kmer.kat.KatPlotSpectraCnV1;
import uk.ac.tgac.rampart.stage.RampartStage;
import uk.ac.tgac.rampart.stage.RampartStageList;
import uk.ac.tgac.rampart.util.CommandLineHelper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This class handles execution of Rampart in run mode.
 */
public class CitadelCLI extends AbstractConanCLI {

    private static Logger log = LoggerFactory.getLogger(CitadelCLI.class);

    public static final File CWD = currentWorkingDir();

    /**
     * Environment specific configuration options and resource files are set in the user's home directory
     */
    public static final File USER_DIR = new File(System.getProperty("user.home") + "/.tgac/rampart");

    /**
     * Gets the application config directory.  This is really messy way to do it... try to think of a better way later
     */
    public static final File APP_DIR = JarUtils.jarForClass(CitadelCLI.class, null) == null ? new File(".") :
            new File(CitadelCLI.class.getProtectionDomain().getCodeSource().getLocation().getPath())
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile();

    public static final String APP_NAME = "CITADEL";

    public static final File ETC_DIR = new File(APP_DIR, "etc");
    //public static final File REPORT_DIR = new File(DATA_DIR, "report");

    // **** Option parameter names ****
    public static final String OPT_STAGES = "stages";
    public static final String OPT_SKIP_CHECKS = "skip_checks";
    public static final String OPT_VERSION = "version";


    // **** Defaults ****

    public static final File    DEFAULT_SYSTEM_CONAN_FILE = new File(ETC_DIR, "conan.properties");
    public static final File    DEFAULT_USER_CONAN_FILE = new File(USER_DIR, "conan.properties");
    public static final File    DEFAULT_CONAN_FILE = DEFAULT_USER_CONAN_FILE.exists() ?
            DEFAULT_USER_CONAN_FILE : DEFAULT_SYSTEM_CONAN_FILE;

    public static final File    DEFAULT_SYSTEM_LOG_FILE = new File(ETC_DIR, "log4j.properties");
    public static final File    DEFAULT_USER_LOG_FILE = new File(USER_DIR, "log4j.properties");
    public static final File    DEFAULT_LOG_FILE = DEFAULT_USER_LOG_FILE.exists() ?
            DEFAULT_USER_LOG_FILE : DEFAULT_SYSTEM_LOG_FILE;

    public static final File    PROPERTIES_FILE = new File(ETC_DIR, "app.properties");

    public static final RampartStageList  DEFAULT_STAGES = RampartStageList.parse("ALL");

    private static String[] externalProcNames = new String[] {
            new KatPlotDensityV1().getName(),
            new KatPlotSpectraCnV1().getName(),
            new JellyfishCountV11().getName(),
            new JellyfishMergeV11().getName(),
            new JellyfishStatsV11().getName()
    };


    // **** Options ****
    private RampartStageList stages;
    private File jobConfig;
    private File inputDir;
    private boolean skipChecks;
    private boolean version;

    private CitadelPipeline.Args args;
    private ExecutionContext executionContext;

    /**
     * Creates a new Citadel instance with default arguments
     * @throws java.io.IOException Thrown if there is an error initialising this RAMPART instance.
     */
    public CitadelCLI() throws IOException {
        super(APP_NAME, ETC_DIR, DEFAULT_CONAN_FILE, DEFAULT_LOG_FILE, currentWorkingDir(),
                APP_NAME + createTimestamp(),
                false,
                false);

        this.stages             = DEFAULT_STAGES;
        this.jobConfig          = null;
        this.inputDir           = null;
        this.args               = null;
        this.version            = false;
        this.executionContext   = null;
    }

    /**
     * Creates a new Citadel instance based on command line arguments
     * @param args List of command line arguments containing information to setup Citadel
     * @throws org.apache.commons.cli.ParseException Thrown if an invalid command line was encountered
     * @throws java.io.IOException Thrown if there is an error initialising this Citadel instance.
     */
    public CitadelCLI(String[] args) throws ParseException, IOException {

        // Creates the instance and sets core variables
        this();

        // Parses the command line using a posix parser and sets all the variables
        this.parse(new PosixParser().parse(createOptions(), args, true));
    }

    public void initialise() throws IOException {

        if (this.jobConfig == null || !this.jobConfig.exists()) {
            throw new IOException("Job config file not specified or does not exist.");
        }

        // Initialise logging and load conan properties
        this.init();

        // Create RnaSeqEvalArgs based on reads from the command line
        this.args = new CitadelPipeline.Args();
        this.args.setOutputDir(this.getOutputDir());
        this.args.setJobPrefix(this.getJobPrefix());

        //TODO Add more setup here

        // Override log level to debug if the verbose flag is set
        if (this.isVerbose()) {
            LogManager.getRootLogger().setLevel(Level.DEBUG);
        }
        log.info("Citadel (RAMPART) Version: " + loadVersion());
        log.info("Output dir: " + this.getOutputDir().getAbsolutePath());
        log.info("Environment configuration file: " + this.getEnvironmentConfig().getAbsolutePath());
        log.info("Logging properties file: " + this.getLogConfig().getAbsolutePath());
        log.info("Job Prefix: " + this.args.getJobPrefix());
        if (ConanProperties.containsKey("externalProcessConfigFile")) {

            File externalProcsFile = new File(ConanProperties.getProperty("externalProcessConfigFile"));
            log.info("External process config file detected: " + externalProcsFile.getAbsolutePath());

            this.validateExternalProcs(externalProcsFile, externalProcNames);
            log.info("External process config file validated");
        }
        log.info("Executing the following stages: " + this.stages.toString());

        // Parse the job config file and set internal variables in RampartArgs
        log.info("Parsing configuration file: " + this.jobConfig.getAbsolutePath());
        //this.args.parseXml();

        // Create an execution context based on environment information detected or provide by the user
        this.executionContext = this.buildExecutionContext();
    }



    private static String loadVersion() throws IOException {
        Properties properties = new Properties();
        properties.load(new BufferedInputStream(new FileInputStream(PROPERTIES_FILE)));
        return properties.getProperty("rampart-version");
    }

    public File getJobConfig() {
        return jobConfig;
    }

    public void setJobConfig(File jobConfig) {
        this.jobConfig = jobConfig;
    }

    public boolean isSkipChecks() {
        return skipChecks;
    }

    public void setSkipChecks(boolean skipChecks) {
        this.skipChecks = skipChecks;
    }

    public CitadelPipeline.Args getArgs() {
        return args;
    }

    @Override
    protected void parseExtra(CommandLine commandLine) throws ParseException {

        this.version = commandLine.hasOption(OPT_VERSION);

        if (version)
            return;

        this.skipChecks = commandLine.hasOption(OPT_SKIP_CHECKS);

        this.stages = commandLine.hasOption(OPT_STAGES) ?
                RampartStageList.parse(commandLine.getOptionValue(OPT_STAGES)) :
                DEFAULT_STAGES;


        // Check for a single arg left on the command line
        if (commandLine.getArgs().length != 1)
            throw new ParseException("Unexpected number of arguments on the command line.  Expected 1, found " +
                    commandLine.getArgs().length);

        // This is the job config file.
        this.jobConfig = new File(commandLine.getArgs()[0]);
    }

    @Override
    public void printHelp() {
        CommandLineHelper.printHelp(
                System.err,
                "citadel [options] <job_config_file>\nOptions: ",
                "Citadel is an en mass de novo prokaryote genome assembly and annotation tool.  It allows you to process cohorts " +
                "of same species bacterial samples from raw sequence reads, produce high-quality draft genome assemblies and then annotates " +
                "those genomes before packaging the relevant resources up for distribution.  It also analyses your samples at each step to " +
                "identify outliers and potential problem samples.\n\nOptions:\n",
                createOptions()
        );
    }

    @Override
    protected List<Option> createExtraOptions() {

        // create Options object
        List<Option> options = new ArrayList<>();

        options.add(OptionBuilder.withArgName("string").withLongOpt(OPT_STAGES).hasArg()
                .withDescription("The Citadel stages to execute: " + RampartStage.getFullListAsString() + ", ALL.  Default: ALL.").create("s"));

        options.add(OptionBuilder.withLongOpt(OPT_SKIP_CHECKS)
                .withDescription("Skips initial checks to see if all requested tools can be found.")
                .create("sc"));

        options.add(new Option("V", OPT_VERSION, false, "Current version"));

        return options;
    }

    @Override
    protected ParamMap createArgMap() {
        return this.args.getArgMap();
    }

    @Override
    protected ConanPipeline createPipeline() throws IOException {

        return new CitadelPipeline(this.args,
                new DefaultExecutorService(
                    new DefaultProcessService(),
                    this.executionContext));
    }

    public void execute() throws InterruptedException, TaskExecutionException, IOException {

        // Run the pipeline as described by the user
        TaskResult result = super.execute(new GuestUser("citadel@tgac.ac.uk"), ConanTask.Priority.HIGH, this.executionContext);

        // Output the resource usage to file
        FileUtils.writeLines(new File(this.args.getOutputDir(), this.args.getJobPrefix() + ".summary"), result.getOutput());
    }


    /**
     * The main entry point for Citadel.
     * @param args Command line arguments
     * @throws java.io.IOException Thrown if there was an error printing the help message
     */
    public static void main(String[] args) throws IOException {

        try {

            CitadelCLI citadelCLI = new CitadelCLI(args);
            StopWatch stopwatch = new StopWatch();
            stopwatch.start();

            if (citadelCLI == null)
                throw new IllegalArgumentException("Invalid arguments, could not create a valid citadel object.");

            if (citadelCLI.isHelp()) {
                citadelCLI.printHelp();
            }
            else if (citadelCLI.version) {
                System.out.println("Version: " + loadVersion());
            }
            else {
                citadelCLI.initialise();
                citadelCLI.execute();

                stopwatch.stop();
                System.out.println("Total runtime (wall clock): " + stopwatch.toString());
            }
        }
        catch (ParseException e) {
            System.err.println("\n" + e.getMessage() + "\n");
            new CitadelCLI().printHelp();
            System.exit(1);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println(StringUtils.join(e.getStackTrace(), "\n"));
            System.exit(2);
        }
    }

}
