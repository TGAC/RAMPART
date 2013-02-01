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
package uk.ac.tgac.rampart.conan.conanx.env.locality;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.rampart.conan.conanx.env.scheduler.Scheduler;
import uk.ac.tgac.rampart.conan.conanx.env.scheduler.WaitCondition;
import uk.ac.tgac.rampart.conan.conanx.process.NativeProcessExecutor;

import java.io.IOException;

/**
 * This environment is used to execute code on the localhost. If the localhost
 * happens to be a single machine and a multi-user environment then it is wise
 * to only submit short single threaded jobs to avoid interfering with other
 * users' processes.
 *
 * @author maplesod
 */
public class Local implements Locality {


    @Autowired
    private NativeProcessExecutor nativeProcessExecutor;

    /**
     * No need to establish a connection to the local machine, so this method always returns true.
     * @return true
     */
    @Override
    public boolean establishConnection() {

        return true;
    }

    /**
     * No need to disconnect from the local machine, so this method always returns true.
     * @return true
     */
    @Override
    public boolean disconnect() {

        return true;
    }

    @Override
    public int executeCommand(String command) throws ProcessExecutionException, InterruptedException {
        return this.executeCommand(command, null);
    }

    @Override
    public int executeCommand(String command, Scheduler scheduler)
            throws ProcessExecutionException, InterruptedException {

        String[] output = null;

        if (scheduler == null) {
            try {
                output = this.nativeProcessExecutor.execute(command);
            }
            catch(IOException ioe) {
                throw new ProcessExecutionException(-1, ioe);
            }
            catch(CommandExecutionException cee) {
                throw new ProcessExecutionException(-1, cee);
            }
        }
        else {
            output = scheduler.executeCommand(command);
        }

        return output == null ? -1 : 0;
    }

    @Override
    public void dispatchCommand(String command, Scheduler scheduler)
            throws ProcessExecutionException, InterruptedException {


    }

    @Override
    public int waitFor(WaitCondition waitCondition, Scheduler architecture) throws ProcessExecutionException, InterruptedException {
        return 0;
    }

}
