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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.user.GuestUser;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
import uk.ac.ebi.fgpt.conan.service.DefaultExecutorService;
import uk.ac.ebi.fgpt.conan.service.DefaultProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.TaskExecutionException;
import uk.ac.ebi.fgpt.conan.util.AbstractConanCLI;
import uk.ac.tgac.conan.core.util.JarUtils;
import uk.ac.tgac.rampart.tool.pipeline.rampart.RampartArgs;
import uk.ac.tgac.rampart.tool.pipeline.rampart.RampartPipeline;
import uk.ac.tgac.rampart.tool.pipeline.rampart.RampartStage;
import uk.ac.tgac.rampart.tool.pipeline.rampart.RampartStageList;
import uk.ac.tgac.rampart.util.CommandLineHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles execution of Rampart in run mode.
 */
public class RampartCLI extends AbstractConanCLI {

    private static Logger log = LoggerFactory.getLogger(RampartCLI.class);

    public static final File CWD = currentWorkingDir();

    /**
     * Environment specific configuration options and resource files are set in the user's home directory
     */
    public static final File USER_DIR = new File(System.getProperty("user.home") + "/.tgac/rampart");

    /**
     * Gets the application config directory.  This is really messy way to do it... try to think of a better way later
     */
    public static final File APP_DIR = JarUtils.jarForClass(RampartCLI.class, null) == null ? new File(".") :
            new File(RampartCLI.class.getProtectionDomain().getCodeSource().getLocation().getPath())
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile();

    public static final String APP_NAME = "RAMPART";

    public static final File ETC_DIR = new File(APP_DIR, "etc");
    //public static final File REPORT_DIR = new File(DATA_DIR, "report");

    // **** Option parameter names ****
    public static final String OPT_STAGES = "stages";
    public static final String OPT_RUN_FIRST_HALF = "run_first_half";
    public static final String OPT_RUN_SECOND_HALF = "run_second_half";
    public static final String OPT_AMP_INPUT = "amp_input";
    public static final String OPT_SKIP_CHECKS = "skip_checks";

    // **** Defaults ****

    public static final File    DEFAULT_SYSTEM_CONAN_FILE = new File(ETC_DIR, "conan.properties");
    public static final File    DEFAULT_USER_CONAN_FILE = new File(USER_DIR, "conan.properties");
    public static final File    DEFAULT_CONAN_FILE = DEFAULT_USER_CONAN_FILE.exists() ?
            DEFAULT_USER_CONAN_FILE : DEFAULT_SYSTEM_CONAN_FILE;

    public static final File    DEFAULT_SYSTEM_LOG_FILE = new File(ETC_DIR, "log4j.properties");
    public static final File    DEFAULT_USER_LOG_FILE = new File(USER_DIR, "log4j.properties");
    public static final File    DEFAULT_LOG_FILE = DEFAULT_USER_LOG_FILE.exists() ?
            DEFAULT_USER_LOG_FILE : DEFAULT_SYSTEM_LOG_FILE;



    public static final RampartStageList  DEFAULT_STAGES = RampartStageList.parse("ALL");


    // **** Options ****
    private RampartStageList stages;
    private File ampInput;
    private File jobConfig;
    private boolean skipChecks;

    private RampartArgs args;

    /**
     * Creates a new RAMPART instance with default arguments
     */
    public RampartCLI() throws IOException {
        super(APP_NAME, ETC_DIR, DEFAULT_CONAN_FILE, DEFAULT_LOG_FILE, currentWorkingDir(),
                APP_NAME + createTimestamp(),
                false,
                false);

        this.stages             = DEFAULT_STAGES;
        this.ampInput           = null;
        this.jobConfig          = null;
        this.args               = null;
    }

    /**
     * Creates a new RAMPART instance based on command line arguments
     * @param args List of command line arguments containing information to setup RAMPART
     * @throws ParseException Thrown if an invalid command line was encountered
     */
    public RampartCLI(String[] args) throws ParseException, IOException {

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
        this.args = new RampartArgs(
                this.jobConfig,
                this.getOutputDir(),
                this.getJobPrefix().replaceAll("TIMESTAMP", createTimestamp()),
                this.stages,
                this.ampInput,
                !this.skipChecks);

        // Parse the job config file and set internal variables in RampartArgs
        this.args.parseXml();

        // Create an execution context based on environment information detected or provide by the user
        this.args.setExecutionContext(this.buildExecutionContext());

        // Log setup

        // Override log level to debug if the verbose flag is set
        if (this.isVerbose()) {
            LogManager.getRootLogger().setLevel(Level.DEBUG);
        }
        log.info("Output dir: " + this.getOutputDir().getAbsolutePath());
        log.info("Environment configuration file: " + this.getEnvironmentConfig().getAbsolutePath());
        log.info("Logging properties file: " + this.getLogConfig().getAbsolutePath());
        log.info("Job Prefix: " + this.args.getJobPrefix());
        if (ConanProperties.containsKey("externalProcessConfigFile")) {
            log.info("External process config file detected: " + new File(ConanProperties.getProperty("externalProcessConfigFile")).getAbsolutePath());
        }
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

    @Override
    protected void parseExtra(CommandLine commandLine) throws ParseException {

        this.skipChecks = commandLine.hasOption(OPT_SKIP_CHECKS);

        this.stages = commandLine.hasOption(OPT_STAGES) ?
                RampartStageList.parse(commandLine.getOptionValue(OPT_STAGES)) :
                DEFAULT_STAGES;

        // If the user hasn't modified the stages then check to see if they've requested any predefined profiles
        if (stages == DEFAULT_STAGES) {

            if (commandLine.hasOption(OPT_RUN_FIRST_HALF)) {

                RampartStageList firstHalf = new RampartStageList();
                firstHalf.add(RampartStage.MECQ);
                firstHalf.add(RampartStage.ANALYSE_READS);
                firstHalf.add(RampartStage.MASS);
                firstHalf.add(RampartStage.ANALYSE_MASS);
                this.stages = firstHalf;

                log.info("Running first half of the RAMPART pipeline only");
            }
            else if (commandLine.hasOption(OPT_RUN_SECOND_HALF)) {
                RampartStageList secondHalf = new RampartStageList();
                secondHalf.add(RampartStage.AMP);
                secondHalf.add(RampartStage.ANALYSE_AMP);
                secondHalf.add(RampartStage.FINALISE);
                this.stages = secondHalf;

                log.info("Running second half of the RAMPART pipeline only");

                if (commandLine.hasOption(OPT_AMP_INPUT)) {
                    this.ampInput = new File(commandLine.getOptionValue(OPT_AMP_INPUT));
                }
            }
        }

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
                "rampart [options] <job_config_file>\nOptions: ",
                "RAMPART is a de novo assembly pipeline that makes use of third party-tools and High Performance Computing " +
                "resources.  It can be used as a single interface to several popular assemblers, and can perform " +
                "automated comparison and analysis of any generated assemblies.\n\n",
                createOptions()
        );
    }

    @Override
    protected List<Option> createExtraOptions() {

        // create Options object
        List<Option> options = new ArrayList<>();

        options.add(OptionBuilder.withArgName("string").withLongOpt(OPT_STAGES).hasArg()
                .withDescription("The RAMPART stages to execute: " + RampartStage.getFullListAsString() + ", ALL.  Default: ALL.").create("s"));

        options.add(OptionBuilder.withLongOpt(OPT_RUN_FIRST_HALF)
                .withDescription("Run the first half of the RAMPART pipeline.  This involves running MECQ and MASS and doing any requested analyses.")
                .create("1"));

        options.add(OptionBuilder.withLongOpt(OPT_RUN_SECOND_HALF)
                .withDescription("Run the second half of the RAMPART pipeline.  This involves enhancing the selected assembly and doing any requested analyses on the final assembly and finalising the assembly so that it's suitable for distribution.")
                .create("2"));

        options.add(OptionBuilder.withArgName("file").withLongOpt(OPT_AMP_INPUT).hasArg()
                .withDescription("If only running the second half of the RAMPART pipeline, this option allows you to specify an alternate assembly to process.  By default the automatically selected assembly is used.")
                .create("a"));

        options.add(OptionBuilder.withLongOpt(OPT_SKIP_CHECKS)
                .withDescription("Skips initial checks to see if all requested tools can be found.")
                .create("sc"));

        return options;
    }

    @Override
    protected ParamMap createArgMap() {
        return this.args.getArgMap();
    }

    @Override
    protected ConanPipeline createPipeline() throws IOException {

        return new RampartPipeline(this.args, new DefaultExecutorService(
                new DefaultProcessService(),
                this.args.getExecutionContext()));
    }

    public void execute() throws InterruptedException, TaskExecutionException, IOException {

        super.execute(new GuestUser("rampart@tgac.ac.uk"), ConanTask.Priority.HIGH, this.args.getExecutionContext());
    }


    /**
     * The main entry point for RAMPART.  Looks at the first argument to decide which mode to run in.  Execution of each
     * mode is handled by RampartMode.
     * @param args Command line arguments
     */
    public static void main(String[] args) {

        try {

            RampartCLI rampartCLI = new RampartCLI(args);

            if (rampartCLI == null)
                throw new IllegalArgumentException("Invalid arguments, could not create a valid rampart object.");

            if (rampartCLI.isHelp()) {
                rampartCLI.printHelp();
            }
            else {
                rampartCLI.initialise();
                rampartCLI.execute();
            }
        }
        catch (IllegalArgumentException | ParseException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println(StringUtils.join(e.getStackTrace(), "\n"));
            System.exit(2);
        }
    }

}
