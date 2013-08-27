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
import uk.ac.tgac.conan.core.util.JarUtils;
import uk.ac.tgac.rampart.tool.pipeline.RampartStage;
import uk.ac.tgac.rampart.tool.pipeline.rampart.RampartArgs;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.JarFile;

public class RampartOptions {

    // **** Option parameter names ****
    public static final String OPT_CLEAN = "clean";
    public static final String OPT_CONFIG = "config";
    public static final String OPT_ENV_CONFIG = "env_config";
    public static final String OPT_LOG_CONFIG = "log_config";
    public static final String OPT_STAGES = "stages";
    public static final String OPT_OUTPUT = "output";
    public static final String OPT_JOB_PREFIX = "job_prefix";
    public static final String OPT_VERBOSE = "verbose";
    public static final String OPT_HELP = "help";


    // **** Options ****
    private File config = null;
    private File environmentConfig = null;
    private File logConfig = null;
    private File output = null;
    private String jobPrefix = "";
    private String stages = null;
    private File clean = null;
    private boolean verbose = false;
    private boolean help = false;


    // **** Jar details ****
    public static final String DEFAULT_JAR_NAME = "rampart-<version>.jar";

    /**
     * Process command line args.  Checks for help first, then clean, then args for normal operation.
     * @param cmdLine The command line used to execute the program
     * @throws ParseException
     */
    public RampartOptions(CommandLine cmdLine) throws ParseException {

        if (cmdLine.getOptions().length == 0) {

            help = true;
        }
        else {

            help = cmdLine.hasOption(OPT_HELP);

            if (!help) {

                clean = cmdLine.hasOption(OPT_CLEAN) ?
                    cmdLine.getOptionValue(OPT_CLEAN) != null ?
                            new File(cmdLine.getOptionValue(OPT_CLEAN)) :
                            RampartCLI.currentWorkingDir() :
                    null;

                if (clean == null) {

                    if (cmdLine.hasOption(OPT_CONFIG)) {
                        config = new File(cmdLine.getOptionValue(OPT_CONFIG));
                    }
                    else {
                        throw new ParseException(OPT_CONFIG + " argument not specified.");
                    }

                    environmentConfig = cmdLine.hasOption(OPT_ENV_CONFIG) ?
                            new File(cmdLine.getOptionValue(OPT_ENV_CONFIG)) :
                            RampartCLI.DEFAULT_ENV_CONFIG;

                    logConfig = cmdLine.hasOption(OPT_LOG_CONFIG) ?
                            new File(cmdLine.getOptionValue(OPT_LOG_CONFIG)) :
                            RampartCLI.DEFAULT_LOG_CONFIG;

                    output = cmdLine.hasOption(OPT_OUTPUT) ?
                            new File(cmdLine.getOptionValue(OPT_OUTPUT)) :
                            RampartCLI.currentWorkingDir();

                    jobPrefix = cmdLine.hasOption(OPT_JOB_PREFIX) ?
                            cmdLine.getOptionValue(OPT_JOB_PREFIX) :
                            createDefaultJobPrefix();

                    stages = cmdLine.hasOption(OPT_STAGES) ? cmdLine.getOptionValue(OPT_STAGES) : "ALL";

                    verbose = cmdLine.hasOption(OPT_VERBOSE);
                }
            }
        }
    }

    public File getConfig() {
        return config;
    }

    public void setConfig(File config) {
        this.config = config;
    }

    public File getEnvironmentConfig() {
        return environmentConfig;
    }

    public void setEnvironmentConfig(File environmentConfig) {
        this.environmentConfig = environmentConfig;
    }

    public File getLogConfig() {
        return logConfig;
    }

    public void setLogConfig(File logConfig) {
        this.logConfig = logConfig;
    }

    public String getStages() {
        return stages;
    }

    public void setStages(String stages) {
        this.stages = stages;
    }

    public File getOutput() {
        return output;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    public String getJobPrefix() {
        return jobPrefix;
    }

    public void setJobPrefix(String jobPrefix) {
        this.jobPrefix = jobPrefix;
    }

    public File getClean() {
        return clean;
    }

    public void setClean(File clean) {
        this.clean = clean;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean doHelp() {
        return help;
    }

    public void setHelp(boolean help) {
        this.help = help;
    }

    public static String getJarName() {
        JarFile jarFile = JarUtils.jarForClass(RampartOptions.class, null);
        return jarFile != null ? jarFile.getName() : DEFAULT_JAR_NAME;
    }

    public static void printUsage(final OutputStream out) {

        final String commandLineSyntax = "java -jar " + getJarName();
        final PrintWriter writer = new PrintWriter(out);

        HelpFormatter formatter = new HelpFormatter();
        formatter.printUsage(writer, 80, commandLineSyntax, createOptions());
        writer.flush();
    }

    public static void printHelp(final OutputStream out) {

        final String commandLineSyntax = "java -jar " + getJarName();
        final PrintWriter writer = new PrintWriter(out);
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(
                writer,
                120,
                commandLineSyntax,
                "",
                createOptions(),
                3,
                3,
                "",
                true);
        writer.flush();
    }

    @SuppressWarnings("static-access")
    public static Options createOptions() {

        // create Options object
        Options options = new Options();

        // add t option
        options.addOption(new Option("v", OPT_VERBOSE, false, "Output extra information while running."));
        options.addOption(new Option("?", OPT_HELP, false, "Print this message."));

        options.addOption(OptionBuilder.withArgName("file").withLongOpt(OPT_CONFIG).hasArg()
                .withDescription("The rampart job configuration file.  Required, unless cleaning or viewing help.").create("c"));

        options.addOption(OptionBuilder.withArgName("file").withLongOpt(OPT_ENV_CONFIG).hasArg()
                .withDescription("The rampart environment configuration file.  Default: " + RampartCLI.DEFAULT_ENV_CONFIG).create("e"));

        options.addOption(OptionBuilder.withArgName("file").withLongOpt(OPT_LOG_CONFIG).hasArg()
                .withDescription("The rampart logging configuration file.  Default: " + RampartCLI.DEFAULT_LOG_CONFIG).create("l"));

        options.addOption(OptionBuilder.withArgName("file").withLongOpt(OPT_OUTPUT).hasArg()
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

        options.addOption(OptionBuilder.withArgName("file").withLongOpt(OPT_CLEAN).hasOptionalArg()
                .withDescription("The directory to clean.").create("z"));

        return options;
    }



    public RampartArgs convert() {

        String actualJobPrefix = this.jobPrefix.replaceAll("TIMESTAMP", createTimestamp());

        RampartArgs args = new RampartArgs();
        args.setConfig(this.config);
        args.setOutputDir(this.output);
        args.setJobPrefix(actualJobPrefix);
        args.setStages(RampartStage.parse(this.stages));

        return args;
    }

    protected final String createTimestamp() {
        Format formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return formatter.format(new Date());
    }

    protected final String createDefaultJobPrefix() {

        return "rampart-" + createTimestamp();
    }
}
