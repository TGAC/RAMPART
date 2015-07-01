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
import uk.ac.ebi.fgpt.conan.core.context.DefaultTaskResult;
import uk.ac.ebi.fgpt.conan.core.param.DefaultParamMap;
import uk.ac.ebi.fgpt.conan.core.param.FlagParameter;
import uk.ac.ebi.fgpt.conan.core.param.PathParameter;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.process.TypicalProcessArgs;
import uk.ac.ebi.fgpt.conan.core.process.TypicalProcessParams;
import uk.ac.ebi.fgpt.conan.model.context.*;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.util.PathUtils;
import uk.ac.tgac.jellyswarm.FileFinder;
import uk.ac.tgac.jellyswarm.FilePair;

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
public class Decompress extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(Decompress.class);

    public Decompress() {
        this(null);
    }

    public Decompress(ConanExecutorService ces) {
       this(ces, new Args());
    }

    public Decompress(ConanExecutorService ces, Args args) {
        super("", args, new Params(), ces);
    }

    @Override
    public ExecutionResult execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        try {

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            // Make a shortcut to the args
            Args args = (Args) this.getProcessArgs();

            // Gets grouped files
            List<FilePair> files = FileFinder.find(args.getInputDir(), args.isRecursive(), args.isPaired());

            log.info("Found " + files.size() + " samples to process.");

            List<ExecutionResult> jobResults = new ArrayList<>();

            // Start afresh
            PathUtils.cleanDirectory(args.getOutputDir());

            for(FilePair fp : files) {

                File leftLinkFile = new File(args.getOutputDir(), fp.getLeft().getName());

                // Link file to output dir
                this.conanExecutorService.getConanProcessService().createLocalSymbolicLink(fp.getLeft(), leftLinkFile);

                if (fp.getLeft().getName().endsWith("gz")) {

                    // Unzip using gunzip
                    jobResults.add(this.conanExecutorService.executeProcess(
                            "gunzip " + leftLinkFile.getAbsolutePath(),
                            args.getOutputDir(),
                            args.getJobPrefix() + leftLinkFile.getName(),
                            1,
                            0,
                            0,
                            args.isRunParallel()));
                }

                if (args.isPaired()) {

                    File rightLinkFile = args.isPaired() ? new File(args.getOutputDir(), fp.getRight().getName()) : null;

                    // Link file to output dir
                    this.conanExecutorService.getConanProcessService().createLocalSymbolicLink(fp.getRight(), rightLinkFile);

                    if (fp.getRight().getName().endsWith("gz")) {

                        // Unzip using gunzip
                        jobResults.add(this.conanExecutorService.executeProcess(
                                "gunzip " + rightLinkFile.getAbsolutePath(),
                                args.getOutputDir(),
                                args.getJobPrefix() + rightLinkFile.getName(),
                                1,
                                0,
                                0,
                                args.isRunParallel()));
                    }
                }
            }


            // Wait for all assembly jobs to finish if they are running in parallel.
            if (executionContext.usingScheduler() && args.isRunParallel()) {
                log.debug("Decompression jobs were executed in parallel, waiting for all to complete");
                this.conanExecutorService.executeScheduledWait(
                        jobResults,
                        args.getJobPrefix(),
                        ExitStatus.Type.COMPLETED_ANY,
                        args.getJobPrefix() + "-wait",
                        args.getOutputDir());

                jobResults.clear();
            }

            stopWatch.stop();

            TaskResult taskResult = new DefaultTaskResult("citadel-decompress", true, jobResults, stopWatch.getTime() / 1000L);

            return new DefaultExecutionResult(
                    taskResult.getTaskName(),
                    0,
                    new String[] {},
                    null,
                    -1,
                    new ResourceUsage(taskResult.getMaxMemUsage(), taskResult.getActualTotalRuntime(), taskResult.getTotalExternalCputime()));
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
        return "Decompress";
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        log.info("Decompress stage is operational.");

        return true;
    }


    public static class Args extends TypicalProcessArgs {

        private File inputDir;
        private boolean paired;
        private boolean recursive;

        public Args() {

            super(new Params());

            this.inputDir = null;
            this.paired = true;
            this.recursive = false;
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

            pvp.put(params.getThreads(), Integer.toString(this.threads));
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
            else if (param.equals(params.getThreads())) {
                this.threads = Integer.parseInt(value);
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



    public static class Params extends TypicalProcessParams {

        private ConanParameter inputDir;
        private ConanParameter recursive;
        private ConanParameter paired;

        public Params() {

            super();

            this.inputDir = new PathParameter(
                    "input",
                    "The input directory containing samples for Citadel to process",
                    false);

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
                    this.recursive,
                    this.paired
            };
        }
    }
}
