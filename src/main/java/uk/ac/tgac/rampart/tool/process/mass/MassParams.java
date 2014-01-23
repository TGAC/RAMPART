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
package uk.ac.tgac.rampart.tool.process.mass;

import uk.ac.ebi.fgpt.conan.core.param.*;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;

/**
 * User: maplesod
 * Date: 11/01/13
 * Time: 13:23
 */
public class MassParams extends AbstractProcessParams {

    private ConanParameter assembler;
    private ConanParameter kmin;
    private ConanParameter kmax;
    private ConanParameter stepSize;
    private ConanParameter libs;
    private ConanParameter outputDir;
    private ConanParameter jobPrefix;
    private ConanParameter threads;
    private ConanParameter memory;
    private ConanParameter parallelismLevel;
    private ConanParameter coverageCutoff;
    private ConanParameter outputLevel;
    private ConanParameter inputSource;


    public MassParams() {

        this.assembler = new ParameterBuilder()
                .longName("asm")
                .description("De Brujin Assembler to use")
                .argValidator(ArgValidator.DEFAULT)
                .isOptional(false)
                .create();

        this.kmin = new NumericParameter(
                "kmin",
                "The minimum k-mer value to assemble. (Default: 51)",
                true);

        this.kmax = new NumericParameter(
                "kmax",
                "The maximum k-mer value to assemble. (Default: 85)",
                true);

        this.stepSize = new ParameterBuilder()
                .longName("step")
                .description("The kmer step size between each assembly: [FINE, MEDIUM, COARSE].  (Default: MEDIUM)")
                .argValidator(ArgValidator.DEFAULT)
                .create();

        this.libs = new ParameterBuilder()
                .longName("libs")
                .description("All libraries to use for this MASS run")
                .argValidator(ArgValidator.OFF)
                .isOptional(false)
                .create();

        this.outputDir = new PathParameter(
                "output",
                "The output directory",
                true);

        this.jobPrefix = new ParameterBuilder()
                .longName("job_prefix")
                .description("The job_prefix to be assigned to all sub processes in MASS.  Useful if executing with a scheduler.")
                .argValidator(ArgValidator.DEFAULT)
                .create();

        this.threads = new NumericParameter(
                "threads",
                "The number of threads to use for each assembly process (Default: 8)",
                true);

        this.memory = new NumericParameter(
                "memory",
                "The amount of memory to request for each assembly process (Default: 50000)",
                true);

        this.parallelismLevel = new ParameterBuilder()
                .longName("parallelismLevel")
                .description("The level of parallelism to use when running MASS: [LINEAR, PARALLEL_ASSEMBLIES_ONLY, PARALLEL_MASS_ONLY, PARALLEL].  (Default: LINEAR)")
                .argValidator(ArgValidator.DEFAULT)
                .create();

        this.coverageCutoff = new NumericParameter(
                "coverageCutoff",
                "The kmer coverage level below which kmers are discarded (Default: -1 i.e. OFF)",
                true);

        this.outputLevel = new ParameterBuilder()
                .longName("outputLevel")
                .description("The output level for the assembler used by MASS: [UNITIGS, CONTIGS, SCAFFOLDS].  (Default: CONTIGS)")
                .argValidator(ArgValidator.DEFAULT)
                .create();

        this.inputSource = new ParameterBuilder()
                .longName("inputSource")
                .description("The input source to use for MASS: [RAW, BEST, ALL, <NUM>].  (Default: ALL)")
                .argValidator(ArgValidator.DEFAULT)
                .create();

    }



    public ConanParameter getAssembler() {
        return assembler;
    }

    public ConanParameter getKmin() {
        return kmin;
    }

    public ConanParameter getKmax() {
        return kmax;
    }

    public ConanParameter getStepSize() {
        return stepSize;
    }

    public ConanParameter getLibs() {
        return libs;
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

    public ConanParameter getParallelismLevel() {
        return parallelismLevel;
    }

    public ConanParameter getCoverageCutoff() {
        return coverageCutoff;
    }

    public ConanParameter getOutputLevel() {
        return outputLevel;
    }

    public ConanParameter getInputSource() {
        return inputSource;
    }

    @Override
    public ConanParameter[] getConanParametersAsArray() {
        return new ConanParameter[]{
                this.assembler,
                this.kmin,
                this.kmax,
                this.stepSize,
                this.libs,
                this.outputDir,
                this.jobPrefix,
                this.threads,
                this.memory,
                this.parallelismLevel,
                this.coverageCutoff,
                this.outputLevel,
                this.inputSource
        };
    }

}
