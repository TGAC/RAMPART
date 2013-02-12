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
package uk.ac.tgac.rampart.conan.conanx.exec.process;

import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.exec.context.ExecutionContext;
import uk.ac.tgac.rampart.conan.conanx.exec.context.WaitCondition;

public interface ProcessExecutionService {

    /**
     * Execute a defined {@link ConanXProcess}.  The proc may be executed in the foreground or the background depending
     * on how the {@link ExecutionContext} is configured.
     * {@link ExecutionContext} and waits for it to complete before returning.
     *
     * @param conanXProcess    The {@link ConanXProcess} to execute
     * @param executionContext The {@link ExecutionContext} within which to execute the {@link ConanXProcess}
     * @throws InterruptedException      Thrown if the executed proc was interrupted during the job
     * @throws ProcessExecutionException Thrown if there were any problems initialising the job or with the job output
     */
    int execute(ConanXProcess conanXProcess, ExecutionContext executionContext)
            throws InterruptedException, ProcessExecutionException;

    /**
     * Execute a command in the shell.  The proc may be executed in the foreground or the background depending
     * on how the {@link ExecutionContext} is configured. This is used for simple shell commands that do not require any
     * specific proc management.  E.g. for changing directory, or linking files.  For tasks that are non-trivial, i.e
     * tasks that do not complete within a couple of seconds it is recommended that the user creates a ConanXProcess and
     * executes that using the alternative variant of this method.
     *
     * @param command          The shell command to execute
     * @param executionContext The {@link ExecutionContext} within which to execute the shell command.
     * @throws InterruptedException      Thrown if the executed proc was interrupted during the job
     * @throws ProcessExecutionException Thrown if there were any problems initialising the job or with the job output
     */
    int execute(String command, ExecutionContext executionContext)
            throws InterruptedException, ProcessExecutionException;

    /**
     * If a proc or command (or set of processes or commands) was executed in the background, then this method can be
     * called to wait for it (them) to complete.
     *
     * @param waitCondition    The {@link WaitCondition} describing which job(s) to wait for.
     * @param executionContext The {@link ExecutionContext} within which the job(s) to wait for is(are) running.
     * @return The exit code for the job, returned after the job(s) has(have) completed.
     * @throws InterruptedException      Thrown if the wait condition was interrupted before the jobs completed
     * @throws ProcessExecutionException Thrown if there were any problems initialising the wait condition or with the
     *                                   result of waiting for the job(s) to complete
     */
    int waitFor(WaitCondition waitCondition, ExecutionContext executionContext)
            throws InterruptedException, ProcessExecutionException;
}
