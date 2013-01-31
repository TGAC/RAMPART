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
package uk.ac.tgac.rampart.conan.conanx.env.scheduler;

import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.rampart.conan.conanx.process.ProcessAdapter;

import java.io.IOException;
import java.net.ConnectException;


public interface Scheduler {

    /**
     * Returns the scheduler args associated with this scheduler
     * @return Scheduler args
     */
    SchedulerArgs getArgs();

    /**
     * Sets the Scheduler Args for this scheduler object.
     * @param args The scheduler args
     */
    void setArgs(SchedulerArgs args);

    /**
     * Creates a command that wraps the provided internalCommand with architecture specific details.
     * @param command The command describing the process to execute on this architecture.
     * @return A command that should be used to execute the provided command on this architecture.
     */
    String createCommand(String command);

    /**
     * Executes the provided command on this architecture in the foreground
     * @param command The command to execute
     * @return the exit code returned from the executed process
     * @throws ProcessExecutionException
     * @throws InterruptedException
     */
    int executeCommand(String command)
            throws ProcessExecutionException, InterruptedException;

    /**
     * Executes the provided command on this architecture in the background
     * @param command The command to execute
     * @return the exit code returned from the executed process
     * @throws ProcessExecutionException
     * @throws InterruptedException
     */
    void dispatchCommand(String command)
            throws ProcessExecutionException, InterruptedException;


    /**
     * Creates a command that can execute the specified wait condition on this architecture.  Typically this is used to
     * generate a command that can be executed as a stand alone process, which will not complete until the wait condition
     * has been fulfilled.
     * @param waitCondition The wait condition to convert into an architecture specific command.
     * @return An architecture specific wait command.
     */
    String createWaitCommand(WaitCondition waitCondition);


    /**
     * Wait for the specified wait condition to be satisifed
     * @param waitCondition The wait condition
     * @return The exit code return by the process that was waited for.
     * @throws InterruptedException
     * @throws ProcessExecutionException
     */
    int executeWaitCommand(WaitCondition waitCondition) throws InterruptedException, ProcessExecutionException;

    /**
     * Creates a wait condition for this architecture
     * @param exitStatus The type of exit status to wait for
     * @param condition The condition to wait for
     * @return A new wait condition object suitable for this architecture
     */
    WaitCondition createWaitCondition(ExitStatusType exitStatus, String condition);

    /**
     * Waits for the provided process adaptor to complete
     * @param adapter The adaptor to monitor
     * @return The exit code when the process completes
     * @throws ProcessExecutionException Thrown if there was an issue during process execution
     */
    int waitFor(ProcessAdapter adapter) throws ProcessExecutionException, InterruptedException;
}
