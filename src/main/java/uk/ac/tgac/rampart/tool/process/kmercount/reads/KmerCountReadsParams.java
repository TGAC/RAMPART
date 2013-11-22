package uk.ac.tgac.rampart.tool.process.kmercount.reads;

import uk.ac.ebi.fgpt.conan.core.param.DefaultConanParameter;
import uk.ac.ebi.fgpt.conan.core.param.FlagParameter;
import uk.ac.ebi.fgpt.conan.core.param.NumericParameter;
import uk.ac.ebi.fgpt.conan.core.param.PathParameter;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 20/11/13
 * Time: 13:11
 * To change this template use File | Settings | File Templates.
 */
public class KmerCountReadsParams implements ProcessParams {

    private ConanParameter allLibraries;
    private ConanParameter allMecqs;
    private ConanParameter jobPrefix;
    private ConanParameter mecqDir;
    private ConanParameter runParallel;
    private ConanParameter outputDir;
    private ConanParameter organism;
    private ConanParameter threadsPerProcess;
    private ConanParameter memoryPerProcess;

    public KmerCountReadsParams() {

         this.allLibraries = new DefaultConanParameter(
                 "all_libraries",
                 "All libraries to process",
                 false,
                 false,
                 true);

        this.allMecqs = new DefaultConanParameter(
                "all_mecqs",
                "All mecq processes that have taken place",
                false,
                false,
                true);

        this.jobPrefix = new DefaultConanParameter(
                "job_prefix",
                "All libraries to process",
                false,
                true,
                true);

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

        this.organism = new DefaultConanParameter(
                "organism",
                "Details about the organism that was sequenced and is being assembled",
                false,
                false,
                true);

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
    public List<ConanParameter> getConanParameters() {
        return new ArrayList<>(Arrays.asList(
                new ConanParameter[]{
                        this.allLibraries,
                        this.allMecqs,
                        this.jobPrefix,
                        this.mecqDir,
                        this.runParallel,
                        this.outputDir,
                        this.organism,
                        this.threadsPerProcess,
                        this.memoryPerProcess
                }));
    }
}
