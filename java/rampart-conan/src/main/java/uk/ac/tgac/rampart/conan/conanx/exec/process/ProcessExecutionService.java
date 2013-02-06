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
package uk.ac.tgac.rampart.conan.conanx.exec.process;

import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.rampart.conan.conanx.exec.context.ExecutionContext;
import uk.ac.tgac.rampart.conan.tool.proc.RampartProcess;

import java.io.IOException;

public interface ProcessExecutionService {

    /**
     * Execute a defined {@link RampartProcess}.  The task may be executed in the foreground or the background depending
     * on how the {@link uk.ac.tgac.rampart.conan.conanx.exec.context.ExecutionContext} is configured.
     * {@link uk.ac.tgac.rampart.conan.conanx.exec.context.ExecutionContext} and waits for it to complete before returning.
     *
     * @param conanProcess The {@link uk.ac.ebi.fgpt.conan.model.ConanProcess} to execute
     * @param executionContext The {@link uk.ac.tgac.rampart.conan.conanx.exec.context.ExecutionContext} within which to execute the {@link uk.ac.tgac.rampart.conan.conanx.exec.task.ConanExternalTask}
     * @throws InterruptedException Thrown if the executed task was interrupted during the job
     * @throws uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException Thrown if there were any problems initialising the job or with the job output
     */
    int execute(RampartProcess conanProcess, ExecutionContext executionContext)
            throws InterruptedException, ProcessExecutionException, IOException, CommandExecutionException;

}
