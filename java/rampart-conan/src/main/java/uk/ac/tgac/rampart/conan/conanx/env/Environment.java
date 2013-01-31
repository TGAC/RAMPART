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
package uk.ac.tgac.rampart.conan.conanx.env;

import java.io.IOException;

import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.rampart.conan.conanx.env.scheduler.Scheduler;
import uk.ac.tgac.rampart.conan.conanx.env.scheduler.SchedulerArgs;
import uk.ac.tgac.rampart.conan.conanx.env.locality.Locality;

public interface Environment {

    /**
     * Retrieves the locality that this process should be executed in
     * @return
     */
    Locality getLocality();

    /**
     * Whether or not a scheduling service is present in this environment
     * @return
     */
    boolean usingScheduler();

    /**
     * Retrieves the scheduling service, or null if none are used
     * @return
     */
    Scheduler getScheduler();

    /**
     * Executes a command in this environment
     * @param command The command to execute
     * @throws ProcessExecutionException Thrown if there was some problem executing the process
     * @throws InterruptedException Thrown if the process was interrupted after it started executing
     */
	void execute(String command) throws ProcessExecutionException, InterruptedException;

    /**
     * Make a deep copy of this environment
     * @return
     */
    Environment copy();
}
