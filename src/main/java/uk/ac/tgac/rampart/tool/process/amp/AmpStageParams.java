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
package uk.ac.tgac.rampart.tool.process.amp;

import uk.ac.ebi.fgpt.conan.core.param.DefaultConanParameter;
import uk.ac.ebi.fgpt.conan.core.param.NumericParameter;
import uk.ac.ebi.fgpt.conan.core.param.PathParameter;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: maplesod
 * Date: 16/08/13
 * Time: 15:55
 */
public class AmpStageParams implements ProcessParams {

    private ConanParameter input;
    private ConanParameter outputDir;
    private ConanParameter jobPrefix;
    private ConanParameter threads;
    private ConanParameter memory;
    private ConanParameter otherArgs;

    public AmpStageParams() {

        this.input = new PathParameter(
                "input",
                "The input assembly containing the assembly to enhance",
                true
        );

        this.outputDir = new PathParameter(
                "output",
                "The output directory which should contain the enhancement steps",
                true
        );

        this.jobPrefix = new DefaultConanParameter(
                "job_prefix",
                "Describes the jobs that will be executed as part of this pipeline",
                false,
                true,
                false
        );

        this.threads = new NumericParameter(
                "threads",
                "The number of threads to use for this AMP stage",
                true
        );

        this.memory = new NumericParameter(
                "memory",
                "The amount of memory to request for this AMP stage",
                true
        );

        this.otherArgs = new DefaultConanParameter(
                "other_args",
                "Any additional arguments to provide to this specific process",
                false,
                true,
                false
        );
    }

    public ConanParameter getInput() {
        return input;
    }

    public ConanParameter getOutputDir() {
        return outputDir;
    }

    public ConanParameter getJobPrefix() {
        return jobPrefix;
    }

    public ConanParameter getThreads() {
        return threads;
    }

    public ConanParameter getMemory() {
        return memory;
    }

    public ConanParameter getOtherArgs() {
        return otherArgs;
    }

    @Override
    public List<ConanParameter> getConanParameters() {
        return new ArrayList<>(Arrays.asList(
                new ConanParameter[]{
                        this.input,
                        this.outputDir,
                        this.jobPrefix,
                        this.threads,
                        this.memory,
                        this.otherArgs
                }));
    }
}
