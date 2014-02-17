package uk.ac.tgac.rampart.tool.process.analyse.reads;

import uk.ac.ebi.fgpt.conan.core.param.*;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 20/01/14
 * Time: 13:37
 * To change this template use File | Settings | File Templates.
 */
public class AnalyseReadsParams extends AbstractProcessParams {


    private ConanParameter kmerAnalysis;
    private ConanParameter allLibraries;
    private ConanParameter allMecqs;
    private ConanParameter jobPrefix;
    private ConanParameter mecqDir;
    private ConanParameter runParallel;
    private ConanParameter outputDir;
    private ConanParameter organism;
    private ConanParameter threadsPerProcess;
    private ConanParameter memoryPerProcess;

    public AnalyseReadsParams() {

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
                this.kmerAnalysis,
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
