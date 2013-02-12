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
package uk.ac.tgac.rampart.conan.conanx.exec.context.scheduler;

import uk.ac.tgac.rampart.conan.conanx.exec.context.WaitCondition;
import uk.ac.tgac.rampart.conan.conanx.exec.process.monitor.ProcessAdapter;

import java.io.File;


public interface Scheduler {


    /**
     * Returns the submit command for this scheduler.  E.g. "bsub" for Platform LSF.
     *
     * @return The submit command for this scheduler.
     */
    String getSubmitCommand();

    /**
     * Returns the scheduler args associated with this scheduler
     *
     * @return Scheduler args
     */
    SchedulerArgs getArgs();

    /**
     * Sets the Scheduler Args for this scheduler object.
     *
     * @param args The scheduler args
     */
    void setArgs(SchedulerArgs args);

    /**
     * Creates a command that wraps the provided internalCommand with this scheduler's specific details.
     *
     * @param command The command describing the proc to execute on this scheduler.
     * @return A command that should be used to execute the provided command on this scheduler.
     */
    String createCommand(String command);

    /**
     * Creates a command that can execute the specified wait condition on this architecture.  Typically this is used to
     * generate a command that can be executed as a stand alone proc, which will not complete until the wait condition
     * has been fulfilled.
     *
     * @param waitCondition The wait condition to convert into an architecture specific command.
     * @return An architecture specific wait command.
     */
    String createWaitCommand(WaitCondition waitCondition);

    /**
     * Generates a job kill command for this scheduler, using the supplied job identifier
     *
     * @param jobId The job identifier
     * @return A command for killing the specified job(s) on this scheduler.
     */
    String createKillCommand(String jobId);

    /**
     * Creates a {@link uk.ac.tgac.rampart.conan.conanx.exec.process.monitor.ProcessAdapter} specific to this scheduler.  Automatically, uses the monitor file and interval stored in
     * this object.
     *
     * @return A {@link uk.ac.tgac.rampart.conan.conanx.exec.process.monitor.ProcessAdapter} that monitors progress of a scheduled proc.
     */
    ProcessAdapter createTaskAdapter();

    /**
     * Creates a proc adapter specific to this scheduler.  Uses a custom monitor file and interval.
     *
     * @param monitorFile     The file that this adapter will monitor.
     * @param monitorInterval The frequency at which this adapter will monitor the file.
     * @return A proc adapter specific to this scheduler.
     */
    ProcessAdapter createTaskAdapter(File monitorFile, int monitorInterval);


    /**
     * Creates a wait condition for this architecture
     *
     * @param exitStatus The type of exit status to wait for
     * @param condition  The condition to wait for
     * @return A new wait condition object suitable for this architecture
     */
    WaitCondition createWaitCondition(ExitStatusType exitStatus, String condition);


    /**
     * Returns a deep copy of this Scheduler
     *
     * @return
     */
    Scheduler copy();
}
