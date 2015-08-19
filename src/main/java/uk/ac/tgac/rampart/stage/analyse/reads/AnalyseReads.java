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

package uk.ac.tgac.rampart.stage.analyse.reads;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionResult;
import uk.ac.ebi.fgpt.conan.core.param.*;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.stage.Mecq;
import uk.ac.tgac.rampart.stage.RampartStageArgs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 20/01/14
 * Time: 11:22
 * To change this template use File | Settings | File Templates.
 */
public class AnalyseReads extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(AnalyseReads.class);

    public AnalyseReads() {
        this(null);
    }

    public AnalyseReads(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public AnalyseReads(ConanExecutorService ces, Args args) {
        super("", args, new Params(), ces);
    }

    public Args getArgs() {
        return (Args)this.getProcessArgs();
    }

    @Override
    public String getName() {
        return "AnalyseReads";
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        // Create shortcut to args for convenience
        Args args = this.getArgs();

        boolean kmer = true;

        // Check what analyses are requested and check if those are operational
        if (args.isKmerAnalysis()) {

            KmerAnalysisReads proc = new KmerAnalysisReads(this.conanExecutorService);

            if (!proc.isOperational(executionContext)) {
                log.warn("Read Kmer analysis is NOT operational.");
                return false;
            }

            log.info("Read Kmer analysis is operational.");
        }

        log.info("Reads Analyser is operational.");

        return kmer;
    }

    @Override
    public ExecutionResult execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        log.info("Starting Read Analysis");

        // Create shortcut to args for convenience
        Args args = this.getArgs();

        // Force run parallel to false if not using a scheduler
        if (!executionContext.usingScheduler() && args.isRunParallel()) {
            log.warn("Forcing linear execution due to lack of job scheduler");
            args.setRunParallel(false);
        }

        ExecutionResult executionResult = null;

        // Check what analyses are requested and check if those are operational
        if (args.isKmerAnalysis()) {

            KmerAnalysisReads.Args kmerArgs = new KmerAnalysisReads.Args();
            kmerArgs.setAllLibraries(args.getAllLibraries());
            kmerArgs.setAllMecqs(args.getAllMecqs());
            kmerArgs.setOrganism(args.getOrganism());
            kmerArgs.setOutputDir(args.getOutputDir());
            kmerArgs.setJobPrefix(args.getJobPrefix() + "-kmer");
            kmerArgs.setMecqDir(args.getMecqDir());
            kmerArgs.setThreadsPerProcess(args.getThreadsPerProcess());
            kmerArgs.setRunParallel(args.isRunParallel());

            KmerAnalysisReads proc = new KmerAnalysisReads(this.conanExecutorService, kmerArgs);
            executionResult = proc.execute(executionContext);
        }

        if (executionResult == null) {
            return new DefaultExecutionResult(
                    "rampart-analyse_reads",
                    0
            );
        }
        else {
            return new DefaultExecutionResult(
                    "rampart-analyse_reads",
                    0,
                    executionResult.getOutput(),
                    null,
                    -1,
                    executionResult.getResourceUsage()
            );
        }
    }

    public static class Args extends AbstractProcessArgs implements RampartStageArgs {

        private static final String KEY_ATTR_PARALLEL = "parallel";
        private static final String KEY_ATTR_THREADS = "threads";
        private static final String KEY_ATTR_MEMORY = "memory";
        private static final String KEY_ATTR_KMER = "kmer";

        public static final boolean DEFAULT_RUN_PARALLEL = false;
        public static final int DEFAULT_THREADS = 1;
        public static final int DEFAULT_MEMORY = 0;
        public static final boolean DEFAULT_KMER = false;

        private boolean kmerAnalysis;

        private List<Library> allLibraries;             // All allLibraries available in this job
        private List<Mecq.Sample> allMecqs;             // All mecq configurations
        private String jobPrefix;
        private File mecqDir;                           // Where all the output lives
        private boolean runParallel;                    // Whether to run MASS groups in parallel
        private File outputDir;
        private Organism organism;
        private int threadsPerProcess;
        private int memoryPerProcess;

        public Args() {

            super(new Params());

            this.allLibraries = new ArrayList<>();
            this.allMecqs = new ArrayList<>();
            this.jobPrefix = "analyse-reads";
            this.mecqDir = null;
            this.runParallel = DEFAULT_RUN_PARALLEL;
            this.outputDir = null;
            this.organism = null;
            this.threadsPerProcess = DEFAULT_THREADS;
            this.memoryPerProcess = DEFAULT_MEMORY;
        }


        public Args(Element element, List<Library> allLibraries, List<Mecq.Sample> allMecqs, String jobPrefix,
                                File outputDir, Organism organism) {

            super(new Params());

            this.allLibraries = allLibraries;
            this.allMecqs = allMecqs;
            this.jobPrefix = jobPrefix;
            this.outputDir = outputDir;
            this.organism = organism;

            // From Xml (optional)
            this.kmerAnalysis = element.hasAttribute(KEY_ATTR_KMER) ?
                    XmlHelper.getBooleanValue(element, KEY_ATTR_KMER) :
                    DEFAULT_KMER;

            this.runParallel = element.hasAttribute(KEY_ATTR_PARALLEL) ?
                    XmlHelper.getBooleanValue(element, KEY_ATTR_PARALLEL) :
                    DEFAULT_RUN_PARALLEL;

            this.threadsPerProcess = element.hasAttribute(KEY_ATTR_THREADS) ?
                    XmlHelper.getIntValue(element, KEY_ATTR_THREADS) :
                    DEFAULT_THREADS;

            this.memoryPerProcess = element.hasAttribute(KEY_ATTR_MEMORY) ?
                    XmlHelper.getIntValue(element, KEY_ATTR_MEMORY) :
                    DEFAULT_MEMORY;
        }

        public boolean isKmerAnalysis() {
            return kmerAnalysis;
        }

        public void setKmerAnalysis(boolean kmerAnalysis) {
            this.kmerAnalysis = kmerAnalysis;
        }

        public List<Library> getAllLibraries() {
            return allLibraries;
        }

        public void setAllLibraries(List<Library> allLibraries) {
            this.allLibraries = allLibraries;
        }

        public List<Mecq.Sample> getAllMecqs() {
            return allMecqs;
        }

        public void setAllMecqs(List<Mecq.Sample> allMecqs) {
            this.allMecqs = allMecqs;
        }

        public String getJobPrefix() {
            return jobPrefix;
        }

        public void setJobPrefix(String jobPrefix) {
            this.jobPrefix = jobPrefix;
        }

        public int getThreadsPerProcess() {
            return threadsPerProcess;
        }

        public void setThreadsPerProcess(int threadsPerProcess) {
            this.threadsPerProcess = threadsPerProcess;
        }

        public int getMemoryPerProcess() {
            return memoryPerProcess;
        }

        public void setMemoryPerProcess(int memoryPerProcess) {
            this.memoryPerProcess = memoryPerProcess;
        }

        public File getMecqDir() {
            return mecqDir;
        }

        public void setMecqDir(File mecqDir) {
            this.mecqDir = mecqDir;
        }

        public boolean isRunParallel() {
            return runParallel;
        }

        public void setRunParallel(boolean runParallel) {
            this.runParallel = runParallel;
        }

        public File getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(File outputDir) {
            this.outputDir = outputDir;
        }

        public Organism getOrganism() {
            return organism;
        }

        public void setOrganism(Organism organism) {
            this.organism = organism;
        }

        @Override
        protected void setOptionFromMapEntry(ConanParameter param, String value) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        protected void setArgFromMapEntry(ConanParameter param, String value) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void parseCommandLine(CommandLine cmdLine) {

        }

        @Override
        public ParamMap getArgMap() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public List<ConanProcess> getExternalProcesses() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }


    public static class Params extends AbstractProcessParams {

        private ConanParameter kmerAnalysis;
        private ConanParameter allLibraries;
        private ConanParameter allMecqs;
        private ConanParameter jobPrefix;
        private ConanParameter runParallel;
        private ConanParameter outputDir;
        private ConanParameter organism;
        private ConanParameter threadsPerProcess;
        private ConanParameter memoryPerProcess;

        public Params() {

            this.kmerAnalysis = new ParameterBuilder()
                    .longName("kmer")
                    .description("Perform a kmer analysis on the reads.  This involves running jellyfish to count kmers and then processing with KAT GCP to compare GC to kmer coverage.")
                    .isOptional(false)
                    .isFlag(true)
                    .argValidator(ArgValidator.OFF)
                    .create();

            this.allLibraries = new ParameterBuilder()
                    .longName("all_libraries")
                    .description("All libraries to process")
                    .isOptional(false)
                    .argValidator(ArgValidator.OFF)
                    .create();

            this.allMecqs = new ParameterBuilder()
                    .longName("all_mecqs")
                    .description("All mecq processes that have taken place")
                    .isOptional(false)
                    .argValidator(ArgValidator.OFF)
                    .create();

            this.jobPrefix = new ParameterBuilder()
                    .longName("job_prefix")
                    .description("The job prefix to apply to all child jobs")
                    .argValidator(ArgValidator.DEFAULT)
                    .create();

            this.runParallel = new FlagParameter(
                    "parallel",
                    "Run all kmer counting for all libraries in parallel");

            this.outputDir = new PathParameter(
                    "output_dir",
                    "Path to the output directory",
                    false);

            this.organism = new ParameterBuilder()
                    .longName("organism")
                    .description("Details about the organism that was sequenced and is being assembled")
                    .isOptional(false)
                    .argValidator(ArgValidator.DEFAULT)
                    .create();

            this.threadsPerProcess = new NumericParameter(
                    "threads",
                    "The number of threads that should be used per process",
                    true);

            this.memoryPerProcess = new NumericParameter(
                    "memory",
                    "The amount of memory that should be requested per process (only of use if running in a scheduled enivonment)",
                    true);

        }


        public ConanParameter getKmerAnalysis() {
            return kmerAnalysis;
        }

        public ConanParameter getAllLibraries() {
            return allLibraries;
        }

        public ConanParameter getAllMecqs() {
            return allMecqs;
        }

        public ConanParameter getJobPrefix() {
            return jobPrefix;
        }

        public ConanParameter getRunParallel() {
            return runParallel;
        }

        public ConanParameter getOutputDir() {
            return outputDir;
        }

        public ConanParameter getOrganism() {
            return organism;
        }

        public ConanParameter getThreadsPerProcess() {
            return threadsPerProcess;
        }

        public ConanParameter getMemoryPerProcess() {
            return memoryPerProcess;
        }

        @Override
        public ConanParameter[] getConanParametersAsArray() {
            return new ConanParameter[] {
                    this.kmerAnalysis,
                    this.allLibraries,
                    this.allMecqs,
                    this.jobPrefix,
                    this.runParallel,
                    this.outputDir,
                    this.organism,
                    this.threadsPerProcess,
                    this.memoryPerProcess
            };
        }
    }

}
