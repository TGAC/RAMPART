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

import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.core.context.locality.Local;
import uk.ac.ebi.fgpt.conan.core.pipeline.AbstractConanPipeline;
import uk.ac.ebi.fgpt.conan.core.user.GuestUser;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.tgac.rampart.tool.process.amp.AmpStageArgs;
import uk.ac.tgac.rampart.tool.process.amp.AmpStageProcess;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 07/01/13
 * Time: 10:55
 * To change this template use File | Settings | File Templates.
 */
public class AmpPipeline extends AbstractConanPipeline {

    private static final String NAME = "AMP";
    private static final ConanUser USER = new GuestUser("rampart@tgac.ac.uk");


    private AmpParams params = new AmpParams();

    private AmpArgs args;

    public AmpPipeline(AmpArgs ampArgs, ConanProcessService conanProcessService, ExecutionContext executionContext) {

        super(NAME, USER, false, false, conanProcessService);

        this.args = ampArgs;
        this.args.setExecutionContext(executionContext);

        this.init();
    }

    public AmpArgs getArgs() {
        return args;
    }

    public void setArgs(AmpArgs args) {
        this.args = args;

        this.init();
    }


    public void init() {

        for(AmpStageArgs ampStageArgs : this.args.getStageArgsList()) {

            AmpStageProcess proc = new AmpStageProcess(ampStageArgs);
            proc.setConanProcessService(getConanProcessService());
            this.addProcess(proc);
        }

        // Check all processes in the pipeline are operational, modify execution context to execute unscheduled locally
        if (!this.isOperational(new DefaultExecutionContext(new Local(), null,
                this.args.getExecutionContext().getExternalProcessConfiguration()))) {
            throw new UnsupportedOperationException("AMP pipeline contains one or more processes that are not currently operational.  " +
                    "Please fix before restarting pipeline.");
        }
    }

}
