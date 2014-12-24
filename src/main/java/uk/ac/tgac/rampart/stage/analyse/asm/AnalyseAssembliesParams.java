/*
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2015  Daniel Mapleson - TGAC
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
 */

package uk.ac.tgac.rampart.stage.analyse.asm;

import uk.ac.ebi.fgpt.conan.core.param.ArgValidator;
import uk.ac.ebi.fgpt.conan.core.param.ParameterBuilder;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.tgac.rampart.stage.analyse.asm.analysers.AssemblyAnalyser;
import uk.ac.tgac.rampart.util.SpiFactory;

/**
 * Created by maplesod on 15/06/14.
 */
public abstract class AnalyseAssembliesParams extends AbstractProcessParams {

    private ConanParameter statsLevels;
    private ConanParameter analyseReadsDir;
    private ConanParameter outputDir;
    private ConanParameter organism;
    private ConanParameter weightingsFile;
    private ConanParameter threadsPerProcess;
    private ConanParameter runParallel;
    private ConanParameter jobPrefix;

    public AnalyseAssembliesParams() {

        this.statsLevels = new ParameterBuilder()
                .longName("statsLevels")
                .description("The type of assembly analysis to conduct.  Available options: " +
                        new SpiFactory<AssemblyAnalyser>(AssemblyAnalyser.class).listServicesAsString())
                .argValidator(ArgValidator.OFF)
                .create();

        this.analyseReadsDir = new ParameterBuilder()
                .longName("analyseReadsDir")
                .description("The location of the reads analysis.  This is required if you are conducting a kmer analysis")
                .argValidator(ArgValidator.PATH)
                .create();

        this.outputDir = new ParameterBuilder()
                .longName("outputDir")
                .description("The location that output from the assembly analyser module should be placed")
                .argValidator(ArgValidator.PATH)
                .create();

        this.organism = new ParameterBuilder()
                .longName("organism")
                .description("A description of the organism's genome")
                .argValidator(ArgValidator.OFF)
                .create();

        this.weightingsFile = new ParameterBuilder()
                .longName("weightingsFile")
                .isOptional(false)
                .description("A file containing the weightings")
                .argValidator(ArgValidator.PATH)
                .create();

        this.threadsPerProcess = new ParameterBuilder()
                .longName("threads")
                .description("The number of threads to use for every child job.  Default: 1")
                .argValidator(ArgValidator.DIGITS)
                .create();

        this.runParallel = new ParameterBuilder()
                .longName("runParallel")
                .isFlag(true)
                .description("Whether to execute child jobs in parallel when possible")
                .argValidator(ArgValidator.OFF)
                .create();

        this.jobPrefix = new ParameterBuilder()
                .longName("jobPrefix")
                .description("The scheduler job name prefix to apply to each spawned child job")
                .argValidator(ArgValidator.DEFAULT)
                .create();
    }

    public ConanParameter getStatsLevels() {
        return statsLevels;
    }

    public ConanParameter getAnalyseReadsDir() {
        return analyseReadsDir;
    }

    public ConanParameter getOutputDir() {
        return outputDir;
    }

    public ConanParameter getOrganism() {
        return organism;
    }

    public ConanParameter getWeightingsFile() {
        return weightingsFile;
    }

    public ConanParameter getThreadsPerProcess() {
        return threadsPerProcess;
    }

    public ConanParameter getRunParallel() {
        return runParallel;
    }

    public ConanParameter getJobPrefix() {
        return jobPrefix;
    }

    @Override
    public ConanParameter[] getConanParametersAsArray() {
        return new ConanParameter[] {
                this.statsLevels,
                this.analyseReadsDir,
                this.outputDir,
                this.organism,
                this.weightingsFile,
                this.threadsPerProcess,
                this.runParallel,
                this.jobPrefix
        };
    }
}
