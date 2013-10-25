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
package uk.ac.tgac.rampart.tool.process.mass.single;

import uk.ac.ebi.fgpt.conan.core.param.DefaultConanParameter;
import uk.ac.ebi.fgpt.conan.core.param.PathParameter;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.tgac.rampart.tool.process.mass.MassParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: maplesod
 * Date: 11/01/13
 * Time: 13:23
 */
public class SingleMassParams extends MassParams {

    private ConanParameter config;
    private ConanParameter jobName;
    private ConanParameter outputDir;
    private ConanParameter jobPrefix;

    public SingleMassParams() {

        this.config = new PathParameter(
                "config",
                "The rampart configuration file containing the libraries to assemble",
                true);

        this.jobName = new DefaultConanParameter(
                "job_name",
                "The job name that distinguishes this MASS run from other mass runs that might be running in parallel.",
                false,
                true,
                false);

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

    public ConanParameter getJobName() {
        return jobName;
    }

    public ConanParameter getOutputDir() {
        return outputDir;
    }

    public ConanParameter getJobPrefix() {
        return jobPrefix;
    }

    @Override
    public List<ConanParameter> getConanParameters() {

        return new ArrayList<>(Arrays.asList(
                new ConanParameter[]{
                        this.config,
                        this.jobName,
                        this.outputDir,
                        this.jobPrefix}));
    }

}
