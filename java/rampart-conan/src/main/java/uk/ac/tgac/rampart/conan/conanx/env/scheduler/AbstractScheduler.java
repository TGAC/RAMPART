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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.ebi.fgpt.conan.utils.ProcessUtils;
import uk.ac.tgac.rampart.conan.conanx.process.InvocationTrackingProcessListener;
import uk.ac.tgac.rampart.conan.conanx.process.NativeProcessExecutor;
import uk.ac.tgac.rampart.conan.conanx.process.ProcessAdapter;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class AbstractScheduler implements Scheduler {

    private static Logger log = LoggerFactory.getLogger(AbstractScheduler.class);

    @Autowired
    private NativeProcessExecutor nativeProcessExecutor;

    private String submitCommand;
    private SchedulerArgs args;

    protected AbstractScheduler(String submitCommand, AbstractSchedulerArgs args) {
        this.submitCommand = submitCommand;
        this.args = args;
    }


    /**
     * Return the command used to submit jobs to this scheduling system
     * @return The submit command for this scheduler
     */
    public String getSubmitCommand() {
        return this.submitCommand;
    }

    @Override
    public SchedulerArgs getArgs() {
        return args;
    }

    @Override
    public void setArgs(SchedulerArgs args) {
        this.args = args;
    }

    @Override
    public String[] executeCommand(String command)
            throws IllegalArgumentException, ProcessExecutionException, InterruptedException {

        String[] output = null;

        if (!this.getArgs().isBackgroundTask()) {
            output = this.monitoredDispatch(command);
            this.waitFor(this.createProcessAdapter());
        }
        else {
            output = this.dispatchCommand(command);
        }

        return output;
    }



    /**
     * Dispatches command and then forgets about it.  No progress tracking is performed.
     * @param command The command to dispatch to the scheduler
     * @throws ProcessExecutionException
     */
    @Override
    public String[] dispatchCommand(String command)
            throws IllegalArgumentException, ProcessExecutionException, InterruptedException {

        String schedulerCommand = this.createCommand(command);

        String[] output = null;

        // Process dispatch
        boolean dispatched = false;
        try{
            log.debug("Dispatching command: " + schedulerCommand);
            output = this.nativeProcessExecutor.execute(schedulerCommand);
        }
        catch (CommandExecutionException e) {

            String message = "Failed to execute job (exited with exit code " + e.getExitCode() + ")";
            // could not dispatch
            log.error(message, e);
            ProcessExecutionException pex = new ProcessExecutionException(
                    e.getExitCode(), message, e);
            pex.setProcessOutput(e.getErrorOutput());
            try {
                pex.setProcessExecutionHost(InetAddress.getLocalHost().getHostName());
            }
            catch (UnknownHostException e1) {
                log.debug("Unknown host", e1);
            }
            throw pex;
        }
        catch (IOException e) {
            String message = "Failed to read output stream of native system process";
            log.error(message);
            log.debug("IOException follows", e);
            throw new ProcessExecutionException(1, message, e);
        }

        return output;
    }


    /**
     * Dispatches this command to the scheduler.  The generated process will run in the background and write its standard
     * output to the monitor file stored in this object.  If the monitor file already exists then we assume that the user
     * if trying to recover from a previous run.  In this case we do not execute the command and delete the monitored file.
     * @param command The command to schedule.
     * @throws ProcessExecutionException
     */
    public String[] monitoredDispatch(String command)
            throws ProcessExecutionException {

        return this.monitoredDispatch(command, this.args.getMonitorFile());
    }

    /**
     * Dispatches this command to the scheduler.  The generated process will run in the background and write its standard
     * output to the specified monitor file.
     * @param command The command to schedule.
     * @param monitorFile The file that will contain the standard output from the scheduled process.  This file will be
     *                    actively monitored in order to ascertain job state and progress.
     * @return Console output produced from executing the command.
     * @throws ProcessExecutionException
     */
    public String[] monitoredDispatch(String command, File monitorFile)
            throws ProcessExecutionException {

        String schedulerCommand = this.createCommand(command);

        // does an existing output file exist? if so, we need to go into recovery mode
        boolean recoveryMode = monitorFile.exists();

        String[] output = null;

        // only create our output file if we're not in recovery mode
        if (!recoveryMode) {
            log.debug("Creating " + monitorFile.getAbsolutePath());
            if (!ProcessUtils.createFiles(monitorFile)) {
                throw new ProcessExecutionException(
                        1,
                        "Unable to create output file at " + monitorFile.getAbsolutePath());
            }
        }

        // process dispatch
        boolean dispatched = false;
        try{
            log.debug("In recovery mode? " + recoveryMode);

            // Only dispatch the process if not in recovery mode... otherwise we will probably have two jobs running
            // simultaneously
            if (!recoveryMode) {
                log.debug("Dispatching command: " + schedulerCommand);
                output = this.nativeProcessExecutor.execute(schedulerCommand);
            }

            dispatched = true;
        }
        catch (CommandExecutionException e) {

            String message = "Failed to execute job (exited with exit code " + e.getExitCode() + ")";
            // could not dispatch
            log.error(message, e);
            ProcessExecutionException pex = new ProcessExecutionException(
                    e.getExitCode(), message, e);
            pex.setProcessOutput(e.getErrorOutput());
            try {
                pex.setProcessExecutionHost(InetAddress.getLocalHost().getHostName());
            }
            catch (UnknownHostException e1) {
                log.debug("Unknown host", e1);
            }
            throw pex;
        }
        catch (IOException e) {
            String message = "Failed to read output stream of native system process";
            log.error(message);
            log.debug("IOException follows", e);
            throw new ProcessExecutionException(1, message, e);
        }
        finally {
            // Whether the process succeeded or failed, delete output files to cleanup before continuing
            log.debug("Deleting " + monitorFile.getAbsolutePath());
            ProcessUtils.deleteFiles(monitorFile);
        }

        return output;
    }

    /**
     * Creates a process adapter specific to this scheduler.  Automatically, uses the monitor file and interval stored in
     * this object.
     * @return
     */
    public ProcessAdapter createProcessAdapter() {
        return createProcessAdapter(this.args.getMonitorFile(), this.args.getMonitorInterval());
    }

    /**
     * Creates a process adapter specific to this scheduler.  Uses a custom monitor file and interval.
     * @param monitorFile The file that this adapter will monitor.
     * @param monitorInterval The frequency at which this adapter will monitor the file.
     * @return A process adapter specific to this scheduler.
     */
    public abstract ProcessAdapter createProcessAdapter(File monitorFile, int monitorInterval);

    /**
     * Typically, a scheduler with queue a submitted job and let it execute in the background when resources are available.
     * This method allows the user to monitor an output file from the scheduler for this job and will parse each line as
     * it is added to the file.  This way we can monitor job progress and wait for it to complete in this method before
     * starting another job.  Once the job has completed the exit value is returned from this method, otherwise an exception
     * is thrown.
     * @param adapter The ProcessAdapter which monitors an output file, which contains details of the progress of the scheduled
     *                job.
     * @return An exit value describing the completion status of the job.
     * @throws ProcessExecutionException
     * @throws InterruptedException
     */
    @Override
    public int waitFor(ProcessAdapter adapter) throws ProcessExecutionException, InterruptedException {

        InvocationTrackingProcessListener listener = new InvocationTrackingProcessListener();
        adapter.addProcessListener(listener);

        // process exit value, initialise to -1
        int exitValue = -1;

        // process monitoring
        try {
            log.debug("Monitoring process, waiting for completion");
            exitValue = listener.waitFor();
            log.debug("Process completed with exit value " + exitValue);

            ProcessExecutionException pex = this.nativeProcessExecutor.interpretExitValue(exitValue);
            if (pex == null) {
                return exitValue;
            }
            else {
                pex.setProcessOutput(adapter.getProcessOutput());
                pex.setProcessExecutionHost(adapter.getProcessExecutionHost());
                throw pex;
            }
        }
        finally {
            // this process DID start, so only delete output files to cleanup if the process actually exited,
            // and wasn't e.g. interrupted prior to completion
            if (exitValue != -1) {
                log.debug("Deleting " + adapter.getFile().getAbsolutePath());
                ProcessUtils.deleteFiles(adapter.getFile());
            }
        }
    }

    @Override
    public String[] executeWaitCommand(WaitCondition waitCondition)
            throws InterruptedException, ProcessExecutionException {

        String waitCommand = this.createWaitCommand(waitCondition);

        return this.executeCommand(waitCommand);
    }
}
