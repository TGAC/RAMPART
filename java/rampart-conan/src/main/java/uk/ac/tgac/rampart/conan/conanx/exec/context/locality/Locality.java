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
package uk.ac.tgac.rampart.conan.conanx.exec.context.locality;

import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.exec.context.WaitCondition;
import uk.ac.tgac.rampart.conan.conanx.exec.process.monitor.ProcessAdapter;

import java.io.IOException;

public interface Locality {

    /**
     * Establish a connection to the execution environment, which will be used for executing any supplied commands.
     *
     * @return true if connection was established, otherwise false.
     */
    boolean establishConnection();

    /**
     * Executes the supplied command using the supplied args directly, at the locality indicated by this object.  Will
     * wait until the command has completed before returning with the exitCode produced from executing
     * the command.
     *
     * @param command The command that is to be executed in the foreground
     * @return The exitCode from the proc that was executed
     * @throws InterruptedException
     * @throws ProcessExecutionException
     */
    int execute(String command)
            throws ProcessExecutionException, InterruptedException;

    /**
     * Intended for executing Scheduled Tasks in the foreground.  A {@link uk.ac.tgac.rampart.conan.conanx.exec.process.monitor.ProcessAdapter} is used to monitor progress of
     * the proc.
     *
     * @param command        The command to execute.
     * @param processAdapter The {@link uk.ac.tgac.rampart.conan.conanx.exec.process.monitor.ProcessAdapter} which should monitor Task progress
     * @return The exitCode from the proc that was executed
     * @throws InterruptedException
     * @throws ProcessExecutionException
     */
    int monitoredExecute(String command, ProcessAdapter processAdapter)
            throws InterruptedException, ProcessExecutionException;

    /**
     * Executes the supplied command using the supplied args, on the requested scheduler at the locality indicated
     * by this object.  Will dispatch the command and leave it running in the background.  This is typically used when
     * the user wants to execute multiple command in parallel.
     *
     * @param command The command that is to be executed in the background
     * @throws InterruptedException
     * @throws ProcessExecutionException
     */
    void dispatch(String command)
            throws ProcessExecutionException, InterruptedException;

    /**
     * If a proc was dispatched and is executing in the background, then the user may want to wait for that proc,
     * or processes, to complete before continueing.
     *
     * @param waitCondition The waitCondition that needs to be satisfied before continueing.
     * @return The exit code produced after the wait condition has been satisfied.
     * @throws ProcessExecutionException
     * @throws InterruptedException
     * @throws IOException
     */
    int waitFor(WaitCondition waitCondition)
            throws ProcessExecutionException, InterruptedException;

    /**
     * Disconnect from the terminal after use.
     *
     * @return true if disconnected successfully otherwise false
     */
    boolean disconnect();


    /**
     * Returns a deep copy of this locality
     *
     * @return A deep copy of this locality
     */
    Locality copy();
}
