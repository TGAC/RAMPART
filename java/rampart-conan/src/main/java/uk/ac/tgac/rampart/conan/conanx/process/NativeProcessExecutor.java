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
package uk.ac.tgac.rampart.conan.conanx.process;

import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;

import java.io.IOException;

/**
 * User: maplesod
 * Date: 08/01/13
 * Time: 10:54
 */
public interface NativeProcessExecutor {

    /**
     * Creates a native system process from the given command.  Current process will wait until the executed command has completed.
     * After execution the result is an array of strings, where each element represents one line of stdout or stderr for
     * the executed native process.
     * @param command the command to execute
     * @return the stdout.stderr of the process
     * @throws uk.ac.ebi.fgpt.conan.utils.CommandExecutionException if the process exits with a failure condition.  This exception wraps the error
     *                                   output and the process exit code.
     * @throws java.io.IOException  if the stdout or stderr of the process could not be read
     */
    String[] execute(String command) throws CommandExecutionException, IOException;


    /**
     * Creates a native system process from the given command.  Current process will continue after dispatching command.
     *
     * @param command the command to execute
     * @return the process ID
     * @throws uk.ac.ebi.fgpt.conan.utils.CommandExecutionException if the process exits with a failure condition.  This exception wraps the error
     *                                   output and the process exit code.
     * @throws java.io.IOException  if the stdout or stderr of the process could not be read
     */
    int dispatch(String command) throws CommandExecutionException, IOException, ProcessExecutionException;


    /**
     * Translates an exit value returned by the process into a meaningful java exception.  Override this method if
     * you want to do something clever with certain exit values.  The default behaviour is to wrap the supplied exit
     * value inside a ProcessExecutionException and provide a generic error message, if the exit code is non-zero.  If
     * an exit code of zero is passed, this method should return null.
     *
     * @param exitValue the exit value returned from the process upon completion
     * @return a ProcessExecutionException that minimally wraps the exit value of the process, and possibly provides
     *         further informative error messages if the exit value is non-zero, otherwise null
     */
    ProcessExecutionException interpretExitValue(int exitValue);


}
