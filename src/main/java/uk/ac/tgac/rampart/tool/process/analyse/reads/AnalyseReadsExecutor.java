package uk.ac.tgac.rampart.tool.process.analyse.reads;

import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishCountV11;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishMergeV11;
import uk.ac.tgac.conan.process.kmer.kat.KatGcpV1;
import uk.ac.tgac.conan.process.kmer.kat.KatPlotDensityV1;
import uk.ac.tgac.rampart.tool.RampartExecutor;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 20/11/13
 * Time: 13:27
 * To change this template use File | Settings | File Templates.
 */
public interface AnalyseReadsExecutor extends RampartExecutor {

    int executeKmerCount(JellyfishCountV11 jellyfishProcess, File outputDir, String jobName, boolean runInParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException;

    int executeKmerMerge(JellyfishMergeV11 jellyfishMerge, File outputDir, String jobName, boolean runParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException;

    int executeKatGcp(KatGcpV1 katGcpProc, File outputDir, String jobName, int threads, boolean runInParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException;

    int executeKatPlotDensity(KatPlotDensityV1 katPlotDensityProc, File outputDir, String jobName, boolean runInParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException;
}
