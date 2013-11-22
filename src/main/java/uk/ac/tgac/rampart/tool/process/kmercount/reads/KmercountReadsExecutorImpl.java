package uk.ac.tgac.rampart.tool.process.kmercount.reads;

import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.SchedulerArgs;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishCountV11Args;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishCountV11Process;
import uk.ac.tgac.rampart.tool.RampartExecutorImpl;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 20/11/13
 * Time: 13:28
 * To change this template use File | Settings | File Templates.
 */
public class KmercountReadsExecutorImpl extends RampartExecutorImpl implements KmerCountReadsExecutor {


    @Override
    public int executeKmerCount(JellyfishCountV11Process jellyfishProcess, File outputDir, String jobName, boolean runInParallel)
            throws InterruptedException, ProcessExecutionException {

        // Duplicate the execution context so we don't modify the original accidentally.
        ExecutionContext executionContextCopy = this.executionContext.copy();
        executionContextCopy.setContext(
                jobName,
                executionContextCopy.usingScheduler() ? !runInParallel : true,
                new File(outputDir, jobName + ".log"));

        if (this.executionContext.usingScheduler()) {

            SchedulerArgs schedulerArgs = executionContextCopy.getScheduler().getArgs();
            JellyfishCountV11Args jellyfishArgs = jellyfishProcess.getArgs();

            schedulerArgs.setThreads(jellyfishArgs.getThreads());
            schedulerArgs.setMemoryMB(jellyfishArgs.getMemoryMb());
        }

        ExecutionResult result = this.conanProcessService.execute(jellyfishProcess, executionContextCopy);

        return result.getJobId();
    }

}
