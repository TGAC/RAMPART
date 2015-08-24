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

package uk.ac.tgac.rampart.stage;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionResult;
import uk.ac.ebi.fgpt.conan.core.context.DefaultTaskResult;
import uk.ac.ebi.fgpt.conan.core.param.DefaultParamMap;
import uk.ac.ebi.fgpt.conan.core.param.FlagParameter;
import uk.ac.ebi.fgpt.conan.core.param.ParameterBuilder;
import uk.ac.ebi.fgpt.conan.core.param.PathParameter;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.*;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Organism;

import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by maplesod on 21/08/15.
 */
public abstract class RampartProcess extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(RampartProcess.class);

    protected TaskResult taskResult;
    protected List<ExecutionResult> results;

    public RampartProcess(ConanExecutorService ces) {
        this(ces, null);
    }

    public RampartProcess(ConanExecutorService ces, RampartProcessArgs args) {
        super("", args, new Params(), ces);
    }


    /**
     * Must be overridden by child process
     * @param executionContext
     * @return
     * @throws ProcessExecutionException
     * @throws InterruptedException
     * @throws IOException
     */
    public abstract TaskResult executeSample(Mecq.Sample sample, File stageOutputDir, ExecutionContext executionContext)
        throws ProcessExecutionException, InterruptedException, IOException;

    public void validateOutput(Mecq.Sample sample) throws IOException, InterruptedException, ProcessExecutionException {}

    @Override
    public ExecutionResult execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            RampartProcessArgs args = this.getRampartArgs();

            // Force run parallel to false if not using a scheduler
            if (!executionContext.usingScheduler() && args.runParallel) {
                log.warn("Forcing linear execution due to lack of job scheduler");
                args.runParallel = false;
            }

            log.info("Starting " + this.getName() + " Process");

            // Loop through all samples to process
            for (Mecq.Sample sample : args.samples) {

                File stageDir = args.getStageDir(sample);

                // Ensure sample output mecq directory exists
                if (!stageDir.exists()) {
                    stageDir.mkdirs();
                }

                // Do samples specific work
                TaskResult sampleResults = this.executeSample(sample, stageDir, executionContext);

                // Collect results
                for(ExecutionResult res : sampleResults.getProcessResults()) {
                    results.add(res);
                }
            }

            // Ensure wait log directory exists
            File logDir = new File(args.outputDir, "wait_logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            // If we're using a scheduler and we have been asked to run jobs
            // in parallel, then we should wait for all those to complete before finishing this stage.
            if (executionContext.usingScheduler() && args.runParallel) {

                log.info("Running all " + this.getName() + " jobs in parallel, waiting for completion");
                MultiWaitResult mrw = this.conanExecutorService.executeScheduledWait(
                        results,
                        args.jobPrefix + "-*",
                        ExitStatus.Type.COMPLETED_SUCCESS,
                        args.jobPrefix + "-wait",
                        logDir);
            }

            // Check all the required output files are in place (delegated to child class)
            // Loop through all samples to process
            for (Mecq.Sample sample : args.samples) {
                this.validateOutput(sample);
            }

            log.info("Finished " + this.getName() + " Process");

            stopWatch.stop();

            this.taskResult = new DefaultTaskResult("rampart-" + this.getName(), true, results, stopWatch.getTime() / 1000L);

            // Output the resource usage to file
            FileUtils.writeLines(new File(logDir, args.jobPrefix + ".summary"), this.taskResult.getOutput());

            return new DefaultExecutionResult(
                    this.taskResult.getTaskName(),
                    0,
                    new String[] {},
                    null,
                    -1,
                    new ResourceUsage(this.taskResult.getMaxMemUsage(), this.taskResult.getActualTotalRuntime(), this.taskResult.getTotalExternalCputime()));

        }
        catch(IOException e) {
            throw new ProcessExecutionException(2, e);
        }
    }

    public RampartProcessArgs getRampartArgs() {
        return (RampartProcessArgs)this.getProcessArgs();
    }

    public static class RampartProcessArgs extends AbstractProcessArgs implements RampartStageArgs {

        // Xml Config Keys
        public static final String KEY_ATTR_THREADS     = "threads";
        public static final String KEY_ATTR_MEMORY      = "memory";
        public static final String KEY_ATTR_PARALLEL    = "parallel";

        public static final int DEFAULT_THREADS = 1;
        public static final int DEFAULT_MEMORY = 0;
        public static final boolean DEFAULT_RUN_PARALLEL = false;

        protected File outputDir;
        protected String jobPrefix;
        protected List<Mecq.Sample> samples;
        protected boolean runParallel;
        protected RampartStage stage;
        protected int threads;
        protected int memoryMb;
        protected Organism organism;


        /**
         * Set defaults
         */
        public RampartProcessArgs(RampartStage stage) {

            super(new Params());

            this.outputDir = new File("");
            this.samples = new ArrayList<>();
            this.threads = DEFAULT_THREADS;
            this.memoryMb = DEFAULT_MEMORY;
            this.runParallel = DEFAULT_RUN_PARALLEL;
            this.stage = stage;
            this.organism = null;

            Format formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String dateTime = formatter.format(new Date());
            this.jobPrefix = "rampart-" + stage.getOutputDirName() + "-" + dateTime;
        }

        public RampartProcessArgs(RampartStage stage, File outputDir, String jobPrefix, List<Mecq.Sample> samples, Organism organism) throws IOException {

            // Set defaults first
            this(stage);

            // Set from parameters
            this.outputDir = outputDir;
            this.jobPrefix = jobPrefix + "-" + stage.getOutputDirName();
            this.samples = samples;
            this.organism = organism;
        }

        protected Params getParams() {
            return (Params)this.params;
        }

        public String getJobPrefix() {
            return jobPrefix;
        }

        public void setJobPrefix(String jobPrefix) {
            this.jobPrefix = jobPrefix;
        }

        public boolean isRunParallel() {
            return runParallel;
        }

        public void setRunParallel(boolean runParallel) {
            this.runParallel = runParallel;
        }

        public RampartStage getStage() {
            return stage;
        }

        public void setStage(RampartStage stage) {
            this.stage = stage;
        }

        public File getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(File outputDir) {
            this.outputDir = outputDir;
        }

        public File getSampleDir(Mecq.Sample sample) {
            return new File(this.outputDir, sample.name);
        }

        public File getStageDir(Mecq.Sample sample) {
            return new File(this.getSampleDir(sample), this.stage.getOutputDirName());
        }

        public List<Mecq.Sample> getSamples() {
            return samples;
        }

        public void setSamples(List<Mecq.Sample> samples) {
            this.samples = samples;
        }

        public int getThreads() {
            return threads;
        }

        public void setThreads(int threads) {
            this.threads = threads;
        }

        public int getMemoryMb() {
            return memoryMb;
        }

        public void setMemoryMb(int memoryMb) {
            this.memoryMb = memoryMb;
        }

        public Organism getOrganism() {
            return organism;
        }

        public void setOrganism(Organism organism) {
            this.organism = organism;
        }

        @Override
        public void parseCommandLine(CommandLine cmdLine) {

        }

        @Override
        public ParamMap getArgMap() {

            Params params = this.getParams();
            ParamMap pvp = new DefaultParamMap();

            pvp.put(params.getRunParallel(), Boolean.toString(this.runParallel));

            if (this.jobPrefix != null) {
                pvp.put(params.getJobPrefix(), this.jobPrefix);
            }

            return pvp;
        }

        @Override
        protected void setOptionFromMapEntry(ConanParameter param, String value) {

            Params params = this.getParams();

            if (param.equals(params.getJobPrefix())) {
                this.jobPrefix = value;
            } else if (param.equals(params.getRunParallel())) {
                this.runParallel = Boolean.parseBoolean(value);
            } else {
                throw new IllegalArgumentException("Unknown param found: " + param);
            }
        }

        @Override
        protected void setArgFromMapEntry(ConanParameter param, String value) {

        }

        @Override
        public List<ConanProcess> getExternalProcesses() {
            return new ArrayList<>();
        }

    }

    public static class Params extends AbstractProcessParams {

        private ConanParameter outputDir;
        private ConanParameter jobPrefix;
        private ConanParameter runParallel;

        public Params() {

            this.outputDir = new PathParameter(
                    "output_dir",
                    "The directory to place the output for this RAMPART stage",
                    false);

            this.jobPrefix = new ParameterBuilder()
                    .longName("jobPrefix")
                    .description("If using a scheduler this prefix is applied to the job names of all child processes")
                    .create();

            this.runParallel = new FlagParameter(
                    "runParallel",
                    "If set to true, and we want to run jobs in a scheduled execution context, then each job will be executed " +
                            "in parallel.  A wait job will be executed in the foreground which will " +
                            "complete after jobs have completed");
        }

        public ConanParameter getOutputDir() {
            return outputDir;
        }

        public ConanParameter getJobPrefix() {
            return jobPrefix;
        }

        public ConanParameter getRunParallel() {
            return runParallel;
        }


        @Override
        public ConanParameter[] getConanParametersAsArray() {
            return new ConanParameter[]{
                    this.outputDir,
                    this.jobPrefix,
                    this.runParallel
            };
        }

    }

}
