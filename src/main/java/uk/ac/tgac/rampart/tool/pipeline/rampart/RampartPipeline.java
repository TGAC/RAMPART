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
package uk.ac.tgac.rampart.tool.pipeline.rampart;

import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.core.context.locality.Local;
import uk.ac.ebi.fgpt.conan.core.pipeline.AbstractConanPipeline;
import uk.ac.ebi.fgpt.conan.core.user.GuestUser;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 07/01/13
 * Time: 10:48
 * To change this template use File | Settings | File Templates.
 */
public class RampartPipeline extends AbstractConanPipeline {

    private RampartArgs args;
    private ConanExecutorService conanExecutorService;

    private static final String NAME = "rampart-pipeline";
    private static final ConanUser USER = new GuestUser("rampart@tgac.ac.uk");

    public RampartPipeline(RampartArgs args, ConanExecutorService ces) throws IOException {

        super(NAME, USER, false, false, ces.getConanProcessService());


        this.conanExecutorService = ces;
        this.args = args;

        this.init();
    }

    public RampartArgs getArgs() {
        return args;
    }

    public void setArgs(RampartArgs args) throws IOException {
        this.args = args;

        this.init();
    }


    public void init() throws IOException {

        // Configure pipeline
        this.clearProcessList();

        // Create all the processes
        this.addProcesses(this.args.getStages().createProcesses(this.conanExecutorService));

        // Check all processes in the pipeline are operational, modify execution context to execute unscheduled locally
        if (this.args.isDoInitialChecks() && !this.isOperational(new DefaultExecutionContext(new Local(), null,
                this.args.getExecutionContext().getExternalProcessConfiguration()))) {
            throw new IOException("RAMPART pipeline contains one or more processes that are not currently operational.  " +
                    "Please fix before restarting pipeline.");
        }
    }

}
