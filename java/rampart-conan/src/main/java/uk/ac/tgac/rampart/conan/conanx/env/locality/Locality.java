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

import java.net.ConnectException;

import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.env.EnvironmentArgs;
import uk.ac.tgac.rampart.conan.conanx.env.arch.Architecture;

public interface Locality {

	
	/**
	 * Do anything necessary to establish a connection with the terminal, which will be used for executing a 
	 * RAMPART process.
	 * @return True if connection was established, otherwise false.
	 */
	boolean establishConnection();
	
	/**
     * Executes the supplied command using the supplied args, on the requested architecture at the locality indicated
     * by this object.
     */
    boolean submitCommand(String command, EnvironmentArgs args, Architecture architecture)
            throws IllegalArgumentException, ProcessExecutionException, InterruptedException, ConnectException;
	
	/**
	 * Disconnect from the terminal after use.
	 * @return true if disconnected successfully otherwise false
	 */
	boolean disconnect();
}
