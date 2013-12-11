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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import uk.ac.tgac.conan.core.util.JarUtils;
import uk.ac.tgac.rampart.util.CommandLineHelper;

import java.io.File;
import java.util.jar.JarFile;

/**
 * Main entry point for RAMPART: a automated de novo assembly pipeline.
 */
public class RampartCLI {

    private static final String OPT_HELP = "help";

    // **** Jar details ****
    public static final String DEFAULT_JAR_NAME = "rampart-<version>.jar";

    public static final String START_COMMAND_LINE = "java -jar " + getJarName();
    public static final String COMMAND_LINE = START_COMMAND_LINE + " <mode> [MODE_ARGS...]";
    public static final File CWD = currentWorkingDir();

    /**
     * Returns the name of the Jar file that this code is being executed from
     * @return The name of this Jar.
     */
    private static String getJarName() {
        JarFile jarFile = JarUtils.jarForClass(RampartCLI.class, null);
        return jarFile != null ? jarFile.getName() : DEFAULT_JAR_NAME;
    }


    /**
     * Returns the current working directory as an absolute file
     * @return The current working directory
     */
    private static File currentWorkingDir() {
        return new File(".").getAbsoluteFile().getParentFile();
    }


    /**
     * Options for RAMPART
     * @return
     */
    private static Options createOptions() {

        // create Options object
        Options options = new Options();

        // add t option
        options.addOption(new Option("?", OPT_HELP, false, "Print this message."));

        return options;
    }



    private static void printHelp() {

        CommandLineHelper.printHelp(
                System.err,
                COMMAND_LINE,
                "RAMPART\n\n" +
                "RAMPART is a de novo assembly pipeline that makes use of third party-tools and High Performance Computing " +
                "resources.  It can be used as a single interface to several popular assemblers, and can perform " +
                "automated comparison and analysis of any generated assemblies.\n\n" +
                "The first argument must describe the mode in which you wish to run RAMPART.  Each mode contains its own " +
                "command line help, which can be accessed by entering the mode then adding \"--help\".  Available modes:\n\n" +
                RampartMode.description() + "\n\n" +
                "Options:\n",
                createOptions());
    }


    /**
     * The main entry point for RAMPART.  Looks at the first argument to decide which mode to run in.  Execution of each
     * mode is handled by RampartMode.
     * @param args Command line arguments
     */
    public static void main(String[] args) {

        // Process the command line
        try {

            if (args.length == 0 || args[0].equals("--" + OPT_HELP)) {
                printHelp();
                System.exit(0);
            }

            RampartMode mode = RampartMode.valueOf(args[0].toUpperCase());
            mode.execute((String[])ArrayUtils.subarray(args, 1, args.length));
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
