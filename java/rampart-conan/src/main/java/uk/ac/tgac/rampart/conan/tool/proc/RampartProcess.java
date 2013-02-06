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
package uk.ac.tgac.rampart.conan.tool.proc;

import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.rampart.conan.conanx.exec.context.ExecutionContext;

import java.io.IOException;

/**
 * User: maplesod
 * Date: 24/01/13
 * Time: 11:57
 */
public interface RampartProcess {

    /**
     * Dispatches this task to the specified environment
     * @param env The environment to dispatch jobs too
     * @throws IOException
     * @throws ProcessExecutionException
     * @throws InterruptedException
     */
    void execute(ExecutionContext env) throws IOException, ProcessExecutionException, InterruptedException, CommandExecutionException;
}
