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
package uk.ac.tgac.rampart.pipeline.tool.pipeline.amp;

import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.user.GuestUser;
import uk.ac.ebi.fgpt.conan.factory.ConanTaskFactory;
import uk.ac.ebi.fgpt.conan.factory.DefaultTaskFactory;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.service.exception.TaskExecutionException;
import uk.ac.tgac.rampart.conan.process.SimpleIOProcess;

import java.util.List;

/**
 * This class wraps a Pipeline to manage each AMP stage
 *
 * User: maplesod
 * Date: 12/02/13
 * Time: 16:30
 */
public class AmpProcess extends AbstractConanProcess {

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

        // This may have been done already but lets make sure that all the processes link together properly
        args.linkProcesses();

        // Create AMP Pipeline
        AmpPipeline ampPipeline = new AmpPipeline(args);

        // Create a guest user
        ConanUser rampartUser = new GuestUser("daniel.mapleson@tgac.ac.uk");

        // Create the RAMPART process
        ConanTaskFactory conanTaskFactory = new DefaultTaskFactory();

        ConanTask<AmpPipeline> ampTask = conanTaskFactory.createTask(
                ampPipeline,
                0,
                null,
                ConanTask.Priority.HIGHEST,
                rampartUser);

        ampTask.setId("");
        ampTask.submit();

        try {
            ampTask.execute();
        } catch (TaskExecutionException e) {
            throw new ProcessExecutionException(-1, e);
        }

        return true;
    }
}
