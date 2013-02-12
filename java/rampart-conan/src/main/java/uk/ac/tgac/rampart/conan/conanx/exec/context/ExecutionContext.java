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
package uk.ac.tgac.rampart.conan.conanx.exec.context;

import uk.ac.tgac.rampart.conan.conanx.exec.context.locality.Local;
import uk.ac.tgac.rampart.conan.conanx.exec.context.locality.Locality;
import uk.ac.tgac.rampart.conan.conanx.exec.context.scheduler.Scheduler;

public class ExecutionContext {

    private Locality locality;
    private Scheduler scheduler;
    private boolean foregroundJob;

    public ExecutionContext() {
        this(true);
    }

    public ExecutionContext(boolean foregroundJob) {
        this(null, foregroundJob);
    }

    public ExecutionContext(Scheduler scheduler, boolean foregroundJob) {

        this(new Local(), scheduler, foregroundJob);
    }

    public ExecutionContext(Locality locality, Scheduler scheduler, boolean foregroundJob) {

        this.locality = locality;
        this.scheduler = scheduler;
        this.foregroundJob = foregroundJob;
    }

    public ExecutionContext(ExecutionContext copy) {

        this.locality = copy.getLocality().copy();
        this.scheduler = copy.usingScheduler() ? copy.getScheduler().copy() : null;
        this.foregroundJob = copy.isForegroundJob();
    }

    public Locality getLocality() {
        return locality;
    }

    public boolean usingScheduler() {
        return this.scheduler != null;
    }

    public Scheduler getScheduler() {
        return this.scheduler;
    }

    public boolean isForegroundJob() {
        return foregroundJob;
    }

    public void setForegroundJob(boolean foregroundJob) {
        this.foregroundJob = foregroundJob;
    }

    public void setLocality(Locality locality) {
        this.locality = locality;
    }

}
