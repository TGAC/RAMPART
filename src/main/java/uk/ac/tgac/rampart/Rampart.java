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
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.DefaultProcessService;
import uk.ac.ebi.fgpt.conan.util.AbstractConanCLI;
import uk.ac.tgac.conan.core.util.JarUtils;
import uk.ac.tgac.rampart.tool.pipeline.RampartStage;
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

    // Environment specific configuration options and resource files are set in the user's home directory
    public static final File SETTINGS_DIR = new File(System.getProperty("user.home") + "/.tgac/rampart/");
    public static final String APP_NAME = "RAMPART";

    public static final File DATA_DIR = new File(SETTINGS_DIR, "data");
    public static final File REPORT_DIR = new File(DATA_DIR, "report");
    public static final File SCRIPTS_DIR = new File(SETTINGS_DIR, "scripts");

    // **** Option parameter names ****
    public static final String OPT_WEIGHTINGS = "weightings";
    public static final String OPT_STAGES = "stages";

    // **** Defaults ****
    public static final File    DEFAULT_WEIGHTINGS_FILE = new File(DATA_DIR, "weightings.tab");
    public static final List<RampartStage>  DEFAULT_STAGES = RampartStage.parse("ALL");


    // **** Options ****
    private File weightingsFile;
    private List<RampartStage> stages;
    private File jobConfig;


    private RampartArgs args;

    /**
     * Creates a new RAMPART instance with default arguments
     */
    public Rampart() throws IOException {

        super(APP_NAME, SETTINGS_DIR);

        this.weightingsFile     = DEFAULT_WEIGHTINGS_FILE;
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
        super(APP_NAME, SETTINGS_DIR);

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

            // Prep resources
            this.configureSystem();

            // Log setup
            log.info("Output dir: " + this.getOutputDir().getAbsolutePath());
            log.info("Environment configuration file: " + this.getEnvironmentConfig().getAbsolutePath());

        }

    }


    @Override
    protected void parseExtra(CommandLine commandLine) throws ParseException {

        this.weightingsFile = commandLine.hasOption(OPT_WEIGHTINGS) ?
                new File(commandLine.getOptionValue(OPT_WEIGHTINGS)) :
                DEFAULT_WEIGHTINGS_FILE;

        this.stages = commandLine.hasOption(OPT_STAGES) ?
                RampartStage.parse(commandLine.getOptionValue(OPT_STAGES)) :
                DEFAULT_STAGES;

        // Check for a single arg left on the command line
        if (commandLine.getArgs().length != 1)
            throw new ParseException("Unexpected number of arguments on the command line.  Expected 1, found " +
                    commandLine.getArgs().length);

        // This is the job config file.
        this.jobConfig = new File(commandLine.getArgs()[0]);
    }


    /**
     * Configures the RAMPART system.  Specifically, this means initialising logging, initialising conan, and copying any
     * required resources to a known location if they are not there already.
     * @throws IOException Thrown if there are any issues dealing with the environment or logging configuration files,
     * or if resources couldn't be copied to a suitable location
     */
    private void configureSystem() throws IOException {

        // Copy resources to external system
        File internalScripts = FileUtils.toFile(RampartCLI.class.getResource("/scripts"));
        File internalData = FileUtils.toFile(RampartCLI.class.getResource("/data"));
        File internalConfig = FileUtils.toFile(RampartCLI.class.getResource("/config"));

        File externalScriptsDir = new File(SETTINGS_DIR, "scripts");
        File externalDataDir = new File(SETTINGS_DIR, "data");
        File externalConfigDir = new File(SETTINGS_DIR, "config");

        JarFile thisJar = JarUtils.jarForClass(RampartCLI.class, null);

        if (thisJar == null) {

            log.debug("Executing from project.  Copying resources to settings directory: \"" +
                    SETTINGS_DIR.getAbsolutePath() + "\"");
            FileUtils.copyDirectory(internalScripts, externalScriptsDir);
            FileUtils.copyDirectory(internalData, externalDataDir);
            FileUtils.copyDirectory(internalConfig, externalConfigDir);
        }
        else {

            log.debug("Executing from jar: " + thisJar.getName() + "; Copying jar resources to settings directory: \"" +
                    SETTINGS_DIR.getAbsolutePath() + "\"");
            JarUtils.copyResourcesToDirectory(thisJar, "scripts", externalScriptsDir.getAbsolutePath());
            JarUtils.copyResourcesToDirectory(thisJar, "data", externalDataDir.getAbsolutePath());
            JarUtils.copyResourcesToDirectory(thisJar, "config", externalConfigDir.getAbsolutePath());
        }
    }


    @Override
    protected void printHelp() {
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

    @Override
    protected List<Option> createExtraOptions() {

        // create Options object
        List<Option> options = new ArrayList<>();

        options.add(OptionBuilder.withArgName("file").withLongOpt(OPT_WEIGHTINGS).hasArg()
                .withDescription("The rampart logging configuration file.  Default: " +
                        Rampart.DEFAULT_WEIGHTINGS_FILE.getAbsolutePath()).create("l"));

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

}
