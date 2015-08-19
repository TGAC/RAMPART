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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionResult;
import uk.ac.ebi.fgpt.conan.core.context.DefaultTaskResult;
import uk.ac.ebi.fgpt.conan.core.param.*;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.*;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.conan.process.kmer.kat.KatGcpV2;
import uk.ac.tgac.rampart.stage.Mecq;
import uk.ac.tgac.rampart.stage.RampartStageArgs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 20/11/13
 * Time: 13:12
 * To change this template use File | Settings | File Templates.
 */
public class KmerAnalysisReads extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(KmerAnalysisReads.class);

    public KmerAnalysisReads() {
        this(null);
    }

    public KmerAnalysisReads(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public KmerAnalysisReads(ConanExecutorService ces, ProcessArgs args) {
        super("", args, new Params(), ces);
    }

    public Args getArgs() {
        return (Args)this.getProcessArgs();
    }

    @Override
    public ExecutionResult execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        try {

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            log.info("Starting Kmer Counting on all Reads");

            // Create shortcut to args for convienience
            Args args = this.getArgs();

            // Force run parallel to false if not using a scheduler
            if (!executionContext.usingScheduler() && args.isRunParallel()) {
                log.warn("Forcing linear execution due to lack of job scheduler");
                args.setRunParallel(false);
            }

            List<ExecutionResult> jobResults = new ArrayList<>();

            for(Mecq.Sample sample : args.getAllMecqs()) {

                File sampleOutDir = new File(args.getOutputDir(), sample.name);
                File raOutDir = new File(sampleOutDir, "2-reads-analyses");

                // Create the output directory for the RAW datasets
                File rawOutputDir = new File(raOutDir, "raw");

                if (!rawOutputDir.exists()) {
                    rawOutputDir.mkdirs();
                }


                // Start kat on all RAW datasets
                for (Library lib : sample.libraries) {

                    // Execute jellyfish and add id to list of job ids
                    File katOutDir = new File(rawOutputDir, lib.getName());
                    ExecutionResult res = this.executeKatGcp(args, "raw", katOutDir, lib);
                    jobResults.add(res);
                }

                // Also start kat on all the pre-processed libraries from MECQ
                for (Mecq.EcqArgs ecqArgs : sample.ecqArgList) {

                    // Create the output directory for the non-RAW datasets
                    File ecqOutputDir = new File(raOutDir, ecqArgs.getName());

                    if (!ecqOutputDir.exists()) {
                        ecqOutputDir.mkdirs();
                    }

                    for (Library lib : ecqArgs.getOutputLibraries(sample)) {

                        // Add jellyfish id to list of job ids
                        File katOutDir = new File(ecqOutputDir, lib.getName());
                        ExecutionResult res = this.executeKatGcp(args, ecqArgs.getName(), katOutDir, lib);
                        jobResults.add(res);
                    }
                }

            }

            // If we're using a scheduler and we have been asked to run each job
            // in parallel, then we should wait for all those to complete before continueing.
            if (executionContext.usingScheduler() && args.isRunParallel()) {
                log.info("Kmer counting all ECQ groups in parallel, waiting for completion");
                this.conanExecutorService.executeScheduledWait(
                        jobResults,
                        args.getJobPrefix() + "-kat_gcp-*",
                        ExitStatus.Type.COMPLETED_ANY,
                        args.getJobPrefix() + "-kmer-wait",
                        args.getOutputDir());
            }

            log.info("Kmer analysis of all read sets has finished.");

            stopWatch.stop();

            TaskResult taskResult = new DefaultTaskResult("rampart-read_analysis-kmer", true, jobResults, stopWatch.getTime() / 1000L);

            // Output the resource usage to file
            FileUtils.writeLines(new File(args.getOutputDir(), args.getJobPrefix() + ".summary"), taskResult.getOutput());

            return new DefaultExecutionResult(
                    taskResult.getTaskName(),
                    0,
                    new String[] {},
                    null,
                    -1,
                    new ResourceUsage(taskResult.getMaxMemUsage(), taskResult.getActualTotalRuntime(), taskResult.getTotalExternalCputime()));
        }
        catch(ConanParameterException | IOException e) {
            throw new ProcessExecutionException(-1, e);
        }
    }


    protected ExecutionResult executeKatGcp(Args args, String ecqName, File outputDir, Library lib)
            throws ProcessExecutionException, InterruptedException, ConanParameterException, IOException {
        String suffix = "kat_gcp-" + ecqName + "_" + lib.getName();

        // Create the process
        KatGcpV2 kat = this.makeKatGcp(
                lib.getFiles().toArray(new File[lib.getFiles().size()]),
                new File(new File(outputDir, ecqName), suffix).getAbsolutePath(),
                args.getOrganism(),
                args.getThreadsPerProcess());

        // Create a job name
        String jobName = args.getJobPrefix() + "-" + suffix;

        // Start kat
        return this.conanExecutorService.executeProcess(
                kat,
                new File(outputDir, ecqName),
                jobName,
                args.getThreadsPerProcess(),
                args.getMemoryPerProcess(),
                args.isRunParallel());
    }


    /**
     * Hopefully this is a conservative estimate for most projects.  We ignore very low count kmers so hopefully this
     * size just needs to accomodate genuine kmers, and should therefore be roughly equivalent to genomesize * ploidy.
     * We multiply by 10 to be on the safe side and make sure we can handle some sequencing errors.
     * @param organism Details about the organism's genome (specifically the genome size and ploidy)
     * @return An overestimate of the expected jellyfish hash size
     */
    public static long guessJellyfishHashSize(Organism organism) throws IOException {

        long hashSize = organism.getGenomeSize() * organism.getPloidy() * 10;

        // Check to make sure we don't have anything weird... if we do use a default of 100 million (this should be enough
        // for most organisms running through RAMPART, although it might still fail on low mem systems.  Need to think
        // this through...
        return hashSize <= 0 ? 100000000L : hashSize;
    }

    protected KatGcpV2 makeKatGcp(File[] inputs, String outputPrefix, Organism organism, int threads) throws ProcessExecutionException, IOException {

        KatGcpV2.Args katArgs = new KatGcpV2.Args();
        katArgs.setOutputPrefix(outputPrefix);
        katArgs.setHashSize(guessJellyfishHashSize(organism));
        katArgs.setKmer(31);     // 31 should be sufficient for most organisms
        katArgs.setCanonical(true);
        katArgs.setThreads(threads);
        katArgs.setInputFiles(inputs);

        return new KatGcpV2(this.conanExecutorService, katArgs);
    }




    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public String getName() {
        return "KmerCountReads";
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        KatGcpV2 katGcpProc = new KatGcpV2(this.conanExecutorService);
        if (!katGcpProc.isOperational(executionContext)) {
            log.warn("KAT GCP is not operational.");
            return false;
        }

        log.info("Read Kmer counting stage is operational.");

        return true;
    }

    public static class Args extends AbstractProcessArgs implements RampartStageArgs {

        private static final String KEY_ATTR_PARALLEL = "parallel";
        private static final String KEY_ATTR_THREADS = "threads";
        private static final String KEY_ATTR_MEMORY = "memory";


        public static final boolean DEFAULT_RUN_PARALLEL = false;
        public static final int DEFAULT_THREADS = 1;
        public static final int DEFAULT_MEMORY = 0;


        private List<Library> allLibraries;             // All allLibraries available in this job
        private List<Mecq.Sample> allMecqs;                 // All mecq configurations
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
            this.jobPrefix = "kmer-reads";
            this.mecqDir = null;
            this.runParallel = DEFAULT_RUN_PARALLEL;
            this.outputDir = null;
            this.organism = null;
            this.threadsPerProcess = DEFAULT_THREADS;
            this.memoryPerProcess = DEFAULT_MEMORY;
        }


        public Args(Element element, List<Library> allLibraries, List<Mecq.Sample> allMecqs, String jobPrefix, File mecqDir,
                                     File outputDir, Organism organism) {

            super(new Params());

            this.allLibraries = allLibraries;
            this.allMecqs = allMecqs;
            this.jobPrefix = jobPrefix;
            this.mecqDir = mecqDir;
            this.outputDir = outputDir;
            this.organism = organism;

            // From Xml (optional)
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

        /**
         * This actually scans the output directory for jellyfish hash files that contain the provided ecq name.
         *
         * @param ecqName The ecq name to search for
         * @return A list of jellyfish hash files
         */
        public Collection<File> getJellyfishHashes(String ecqName) {
            return FileUtils.listFiles(new File(this.getOutputDir(), ecqName), new String[]{".jf31_0"}, false);
        }

        @Override
        public void parseCommandLine(CommandLine cmdLine) {

        }

        @Override
        public ParamMap getArgMap() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        protected void setOptionFromMapEntry(ConanParameter param, String value) {

        }

        @Override
        protected void setArgFromMapEntry(ConanParameter param, String value) {

        }

        @Override
        public List<ConanProcess> getExternalProcesses() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }


    public static class Params extends AbstractProcessParams {

        private ConanParameter allLibraries;
        private ConanParameter allMecqs;
        private ConanParameter jobPrefix;
        private ConanParameter mecqDir;
        private ConanParameter runParallel;
        private ConanParameter outputDir;
        private ConanParameter organism;
        private ConanParameter threadsPerProcess;
        private ConanParameter memoryPerProcess;

        public Params() {

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

            this.mecqDir = new PathParameter(
                    "mecq_dir",
                    "Path to mecq directory",
                    false);

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



        public ConanParameter getAllLibraries() {
            return allLibraries;
        }

        public ConanParameter getAllMecqs() {
            return allMecqs;
        }

        public ConanParameter getJobPrefix() {
            return jobPrefix;
        }

        public ConanParameter getMecqDir() {
            return mecqDir;
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
                    this.allLibraries,
                    this.allMecqs,
                    this.jobPrefix,
                    this.mecqDir,
                    this.runParallel,
                    this.outputDir,
                    this.organism,
                    this.threadsPerProcess,
                    this.memoryPerProcess
            };
        }

    }

}
