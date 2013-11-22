package uk.ac.tgac.rampart.tool.process.kmercount.reads;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.process.ec.ErrorCorrector;
import uk.ac.tgac.conan.process.ec.ErrorCorrectorArgs;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishCountV11Args;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishCountV11Process;
import uk.ac.tgac.rampart.tool.process.mecq.EcqArgs;
import uk.ac.tgac.rampart.tool.process.mecq.MecqProcess;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 20/11/13
 * Time: 13:12
 * To change this template use File | Settings | File Templates.
 */
public class KmerCountReadsProcess extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(KmerCountReadsProcess.class);

    private KmerCountReadsExecutor kmerCountReadsExecutor;

    public KmerCountReadsProcess() {
        this(new KmerCountReadsArgs());
    }

    public KmerCountReadsProcess(ProcessArgs args) {
        super("", args, new KmerCountReadsParams());

        this.kmerCountReadsExecutor = new KmercountReadsExecutorImpl();
    }

    @Override
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        log.info("Starting Kmer Counting on all Reads");

        // Initialise the object that makes system calls
        this.kmerCountReadsExecutor.initialise(this.conanProcessService, executionContext);

        // Create shortcut to args for convienience
        KmerCountReadsArgs args = (KmerCountReadsArgs) this.getProcessArgs();

        // Create the output directory
        args.getOutputDir().mkdirs();

        List<Integer> jobIds = new ArrayList<>();

        // Start jellyfish on all RAW datasets
        for(Library lib : args.getAllLibraries()) {

            // Execute jellyfish and add id to list of job ids
            jobIds.add(this.executeJellyfish(args, "raw", lib));
        }

        // Also start jellyfish on all the prep-processed libraries from MECQ
        if (args.getAllMecqs() != null) {
            for(EcqArgs ecqArgs : args.getAllMecqs()) {
                for(Library lib : ecqArgs.getLibraries()) {

                    // Check the library and make sure we can create a modified library containing the output from this ECQ.
                    Library modifiedLib = validateLib(lib, ecqArgs, args.getMecqDir());

                    // Add jellyfish id to list of job ids
                    jobIds.add(this.executeJellyfish(args, ecqArgs.getName(), modifiedLib));
                }
            }
        }


        // If we're using a scheduler and we have been asked to run each MECQ group for each library
        // in parallel, then we should wait for all those to complete before continueing.
        if (executionContext.usingScheduler() && args.isRunParallel()) {
            log.debug("Running all ECQ groups in parallel, waiting for completion");
            this.kmerCountReadsExecutor.executeScheduledWait(
                    jobIds,
                    args.getJobPrefix() + "-jellyfish*",
                    ExitStatus.Type.COMPLETED_SUCCESS,
                    args.getJobPrefix() + "-wait",
                    args.getOutputDir());
        }

        log.info("Kmer counting of all reads finished.");

        return true;
    }

    protected int executeJellyfish(KmerCountReadsArgs args, String ecqName, Library lib)
            throws ProcessExecutionException, InterruptedException {
        String suffix = "jellyfish_" + ecqName + "_" + lib.getName();

        // Create the process
        JellyfishCountV11Process jellyfishProcess = this.makeJellyfish(
                this.makeInputStringFromLib(lib),
                args.getOutputDir().getAbsolutePath() + "/" + suffix,
                args.getOrganism(),
                args.getThreadsPerProcess());

        // Create a job name
        String jobName = args.getJobPrefix() + "-" + suffix;

        // Start jellyfish
        return this.kmerCountReadsExecutor.executeKmerCount(jellyfishProcess, args.getOutputDir(), jobName, args.isRunParallel());
    }

    protected JellyfishCountV11Process makeJellyfish(String inputFilesStr, String outputPrefix, Organism organism, int threads) throws ProcessExecutionException {

        JellyfishCountV11Args jellyfishArgs = new JellyfishCountV11Args();
        jellyfishArgs.setOutputPrefix(outputPrefix);
        jellyfishArgs.setLowerCount(2);     // Lets save space on disk by ignoring anything that only occurs once
        jellyfishArgs.setHashSize(organism.getEstGenomeSize() * organism.getPloidy() * 10);  // VERY conservative guess based on genome size and ploidy
        jellyfishArgs.setMerLength(31);     // 31 should be sufficient for all organisms
        jellyfishArgs.setBothStrands(true);
        jellyfishArgs.setThreads(threads);
        jellyfishArgs.setCounterLength(32); // This is probably overkill... consider changing this later (or using jellyfish
        // 2 which auto adjusts this figure)
        jellyfishArgs.setInputFile(inputFilesStr);

        return new JellyfishCountV11Process(jellyfishArgs);
    }

    protected String makeInputStringFromEcq(ErrorCorrectorArgs args) throws ProcessExecutionException {

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

    protected Library validateLib(Library lib, EcqArgs ecqArgs, File mecqDir) throws ProcessExecutionException {

        Library modLib = lib.copy();

        ErrorCorrector ec = new MecqProcess().makeErrorCorrector(ecqArgs, modLib, mecqDir);
        List<File> files = ec.getArgs().getCorrectedFiles();

        try {
            if (modLib.isPairedEnd()) {
                if (files.size() < 2 || files.size() > 3) {
                    throw new IOException("Paired end library: " + modLib.getName() + " from " + ecqArgs.getName() + " does not have two or three files");
                }

                modLib.setFiles(files.get(0), files.get(1));
            }
            else {
                if (files.size() != 1) {
                    throw new IOException("Single end library: " + modLib.getName() + " from " + ecqArgs.getName() + " does not have one file");
                }

                modLib.setFiles(files.get(0), null);
            }

            return modLib;
        }
        catch(IOException ioe) {
            throw new ProcessExecutionException(3, ioe);
        }
    }


    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public String getName() {
        return "KmerCountReads";
    }
}
