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

package uk.ac.tgac.jellyswarm;

import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.user.GuestUser;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
import uk.ac.ebi.fgpt.conan.service.DefaultExecutorService;
import uk.ac.ebi.fgpt.conan.service.DefaultProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.TaskExecutionException;
import uk.ac.ebi.fgpt.conan.util.AbstractConanCLI;
import uk.ac.tgac.conan.core.util.JarUtils;
import uk.ac.tgac.rampart.util.CommandLineHelper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class JellyswarmCLI extends AbstractConanCLI
{
    private static Logger log = LoggerFactory.getLogger(JellyswarmCLI.class);

    public static final String OPT_STAGES = "stages";
    public static final String OPT_LOWER = "lower";
    public static final String OPT_RECURSIVE = "recursive";
    public static final String OPT_SINGLE = "single";
    public static final String OPT_THREADS = "threads";
    public static final String OPT_MEMORY = "memory";
    public static final String OPT_HASH = "hash";
    public static final String OPT_VERSION = "version";


    public static final List<JellyswarmStage>  DEFAULT_STAGES = JellyswarmStage.parse("ALL");
    public static final long DEFAULT_LOWER = 0;
    public static final int DEFAULT_THREADS = 1;
    public static final int DEFAULT_MEMORY = 60;
    public static final long DEFAULT_HASH = 4000000000L;

    /**
     * Environment specific configuration options and resource files are set in the user's home directory
     */
    public static final File USER_DIR = new File(System.getProperty("user.home") + "/.tgac/rampart");

    /**
     * Gets the application config directory.  This is really messy way to do it... try to think of a better way later
     */
    public static final File APP_DIR = JarUtils.jarForClass(JellyswarmCLI.class, null) == null ? new File(".") :
            new File(JellyswarmCLI.class.getProtectionDomain().getCodeSource().getLocation().getPath())
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile();

    public static final String FOOTER = "Contact: daniel.mapleson@tgac.ac.uk";
    public static final File CWD = currentWorkingDir();
    public static final File ETC_DIR = new File(APP_DIR, "etc");
    public static final String APP_NAME = "jellyswarm";

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

    private File inputDir;
    private List<JellyswarmStage> stages;
    private long lower;
    private boolean recursive;
    private boolean single;
    private int threads;
    private int memory;
    private long hash;
    private boolean version;


    private Jellyswarm.Args args;
    private ExecutionContext executionContext;

    public JellyswarmCLI() throws IOException {
        super(APP_NAME, ETC_DIR, DEFAULT_CONAN_FILE, DEFAULT_LOG_FILE, currentWorkingDir(),
                APP_NAME + createTimestamp(), false, false);
    }

    public JellyswarmCLI(String[] cliArgs) throws IOException, ParseException {

        super(APP_NAME, ETC_DIR, DEFAULT_CONAN_FILE, DEFAULT_LOG_FILE, currentWorkingDir(),
                APP_NAME + createTimestamp(), false, false);

        // Parses the command line using a posix parser and sets all the variables
        this.parse(new PosixParser().parse(createOptions(), cliArgs, false));

        if (this.version) {
            return;
        }

        // Only bother doing more if help is not requested
        if (!this.isHelp() && cliArgs.length != 0) {

            // Initialise logging and load conan properties
            this.init();

            this.args = new Jellyswarm.Args();
            this.args.setInputDir(inputDir);
            this.args.setOutputDir(this.getOutputDir());
            this.args.setJobPrefix(this.getJobPrefix().replaceAll("TIMESTAMP", createTimestamp()));
            this.args.setLowerCount(this.lower);
            this.args.setHashSize(this.hash);
            this.args.setStages(this.stages);
            this.args.setThreads(this.threads);
            this.args.setMemory(this.memory / 1000);
            this.args.setRunParallel(true);
            this.args.setRecursive(this.recursive);
            this.args.setPaired(!this.single);

            this.executionContext = this.buildExecutionContext();

            // Create an execution context based on environment information detected or provide by the user
            this.args.setExecutionContext(this.executionContext);

            log.info("Jellyswarm Version (linked to RAMPART): " + loadVersion());
            log.info("Output dir: " + this.getOutputDir().getAbsolutePath());
            log.info("Environment configuration file: " + this.getEnvironmentConfig().getAbsolutePath());
            log.info("Logging properties file: " + this.getLogConfig().getAbsolutePath());
            log.info("Job Prefix: " + this.args.getJobPrefix());
            if (ConanProperties.containsKey("externalProcessConfigFile")) {
                log.info("External process config file detected: " + new File(ConanProperties.getProperty("externalProcessConfigFile")).getAbsolutePath());
            }
        }
    }

    private static String loadVersion() throws IOException {
        Properties properties = new Properties();
        properties.load(new BufferedInputStream(new FileInputStream(PROPERTIES_FILE)));
        return properties.getProperty("rampart-version");
    }


    /**
     * Extra command line options specific to jellyswarm
     * @return Extra Options
     */
    @Override
    protected List<Option> createExtraOptions() {

        // create Options object
        List<Option> options = new ArrayList<>();

        options.add(OptionBuilder.withArgName("number").withLongOpt(OPT_LOWER).hasArg()
                .withDescription("K-mer counts below this number are discarded.  Default: " + DEFAULT_LOWER).create("l"));

        options.add(OptionBuilder.withArgName("number").withLongOpt(OPT_HASH).hasArg()
                .withDescription("The hash size to use for each jellyfish count process.  Default: " + DEFAULT_HASH).create("h"));

        options.add(OptionBuilder.withArgName("number").withLongOpt(OPT_THREADS).hasArg()
                .withDescription("Number of threads to use for each jellyfish process.  Default: " + DEFAULT_THREADS).create("t"));

        options.add(OptionBuilder.withArgName("number").withLongOpt(OPT_MEMORY).hasArg()
                .withDescription("Memory limit in GB.  Limit may or may not be enforced depending on scheduling system and architecture.  Default: " + DEFAULT_MEMORY).create("m"));

        options.add(OptionBuilder.withArgName("string").withLongOpt(OPT_STAGES).hasArg()
                .withDescription("The jellyswarm stages to execute: " + JellyswarmStage.getFullListAsString() + ", ALL.  Default: ALL.")
                .create("s"));

        options.add(new Option("r", OPT_RECURSIVE, false, "If set, tells jellyswarm to recurse through sub-directories in the input directory to search for read files"));

        options.add(new Option("1", OPT_SINGLE, false, "If set, tells jellyswarm to process single end data, or interleaved paired end data, i.e. one file per sample"));

        options.add(new Option("V", OPT_VERSION, false, "Current version"));

        return options;
    }

    @Override
    protected void parseExtra(CommandLine commandLine) throws ParseException {

        this.version = commandLine.hasOption(OPT_VERSION);

        if (this.version)
            return;

        this.stages = commandLine.hasOption(OPT_STAGES) ?
                JellyswarmStage.parse(commandLine.getOptionValue(OPT_STAGES)) :
                DEFAULT_STAGES;

        this.lower = commandLine.hasOption(OPT_LOWER) ?
                Long.parseLong(commandLine.getOptionValue(OPT_LOWER)) :
                DEFAULT_LOWER;

        this.hash = commandLine.hasOption(OPT_HASH) ?
                Long.parseLong(commandLine.getOptionValue(OPT_HASH)) :
                DEFAULT_HASH;

        this.threads = commandLine.hasOption(OPT_THREADS) ?
                Integer.parseInt(commandLine.getOptionValue(OPT_THREADS)) :
                DEFAULT_THREADS;

        this.memory = commandLine.hasOption(OPT_MEMORY) ?
                Integer.parseInt(commandLine.getOptionValue(OPT_MEMORY)) :
                DEFAULT_MEMORY;

        this.recursive = commandLine.hasOption(OPT_RECURSIVE);

        this.single = commandLine.hasOption(OPT_SINGLE);

        // Check for a single arg left on the command line
        if (commandLine.getArgs().length != 1)
            throw new ParseException("Unexpected number of arguments on the command line.  Expected 1, found " +
                    commandLine.getArgs().length);

        // This is the input directory containing libraries to analyse.
        this.inputDir = new File(commandLine.getArgs()[0]);
    }

    @Override
    protected ParamMap createArgMap() {
        return this.args.getArgMap();
    }

    @Override
    protected ConanPipeline createPipeline() throws IOException {
        return new Jellyswarm.Pipeline(this.args, new DefaultExecutorService(new DefaultProcessService(), this.executionContext));
    }

    @Override
    protected void printHelp() {
        CommandLineHelper.printHelp(
                System.err,
                "jellyswarm [options] <input directory>\nOptions: ",
                "Runs jellyswarm.  Finds FastQ files in a directory and counts the kmers in them.  Compares the number of kmers found between them, at various coverage cutoff levels.\n\nOptions:\n",
                createOptions());
    }

    public void execute() throws InterruptedException, TaskExecutionException, IOException {

        super.execute(new GuestUser("rampart@tgac.ac.uk"), ConanTask.Priority.HIGH, this.args.getExecutionContext());
    }


    public static void main( String[] args ) throws IOException {
        try {
            // Create new app and parse args
            JellyswarmCLI jellyswarm = new JellyswarmCLI(args);

            if (jellyswarm == null)
                throw new IllegalArgumentException("Invalid arguments, could not create a valid jellyswarm object.");

            if (jellyswarm.isHelp() || args.length == 0) {
                jellyswarm.printHelp();
            }
            else if (jellyswarm.version) {
                System.out.println("Version: " + loadVersion());
            }
            else {
                jellyswarm.execute();
            }
        }
        catch (ParseException e) {
            System.err.println("\n" + e.getMessage() + "\n");
            new JellyswarmCLI().printHelp();
            System.exit(1);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println(StringUtils.join(e.getStackTrace(), "\n"));
            System.exit(2);
        }
    }
}

