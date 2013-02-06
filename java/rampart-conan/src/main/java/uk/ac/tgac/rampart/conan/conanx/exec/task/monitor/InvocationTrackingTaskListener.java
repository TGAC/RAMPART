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
package uk.ac.tgac.rampart.conan.conanx.exec.task.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link TaskListener} that encapsulates the state of each invocation of
 * a task and updates flags for completion and success.  Processes using this listener implementation can block on
 * {@link #waitFor()}, which only returns once the task being listened to is complete.
 *
 * User: maplesod
 * Date: 24/01/13
 * Time: 16:49
 */
public class InvocationTrackingTaskListener implements TaskListener {

    private static Logger log = LoggerFactory.getLogger(InvocationTrackingTaskListener.class);

    private boolean complete;
    private int exitValue;

    public InvocationTrackingTaskListener() {
        complete = false;
        exitValue = -1;
    }

    public void processComplete(TaskEvent evt) {
        log.debug("Task complete, task exit value was " + evt.getExitValue());
        exitValue = evt.getExitValue();
        complete = true;
        synchronized (this) {
            notifyAll();
        }
    }

    public void processUpdate(TaskEvent evt) {
        // do nothing, not yet finished
        log.debug("File was modified");
        synchronized (this) {
            notifyAll();
        }
    }

    public void processError(TaskEvent evt) {
        // something went wrong
        log.debug("File was deleted by an task task");
        exitValue = 1;
        complete = true;
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * Returns the success of the Process being listened to, only once complete.  This method blocks until
     * completion or an interruption occurs.
     *
     * @return the exit value of the underlying task
     * @throws InterruptedException if the thread was interrupted whilst waiting
     */
    public int waitFor() throws InterruptedException {
        while (!complete) {
            synchronized (this) {
                wait();
            }
        }
        log.debug("Process completed: exitValue = " + exitValue);
        return exitValue;
    }
}
