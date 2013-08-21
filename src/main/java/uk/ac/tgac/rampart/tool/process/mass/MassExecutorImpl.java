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
package uk.ac.tgac.rampart.tool.process.mass;

import org.springframework.stereotype.Service;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.tool.process.mass.selector.MassSelectorArgs;
import uk.ac.tgac.rampart.tool.process.mass.selector.MassSelectorProcess;
import uk.ac.tgac.rampart.tool.process.mass.single.SingleMassArgs;
import uk.ac.tgac.rampart.tool.process.mass.single.SingleMassProcess;

import java.io.File;

/**
 * User: maplesod
 * Date: 25/03/13
 * Time: 11:10
 */
@Service
public class MassExecutorImpl implements MassExecutor {

    private ConanProcessService conanProcessService;

    @Override
    public void executeSingleMass(SingleMassArgs singleMassArgs, ConanProcessService conanProcessService, ExecutionContext executionContext)
            throws InterruptedException, ProcessExecutionException {

        SingleMassProcess singleMassProcess = new SingleMassProcess(singleMassArgs);
        singleMassProcess.setConanProcessService(conanProcessService);
        singleMassProcess.execute(executionContext);

        this.conanProcessService = conanProcessService;
    }


    @Override
    public void executeScheduledWait(String jobPrefix, File outputDir, ExecutionContext executionContext)
            throws ProcessExecutionException, InterruptedException {

        // Duplicate the execution context so we don't modify the original accidentally.
        ExecutionContext executionContextCopy = executionContext.copy();

        if (executionContext.usingScheduler()) {

            String jobName = jobPrefix + "_wait";
            executionContextCopy.getScheduler().getArgs().setJobName(jobName);
            executionContextCopy.getScheduler().getArgs().setMonitorFile(new File(outputDir, jobName + ".log"));
            executionContextCopy.setForegroundJob(true);
        }

        this.conanProcessService.waitFor(
                executionContextCopy.getScheduler().createWaitCondition(ExitStatus.Type.COMPLETED_SUCCESS, jobPrefix + "*"),
                executionContextCopy);
    }

    @Override
    public void executeMassSelector(MassSelectorArgs massSelectorArgs, ConanProcessService conanProcessService, ExecutionContext executionContext)
            throws InterruptedException, ProcessExecutionException {

        MassSelectorProcess massSelectorProcess = new MassSelectorProcess(massSelectorArgs);
        massSelectorProcess.setConanProcessService(conanProcessService);
        massSelectorProcess.execute(executionContext);

        this.conanProcessService = conanProcessService;
    }
}
