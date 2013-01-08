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

import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.env.EnvironmentArgs;
import uk.ac.tgac.rampart.conan.conanx.env.arch.Architecture;

import java.net.ConnectException;

/**
 * This environment is used to execute code on the localhost. If the localhost
 * happens to be a single machine and a multi-user environment then it is wise
 * to only submit short single threaded jobs to avoid interfering with other
 * users' processes.
 *
 * @author maplesod
 */
public class Local implements Locality {


    @Override
    public boolean establishConnection() {

        // Always can connect to a local machine.
        return true;
    }

    @Override
    public boolean disconnect() {

        // Always can disconnect from a local machine.
        return true;
    }

    @Override
    public boolean submitCommand(String command, EnvironmentArgs args, Architecture architecture)
            throws IllegalArgumentException, ProcessExecutionException,
            InterruptedException, ConnectException {

        if (architecture.isGridEngine()) {
            // Been asked to run locally on a grid engine.
            // Do something? maybe some validation?
        }

        architecture.submitCommand(command, args);

        return true;
    }

}
