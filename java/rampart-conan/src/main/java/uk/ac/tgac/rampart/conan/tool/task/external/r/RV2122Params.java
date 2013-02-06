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
import uk.ac.tgac.rampart.conan.conanx.param.DefaultConanParameter;
import uk.ac.tgac.rampart.conan.conanx.param.PathParameter;
import uk.ac.tgac.rampart.conan.conanx.param.ToolParams;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RV2122Params implements ToolParams {

    private ConanParameter args;
    private ConanParameter script;
    private ConanParameter output;

    public RV2122Params() {

        this.args = new DefaultConanParameter(
                "args",
                "Any arguments that should be provided to the script",
                false,
                false,
                false);

        this.script = new PathParameter(
                "script",
                "The R script to execute",
                false);

        this.output = new PathParameter(
                "output",
                "The location to store output from R",
                false);
    }

    public ConanParameter getArgs() {
        return args;
    }

    public ConanParameter getScript() {
        return script;
    }

    public ConanParameter getOutput() {
        return output;
    }

    @Override
    public Set<ConanParameter> getConanParameters() {
        return new HashSet<ConanParameter>(Arrays.asList(
                new ConanParameter[]{
                        this.args,
                        this.script,
                        this.output
                }
        ));
    }
}
