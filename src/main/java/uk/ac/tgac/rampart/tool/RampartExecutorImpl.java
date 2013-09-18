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
package uk.ac.tgac.rampart.tool;

import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.model.context.Scheduler;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.io.File;
import java.util.List;

/**
 * User: maplesod
 * Date: 22/08/13
 * Time: 15:16
 */
public class RampartExecutorImpl implements RampartExecutor {


    protected ConanProcessService conanProcessService;
    protected ExecutionContext executionContext;

    @Override
    public void initialise(ConanProcessService conanProcessService, ExecutionContext executionContext) {
        this.conanProcessService = conanProcessService;
        this.executionContext = executionContext;
    }


    @Override
    public String makeLinkCommand(File inputFile, File outputFile) {

        return "ln -s -f " + inputFile.getAbsolutePath() + " " + outputFile.getAbsolutePath();
    }

    /**
     * Creates a symbolic link between the two provided files now! (i.e. we ignore any scheduling information in the
     * execution context)
     * @param inputFile
     * @param outputFile
     */
    @Override
    public void createLink(File inputFile, File outputFile)
            throws ProcessExecutionException, InterruptedException {

        ExecutionContext linkingExecutionContext = new DefaultExecutionContext(executionContext.getLocality(), null, null, true);

        this.conanProcessService.execute(makeLinkCommand(inputFile, outputFile), linkingExecutionContext);
    }


    @Override
    public void executeScheduledWait(List<Integer> jobIds, String waitCondition, ExitStatus.Type exitStatusType, String jobName, File outputDir)
            throws ProcessExecutionException, InterruptedException {

        if (!executionContext.usingScheduler())
            throw new UnsupportedOperationException("Cannot dispatch a scheduled wait job without using a scheduler");

        // Duplicate the execution context so we don't modify the original accidentally.
        ExecutionContext executionContextCopy = executionContext.copy();

        Scheduler scheduler = executionContextCopy.getScheduler();

        scheduler.getArgs().setJobName(jobName);
        scheduler.getArgs().setMonitorFile(new File(outputDir, jobName + ".log"));
        executionContextCopy.setForegroundJob(true);

        String condition = scheduler.generatesJobIdFromOutput() ?
                scheduler.createWaitCondition(exitStatusType, jobIds) :
                scheduler.createWaitCondition(exitStatusType, waitCondition);

        this.conanProcessService.waitFor(condition, executionContextCopy);
    }
}
