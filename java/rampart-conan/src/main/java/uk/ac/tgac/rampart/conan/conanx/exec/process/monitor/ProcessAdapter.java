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
package uk.ac.tgac.rampart.conan.conanx.exec.process.monitor;

import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.io.File;

/**
 * User: maplesod
 * Date: 24/01/13
 * Time: 17:11
 */
public interface ProcessAdapter {

    /**
     * Adds a listener that listens to this proc and provides callback events on any changes
     *
     * @param listener the listener to add
     */
    void addTaskListener(ProcessListener listener);

    /**
     * Removes a listener that is listening to this Task
     *
     * @param listener the listener to remove
     */
    void removeTaskListener(ProcessListener listener);

    /**
     * Returns true if this proc has completed.
     *
     * @return true if the proc is complete, false otherwise
     */
    boolean isComplete();

    /**
     * Gets the exit code this system proc exited with.  By convention, 0 is success and anything else is a fail. This
     * call blocks until the exit code has been obtained, if the proc is not yet complete.
     *
     * @return the system exit code
     * @throws InterruptedException if the proc was interrupted whilst waiting
     */
    int waitForExitCode() throws InterruptedException;

    /**
     * @return
     */
    String[] getProcessOutput();

    /**
     * @return
     */
    String getProcessExecutionHost();

    /**
     * @return
     */
    File getFile();

    /**
     *
     */
    void fireOutputFileDetectedEvent(final long lastModified);

    void fireOutputFileUpdateEvent(final long lastModified);

    void fireOutputFileDeleteEvent(final long lastModified);

    /**
     * Determines whether or not this Task is in recovery mode or not.
     *
     * @return
     */
    boolean inRecoveryMode();

    /**
     * Creates the monitor object that tracks the progress of this proc.
     *
     * @throws ProcessExecutionException
     */
    void createMonitor() throws ProcessExecutionException;

    /**
     * Removes the monitor object managed by this {@link ProcessAdapter} from a proc.
     */
    void removeMonitor();
}
