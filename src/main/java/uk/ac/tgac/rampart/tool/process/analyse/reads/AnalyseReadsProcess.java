package uk.ac.tgac.rampart.tool.process.analyse.reads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.tool.pipeline.RampartStageArgs;
import uk.ac.tgac.rampart.tool.process.analyse.reads.kmer.KmerAnalysisReadsArgs;
import uk.ac.tgac.rampart.tool.process.analyse.reads.kmer.KmerAnalysisReadsProcess;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 20/01/14
 * Time: 11:22
 * To change this template use File | Settings | File Templates.
 */
public class AnalyseReadsProcess extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(AnalyseReadsProcess.class);

    private AnalyseReadsExecutor analyseReadsExecutor;

    public AnalyseReadsProcess() {
        this(new AnalyseReadsArgs());
    }

    public AnalyseReadsProcess(RampartStageArgs args) {
        super("", args, new AnalyseReadsParams());

        this.analyseReadsExecutor = new AnalyseReadsExecutorImpl();
    }


    @Override
    public String getName() {
        return "AnalyseReads";
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        // Create shortcut to args for convenience
        AnalyseReadsArgs args = (AnalyseReadsArgs) this.getProcessArgs();

        boolean kmer = true;

        // Check what analyses are requested and check if those are operational
        if (args.isKmerAnalysis()) {

            KmerAnalysisReadsProcess proc = new KmerAnalysisReadsProcess();
            proc.setConanProcessService(this.getConanProcessService());

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
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        log.info("Starting Read Analysis");

        // Initialise the object that makes system calls
        this.analyseReadsExecutor.initialise(this.getConanProcessService(), executionContext);

        // Create shortcut to args for convenience
        AnalyseReadsArgs args = (AnalyseReadsArgs) this.getProcessArgs();

        // Check what analyses are requested and check if those are operational
        if (args.isKmerAnalysis()) {

            KmerAnalysisReadsArgs kmerArgs = new KmerAnalysisReadsArgs();
            kmerArgs.setAllLibraries(args.getAllLibraries());
            kmerArgs.setAllMecqs(args.getAllMecqs());
            kmerArgs.setOrganism(args.getOrganism());
            kmerArgs.setOutputDir(new File(args.getOutputDir(), "kmer"));
            kmerArgs.setJobPrefix(args.getJobPrefix() + "-kmer");
            kmerArgs.setMecqDir(args.getMecqDir());
            kmerArgs.setThreadsPerProcess(args.getThreadsPerProcess());
            kmerArgs.setRunParallel(args.isRunParallel());

            KmerAnalysisReadsProcess proc = new KmerAnalysisReadsProcess(kmerArgs);
            proc.setConanProcessService(this.getConanProcessService());
            proc.execute(executionContext);
        }

        return true;
    }
}
