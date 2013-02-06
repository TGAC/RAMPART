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
package uk.ac.tgac.rampart.conan.conanx.exec.context.scheduler.pbs;

import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.exec.context.scheduler.AbstractScheduler;
import uk.ac.tgac.rampart.conan.conanx.exec.context.scheduler.ExitStatusType;
import uk.ac.tgac.rampart.conan.conanx.exec.context.WaitCondition;
import uk.ac.tgac.rampart.conan.conanx.exec.task.monitor.TaskAdapter;

import java.io.File;

public class PBS extends AbstractScheduler {

    public static final String QSUB = "qsub";

    public PBS() {
        this(new PBSArgs());
    }

    public PBS(PBSArgs args) {
        super(QSUB, args);
    }

    @Override
    public TaskAdapter createTaskAdapter(File monitorFile, int monitorInterval) {
        return null;
    }

    @Override
    public String createCommand(String internalCommand) {
        return internalCommand;
    }

    @Override
    public String createWaitCommand(WaitCondition waitCondition) {
        return null;
    }

    @Override
    public String createKillCommand(String jobId) {
        return "qdel " + jobId;
    }

    @Override
    public WaitCondition createWaitCondition(ExitStatusType exitStatus, String condition) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
