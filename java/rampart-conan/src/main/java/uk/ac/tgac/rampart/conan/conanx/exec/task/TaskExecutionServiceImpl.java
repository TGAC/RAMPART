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
package uk.ac.tgac.rampart.conan.conanx.exec.task;

import org.springframework.stereotype.Service;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.exec.context.ExecutionContext;
import uk.ac.tgac.rampart.conan.conanx.exec.context.WaitCondition;
import uk.ac.tgac.rampart.conan.conanx.exec.context.locality.Locality;
import uk.ac.tgac.rampart.conan.conanx.exec.context.scheduler.Scheduler;
import uk.ac.tgac.rampart.conan.conanx.exec.task.ConanExternalTask;
import uk.ac.tgac.rampart.conan.conanx.exec.task.monitor.TaskAdapter;
import uk.ac.tgac.rampart.conan.conanx.exec.task.TaskExecutionService;

@Service
public class TaskExecutionServiceImpl implements TaskExecutionService {

	@Override
    public int execute(ConanExternalTask process, ExecutionContext executionContext)
            throws InterruptedException, ProcessExecutionException {

        return this.execute(process.getFullCommand(), executionContext);
    }

    @Override
    public int execute(String command, ExecutionContext executionContext) throws InterruptedException, ProcessExecutionException {

        Locality locality = executionContext.getLocality();

        if (!locality.establishConnection()) {
            throw new ProcessExecutionException(-1, "Could not establish connection to the terminal.  Command " +
                    command + " will not be submitted.");
        }

        int exitCode = -1;

        if (executionContext.usingScheduler()) {

            Scheduler scheduler = executionContext.getScheduler();

            String commandToExecute = scheduler.createCommand(command);

            if (executionContext.isForegroundJob()) {

                exitCode = locality.monitoredExecute(commandToExecute, scheduler.createTaskAdapter());
            }
            else {
                locality.dispatch(command);
                exitCode = 0; // Doesn't return an exit code, so if there were no exceptions assume everything went well
            }
        }
        else {

            if (executionContext.isForegroundJob()) {
                locality.execute(command);
            }
            else {
                throw new UnsupportedOperationException("Can't dispatch simple commands yet");
            }
        }


        if (!locality.disconnect()) {
            throw new ProcessExecutionException(-1, "Command was submitted but could not disconnect the terminal session.  Future jobs may not work.");
        }

        return exitCode;
    }


    @Override
    public int waitFor(WaitCondition waitCondition, ExecutionContext executionContext) throws InterruptedException, ProcessExecutionException {

        if (!executionContext.usingScheduler()) {
            throw new UnsupportedOperationException("Can't wait for non-scheduled tasks yet");
        }

        Scheduler scheduler = executionContext.getScheduler();

        String waitCommand = scheduler.createWaitCommand(waitCondition);

        TaskAdapter taskAdapter = scheduler.createTaskAdapter();

        return executionContext.getLocality().monitoredExecute(waitCommand, taskAdapter);
    }

}
