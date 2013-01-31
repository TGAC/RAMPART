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

import java.net.ConnectException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.env.scheduler.Scheduler;
import uk.ac.tgac.rampart.conan.conanx.env.scheduler.SchedulerArgs;
import uk.ac.tgac.rampart.conan.conanx.env.locality.Locality;

/**
 * This environment will execute all internal aspects of the job but will not
 * dispatch any command for submission outside the JVM. This is a useful
 * environment for sanity testing processes to see if commands are being generated
 * correctly.
 * 
 * @author maplesod
 */
public class NullEnvironment implements Environment {

	Logger log = LoggerFactory.getLogger(NullEnvironment.class);
	
    @Override
    public void execute(String command) throws IllegalArgumentException,
            ProcessExecutionException, InterruptedException {

        // Do not execute anything... just log the request.
        log.info("Command will not be executed on a Null Environment: {0}.", command);
    }

    @Override
    public Environment copy() {
        return null;
    }

    @Override
    public Locality getLocality() {
        return null;
    }

    @Override
    public boolean usingScheduler() {
        return false;
    }

    @Override
    public Scheduler getScheduler() {
        return null;
    }


}
