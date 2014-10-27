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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
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
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishStatsV11;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 11/11/13
 * Time: 17:40
 * To change this template use File | Settings | File Templates.
 */
public class StatsProcess extends AbstractConanProcess {
    private static Logger log = LoggerFactory.getLogger(StatsProcess.class);

    private static final int[] lowerCounts = new int[]{2, 5, 10, 20};


    public StatsProcess() {
        this(null);
    }

    public StatsProcess(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public StatsProcess(ConanExecutorService ces, Args args) {
        super("", args, new Params(), ces);
    }

    @Override
    public ExecutionResult execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        try {

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            // Make a shortcut to the args
            Args args = (Args)this.getProcessArgs();

            // Gets jellyfish count files
            List<File> countFiles = this.findFiles(args.getInputDir(), "_0");

            List<ExecutionResult> jobResults = new ArrayList<>();
            List<ExecutionResult> allJobResults = new ArrayList<>();

            log.debug("Found " + countFiles.size() + " jellyfish stats files to process");

            // Make the output directory for this child job (delete the directory if it already exists)
            args.getOutputDir().mkdirs();

            int i =0;
            for(File file : countFiles) {


                for(int j = 0; j < lowerCounts.length; j++) {

                    int lc = lowerCounts[j];

                    JellyfishStatsV11.Args jArgs = new JellyfishStatsV11.Args();
                    jArgs.setInput(file);
                    jArgs.setOutput(new File(args.getOutputDir(), file.getName() + ".lc" + lc + ".stats"));
                    jArgs.setLowerCount(lc);

                    JellyfishStatsV11 jProc = new JellyfishStatsV11(this.conanExecutorService, jArgs);

                    // Execute the assembler
                    ExecutionResult result = this.conanExecutorService.executeProcess(
                            jProc,
                            args.getOutputDir(),
                            args.getJobPrefix() + "-" + i,
                            1,
                            0,
                            args.isRunParallel());

                    // Add assembler id to list
                    jobResults.add(result);
                    allJobResults.add(result);

                    i++;
                }
            }

            // Wait for all assembly jobs to finish if they are running in parallel.
            if (executionContext.usingScheduler() && args.isRunParallel()) {
                log.debug("Jellyfish stats jobs were executed in parallel, waiting for all to complete");
                this.conanExecutorService.executeScheduledWait(
                        jobResults,
                        args.getJobPrefix() + "-group*",
                        ExitStatus.Type.COMPLETED_ANY,
                        args.getJobPrefix() + "-wait",
                        args.getOutputDir());

                jobResults.clear();
            }

            // Aggregates stats files
            List<File> statsFiles = new ArrayList<>();

            for(int j = 0; j < lowerCounts.length; j++) {

                int lc = lowerCounts[j];

                File statsFile = new File(args.getOutputDir(), "summary.lc" + lc + ".tab");
                statsFiles.add(statsFile);

                this.createStatsFile(this.findFiles(args.getOutputDir(), "lc" + lc + ".stats"), statsFile);
            }

            this.createDistinctStatsFile(statsFiles, new File(args.getOutputDir(), "summary.tab"));

            stopWatch.stop();

            TaskResult taskResult = new DefaultTaskResult("citadel-jellyswarm-stats", true, allJobResults, stopWatch.getTime() / 1000L);

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
        return "Stats";
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        JellyfishStatsV11 stats = new JellyfishStatsV11(this.conanExecutorService);

        if (!stats.isOperational(executionContext)) {
            log.warn("Jellyfish stats is NOT operational.");
            return false;
        }

        log.info("Jellyfish stats is operational.");

        return true;
    }



    protected List<File> findFiles(File inputDir, String ext) throws IOException {

        File[] allFiles = inputDir.listFiles();

        List<File> fileList = new ArrayList<>();

        for(File f : allFiles) {
            if (f.getName().endsWith(ext)) {
                fileList.add(f);
            }
        }

        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
            }
        });

        return fileList;
    }

    protected void createStatsFile(List<File> statsFiles, File outputFile) throws IOException {



        List<String> outputLines = new ArrayList<>();

        // Add header
        outputLines.add("filename\tunique\tdistinct\ttotal\tmax_count");

        for(File file : statsFiles) {

            List<String> statsLines = FileUtils.readLines(file);

            long unique = 0;
            long distinct = 0;
            long total = 0;
            long maxCount = 0;

            for(String line : statsLines) {
                if (line.startsWith("Unique")) {
                    String[] parts = line.split("\\s+");
                    unique = Long.parseLong(parts[1]);
                }
                if (line.startsWith("Distinct")) {
                    String[] parts = line.split("\\s+");
                    distinct = Long.parseLong(parts[1]);
                }
                if (line.startsWith("Total")) {
                    String[] parts = line.split("\\s+");
                    total = Long.parseLong(parts[1]);
                }
                if (line.startsWith("Max_count")) {
                    String[] parts = line.split("\\s+");
                    maxCount = Long.parseLong(parts[1]);
                }
            }

            StringBuilder sb = new StringBuilder();

            sb.append(file.getName())
                    .append("\t")
                    .append(unique)
                    .append("\t")
                    .append(distinct)
                    .append("\t")
                    .append(total)
                    .append("\t")
                    .append(maxCount);

            outputLines.add(sb.toString());
        }

        FileUtils.writeLines(outputFile, outputLines);
    }


    private void createDistinctStatsFile(List<File> statsFiles, File outputFile) throws IOException {

        List<String> lines = new ArrayList<>();

        lines.add("filename\tmin\tmax\tmean\tvariance\tstddev");

        for(File file : statsFiles) {

            long min = Long.MAX_VALUE;
            long max = 0;
            long sum = 0;
            long sum2 = 0;

            List<String> statsLines = FileUtils.readLines(file);

            for(int i = 1; i < statsLines.size(); i++) {

                String line = statsLines.get(i);

                String[] parts = line.split("\t");

                long distinctVal = Long.parseLong(parts[2]);

                min = distinctVal < min ? distinctVal : min;
                max = distinctVal > max ? distinctVal : max;

                sum += distinctVal;
                sum2 += distinctVal * distinctVal;
            }

            int entries = statsLines.size() - 1;
            double mean = (double)sum / (double)entries;
            double var = ((sum*sum) - sum2)/(double)entries;
            double stddev = Math.sqrt(var);

            lines.add(file.getName() + "\t" + min + "\t" + max + "\t" + mean + "\t" + var + "\t" + stddev);
        }

        FileUtils.writeLines(outputFile, lines);
    }


    public static class Args extends AbstractProcessArgs {

        private File inputDir;
        private File outputDir;
        private boolean runParallel;
        private String jobPrefix;
        private long lowerCount;

        public Args() {

            super(new Params());

            this.inputDir = JellyswarmCLI.CWD;
            this.outputDir = JellyswarmCLI.CWD;
            this.runParallel = false;
            this.jobPrefix = "stats";
            this.lowerCount = 0;
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

        public boolean isRunParallel() {
            return runParallel;
        }

        public void setRunParallel(boolean runParallel) {
            this.runParallel = runParallel;
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
            else if (param.equals(params.getLowerCount())) {
                this.lowerCount = Long.parseLong(value);
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
        private ConanParameter lowerCount;
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

            this.lowerCount = new NumericParameter(
                    "lower",
                    "Don't output k-mer with count < lower-count",
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

        public ConanParameter getLowerCount() {
            return lowerCount;
        }

        public ConanParameter getRunParallel() {
            return runParallel;
        }

        @Override
        public ConanParameter[] getConanParametersAsArray() {
            return new ConanParameter[] {
                    this.inputDir,
                    this.outputDir,
                    this.lowerCount,
                    this.jobPrefix,
                    this.runParallel
            };
        }
    }
}
