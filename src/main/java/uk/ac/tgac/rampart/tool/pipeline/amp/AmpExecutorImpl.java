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
package uk.ac.tgac.rampart.tool.pipeline.amp;

import org.springframework.stereotype.Service;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.SchedulerArgs;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.tool.RampartExecutorImpl;

import java.io.File;

/**
 * User: maplesod
 * Date: 25/03/13
 * Time: 11:10
 */
@Service
public class AmpExecutorImpl extends RampartExecutorImpl implements AmpExecutor {

    @Override
    public void executeAnalysisJob(AmpArgs ampArgs)
            throws InterruptedException, ProcessExecutionException {

        ExecutionContext executionContextCopy = executionContext.copy();


        // I think it's safe to assume we have at least one lib otherwise we wouldn't have got this far.
        String jobName = ampArgs.getJobPrefix() + "-analyser";

        if (executionContextCopy.usingScheduler()) {
            SchedulerArgs schedulerArgs = executionContextCopy.getScheduler().getArgs();

            schedulerArgs.setJobName(jobName);
            schedulerArgs.setThreads(1);
            schedulerArgs.setMemoryMB(0);
            schedulerArgs.setMonitorFile(new File(ampArgs.getAssembliesDir(), jobName + ".log"));

            executionContextCopy.setForegroundJob(true);
        }

        // TODO Need to fix this... can't use ASC any longer.

        /*AscV10Args ascArgs = new AscV10Args();
        ascArgs.setInput(ampArgs.getAssembliesDir());
        ascArgs.setOutput(ampArgs.getAssembliesDir());
        ascArgs.setMode("FULL");

        AscV10Process ascProcess = new AscV10Process(ascArgs);

        this.conanProcessService.execute(ascProcess, executionContextCopy);*/

    }

    @Override
    public void createInitialLink(File sourceFile, File outputDir)
            throws InterruptedException, ProcessExecutionException {

        String linkCommand = this.makeLinkCommand(sourceFile, new File(outputDir, "amp-stage-0-scaffolds.fa"));

        ExecutionContext linkingExecutionContext = new DefaultExecutionContext(executionContext.getLocality(), null, null, true);

        this.conanProcessService.execute(linkCommand, linkingExecutionContext);
    }

}
