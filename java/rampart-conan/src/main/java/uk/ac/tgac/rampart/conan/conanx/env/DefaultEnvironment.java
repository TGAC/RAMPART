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

import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.env.locality.Local;
import uk.ac.tgac.rampart.conan.conanx.env.locality.Locality;
import uk.ac.tgac.rampart.conan.conanx.env.scheduler.Scheduler;

public class DefaultEnvironment implements Environment {

    private Locality locality;
    private Scheduler scheduler;

    public DefaultEnvironment() {
        this((Scheduler)null);
    }

    public DefaultEnvironment(Scheduler scheduler) {

        this(new Local(), scheduler);
    }

    public DefaultEnvironment(Locality locality, Scheduler scheduler) {

        this.locality = locality;
        this.scheduler = scheduler;
    }

    public DefaultEnvironment(Environment env) {

        this.locality = env.getLocality();
    }

    @Override
    public Locality getLocality() {
        return locality;
    }

    @Override
    public boolean usingScheduler() {
        return this.scheduler != null;
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    public void setLocality(Locality locality) {
        this.locality = locality;
    }

    @Override
    public void execute(String command)
            throws ProcessExecutionException, InterruptedException {
        if (!this.locality.establishConnection()) {
            throw new ProcessExecutionException(-1, "Could not establish connection to the terminal.  Command " +
                    command + " will not be submitted.");
        }

        this.locality.executeCommand(command, this.scheduler);

        if (!this.locality.disconnect()) {
            throw new ProcessExecutionException(-1, "Command was submitted but could not disconnect the terminal session.  Future jobs may not work.");
        }
    }

    @Override
    public Environment copy() {
        return new DefaultEnvironment(this);
    }

}
