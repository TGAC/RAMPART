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

import java.io.File;

public class RampartOptions {

    public static final String OPT_CLEAN = "clean";
    public static final String OPT_CONFIG = "config";
    public static final String OPT_STAGES = "stages";
    public static final String OPT_OUTPUT = "output";
    public static final String OPT_VERBOSE = "verbose";
    public static final String OPT_HELP = "help";

    private File config = null;
    private File output = null;
    private String stages = null;
    private File clean = null;
    private boolean verbose = false;
    private boolean help = false;



    public RampartOptions(CommandLine cmdLine) throws ParseException {

        help = cmdLine.hasOption(OPT_HELP);

        clean = cmdLine.hasOption(OPT_CLEAN) ?
            cmdLine.getOptionValue(OPT_CLEAN) != null ?
                    new File(cmdLine.getOptionValue(OPT_CLEAN)) :
                    new File(".") :
            null;

        if (!help && clean == null) {
            if (cmdLine.hasOption(OPT_CONFIG)) {
                config = new File(cmdLine.getOptionValue(OPT_CONFIG));
            }
            else {
                throw new ParseException(OPT_CONFIG + " argument not specified.");
            }

            output = cmdLine.hasOption(OPT_OUTPUT) ? new File(cmdLine.getOptionValue(OPT_OUTPUT)) : new File(".");
            stages = cmdLine.hasOption(OPT_STAGES) ? cmdLine.getOptionValue(OPT_STAGES) : "ALL";
            verbose = cmdLine.hasOption(OPT_VERBOSE);
        }
    }

    public File getConfig() {
        return config;
    }

    public void setConfig(File config) {
        this.config = config;
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

    public void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(this.getClass().getName(), createOptions());
    }

    @SuppressWarnings("static-access")
    public static Options createOptions() {

        // Boolean options
        Option opt_verbose = new Option("v", OPT_VERBOSE, false, "Output extra information while running.");
        Option opt_help = new Option("?", OPT_HELP, false, "Print this message.");

        // Options with arguments
        Option opt_config = OptionBuilder.withArgName("file").withLongOpt(OPT_CONFIG).hasArg()
                .withDescription("The rampart configuration file.").create("c");

        Option opt_output = OptionBuilder.withArgName("file").withLongOpt(OPT_OUTPUT).hasArg()
                .withDescription("The directory to put output from this job.").create("o");

        Option opt_stages = OptionBuilder.withArgName("string").withLongOpt(OPT_STAGES).hasArg()
                .withDescription("The RAMPART stages to execute: QT, MASS, AMP, HELPER, ALL.  Default: ALL.").create("s");

        Option opt_clean = OptionBuilder.withArgName("file").withLongOpt(OPT_CLEAN).hasArg()
                .withDescription("The directory to clean.").create("z");

        // create Options object
        Options options = new Options();

        // add t option
        options.addOption(opt_verbose);
        options.addOption(opt_help);
        options.addOption(opt_config);
        options.addOption(opt_output);
        options.addOption(opt_stages);
        options.addOption(opt_clean);

        return options;
    }


}
