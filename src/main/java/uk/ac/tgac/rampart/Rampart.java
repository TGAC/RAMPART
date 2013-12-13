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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.user.GuestUser;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
import uk.ac.ebi.fgpt.conan.service.DefaultProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.TaskExecutionException;
import uk.ac.ebi.fgpt.conan.util.AbstractConanCLI;
import uk.ac.tgac.conan.core.util.JarUtils;
import uk.ac.tgac.rampart.tool.pipeline.RampartStage;
import uk.ac.tgac.rampart.tool.pipeline.RampartStageList;
import uk.ac.tgac.rampart.tool.pipeline.rampart.RampartArgs;
import uk.ac.tgac.rampart.tool.pipeline.rampart.RampartPipeline;
import uk.ac.tgac.rampart.util.CommandLineHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * This class handles execution of Rampart in run mode.
 */
public class Rampart extends AbstractConanCLI {

    private static Logger log = LoggerFactory.getLogger(Rampart.class);

    /**
     * Environment specific configuration options and resource files are set in the user's home directory
     */
    public static final File USER_DIR = new File(System.getProperty("user.home") + "/.tgac/rampart");

    /**
     * Gets the application config directory.  This is really messy way to do it... try to think of a better way later
     */
    public static final File APP_DIR = JarUtils.jarForClass(Rampart.class, null) == null ? new File(".") :
            new File(Rampart.class.getProtectionDomain().getCodeSource().getLocation().getPath())
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
    public static final String OPT_WEIGHTINGS = "weightings";
    public static final String OPT_STAGES = "stages";

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
    private File jobConfig;


    private RampartArgs args;

    /**
     * Creates a new RAMPART instance with default arguments
     */
    public Rampart() throws IOException {

        super(APP_NAME, ETC_DIR, DEFAULT_CONAN_FILE, DEFAULT_LOG_FILE, currentWorkingDir(),
                APP_NAME + createTimestamp(),
                false,
                false);

        this.stages             = DEFAULT_STAGES;
        this.jobConfig          = null;

        this.args               = null;
    }

    /**
     * Creates a new RAMPART instance based on command line arguments
     * @param args List of command line arguments containing information to setup RAMPART
     * @throws ParseException Thrown if an invalid command line was encountered
     */
    public Rampart(String[] args) throws ParseException, IOException {

        // Creates the instance and parses the command line, setting the class variables
        super(APP_NAME, ETC_DIR, DEFAULT_CONAN_FILE, DEFAULT_LOG_FILE, currentWorkingDir(),
                APP_NAME + createTimestamp(),
                false,
                false);

        // Parses the command line using a posix parser and sets all the variables
        this.parse(new PosixParser().parse(createOptions(), args, true));

        // Only bother doing more if help is not requested
        if (!this.isHelp()) {

            // Initialise logging and load conan properties
            this.init();

            // Create RnaSeqEvalArgs based on reads from the command line
            this.args = new RampartArgs(
                    this.jobConfig,
                    this.getOutputDir(),
                    this.getJobPrefix().replaceAll("TIMESTAMP", createTimestamp()),
                    this.stages);

            // Parse the job config file and set internal variables in RampartArgs
            this.args.parseXml();

            // Create an execution context based on environment information detected or provide by the user
            this.args.setExecutionContext(this.buildExecutionContext());

            // Log setup
            log.info("Output dir: " + this.getOutputDir().getAbsolutePath());
            log.info("Environment configuration file: " + this.getEnvironmentConfig().getAbsolutePath());
            log.info("Logging properties file: " + this.getLogConfig().getAbsolutePath());
            log.info("Job Prefix: " + this.args.getJobPrefix());
            if (ConanProperties.containsKey("externalProcessConfigFile")) {
                log.info("External process config file detected: " + new File(ConanProperties.getProperty("externalProcessConfigFile")).getAbsolutePath());
            }
        }
    }


    @Override
    protected void parseExtra(CommandLine commandLine) throws ParseException {

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
    protected void printHelp() {
        CommandLineHelper.printHelp(
                System.err,
                "rampart run <job_config_file>",
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

        return options;
    }

    @Override
    protected Map<ConanParameter, String> createArgMap() {
        return this.args.getArgMap();
    }

    @Override
    protected ConanPipeline createPipeline() throws IOException {

        return new RampartPipeline(this.args, new DefaultProcessService());
    }

    public void execute() throws InterruptedException, TaskExecutionException, IOException {

        super.execute(new GuestUser("rampart@tgac.ac.uk"), ConanTask.Priority.HIGH, this.args.getExecutionContext());
    }

}
