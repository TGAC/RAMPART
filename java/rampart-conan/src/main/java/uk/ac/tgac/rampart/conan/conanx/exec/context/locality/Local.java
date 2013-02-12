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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.ebi.fgpt.conan.utils.ProcessRunner;
import uk.ac.ebi.fgpt.conan.utils.ProcessUtils;
import uk.ac.tgac.rampart.conan.conanx.exec.context.WaitCondition;
import uk.ac.tgac.rampart.conan.conanx.exec.process.monitor.InvocationTrackingProcessListener;
import uk.ac.tgac.rampart.conan.conanx.exec.process.monitor.ProcessAdapter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This environment is used to execute code on the localhost. If the localhost
 * happens to be a single machine and a multi-user environment then it is wise
 * to only submit short single threaded jobs to avoid interfering with other
 * users' processes.
 *
 * @author maplesod
 */
public class Local implements Locality {


    private static Logger log = LoggerFactory.getLogger(Local.class);


    /**
     * No need to establish a connection to the local machine, so this method always returns true.
     *
     * @return true
     */
    @Override
    public boolean establishConnection() {

        return true;
    }

    /**
     * No need to disconnect from the local machine, so this method always returns true.
     *
     * @return true
     */
    @Override
    public boolean disconnect() {

        return true;
    }

    /**
     * This is pretty simple as a Local locality does not contain any state.
     *
     * @return
     */
    @Override
    public Locality copy() {
        return new Local();
    }

    @Override
    public int monitoredExecute(String command, ProcessAdapter processAdapter) throws InterruptedException, ProcessExecutionException {

        boolean dispatched = false;
        boolean recoveryMode = processAdapter.inRecoveryMode();

        log.debug("In recovery mode? " + recoveryMode);

        // Only dispatch the proc if not in recovery mode... otherwise we will probably have two jobs running
        // simultaneously
        try {
            if (!recoveryMode) {

                // Create the proc monitor
                processAdapter.createMonitor();

                // Execute the proc (this is a scheduled proc so it should run in the background, managed by the scheduler,
                // therefore we call the normal foreground execute method)
                this.execute(command);

                // Wait for the proc to complete by using the proc monitor
                return this.waitFor(processAdapter);
            }
        } finally {
            // Remove the monitor, even if we are in recovery mode or there was an error.
            processAdapter.removeMonitor();
        }

        // Hopefully we don't reach here but if we do then just return exit code 1 to signal an error.
        return 1;
    }


    @Override
    public int execute(String command) throws ProcessExecutionException, InterruptedException {

        String[] output;

        try {
            log.debug("Issuing command: [" + command + "]");
            ProcessRunner runner = new ProcessRunner();
            runner.redirectStderr(true);
            output = runner.runCommmand(command);
            if (output.length > 0) {
                log.debug("Response from command [" + command + "]: " +
                        output.length + " lines, first line was " + output[0]);
            }
        } catch (CommandExecutionException e) {

            String message = "Failed to execute job (exited with exit code " + e.getExitCode() + ")";

            log.error(message, e);
            ProcessExecutionException pex = new ProcessExecutionException(
                    e.getExitCode(), message, e);
            pex.setProcessOutput(e.getErrorOutput());
            try {
                pex.setProcessExecutionHost(InetAddress.getLocalHost().getHostName());
            } catch (UnknownHostException e1) {
                log.debug("Unknown host", e1);
            }
            throw pex;
        } catch (IOException e) {
            String message = "Failed to read output stream of native system proc";
            log.error(message);
            log.debug("IOException follows", e);
            throw new ProcessExecutionException(1, message, e);
        }

        return 0;
    }

    @Override
    public void dispatch(String command) throws ProcessExecutionException, InterruptedException {

        // Actually for the moment we assume that this is only called for scheduled tasks.  Doesn't make much sense
        // as implemented right now for tasks running on the local host.
        this.execute(command);
    }


    @Override
    public int waitFor(WaitCondition waitCondition) throws ProcessExecutionException, InterruptedException {
        throw new UnsupportedOperationException("Can't wait for tasks in this fashion yet");
    }

    /**
     * Typically, a scheduler will queue a submitted job and let it execute in the background when resources are available.
     * This method allows the user to monitor an output file from the scheduler for this job and will parse each line as
     * it is added to the file.  This way we can monitor job progress and wait for it to complete in this method before
     * starting another job.  Once the job has completed the exit value is returned from this method, otherwise an exception
     * is thrown.
     *
     * @param adapter The ProcessAdapter which monitors an output file, which contains details of the progress of the scheduled
     *                job.
     * @return An exit value describing the completion status of the job.
     * @throws uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException
     *
     * @throws InterruptedException
     */
    protected int waitFor(ProcessAdapter adapter) throws ProcessExecutionException, InterruptedException {

        InvocationTrackingProcessListener listener = new InvocationTrackingProcessListener();
        adapter.addTaskListener(listener);

        // proc exit value, initialise to -1
        int exitValue = -1;

        // proc monitoring
        try {
            log.debug("Monitoring proc, waiting for completion");
            exitValue = listener.waitFor();
            log.debug("Process completed with exit value " + exitValue);

            if (exitValue != 0) {
                return exitValue;
            } else {
                ProcessExecutionException pex = new ProcessExecutionException(exitValue);
                pex.setProcessOutput(adapter.getProcessOutput());
                pex.setProcessExecutionHost(adapter.getProcessExecutionHost());
                throw pex;
            }
        } finally {
            // this proc DID start, so only delete output files to cleanup if the proc actually exited,
            // and wasn't e.g. interrupted prior to completion
            if (exitValue != -1) {
                log.debug("Deleting " + adapter.getFile().getAbsolutePath());
                ProcessUtils.deleteFiles(adapter.getFile());
            }
        }
    }
}
