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

package uk.ac.tgac.rampart.util;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 25/10/13
 * Time: 11:26
 * To change this template use File | Settings | File Templates.
 */
public class JobCleaner {

    // **** Constants ****
    public static final String DESC = "Cleans a RAMPART job directory of any temporary information created by RAMPART.";

    // **** Option parameter names ****
    public static final String OPT_VERBOSE = "verbose";
    public static final String OPT_HELP = "help";

    // **** Options ****
    private File targetDir;
    private boolean verbose;
    private boolean help;

    public JobCleaner() {
        this.targetDir = new File("");
        this.verbose = false;
        this.help = false;
    }

    public JobCleaner(String[] args) throws ParseException {

        // Parse the command line arguments
        CommandLine cmdLine = new PosixParser().parse(createOptions(), args, true);

        // Extract optional boolean flags
        this.help = cmdLine.hasOption(OPT_HELP);
        this.verbose = cmdLine.hasOption(OPT_VERBOSE);

        // If there's a remaining argument then this will be the target directory, if not then assume the user wants
        // to download packages into the current working directory
        this.targetDir = cmdLine.getArgList().isEmpty() ? new File("").getAbsoluteFile() : new File((String)cmdLine.getArgList().get(0));
    }

    private static Options createOptions() {

        // create Options object
        Options options = new Options();

        // add t option
        options.addOption(new Option("v", OPT_VERBOSE, false, "Output extra information while running."));
        options.addOption(new Option("?", OPT_HELP, false, "Print this message."));

        return options;
    }

    private static void printHelp() {

        CommandLineHelper.printHelp(
                System.err,
                "rampart-clean [<target_dir>]",
                "RAMPART job cleaning tool\n\n" +
                "This tool removes temporary data produced by RAMPART within a given output directory.  This tools saves " +
                "some trouble to manually delete data.\n\n" +
                "If no final argument is specified then this tool will try to clean the current working directory otherwise " +
                "it will try to clean the directory pointed to by the final argument.\n\n",
                createOptions());
    }

    public void execute() throws IOException {

        if (this.help) {
            printHelp();
        }
        else {
            cleanJob(this.targetDir);
        }
    }

    /**
     * Cleans a RAMPART job directory of any known temporary information.  This will not delete any information not in
     * the MECQ, MASS, AMP or REPORT directories.
     * @param jobDir The RAMPART job directory to clean
     * @throws java.io.IOException Thrown if there were an issues cleaning the directory
     */
    public void cleanJob(File jobDir) throws IOException {

        if (this.verbose) {
            System.out.print("Cleaning RAMPART job directory: " + jobDir.getAbsolutePath() + " ...");
        }

        FileUtils.deleteDirectory(new File(jobDir, "rampart_out"));
        FileUtils.deleteDirectory(new File(jobDir, "wait_logs"));

        if (this.verbose) {
            System.out.println(" done.");
        }
    }

    /**
     * The main entry point for RAMPART's job cleaner.
     * @param args Command line arguments
     */
    public static void main(String[] args) {

        // Process the command line
        try {
            new JobCleaner(args).execute();
        }
        catch (IllegalArgumentException | ParseException e) {
            System.err.println(e.getMessage());
            printHelp();
            System.exit(1);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println(StringUtils.join(e.getStackTrace(), "\n"));
            System.exit(2);
        }
    }

}
