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
package uk.ac.tgac.rampart.pipeline.tool.process.mass.single;

import org.springframework.stereotype.Service;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

/**
 * User: maplesod
 * Date: 25/03/13
 * Time: 11:10
 */
@Service
public class SingleMassExecutorImpl implements SingleMassExecutor {

    @Override
    public void executeSingleMass(SingleMassArgs singleMassArgs, ConanProcessService conanProcessService, ExecutionContext executionContext)
            throws InterruptedException, ProcessExecutionException {

        SingleMassProcess singleMassProcess = new SingleMassProcess(singleMassArgs);
        singleMassProcess.setConanProcessService(conanProcessService);
        singleMassProcess.execute(executionContext);
    }
}
