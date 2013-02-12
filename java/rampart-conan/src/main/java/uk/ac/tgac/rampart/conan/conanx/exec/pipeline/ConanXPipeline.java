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
package uk.ac.tgac.rampart.conan.conanx.exec.pipeline;

import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.tgac.rampart.conan.conanx.exec.context.ExecutionContext;
import uk.ac.tgac.rampart.conan.conanx.exec.process.ConanXProcess;

import java.util.List;

/**
 * User: maplesod
 * Date: 24/01/13
 * Time: 11:57
 */
public interface ConanXPipeline extends ConanPipeline {

    /**
     * Retrieves all the {@link ConanXProcess}es to execute within this pipeline.
     *
     * @param executionContext The execution context within which to execute this pipeline.
     * @return A list of {@link ConanXProcess}es to execute.
     */
    List<ConanXProcess> getProcesses(ExecutionContext executionContext);
}
