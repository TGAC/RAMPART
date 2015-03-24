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

package uk.ac.tgac.citadel.stage;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionResult;
import uk.ac.ebi.fgpt.conan.core.context.DefaultTaskResult;
import uk.ac.ebi.fgpt.conan.core.param.*;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.model.context.*;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.PathUtils;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishCountV11;
import uk.ac.tgac.jellyswarm.FileFinder;
import uk.ac.tgac.jellyswarm.FilePair;
import uk.ac.tgac.jellyswarm.JellyswarmCLI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 11/11/13
 * Time: 14:41
 * To change this template use File | Settings | File Templates.
 */
public class Rampart extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(Rampart.class);

    public Rampart() {
        this(null);
    }

    public Rampart(ConanExecutorService ces) {
       this(ces, new Args());
    }

    public Rampart(ConanExecutorService ces, Args args) {
        super("", args, new Params(), ces);
    }


    protected enum AssemblyCode {
        ABYSS,
        SPADES,
        VELVET
    }

    @Override
    public ExecutionResult execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        try {

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            // Make a shortcut to the args
            Args args = (Args) this.getProcessArgs();

            // Create output dir
            PathUtils.cleanDirectory(args.getOutputDir());


            // Go through jellyswarm output and work out which 3 samples are most representative of your collection
            List<FilePair> representativeSamples = this.findRepresentativeSamples();

            List<ExecutionResult> jobResults = new ArrayList<>();
            List<File> bestAssembly = new ArrayList<>();

            // For each of these 3 samples, run ABySS with a kmer spread, SPADEs and velvet (these assemblers are
            // normally pretty good with bacterial genomes)
            for(FilePair sample : representativeSamples) {

                // - Run RAMPART
                Pair<ExecutionResult, File> result = this.executeRampart(sample);
                jobResults.add(result.getLeft());
                bestAssembly.add(result.getRight());
            }


            //  - Wait for all assembly jobs to finish if they are running in parallel.
            if (executionContext.usingScheduler() && args.isRunParallel()) {
                log.debug("Jellyfish counter jobs were executed in parallel, waiting for all to complete");
                this.conanExecutorService.executeScheduledWait(
                        jobResults,
                        args.getJobPrefix() + "-group*",
                        ExitStatus.Type.COMPLETED_ANY,
                        args.getJobPrefix() + "-wait",
                        args.getOutputDir());

                jobResults.clear();
            }



            // Identify best assembly from each of the 3 samples.  If there is a consensus suggest that to user,
            // otherwise just report results
            for(File f : bestAssembly) {
                // Do something...
            }



            stopWatch.stop();

            TaskResult taskResult = new DefaultTaskResult("citadel-rampart", true, jobResults, stopWatch.getTime() / 1000L);

            return new DefaultExecutionResult(
                    taskResult.getTaskName(),
                    0,
                    new String[] {},
                    null,
                    -1,
                    new ResourceUsage(taskResult.getMaxMemUsage(), taskResult.getActualTotalRuntime(), taskResult.getTotalExternalCputime()));
        }
        catch(Exception e) {
            throw new ProcessExecutionException(-1, e);
        }
    }

    protected List<FilePair> findRepresentativeSamples() {

        return null;
    }

    protected Pair<ExecutionResult, File> executeRampart(FilePair input) {

        return null;
    }


    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public String getName() {
        return "Rampart";
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        JellyfishCountV11 count = new JellyfishCountV11(this.conanExecutorService);

        if (!count.isOperational(executionContext)) {
            log.warn("Jellyfish count is NOT operational.");
            return false;
        }

        log.info("Jellyfish count is operational.");

        return true;
    }


    public static class Args extends AbstractProcessArgs {

        private File inputDir;
        private File outputDir;
        private int threads;
        private boolean runParallel;
        private int memory;
        private String jobPrefix;
        private Organism organism;

        public Args() {

            super(new Params());

            this.inputDir = JellyswarmCLI.CWD;
            this.outputDir = JellyswarmCLI.CWD;
            this.threads = 1;
            this.runParallel = false;
            this.memory = 0;
            this.jobPrefix = "counter";
            this.organism = null;
        }

        public Params getParams() {
            return (Params)this.params;
        }

        public File getInputDir() {
            return inputDir;
        }

        public void setInputDir(File inputDir) {
            this.inputDir = inputDir;
        }

        public File getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(File outputDir) {
            this.outputDir = outputDir;
        }

        public int getThreads() {
            return threads;
        }

        public void setThreads(int threads) {
            this.threads = threads;
        }

        public boolean isRunParallel() {
            return runParallel;
        }

        public void setRunParallel(boolean runParallel) {
            this.runParallel = runParallel;
        }

        public int getMemory() {
            return memory;
        }

        public void setMemory(int memory) {
            this.memory = memory;
        }

        public String getJobPrefix() {
            return jobPrefix;
        }

        public void setJobPrefix(String jobPrefix) {
            this.jobPrefix = jobPrefix;
        }

        public Organism getOrganism() {
            return organism;
        }

        public void setOrganism(Organism organism) {
            this.organism = organism;
        }

        @Override
        public void parseCommandLine(CommandLine cmdLine) {

            Params params = this.getParams();
        }

        @Override
        public ParamMap getArgMap() {

            Params params = this.getParams();

            ParamMap pvp = new DefaultParamMap();

            if (this.inputDir != null) {
                pvp.put(params.getInputDir(), this.inputDir.getAbsolutePath());
            }

            if (this.outputDir != null) {
                pvp.put(params.getOutputDir(), this.outputDir.getAbsolutePath());
            }

            if (this.jobPrefix != null && !this.jobPrefix.isEmpty()) {
                pvp.put(params.getJobPrefix(), jobPrefix);
            }

            pvp.put(params.getThreads(), Integer.toString(this.threads));
            pvp.put(params.getMemory(), Integer.toString(this.memory));
            pvp.put(params.getRunParallel(), Boolean.toString(this.runParallel));

            return pvp;
        }


        @Override
        protected void setOptionFromMapEntry(ConanParameter param, String value) {

            Params params = this.getParams();

            if (param.equals(params.getInputDir())) {
                this.inputDir = new File(value);
            }
            else if (param.equals(params.getOutputDir())) {
                this.outputDir = new File(value);
            }
            else if (param.equals(params.getJobPrefix())) {
                this.jobPrefix = value;
            }
            else if (param.equals(params.getThreads())) {
                this.threads = Integer.parseInt(value);
            }
            else if (param.equals(params.getMemory())) {
                this.memory = Integer.parseInt(value);
            }
            else if (param.equals(params.getRunParallel())) {
                this.runParallel = Boolean.parseBoolean(value);
            }
            else {
                throw new IllegalArgumentException("Unknown param found: " + param);
            }
        }

        @Override
        protected void setArgFromMapEntry(ConanParameter param, String value) {

        }
    }



    public static class Params extends AbstractProcessParams {

        private ConanParameter inputDir;
        private ConanParameter outputDir;
        private ConanParameter jobPrefix;
        private ConanParameter threads;
        private ConanParameter memory;
        private ConanParameter runParallel;

        public Params() {

            this.inputDir = new PathParameter(
                    "input",
                    "The RAMPART configuration file",
                    false);

            this.outputDir = new PathParameter(
                    "output",
                    "The path to the folder where all output should be created",
                    true);

            this.jobPrefix = new ParameterBuilder()
                    .longName("prefix")
                    .description("The prefix to use for any child processes that are forked from this process")
                    .argValidator(ArgValidator.OFF)
                    .create();

            this.threads = new NumericParameter(
                    "threads",
                    "The number of threads to use for each process.  Default: 1",
                    true);

            this.memory = new NumericParameter(
                    "memory",
                    "The estimated amount of memory in MB that each process is likely to use.  Default: 0",
                    true);

            this.runParallel = new FlagParameter(
                    "parallel",
                    "Whether to run each process in parallel or not");
        }

        public ConanParameter getInputDir() {
            return inputDir;
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

        public ConanParameter getRunParallel() {
            return runParallel;
        }

        @Override
        public ConanParameter[] getConanParametersAsArray() {
            return new ConanParameter[] {
                    this.inputDir,
                    this.outputDir,
                    this.jobPrefix,
                    this.threads,
                    this.memory,
                    this.runParallel,
            };
        }
    }
}
