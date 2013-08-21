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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.user.GuestUser;
import uk.ac.ebi.fgpt.conan.factory.DefaultTaskFactory;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.SchedulerArgs;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.service.exception.TaskExecutionException;
import uk.ac.tgac.conan.process.asm.stats.AscV10Args;
import uk.ac.tgac.conan.process.asm.stats.AscV10Process;

import java.io.File;

/**
 * This class wraps a Pipeline to manage each AMP stage
 *
 * User: maplesod
 * Date: 12/02/13
 * Time: 16:30
 */
@Component
public class AmpProcess extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(AmpProcess.class);

    public AmpProcess() {
        this(new AmpArgs());
    }

    public AmpProcess(AmpArgs args) {
        super("", args, new AmpParams());
    }


    @Override
    public String getName() {
        return "AMP (Assembly iMProver)";
    }

    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public boolean execute(ExecutionContext executionContext) throws InterruptedException, ProcessExecutionException {

        AmpArgs args = (AmpArgs)this.getProcessArgs();

        // Create AMP Pipeline
        AmpPipeline ampPipeline = new AmpPipeline();
        ampPipeline.setConanProcessService(this.getConanProcessService());
        ampPipeline.configureProcesses();

        // Make sure the output directory exists
        args.getAssembliesDir().mkdirs();

        // Create link for the input file
        this.createInitialLink(args.getInputAssembly(), args.getAssembliesDir(), executionContext);

        // Create a guest user
        ConanUser rampartUser = new GuestUser("daniel.mapleson@tgac.ac.uk");

        // Create the AMP task
        ConanTask<AmpPipeline> ampTask = new DefaultTaskFactory().createTask(
                ampPipeline,
                0,
                ampPipeline.getArgs().getArgMap(),
                ConanTask.Priority.HIGHEST,
                rampartUser);

        ampTask.setId("AMP");
        ampTask.submit();

        // Run the AMP pipeline
        try {
            ampTask.execute(executionContext);
        } catch (TaskExecutionException e) {
            throw new ProcessExecutionException(-1, e);
        }

        // Process assemblies and generate stats after pipeline has completed
        this.dispatchStatsJob(executionContext);

        return true;
    }

    private void dispatchStatsJob(ExecutionContext executionContext) throws InterruptedException, ProcessExecutionException {

        ExecutionContext executionContextCopy = executionContext.copy();


        AmpArgs args = (AmpArgs)this.getProcessArgs();
        // I think it's safe to assume we have at least one lib otherwise we wouldn't have got this far.

        String jobName = args.getJobPrefix() + "-analyser";

        if (executionContextCopy.usingScheduler()) {
            SchedulerArgs schedulerArgs = executionContextCopy.getScheduler().getArgs();

            schedulerArgs.setJobName(jobName);
            schedulerArgs.setThreads(1);
            schedulerArgs.setMemoryMB(0);
            schedulerArgs.setMonitorFile(new File(args.getAssembliesDir(), jobName + ".log"));

            executionContextCopy.setForegroundJob(true);
        }

        AscV10Args ascArgs = new AscV10Args();
        ascArgs.setInput(args.getAssembliesDir());
        ascArgs.setOutput(args.getAssembliesDir());
        ascArgs.setMode("FULL");

        AscV10Process ascProcess = new AscV10Process(ascArgs);

        try {
            this.conanProcessService.execute(ascProcess, executionContextCopy);
        }
        catch(ProcessExecutionException pee) {
            // If an error occurs here it isn't critical so just log the error and continue
            log.error(pee.getMessage(), pee);
        }
    }

    /**
     * Creates the initial symbolic link between the input assembly and the output directory.  This is basically a
     * convienience for the user and helps to compare assemblies at each stage later.
     * @param inputFile
     * @param outputDir
     * @param executionContext
     * @throws ProcessExecutionException
     * @throws InterruptedException
     */
    protected void createInitialLink(File inputFile, File outputDir, ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {
        String linkCommand = this.makeLinkCommand(inputFile, new File(outputDir, "amp-stage-0-scaffolds.fa"));

        ExecutionContext linkingExecutionContext = new DefaultExecutionContext(executionContext.getLocality(), null, null, true);

        this.conanProcessService.execute(linkCommand, linkingExecutionContext);
    }

    protected String makeLinkCommand(File source, File target) {
        return "ln -s -f " + source.getAbsolutePath() + " " + target.getAbsolutePath();
    }
}
