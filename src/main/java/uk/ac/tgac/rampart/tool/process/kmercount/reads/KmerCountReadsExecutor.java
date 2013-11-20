package uk.ac.tgac.rampart.tool.process.kmercount.reads;

import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishCountV11Process;
import uk.ac.tgac.rampart.tool.RampartExecutor;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 20/11/13
 * Time: 13:27
 * To change this template use File | Settings | File Templates.
 */
public interface KmerCountReadsExecutor extends RampartExecutor {

    void executeKmerCount(JellyfishCountV11Process jellyfishProcess, File outputDir, String jobName, boolean runInParallel)
            throws InterruptedException, ProcessExecutionException;
}
