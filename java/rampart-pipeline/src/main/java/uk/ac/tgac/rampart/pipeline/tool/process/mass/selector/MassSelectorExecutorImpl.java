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
package uk.ac.tgac.rampart.pipeline.tool.process.mass.selector;

import org.springframework.stereotype.Service;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

/**
 * User: maplesod
 * Date: 20/03/13
 * Time: 12:09
 */
@Service
public class MassSelectorExecutorImpl implements MassSelectorExecutor {


    @Override
    public void executeMassSelector(MassSelectorArgs massSelectorArgs, ConanProcessService conanProcessService, ExecutionContext executionContext)
            throws InterruptedException, ProcessExecutionException {

        MassSelectorProcess massSelectorProcess = new MassSelectorProcess(massSelectorArgs);
        massSelectorProcess.setConanProcessService(conanProcessService);
        massSelectorProcess.execute(executionContext);
    }
}
