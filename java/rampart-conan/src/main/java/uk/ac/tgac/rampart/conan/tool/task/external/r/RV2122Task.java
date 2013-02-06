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
package uk.ac.tgac.rampart.conan.tool.task.external.r;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.exec.task.AbstractConanExternalTask;

import java.util.Collection;
import java.util.Map;

/**
 * User: maplesod
 * Date: 16/01/13
 * Time: 16:13
 */
public class RV2122Task extends AbstractConanExternalTask {

    public static final String EXE = "Rscript";

    private RV2122Args args;

    public RV2122Task() {

        this(new RV2122Args());
    }

    public RV2122Task(RV2122Args args) {

        super(EXE);
        this.args = args;
    }


    @Override
    public String getCommand() {
        return this.getCommand(args, false);
    }

    @Override
    public String getName() {
        return "R V2.12.2";
    }

    @Override
    public Collection<ConanParameter> getParameters() {
        return new RV2122Params().getConanParameters();
    }
}
