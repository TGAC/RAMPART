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
package uk.ac.tgac.rampart.tool.process.mecq;

import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.process.ec.AbstractErrorCorrector;
import uk.ac.tgac.conan.process.ec.AbstractErrorCorrectorArgs;
import uk.ac.tgac.rampart.tool.RampartExecutor;

import java.io.File;

/**
 * User: maplesod
 * Date: 20/03/13
 * Time: 15:44
 */
public interface MecqExecutor extends RampartExecutor {

    ExecutionResult executeEcq(AbstractErrorCorrector errorCorrector, File outputDir, String jobName, boolean runInParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException;

    void createInputLinks(Library library, AbstractErrorCorrectorArgs args)
            throws ProcessExecutionException, InterruptedException;

    void createOutputLinks(File outputDir, AbstractErrorCorrector ec, EcqArgs ecqArgs, Library libraries)
            throws ProcessExecutionException, InterruptedException;
}
