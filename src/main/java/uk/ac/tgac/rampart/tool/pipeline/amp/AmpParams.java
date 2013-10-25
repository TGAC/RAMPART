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
package uk.ac.tgac.rampart.tool.pipeline.amp;

import uk.ac.ebi.fgpt.conan.core.param.DefaultConanParameter;
import uk.ac.ebi.fgpt.conan.core.param.PathParameter;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: maplesod
 * Date: 12/02/13
 * Time: 16:56
 */
public class AmpParams implements ProcessParams {

    private ConanParameter inputAssembly;
    private ConanParameter outputDir;
    private ConanParameter processes;
    private ConanParameter jobPrefix;

    public AmpParams() {

        this.inputAssembly = new PathParameter(
                "input",
                "The input assembly containing the assembly to enhance",
                true);

        this.outputDir = new PathParameter(
                "output",
                "The output directory which should contain the enhancement steps",
                true);

        this.processes = new DefaultConanParameter(
                "processes",
                "The processes to execute to enhance the assembly",
                false,
                true,
                false);

        this.jobPrefix = new DefaultConanParameter(
                "jobPrefix",
                "Describes the jobs that will be executed as part of this pipeline",
                false,
                true,
                false);
    }

    public ConanParameter getInputAssembly() {
        return inputAssembly;
    }

    public ConanParameter getOutputDir() {
        return outputDir;
    }

    public ConanParameter getProcesses() {
        return processes;
    }

    public ConanParameter getJobPrefix() {
        return jobPrefix;
    }

    @Override
    public List<ConanParameter> getConanParameters() {
        return new ArrayList<>(Arrays.asList(
                new ConanParameter[]{
                        this.inputAssembly,
                        this.outputDir,
                        this.processes,
                        this.jobPrefix}));
    }
}
