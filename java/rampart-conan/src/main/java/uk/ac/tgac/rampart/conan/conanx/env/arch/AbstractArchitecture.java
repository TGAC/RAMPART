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
package uk.ac.tgac.rampart.conan.conanx.env.arch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.ebi.fgpt.conan.utils.ProcessUtils;
import uk.ac.tgac.rampart.conan.conanx.env.EnvironmentArgs;
import uk.ac.tgac.rampart.conan.conanx.process.NativeProcessExecutor;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * User: maplesod
 * Date: 08/01/13
 * Time: 13:40
 */
public abstract class AbstractArchitecture implements Architecture {

    private static Logger log = LoggerFactory.getLogger(Single.class);

    @Autowired
    protected NativeProcessExecutor processExecutor;

    @Override
    public void submitCommand(String command, EnvironmentArgs envArgs) throws IllegalArgumentException, ProcessExecutionException, InterruptedException, ConnectException {

        // cleanup any old copies of the output files and create a new one
        File outputFile = envArgs.getCmdLineOutputFile();

        // does an existing output file exist? if so, we need to go into recovery mode
        boolean recoveryMode = outputFile.exists();

        // only create our output file if we're not in recovery mode
        if (!recoveryMode) {
            log.debug("Creating " + outputFile.getAbsolutePath());
            if (!ProcessUtils.createFiles(outputFile)) {
                throw new ProcessExecutionException(
                        1,
                        "Unable to create output file at " + outputFile.getAbsolutePath());
            }
        }

        // process dispatch
        boolean dispatched = false;
        try{
            log.debug("In recovery mode? " + recoveryMode);
            if (!recoveryMode) {
                // dispatch the process
                log.debug("Dispatching command: " + command);
                processExecutor.execute(command);
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
            log.debug("Deleting " + outputFile.getAbsolutePath());
            ProcessUtils.deleteFiles(outputFile);
        }
    }
}
