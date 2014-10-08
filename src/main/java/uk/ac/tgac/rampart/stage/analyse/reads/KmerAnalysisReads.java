package uk.ac.tgac.rampart.stage.analyse.reads;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import uk.ac.ebi.fgpt.conan.core.param.*;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
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
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishCountV11;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishMergeV11;
import uk.ac.tgac.conan.process.kmer.kat.KatGcpV1;
import uk.ac.tgac.conan.process.kmer.kat.KatPlotDensityV1;
import uk.ac.tgac.rampart.stage.Mecq;
import uk.ac.tgac.rampart.stage.RampartStageArgs;
import uk.ac.tgac.rampart.util.JobOutput;
import uk.ac.tgac.rampart.util.JobOutputList;
import uk.ac.tgac.rampart.util.JobOutputMap;

import java.io.File;
import java.util.*;

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
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        try {

            log.info("Starting Kmer Counting on all Reads");

            // Create shortcut to args for convienience
            Args args = this.getArgs();

            // Create the output directory
            args.getOutputDir().mkdirs();

            JobOutputMap jfCountOutputs = new JobOutputMap();
            List<Integer> jobIds = new ArrayList<>();

            // Create the output directory for the RAW datasets
            File rawOutputDir = new File(args.getOutputDir(), "raw");

            if (!rawOutputDir.exists()) {
                rawOutputDir.mkdirs();
            }

            // Start jellyfish on all RAW datasets
            for(Library lib : args.getAllLibraries()) {

                // Execute jellyfish and add id to list of job ids
                JobOutput jfOut = this.executeJellyfishCount(args, "raw", args.getOutputDir(), lib);
                jobIds.add(jfOut.getJobId());
                jfCountOutputs.updateTracker("raw", jfOut.getOutputFile());
            }

            // Also start jellyfish on all the prep-processed libraries from MECQ
            if (args.getAllMecqs() != null) {
                for(Mecq.EcqArgs ecqArgs : args.getAllMecqs()) {

                    // Create the output directory for the RAW datasets
                    File ecqOutputDir = new File(args.getOutputDir(), ecqArgs.getName());

                    if (!ecqOutputDir.exists()) {
                        ecqOutputDir.mkdirs();
                    }

                    for(Library lib : ecqArgs.getOutputLibraries()) {

                        // Add jellyfish id to list of job ids
                        JobOutput jfOut = this.executeJellyfishCount(args, ecqArgs.getName(), args.getOutputDir(), lib);
                        jobIds.add(jfOut.getJobId());
                        jfCountOutputs.updateTracker(ecqArgs.getName(), jfOut.getOutputFile());
                    }
                }
            }


            // If we're using a scheduler and we have been asked to run each MECQ group for each library
            // in parallel, then we should wait for all those to complete before continueing.
            if (executionContext.usingScheduler() && args.isRunParallel()) {
                log.debug("Kmer counting all ECQ groups in parallel, waiting for completion");
                this.conanExecutorService.executeScheduledWait(
                        jobIds,
                        args.getJobPrefix() + "-count-*",
                        ExitStatus.Type.COMPLETED_ANY,
                        args.getJobPrefix() + "-kmer-count-wait",
                        args.getOutputDir());
            }

            // Waiting point... clear job ids.
            jobIds.clear();

            JobOutputMap mergedOutputs = new JobOutputMap();

            // Now execute merge jobs if required
            for(Map.Entry<String, Set<File>> entry : jfCountOutputs.entrySet()) {

                String ecqName = entry.getKey();
                Set<File> fileSet = entry.getValue();

                // Only merge if there's more than one library
                if (fileSet.size() > 1) {
                    JobOutput jfOut = this.executeJellyfishMerger(args, ecqName, fileSet, new File(args.getOutputDir(), ecqName));

                    jobIds.add(jfOut.getJobId());
                    mergedOutputs.updateTracker(ecqName, jfOut.getOutputFile());
                }
            }

            // If we're using a scheduler and we have been asked to run each MECQ group for each library
            // in parallel, then we should wait for all those to complete before continueing.
            if (executionContext.usingScheduler() && args.isRunParallel()) {
                log.debug("Creating merged kmer counts for all ECQ groups in parallel, waiting for completion");
                this.conanExecutorService.executeScheduledWait(
                        jobIds,
                        args.getJobPrefix() + "-merge-*",
                        ExitStatus.Type.COMPLETED_ANY,
                        args.getJobPrefix() + "-kmer-merge-wait",
                        args.getOutputDir());
            }

            // Combine all jellyfish out maps
            jfCountOutputs.combine(mergedOutputs);

            String katGcpJobPrefix = args.getJobPrefix() + "-kat-gcp";

            // Run KAT GCP on everything
            JobOutputList katGcpFiles = this.executeKatGcp(jfCountOutputs, katGcpJobPrefix, args.getThreadsPerProcess(), args.getMemoryPerProcess(), args.isRunParallel());

            // If we're using a scheduler and we have been asked to run each MECQ group for each library
            // in parallel, then we should wait for all those to complete before continueing.
            if (executionContext.usingScheduler() && args.isRunParallel()) {
                log.debug("Creating merged kmer counts for all ECQ groups in parallel, waiting for completion");
                this.conanExecutorService.executeScheduledWait(
                        katGcpFiles.getJobIds(),
                        katGcpJobPrefix + "*",
                        ExitStatus.Type.COMPLETED_ANY,
                        args.getJobPrefix() + "-kat-gcp-wait",
                        args.getOutputDir());
            }

            // Run KAT plot density on everything (just use default values)
            String katPlotDensityJobPrefix = args.getJobPrefix() + "-kat-plot-density";

            JobOutputList katPlotFiles = this.executeKatPlotDensity(katGcpFiles.getFiles(), katPlotDensityJobPrefix, args.isRunParallel());

            // Don't worry about waiting for the plot files to finish... they should be quick and we don't need them for anything.


            log.info("Kmer counting of all reads finished.");
        }
        catch(ConanParameterException e) {
            throw new ProcessExecutionException(-1, e);
        }

        return true;
    }

    private JobOutputList executeKatPlotDensity(List<File> files, String jobPrefix, boolean runInParallel)
        throws InterruptedException, ProcessExecutionException, ConanParameterException {

        JobOutputList output = new JobOutputList();

        for(File inputFile : files) {

            File outputFile = new File(inputFile.getAbsolutePath() + ".png");

            KatPlotDensityV1 katPlotDensityProc = this.makeKatPlotDensityProc(inputFile, outputFile);

            int jobId = this.conanExecutorService.executeProcess(
                    katPlotDensityProc,
                    inputFile.getParentFile(),
                    jobPrefix + "-" + inputFile.getName(),
                    1,
                    0,
                    runInParallel).getJobId();

            output.add(new JobOutput(jobId, outputFile));
        }

        return output;
    }

    private KatPlotDensityV1 makeKatPlotDensityProc(File inputFile, File outputFile) {
        KatPlotDensityV1.Args katPlotDensityArgs = new KatPlotDensityV1.Args();
        katPlotDensityArgs.setOutput(outputFile);
        katPlotDensityArgs.setInput(inputFile);

        return new KatPlotDensityV1(katPlotDensityArgs);
    }

    private JobOutputList executeKatGcp(JobOutputMap jfCountOutputs, String jobPrefix, int threads, int memory, boolean runInParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException {

        JobOutputList output = new JobOutputList();

        for(Map.Entry<String, Set<File>> entry : jfCountOutputs.entrySet()) {

            for(File inputFile : entry.getValue()) {

                File outputPrefix = new File(inputFile.getAbsolutePath() + ".kat-gcp");

                KatGcpV1 katGcpProc = this.makeKatGcpProc(inputFile, outputPrefix, threads);

                int jobId = this.conanExecutorService.executeProcess(
                        katGcpProc,
                        inputFile.getParentFile(),
                        jobPrefix + "-" + inputFile.getName(),
                        threads,
                        memory,
                        runInParallel).getJobId();

                output.add(new JobOutput(jobId, new File(outputPrefix + ".mx")));
            }
        }

        return output;
    }

    private KatGcpV1 makeKatGcpProc(File inputFile, File outputPrefix, int threads) {

        KatGcpV1.Args katGcpArgs = new KatGcpV1.Args();
        katGcpArgs.setOutputPrefix(outputPrefix.getAbsolutePath());
        katGcpArgs.setThreads(threads);
        katGcpArgs.setJellyfishHash(inputFile);

        return new KatGcpV1(katGcpArgs);
    }


    protected JobOutput executeJellyfishCount(Args args, String ecqName, File outputDir, Library lib)
            throws ProcessExecutionException, InterruptedException, ConanParameterException {
        String suffix = "jellyfish_" + ecqName + "_" + lib.getName() + ".jf31";

        // Create the process
        JellyfishCountV11 jellyfishProcess = this.makeJellyfishCount(
                this.makeInputStringFromLib(lib),
                new File(new File(outputDir, ecqName), suffix).getAbsolutePath(),
                args.getOrganism(),
                args.getThreadsPerProcess());

        File outputFile = jellyfishProcess.getArgs().getOutputFile();

        // Create a job name
        String jobName = args.getJobPrefix() + "-count-" + suffix;

        // Start jellyfish
        final int id = this.conanExecutorService.executeProcess(
                jellyfishProcess,
                args.getOutputDir(),
                jobName,
                args.getThreadsPerProcess(),
                args.getMemoryPerProcess(),
                args.isRunParallel()).getJobId();

        return new JobOutput(id, outputFile);
    }

    protected JobOutput executeJellyfishMerger(Args args, String ecqName, Set<File> fileSet, File outputDir)
            throws InterruptedException, ProcessExecutionException, ConanParameterException {

        String suffix = "jellyfish_" + ecqName + "_all.jf31_0";

        String jobName = args.getJobPrefix() + "-merge-" + suffix;

        List<File> files = new ArrayList<>();
        files.addAll(fileSet);

        File outputFile = new File(outputDir, suffix);

        JellyfishMergeV11 jellyfishMerge = this.makeJellyfishMerge(files, outputFile, args.getOrganism());

        int id = this.conanExecutorService.executeProcess(
                jellyfishMerge,
                args.getOutputDir(),
                jobName,
                args.getThreadsPerProcess(),
                args.getMemoryPerProcess(),
                args.isRunParallel()).getJobId();

        return new JobOutput(id, outputFile);
    }

    /**
     * Hopefully this is a conservative estimate for most projects.  We ignore very low count kmers so hopefully this
     * size just needs to accomodate genuine kmers, and should therefore be roughly equivalent to genomesize * ploidy.
     * We multiply by 5 to be on the safe side and make sure we can handle some sequencing errors.
     * @param organism Details about the organism's genome (specifically the genome size and ploidy)
     * @return An overestimate of the expected jellyfish hash size
     */
    public static long guessJellyfishHashSize(Organism organism) {

        long hashSize = organism.getEstGenomeSize() * organism.getPloidy() * 5;

        // Check to make sure we don't have anything weird... if we do use a default of 5 billion (this should be enough
        // for most organisms running through RAMPART, although it might still fail on low mem systems.  Need to think
        // this through...
        return hashSize < 0 ? 5000000000L : hashSize;
    }

    protected JellyfishCountV11 makeJellyfishCount(String inputFilesStr, String outputPrefix, Organism organism, int threads) throws ProcessExecutionException {

        JellyfishCountV11.Args jellyfishArgs = new JellyfishCountV11.Args();
        jellyfishArgs.setOutputPrefix(outputPrefix);
        jellyfishArgs.setLowerCount(3);     // Lets save some space on disk by ignoring anything that only occurs only a couple of times
        jellyfishArgs.setHashSize(guessJellyfishHashSize(organism));
        jellyfishArgs.setMerLength(31);     // 31 should be sufficient for all organisms
        jellyfishArgs.setBothStrands(true);
        jellyfishArgs.setThreads(threads);
        jellyfishArgs.setCounterLength(32); // This is probably overkill... consider changing this later (or using jellyfish
        // 2 which auto adjusts this figure)
        jellyfishArgs.setInputFile(inputFilesStr);

        return new JellyfishCountV11(this.conanExecutorService, jellyfishArgs);
    }

    protected JellyfishMergeV11 makeJellyfishMerge(List<File> inputFiles, File outputFile, Organism organism) {

        // Setup jellyfish for merging all the reads for this mass run
        JellyfishMergeV11.Args mergeArgs = new JellyfishMergeV11.Args();
        mergeArgs.setBufferSize(guessJellyfishHashSize(organism) * inputFiles.size());
        mergeArgs.setOutputFile(outputFile);
        mergeArgs.setInputFiles(inputFiles);

        return new JellyfishMergeV11(this.conanExecutorService, mergeArgs);
    }

    protected String makeInputStringFromLib(Library lib) throws ProcessExecutionException {
        String inputFilesStr = "";

        for (File file : lib.getFiles()) {

            if (!file.exists()) {
                throw new ProcessExecutionException(2, "Couldn't locate: " + file.getAbsolutePath() + "; for kmer counting.");
            }

            inputFilesStr = StringUtils.join(file.getAbsolutePath(), " ");
        }

        inputFilesStr = inputFilesStr.trim();

        // Check we have something plausible to work with
        if (inputFilesStr.isEmpty())
            throw new ProcessExecutionException(2, "Couldn't locate raw library files: " +
                    lib.getName());

        return inputFilesStr;
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

        if (!new JellyfishCountV11(this.conanExecutorService).isOperational(executionContext)) {
            log.warn("Jellyfish Count is not operational.");
            return false;
        }

        if (!new JellyfishMergeV11(this.conanExecutorService).isOperational(executionContext)) {
            log.warn("Jellyfish Merge is not operational.");
            return false;
        }

        KatGcpV1 katGcpProc = new KatGcpV1();
        katGcpProc.setConanProcessService(this.getConanProcessService());

        if (!katGcpProc.isOperational(executionContext)) {
            log.warn("KAT GCP is not operational.");
            return false;
        }

        KatPlotDensityV1 katPlotDensityProc = new KatPlotDensityV1();
        katPlotDensityProc.setConanProcessService(this.getConanProcessService());

        if (!katPlotDensityProc.isOperational(executionContext)) {
            log.warn("KAT Plot Density is not operational.");
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
        private List<Mecq.EcqArgs> allMecqs;                 // All mecq configurations
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


        public Args(Element element, List<Library> allLibraries, List<Mecq.EcqArgs> allMecqs, String jobPrefix, File mecqDir,
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

        public List<Mecq.EcqArgs> getAllMecqs() {
            return allMecqs;
        }

        public void setAllMecqs(List<Mecq.EcqArgs> allMecqs) {
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
