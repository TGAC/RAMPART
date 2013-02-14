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
package uk.ac.tgac.rampart.pipeline.tool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.core.user.GuestUser;
import uk.ac.ebi.fgpt.conan.factory.ConanTaskFactory;
import uk.ac.ebi.fgpt.conan.factory.DefaultTaskFactory;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.service.exception.TaskExecutionException;
import uk.ac.tgac.rampart.pipeline.cli.RampartOptions;
import uk.ac.tgac.rampart.pipeline.tool.pipeline.rampart.RampartArgs;
import uk.ac.tgac.rampart.pipeline.tool.pipeline.rampart.RampartPipeline;
import uk.ac.tgac.rampart.pipeline.tool.proc.internal.util.clean.CleanJobArgs;
import uk.ac.tgac.rampart.pipeline.tool.proc.internal.util.clean.CleanJobProcess;

/**
 * User: maplesod
 * Date: 12/02/13
 * Time: 14:55
 */
@Component
public class Rampart {

    @Autowired
    private ConanProcessService conanProcessService;

    @Autowired
    private ExecutionContext executionContext;

    @Autowired
    private RampartPipeline rampartPipeline;

    private RampartOptions options;

    public Rampart() {
        this(null);
    }

    public Rampart(RampartOptions options) {
        this.options = options;

    }

    public RampartOptions getOptions() {
        return this.options;
    }

    public void setOptions(RampartOptions options) {
        this.options = options;
    }


    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    protected void cleanJob() throws ProcessExecutionException, InterruptedException {

        CleanJobArgs cleanJobArgs = new CleanJobArgs();
        cleanJobArgs.setJobDir(this.options.getClean());

        CleanJobProcess cleanJobProcess = new CleanJobProcess(cleanJobArgs);

        this.conanProcessService.execute(cleanJobProcess, new DefaultExecutionContext());
    }

    protected void startJob() throws InterruptedException, TaskExecutionException {

        // Create RAMPART Pipeline
//        RampartPipeline rampartPipeline = new RampartPipeline();

        RampartArgs args = new RampartArgs();
        args.setConfig(this.options.getConfig());
        args.setOutputDir(this.options.getOutput());

        // Create a guest user
        ConanUser rampartUser = new GuestUser("daniel.mapleson@tgac.ac.uk");

        // Create the RAMPART proc
        ConanTaskFactory conanTaskFactory = new DefaultTaskFactory();
        ConanTask<RampartPipeline> rampartTask = conanTaskFactory.createTask(
                rampartPipeline,
                0,
                args.getArgMap(),
                ConanTask.Priority.HIGHEST,
                rampartUser);
        rampartTask.setId("");
        rampartTask.submit();
        rampartTask.execute(this.executionContext);
    }

    public void process() throws ProcessExecutionException, InterruptedException, TaskExecutionException {

        if (this.options.getClean() != null) {
            cleanJob();
        }
        else {
            startJob();
        }

    }


}
