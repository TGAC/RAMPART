package uk.ac.tgac.rampart.tool.process.analyse.reads.kmer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.process.ec.AbstractErrorCorrectorArgs;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishCountV11;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishMergeV11;
import uk.ac.tgac.conan.process.kmer.kat.KatGcpV1;
import uk.ac.tgac.conan.process.kmer.kat.KatPlotDensityV1;
import uk.ac.tgac.rampart.tool.process.analyse.reads.AnalyseReadsExecutor;
import uk.ac.tgac.rampart.tool.process.analyse.reads.AnalyseReadsExecutorImpl;
import uk.ac.tgac.rampart.tool.process.mecq.EcqArgs;
import uk.ac.tgac.rampart.util.JobOutput;
import uk.ac.tgac.rampart.util.JobOutputList;
import uk.ac.tgac.rampart.util.JobOutputMap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 20/11/13
 * Time: 13:12
 * To change this template use File | Settings | File Templates.
 */
public class KmerAnalysisReadsProcess extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(KmerAnalysisReadsProcess.class);

    private AnalyseReadsExecutor analyseReadsExecutor;

    public KmerAnalysisReadsProcess() {
        this(new KmerAnalysisReadsArgs());
    }

    public KmerAnalysisReadsProcess(ProcessArgs args) {
        super("", args, new KmerAnalysisReadsParams());

        this.analyseReadsExecutor = new AnalyseReadsExecutorImpl();
    }

    @Override
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        try {

            log.info("Starting Kmer Counting on all Reads");

            // Initialise the object that makes system calls
            this.analyseReadsExecutor.initialise(this.getConanProcessService(), executionContext);

            // Create shortcut to args for convienience
            KmerAnalysisReadsArgs args = (KmerAnalysisReadsArgs) this.getProcessArgs();

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
                for(EcqArgs ecqArgs : args.getAllMecqs()) {

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
                this.analyseReadsExecutor.executeScheduledWait(
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
                this.analyseReadsExecutor.executeScheduledWait(
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
            JobOutputList katGcpFiles = this.executeKatGcp(jfCountOutputs, katGcpJobPrefix, args.getThreadsPerProcess(), args.isRunParallel());

            // If we're using a scheduler and we have been asked to run each MECQ group for each library
            // in parallel, then we should wait for all those to complete before continueing.
            if (executionContext.usingScheduler() && args.isRunParallel()) {
                log.debug("Creating merged kmer counts for all ECQ groups in parallel, waiting for completion");
                this.analyseReadsExecutor.executeScheduledWait(
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

            int jobId = this.analyseReadsExecutor.executeKatPlotDensity(katPlotDensityProc, inputFile.getParentFile(),
                    jobPrefix + "-" + inputFile.getName(),
                    runInParallel);

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

    private JobOutputList executeKatGcp(JobOutputMap jfCountOutputs, String jobPrefix, int threads, boolean runInParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException {

        JobOutputList output = new JobOutputList();

        for(Map.Entry<String, Set<File>> entry : jfCountOutputs.entrySet()) {

            for(File inputFile : entry.getValue()) {

                File outputPrefix = new File(inputFile.getAbsolutePath() + ".kat-gcp");

                KatGcpV1 katGcpProc = this.makeKatGcpProc(inputFile, outputPrefix, threads);

                int jobId = this.analyseReadsExecutor.executeKatGcp(katGcpProc, inputFile.getParentFile(),
                        jobPrefix + "-" + inputFile.getName(),
                        threads, runInParallel);

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


    protected JobOutput executeJellyfishCount(KmerAnalysisReadsArgs args, String ecqName, File outputDir, Library lib)
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
        int id = this.analyseReadsExecutor.executeKmerCount(jellyfishProcess, args.getOutputDir(), jobName, args.isRunParallel());

        return new JobOutput(id, outputFile);
    }

    protected JobOutput executeJellyfishMerger(KmerAnalysisReadsArgs args, String ecqName, Set<File> fileSet, File outputDir)
            throws InterruptedException, ProcessExecutionException, ConanParameterException {

        String suffix = "jellyfish_" + ecqName + "_all.jf31_0";

        String jobName = args.getJobPrefix() + "-merge-" + suffix;

        List<File> files = new ArrayList<>();
        files.addAll(fileSet);

        File outputFile = new File(outputDir, suffix);

        JellyfishMergeV11 jellyfishMerge = this.makeJellyfishMerge(files, outputFile, args.getOrganism());

        int id = this.analyseReadsExecutor.executeKmerMerge(jellyfishMerge, args.getOutputDir(), jobName, args.isRunParallel());

        return new JobOutput(id, outputFile);
    }

    /**
     * Hopefully this is a conservative estimate for most projects.
     * @param organism
     * @return
     */
    public static int guessJellyfishHashSize(Organism organism) {
        return organism.getEstGenomeSize() * organism.getPloidy() * 10;
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

        return new JellyfishCountV11(jellyfishArgs);
    }

    protected JellyfishMergeV11 makeJellyfishMerge(List<File> inputFiles, File outputFile, Organism organism) {

        // Setup jellyfish for merging all the reads for this mass run
        JellyfishMergeV11.Args mergeArgs = new JellyfishMergeV11.Args();
        mergeArgs.setBufferSize(guessJellyfishHashSize(organism) * inputFiles.size());
        mergeArgs.setOutputFile(outputFile);
        mergeArgs.setInputFiles(inputFiles);

        return new JellyfishMergeV11(mergeArgs);
    }

    protected String makeInputStringFromEcq(AbstractErrorCorrectorArgs args) throws ProcessExecutionException {

        String inputFilesStr = "";

        for (File file : args.getCorrectedFiles()) {

            if (!file.exists()) {
                throw new ProcessExecutionException(2, "Couldn't locate: " + file.getAbsolutePath() + "; for kmer counting.");
            }

            StringUtils.join(file.getAbsolutePath(), " ");
        }

        inputFilesStr = inputFilesStr.trim();

        // Check we have something plausible to work with
        if (inputFilesStr.isEmpty())
            throw new ProcessExecutionException(2, "Couldn't locate output files for at: " +
                    args.getOutputDir().getAbsolutePath());

        return inputFilesStr;
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

        JellyfishCountV11 jfCountProc = new JellyfishCountV11();
        jfCountProc.setConanProcessService(this.getConanProcessService());

        if (!jfCountProc.isOperational(executionContext)) {
            log.warn("Jellyfish Count is not operational.");
            return false;
        }

        JellyfishMergeV11 jfMergeProc = new JellyfishMergeV11();
        jfMergeProc.setConanProcessService(this.getConanProcessService());

        if (!jfMergeProc.isOperational(executionContext)) {
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
}
