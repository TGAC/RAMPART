package uk.ac.tgac.rampart.tool.process.analyse.reads;

import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.SchedulerArgs;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishCountV11;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishMergeV11;
import uk.ac.tgac.conan.process.kmer.kat.KatGcpV1;
import uk.ac.tgac.conan.process.kmer.kat.KatPlotDensityV1;
import uk.ac.tgac.rampart.tool.RampartExecutorImpl;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 20/11/13
 * Time: 13:28
 * To change this template use File | Settings | File Templates.
 */
public class AnalyseReadsExecutorImpl extends RampartExecutorImpl implements AnalyseReadsExecutor {


    @Override
    public int executeKmerCount(JellyfishCountV11 jellyfishProcess, File outputDir, String jobName, boolean runInParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException {

        // Duplicate the execution context so we don't modify the original accidentally.
        ExecutionContext executionContextCopy = this.executionContext.copy();
        executionContextCopy.setContext(
                jobName,
                executionContextCopy.usingScheduler() ? !runInParallel : true,
                new File(outputDir, jobName + ".log"));

        if (this.executionContext.usingScheduler()) {

            SchedulerArgs schedulerArgs = executionContextCopy.getScheduler().getArgs();
            JellyfishCountV11.Args jellyfishArgs = jellyfishProcess.getArgs();

            schedulerArgs.setThreads(jellyfishArgs.getThreads());
            schedulerArgs.setMemoryMB(jellyfishArgs.getMemoryMb());
        }

        ExecutionResult result = this.conanProcessService.execute(jellyfishProcess, executionContextCopy);

        return result.getJobId();
    }

    @Override
    public int executeKmerMerge(JellyfishMergeV11 jellyfishMerge, File outputDir, String jobName, boolean runInParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException {
        // Duplicate the execution context so we don't modify the original accidentally.
        ExecutionContext executionContextCopy = this.executionContext.copy();
        executionContextCopy.setContext(
                jobName,
                executionContextCopy.usingScheduler() ? !runInParallel : true,
                new File(outputDir, jobName + ".log"));

        ExecutionResult result = this.conanProcessService.execute(jellyfishMerge, executionContextCopy);

        return result.getJobId();
    }

    @Override
    public int executeKatGcp(KatGcpV1 katGcpProc, File outputDir, String jobName, int threads, boolean runInParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException {
        // Duplicate the execution context so we don't modify the original accidentally.
        ExecutionContext executionContextCopy = this.executionContext.copy();
        executionContextCopy.setContext(
                jobName,
                executionContextCopy.usingScheduler() ? !runInParallel : true,
                new File(outputDir, jobName + ".log"));

        ExecutionResult result = this.conanProcessService.execute(katGcpProc, executionContextCopy);

        return result.getJobId();
    }

    @Override
    public int executeKatPlotDensity(KatPlotDensityV1 katPlotDensityProc, File outputDir, String jobName, boolean runInParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException {

        // Duplicate the execution context so we don't modify the original accidentally.
        ExecutionContext executionContextCopy = this.executionContext.copy();
        executionContextCopy.setContext(
                jobName,
                executionContextCopy.usingScheduler() ? !runInParallel : true,
                new File(outputDir, jobName + ".log"));

        ExecutionResult result = this.conanProcessService.execute(katPlotDensityProc, executionContextCopy);

        return result.getJobId();
    }


}
