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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionResult;
import uk.ac.ebi.fgpt.conan.core.param.*;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.core.user.GuestUser;
import uk.ac.ebi.fgpt.conan.factory.DefaultTaskFactory;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.ResourceUsage;
import uk.ac.ebi.fgpt.conan.model.context.TaskResult;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.service.exception.TaskExecutionException;
import uk.ac.tgac.conan.core.util.PathUtils;
import uk.ac.tgac.jellyswarm.JellyswarmCLI;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 11/11/13
 * Time: 14:41
 * To change this template use File | Settings | File Templates.
 */
public class Jellyswarm extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(Jellyswarm.class);

    public Jellyswarm() {
        this(null);
    }

    public Jellyswarm(ConanExecutorService ces) {
       this(ces, new Args());
    }

    public Jellyswarm(ConanExecutorService ces, Args args) {
        super("", args, new Params(), ces);
    }

    @Override
    public ExecutionResult execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        try {

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            // Make a shortcut to the args
            Args args = (Args) this.getProcessArgs();

            // Make the output directory for this child job (delete the directory if it already exists)
            PathUtils.cleanDirectory(args.getOutputDir());

            uk.ac.tgac.jellyswarm.Jellyswarm.Args jellyswarmArgs = new uk.ac.tgac.jellyswarm.Jellyswarm.Args();
            jellyswarmArgs.setInputDir(args.getInputDir());
            jellyswarmArgs.setOutputDir(args.getOutputDir());
            jellyswarmArgs.setPaired(args.isPaired());
            jellyswarmArgs.setThreads(args.getThreads());
            jellyswarmArgs.setJobPrefix(args.getJobPrefix());
            jellyswarmArgs.setHashSize(4000000000L);
            jellyswarmArgs.setThreads(args.getThreads());
            jellyswarmArgs.setRecursive(false);
            jellyswarmArgs.setLowerCount(args.getLowerCount());

            uk.ac.tgac.jellyswarm.Jellyswarm.Pipeline jellyswarm = new uk.ac.tgac.jellyswarm.Jellyswarm.Pipeline(jellyswarmArgs, this.conanExecutorService);

            // Create a guest user
            ConanUser user = new GuestUser("citadel@tgac.ac.uk");

            // Create the AMP task
            ConanTask<uk.ac.tgac.jellyswarm.Jellyswarm.Pipeline> jellyswarmTask = new DefaultTaskFactory().createTask(
                    jellyswarm,
                    0,
                    jellyswarm.getArgs().getArgMap(),
                    ConanTask.Priority.HIGHEST,
                    user);

            jellyswarmTask.setId("Jellyswarm");
            jellyswarmTask.submit();

            // Run the AMP pipeline
            TaskResult result;
            try {
                result = jellyswarmTask.execute(executionContext);
            } catch (TaskExecutionException e) {
                throw new ProcessExecutionException(-1, e);
            }

            stopWatch.stop();

            return new DefaultExecutionResult(
                    result.getTaskName(),
                    0,
                    new String[] {},
                    null,
                    -1,
                    new ResourceUsage(result.getMaxMemUsage(), result.getActualTotalRuntime(), result.getTotalExternalCputime()));
        }
        catch(IOException e) {
            throw new ProcessExecutionException(-1, e);
        }
    }


    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public String getName() {
        return "Jellyswarm";
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        uk.ac.tgac.jellyswarm.Jellyswarm.Pipeline js = null;

        try {
            js = new uk.ac.tgac.jellyswarm.Jellyswarm.Pipeline(null, this.conanExecutorService);
        }
        catch (IOException e) {
            log.warn("Jellyswarm is NOT operational.");
            return false;
        }

        if (!js.isOperational(executionContext)) {
            log.warn("Jellyswarm is NOT operational.");
            return false;
        }

        log.info("Jellyswarm is operational.");

        return true;
    }


    public static class Args extends AbstractProcessArgs {

        private File inputDir;
        private File outputDir;
        private int threads;
        private boolean runParallel;
        private int memory;
        private String jobPrefix;
        private long lowerCount;
        private long hashSize;
        private boolean recursive;
        private boolean paired;

        public Args() {

            super(new Params());

            this.inputDir = JellyswarmCLI.CWD;
            this.outputDir = JellyswarmCLI.CWD;
            this.threads = 1;
            this.runParallel = false;
            this.memory = 0;
            this.jobPrefix = "counter";
            this.lowerCount = 0;
            this.hashSize = 4000000000L;
            this.recursive = false;
            this.paired = true;
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

            pvp.put(params.getLowerCount(), Long.toString(this.lowerCount));
            pvp.put(params.getHashSize(), Long.toString(this.hashSize));
            pvp.put(params.getThreads(), Integer.toString(this.threads));
            pvp.put(params.getMemory(), Integer.toString(this.memory));
            pvp.put(params.getRunParallel(), Boolean.toString(this.runParallel));
            pvp.put(params.getRecursive(), Boolean.toString(this.recursive));
            pvp.put(params.getPaired(), Boolean.toString(this.paired));

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
            else if (param.equals(params.getLowerCount())) {
                this.lowerCount = Long.parseLong(value);
            }
            else if (param.equals(params.getHashSize())) {
                this.hashSize = Long.parseLong(value);
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
            else if (param.equals(params.getRecursive())) {
                this.recursive = Boolean.parseBoolean(value);
            }
            else if (param.equals(params.getPaired())) {
                this.paired = Boolean.parseBoolean(value);
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
        private ConanParameter lowerCount;
        private ConanParameter hashSize;
        private ConanParameter threads;
        private ConanParameter memory;
        private ConanParameter runParallel;
        private ConanParameter recursive;
        private ConanParameter paired;

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

            this.lowerCount = new NumericParameter(
                    "lower",
                    "Don't output k-mer with count < lower-count",
                    true);

            this.hashSize = new ParameterBuilder()
                    .shortName("h")
                    .longName("hash")
                    .description("The hash size to pass to each jellyfish count instance")
                    .argValidator(ArgValidator.DIGITS)
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

        public ConanParameter getLowerCount() {
            return lowerCount;
        }

        public ConanParameter getHashSize() {
            return hashSize;
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

        public ConanParameter getRecursive() {
            return recursive;
        }

        public ConanParameter getPaired() {
            return paired;
        }

        @Override
        public ConanParameter[] getConanParametersAsArray() {
            return new ConanParameter[] {
                    this.inputDir,
                    this.outputDir,
                    this.lowerCount,
                    this.hashSize,
                    this.jobPrefix,
                    this.threads,
                    this.memory,
                    this.runParallel,
                    this.recursive,
                    this.paired
            };
        }
    }
}
