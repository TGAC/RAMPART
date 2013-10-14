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
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.user.GuestUser;
import uk.ac.ebi.fgpt.conan.factory.DefaultTaskFactory;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.service.exception.TaskExecutionException;

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

    private AmpExecutor ampExecutor;

    public AmpProcess() {
        this(new AmpArgs());
    }

    public AmpProcess(AmpArgs args) {
        super("", args, new AmpParams());
        this.ampExecutor = new AmpExecutorImpl();
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

        // Short cut to arguments
        AmpArgs args = (AmpArgs)this.getProcessArgs();

        // Initialise object that makes system calls
        this.ampExecutor.initialise(this.conanProcessService, executionContext);

        // Create AMP Pipeline
        AmpPipeline ampPipeline = new AmpPipeline();
        ampPipeline.setConanProcessService(this.getConanProcessService());
        ampPipeline.configureProcesses();

        log.debug("Found " + ampPipeline.getProcesses().size() + " AMP stages in pipeline to process");

        // Make sure the output directory exists
        args.getAssembliesDir().mkdirs();

        // Create link for the input file
        this.ampExecutor.createInitialLink(args.getInputAssembly(), args.getAssembliesDir());

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

        // Process assemblies and generate stats for each stage after pipeline has completed
        try {
            this.ampExecutor.executeAnalysisJob(args);
        }
        catch(ProcessExecutionException pee) {
            // If an error occurs here it isn't critical so just log the error and continue
            log.error(pee.getMessage(), pee);
        }

        return true;
    }

}
