/*
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
 */

package uk.ac.tgac.rampart;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.fgpt.conan.core.user.GuestUser;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.service.exception.TaskExecutionException;
import uk.ac.tgac.rampart.util.DependencyDownloader;
import uk.ac.tgac.rampart.util.JobCleaner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines the various modes in which RAMPART can be run.
 */
public enum RampartMode {

    /**
     * Normal operating mode for RAMPART.  Executes a job defined in a configuration file.
     */
    RUN {
        @Override
        public void execute(String[] args)
                throws IOException, ParseException, InterruptedException, TaskExecutionException {
            Rampart rampart = new Rampart(args);

            if (rampart.isHelp()) {
                rampart.printHelp();
            }
            else {
                rampart.execute();
            }
        }

        @Override
        public String getDescription() {
            return "Runs a RAMPART job given a job configuration file.";
        }
    },
    /**
     * Cleans a job directory of temporary files.
     */
    CLEAN {
        @Override
        public void execute(String[] args)
                throws IOException, ParseException {

            new JobCleaner(args).execute();
        }

        @Override
        public String getDescription() {
            return "Cleans a RAMPART job directory of any temporary information created by RAMPART.";
        }
    },
    /**
     * Downloads RAMPART dependencies
     */
    DOWNLOAD {
        @Override
        public void execute(String[] args)
                throws IOException, ParseException {

            new DependencyDownloader(args).execute();
        }

        @Override
        public String getDescription() {
            return "Downloads source code packages for RAMPART dependencies.";
        }
    };


    /**
     * Runs RAMPART in the mode given the command line arguments
     * @param args
     * @throws IOException
     * @throws ParseException
     * @throws InterruptedException
     * @throws TaskExecutionException
     */
    public abstract void execute(String[] args) throws IOException, ParseException, InterruptedException, TaskExecutionException;

    /**
     * Returns a short description describing what this mode does.
     * @return
     */
    public abstract String getDescription();


    /**
     * List all the available modes, represented as a String.
     * @return
     */
    public static String listModes() {

        List<String> toolStrings = new ArrayList<>();

        for (RampartMode mode : RampartMode.values()) {
            toolStrings.add(mode.toString().toLowerCase());
        }

        return "[" + StringUtils.join(toolStrings, ", ") + "]";
    }


    private static String pad(String name, int length) {

        StringBuilder sb = new StringBuilder();
        for(int i = name.length(); i < length; i++) {
            sb.append(" ");
        }

        return sb.toString();
    }

    public static String description() {

        StringBuilder sb = new StringBuilder();

        for(RampartMode mode : RampartMode.values()) {
             sb.append(" - ").append(mode.toString().toLowerCase()).append(pad(mode.toString(),15)).append(mode.getDescription()).append("\n");
        }

        return sb.toString();
    }
}
