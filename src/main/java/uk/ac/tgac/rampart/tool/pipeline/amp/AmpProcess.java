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
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.user.GuestUser;
import uk.ac.ebi.fgpt.conan.factory.DefaultTaskFactory;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.service.exception.TaskExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;

import java.io.File;
import java.io.IOException;

/**
 * This class wraps a Pipeline to manage each AMP stage
 *
 * User: maplesod
 * Date: 12/02/13
 * Time: 16:30
 */
public class AmpProcess extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(AmpProcess.class);

    private AmpExecutor ampExecutor;

    public AmpProcess() {
        this(new AmpArgs());
    }

    public AmpProcess(ProcessArgs args) {
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

        if (!args.isStatsOnly()) {

            // Create AMP Pipeline
            AmpPipeline ampPipeline = new AmpPipeline(args, this.getConanProcessService());

            log.debug("Found " + ampPipeline.getProcesses().size() + " AMP stages in pipeline to process");

            // Make sure the output directory exists
            args.getAssembliesDir().mkdirs();

            // Create link for the input file
            this.conanProcessService.createLocalSymbolicLink(
                    args.getInputAssembly(),
                    new File(args.getAssembliesDir(), "amp-stage-0-scaffolds.fa"));

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
            }
            catch (TaskExecutionException e) {
                throw new ProcessExecutionException(-1, e);
            }
        }

        // Process assemblies and generate stats for each stage after pipeline has completed
        try {
            this.ampExecutor.executeAnalysisJob(args);
        }
        catch(IOException |CommandExecutionException e) {
            throw new ProcessExecutionException(-2, e);
        }

        return true;
    }

}
