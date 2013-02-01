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
package uk.ac.tgac.rampart.conan.tool.module.mass.single;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.parameter.DefaultConanParameter;
import uk.ac.tgac.rampart.conan.conanx.parameter.PathParameter;
import uk.ac.tgac.rampart.conan.tool.module.mass.MassParams;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * User: maplesod
 * Date: 11/01/13
 * Time: 13:23
 */
public class SingleMassParams extends MassParams {

    private ConanParameter config;
    private ConanParameter outputDir;
    private ConanParameter jobPrefix;

    public SingleMassParams() {

        this.config = new PathParameter(
                "config",
                "The rampart configuration file containing the libraries to assemble",
                true);

        this.outputDir = new PathParameter(
                "output",
                "The output directory",
                true);

        this.jobPrefix = new DefaultConanParameter(
                "job_prefix",
                "The job_prefix to be assigned to all sub processes in MASS.  Useful if executing with a scheduler.",
                false,
                true,
                false);
    }

    public ConanParameter getConfig() {
        return config;
    }

    public ConanParameter getOutputDir() {
        return outputDir;
    }

    public ConanParameter getJobPrefix() {
        return jobPrefix;
    }

    @Override
    public Set<ConanParameter> getConanParameters() {

        return new HashSet<ConanParameter>(Arrays.asList(
                new ConanParameter[]{
                        this.config,
                        this.outputDir,
                        this.jobPrefix}));
    }

}
