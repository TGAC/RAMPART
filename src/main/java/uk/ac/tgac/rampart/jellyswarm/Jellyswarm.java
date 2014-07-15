/*
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
 */

package uk.ac.tgac.rampart.jellyswarm;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.core.context.locality.Local;
import uk.ac.ebi.fgpt.conan.core.param.*;
import uk.ac.ebi.fgpt.conan.core.pipeline.AbstractConanPipeline;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.core.user.GuestUser;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by maplesod on 08/07/14.
 */
public class Jellyswarm {

    private static Logger log = LoggerFactory.getLogger(Jellyswarm.class);

    public static class Pipeline extends AbstractConanPipeline {

        private Args args;
        private ConanExecutorService conanExecutorService;

        private static final String NAME = "jellyswarm-pipeline";
        private static final ConanUser USER = new GuestUser("jellyswarm@tgac.ac.uk");

        public Pipeline(Args args, ConanExecutorService ces) throws IOException {

            super(NAME, USER, false, false, ces.getConanProcessService());

            this.args = args;
            this.conanExecutorService = ces;

            File countsDir = new File(args.getOutputDir(), "counts");
            File statsDir = new File(args.getOutputDir(), "stats");

            // Add the counter stage
            CounterProcess.Args counterArgs = new CounterProcess.Args();
            counterArgs.setInputDir(args.getInputDir());
            counterArgs.setOutputDir(countsDir);
            counterArgs.setThreads(args.getThreads());
            counterArgs.setMemory(args.getMemory());
            counterArgs.setJobPrefix(args.getJobPrefix() + "-counter");
            counterArgs.setRunParallel(true);
            counterArgs.setLowerCount(args.getLowerCount());
            counterArgs.setHashSize(args.getHashSize());
            counterArgs.setRecursive(args.isRecursive());
            counterArgs.setPaired(args.isPaired());

            addProcessIfRequested(JellyswarmStage.COUNTER, counterArgs);


            // Add the stats stage
            StatsProcess.Args statsArgs = new StatsProcess.Args();
            statsArgs.setInputDir(countsDir);
            statsArgs.setOutputDir(statsDir);
            statsArgs.setLowerCount(args.getLowerCount());
            statsArgs.setJobPrefix(args.getJobPrefix() + "-stats");
            statsArgs.setRunParallel(true);

            addProcessIfRequested(JellyswarmStage.STATS, statsArgs);

            // Check all processes in the pipeline are operational, modify execution context to execute unscheduled locally
            if (!this.isOperational(new DefaultExecutionContext(new Local(), null,
                    this.args.getExecutionContext().getExternalProcessConfiguration()))) {
                throw new IOException("The Jellyfish pipeline contains one or more processes that are not currently operational.  " +
                        "Please fix before restarting pipeline.");
            }
        }


        private void addProcessIfRequested(JellyswarmStage stage, ProcessArgs stageArgs) {
            if (this.args.getStages().contains(stage) && stageArgs != null) {
                this.addProcess(stage.create(this.conanExecutorService, stageArgs));
            }
        }


    }


    public static class Args extends AbstractProcessArgs {

        private File inputDir;
        private File outputDir;
        private String jobPrefix;
        private long lowerCount;
        private long hashSize;
        private List<JellyswarmStage> stages;
        private int threads;
        private int memory;
        private boolean runParallel;
        private boolean recursive;
        private boolean paired;

        private ExecutionContext executionContext;

        public Args() {
            super(new Params());
            this.inputDir = null;
            this.outputDir = new File("");
            this.jobPrefix = "";
            this.lowerCount = 0;
            this.hashSize = 4000000000L;
            this.stages = null;
            this.threads = 1;
            this.memory = 0;
            this.runParallel = false;
            this.recursive = false;
            this.paired = true;
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

        public String getJobPrefix() {
            return jobPrefix;
        }

        public void setJobPrefix(String jobPrefix) {
            this.jobPrefix = jobPrefix;
        }


        public long getLowerCount() {
            return lowerCount;
        }

        public void setLowerCount(long lowerCount) {
            this.lowerCount = lowerCount;
        }

        public long getHashSize() {
            return hashSize;
        }

        public void setHashSize(long hashSize) {
            this.hashSize = hashSize;
        }

        public List<JellyswarmStage> getStages() {
            return stages;
        }

        public void setStages(List<JellyswarmStage> stages) {
            this.stages = stages;
        }

        public int getThreads() {
            return threads;
        }

        public void setThreads(int threads) {
            this.threads = threads;
        }

        public int getMemory() {
            return memory;
        }

        public void setMemory(int memory) {
            this.memory = memory;
        }

        public boolean isRunParallel() {
            return runParallel;
        }

        public void setRunParallel(boolean runParallel) {
            this.runParallel = runParallel;
        }

        public boolean isRecursive() {
            return recursive;
        }

        public void setRecursive(boolean recursive) {
            this.recursive = recursive;
        }

        public boolean isPaired() {
            return paired;
        }

        public void setPaired(boolean paired) {
            this.paired = paired;
        }


        public ExecutionContext getExecutionContext() {
            return executionContext;
        }

        public void setExecutionContext(ExecutionContext executionContext) {
            this.executionContext = executionContext;
        }

        @Override
        public void parseCommandLine(CommandLine cmdLine) {

        }

        @Override
        protected void setOptionFromMapEntry(ConanParameter param, String value) {

        }

        @Override
        protected void setArgFromMapEntry(ConanParameter param, String value) {

        }

        @Override
        public String getUncheckedArgs() {
            return null;
        }

        @Override
        public ParamMap getArgMap() {

            Params params = (Params)this.params;
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

            if (this.stages != null && !this.stages.isEmpty()) {
                pvp.put(params.getStageList(), JellyswarmStage.toString(stages));
            }

            pvp.put(params.getLower(), Long.toString(this.lowerCount));
            pvp.put(params.getHashSize(), Long.toString(this.hashSize));
            pvp.put(params.getThreads(), Integer.toString(this.threads));
            pvp.put(params.getMemory(), Integer.toString(this.memory));
            pvp.put(params.getRunParallel(), Boolean.toString(this.runParallel));
            pvp.put(params.getRecursive(), Boolean.toString(this.recursive));
            pvp.put(params.getPaired(), Boolean.toString(this.paired));

            return pvp;
        }

        @Override
        public void setFromArgMap(ParamMap pvp) throws IOException {

            log.warn("Trying to set jellyswarm args from arg map");
        }

        @Override
        public String toString() {
            return "Input dir: " + inputDir.getAbsolutePath() + "\n" +
                    "Output dir: " + outputDir.getAbsolutePath() + "\n" +
                    "Lower count: " + lowerCount + "\n" +
                    "Recursive file scan: " + Boolean.toString(recursive) + "\n" +
                    "Paired End files: " + Boolean.toString(paired) + "\n";
        }
    }

    public static class Params extends AbstractProcessParams {

        private ConanParameter inputDir;
        private ConanParameter outputDir;
        private ConanParameter lower;
        private ConanParameter hashSize;
        private ConanParameter jobPrefix;
        private ConanParameter threads;
        private ConanParameter runParallel;
        private ConanParameter recursive;
        private ConanParameter paired;
        private ConanParameter memory;
        private ConanParameter stageList;

        public Params() {

            this.inputDir = new PathParameter(
                    "input",
                    "The RAMPART configuration file",
                    false);

            this.outputDir = new PathParameter(
                    "output",
                    "The path to the folder where all output should be created",
                    true);

            this.lower = new NumericParameter(
                    "lower",
                    "Don't output k-mer with count < lower-count",
                    true);

            this.hashSize = new ParameterBuilder()
                    .shortName("h")
                    .longName("hash")
                    .description("The hash size to pass to each jellyfish count instance")
                    .argValidator(ArgValidator.DIGITS)
                    .create();

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

            this.recursive = new FlagParameter(
                    "recursive",
                    "Whether or not to recurse through directories in the input directory to search for files");

            this.paired = new FlagParameter(
                    "paired",
                    "Whether or not to you are processing paired end data or not.  Default: true");

            this.stageList = new ParameterBuilder()
                    .longName("stages")
                    .description("The stages to execute: " + JellyswarmStage.getFullListAsString() + ", ALL.  Default: ALL.")
                    .argValidator(ArgValidator.OFF)
                    .create();
        }

        public ConanParameter getInputDir() {
            return inputDir;
        }

        public ConanParameter getOutputDir() {
            return outputDir;
        }

        public ConanParameter getLower() {
            return lower;
        }

        public ConanParameter getHashSize() {
            return hashSize;
        }

        public ConanParameter getJobPrefix() {
            return jobPrefix;
        }

        public ConanParameter getThreads() {
            return threads;
        }

        public ConanParameter getRunParallel() {
            return runParallel;
        }

        public ConanParameter getRecursive() {
            return recursive;
        }

        public ConanParameter getPaired() {
            return paired;
        }

        public ConanParameter getMemory() {
            return memory;
        }

        public ConanParameter getStageList() {
            return stageList;
        }

        @Override
        public ConanParameter[] getConanParametersAsArray() {
            return new ConanParameter[] {
                    this.inputDir,
                    this.outputDir,
                    this.lower,
                    this.hashSize,
                    this.jobPrefix,
                    this.threads,
                    this.memory,
                    this.runParallel,
                    this.recursive,
                    this.paired,
                    this.stageList
            };
        }

    }
}
