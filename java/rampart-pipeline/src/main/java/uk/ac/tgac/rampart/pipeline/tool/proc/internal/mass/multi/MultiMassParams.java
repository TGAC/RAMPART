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
package uk.ac.tgac.rampart.pipeline.tool.proc.internal.mass.multi;

import uk.ac.ebi.fgpt.conan.core.param.DefaultConanParameter;
import uk.ac.ebi.fgpt.conan.core.param.FlagParameter;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.tgac.rampart.pipeline.tool.proc.internal.mass.MassParams;

import java.util.Arrays;
import java.util.List;

/**
 * User: maplesod
 * Date: 11/01/13
 * Time: 13:23
 */
public class MultiMassParams extends MassParams {

    private ConanParameter configs;
    private ConanParameter parallelMass;

    public MultiMassParams() {

        this.configs = new DefaultConanParameter(
                "configs",
                "The mass configuration files to assemble",
                false,
                true,
                false);


        this.parallelMass = new FlagParameter(
                "parallel",
                "Whether or not to execute the RAW and QT MASS processes in parallel, assuming that we are executing the job using a scheduling system.  Default: TRUE");
    }

    public ConanParameter getConfigs() {
        return configs;
    }

    public ConanParameter getParallelMass() {
        return parallelMass;
    }

    @Override
    public List<ConanParameter> getConanParameters() {

        List<ConanParameter> params = super.getConanParameters();

        params.addAll(Arrays.asList(
                this.configs,
                this.parallelMass));

        return params;
    }

}
