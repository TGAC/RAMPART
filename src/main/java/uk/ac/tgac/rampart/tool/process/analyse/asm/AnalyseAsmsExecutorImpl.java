/**
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2013  Daniel Mapleson - TGAC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package uk.ac.tgac.rampart.tool.process.analyse.asm;

import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.process.asm.stats.CegmaV2_4Args;
import uk.ac.tgac.conan.process.asm.stats.CegmaV2_4Process;
import uk.ac.tgac.conan.process.asm.stats.QuastV2_2Args;
import uk.ac.tgac.conan.process.asm.stats.QuastV2_2Process;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishCountV11;
import uk.ac.tgac.conan.process.kmer.kat.KatCompV1;
import uk.ac.tgac.conan.process.kmer.kat.KatPlotSpectraCnV1;
import uk.ac.tgac.rampart.tool.RampartExecutorImpl;

import java.io.File;

/**
 * User: maplesod
 * Date: 23/08/13
 * Time: 11:18
 */
public class AnalyseAsmsExecutorImpl extends RampartExecutorImpl implements AnalyseAsmsExecutor {

    public ExecutionResult executeKmerCount(JellyfishCountV11 jellyfishProcess, ExecutionContext executionContext,
                                            String jobName, boolean runParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException {

        JellyfishCountV11.Args args = (JellyfishCountV11.Args)jellyfishProcess.getProcessArgs();

        ExecutionContext executionContextCopy = executionContext.copy();
        executionContextCopy.setContext(jobName, !runParallel,
                new File(new File(args.getOutputPrefix()).getParentFile(), jobName + ".log"));

        if (executionContextCopy.usingScheduler()) {
            executionContext.getScheduler().getArgs().setThreads(args.getThreads());
        }

        return this.conanProcessService.execute(jellyfishProcess, executionContextCopy);
    }

    @Override
    public ExecutionResult executeKatComp(KatCompV1 katCompProcess, ExecutionContext executionContext, String jobName,
                                          boolean runParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException {

        KatCompV1.Args args = (KatCompV1.Args)katCompProcess.getProcessArgs();

        ExecutionContext executionContextCopy = executionContext.copy();
        executionContextCopy.setContext(jobName, !runParallel,
                new File(new File(args.getOutputPrefix()).getParentFile(), jobName + ".log"));

        if (executionContextCopy.usingScheduler()) {
            executionContext.getScheduler().getArgs().setThreads(args.getThreads());
        }

        return this.conanProcessService.execute(katCompProcess, executionContextCopy);
    }

    @Override
    public ExecutionResult executeKatPlotSpectraCn(KatPlotSpectraCnV1 katPlotSpectraCnProcess, ExecutionContext executionContext,
                                                   String jobName, boolean runParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException {

        KatPlotSpectraCnV1.Args args = (KatPlotSpectraCnV1.Args)katPlotSpectraCnProcess.getProcessArgs();

        ExecutionContext executionContextCopy = executionContext.copy();
        executionContextCopy.setContext(jobName, !runParallel,
                new File(args.getOutput().getParentFile(), jobName + ".log"));

        return this.conanProcessService.execute(katPlotSpectraCnProcess, executionContextCopy);
    }


    public ExecutionResult executeCegma(CegmaV2_4Process cegmaProcess, ExecutionContext executionContext, String jobName,
                                        boolean runParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException {

        CegmaV2_4Args args = (CegmaV2_4Args)cegmaProcess.getProcessArgs();

        ExecutionContext executionContextCopy = executionContext.copy();
        executionContextCopy.setContext(jobName, !runParallel, new File(args.getOutputPrefix().getParentFile(), jobName + ".log"));

        if (executionContextCopy.usingScheduler()) {
            executionContext.getScheduler().getArgs().setThreads(args.getThreads());
        }

        return this.conanProcessService.execute(cegmaProcess, executionContextCopy);
    }



    public ExecutionResult executeQuast(QuastV2_2Process quastProcess, ExecutionContext executionContext, String jobName,
                                        boolean runParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException {

        QuastV2_2Args args = (QuastV2_2Args)quastProcess.getProcessArgs();

        ExecutionContext executionContextCopy = executionContext.copy();
        executionContextCopy.setContext(jobName, !runParallel, new File(args.getOutputDir(), jobName + ".log"));

        if (executionContextCopy.usingScheduler()) {
            executionContext.getScheduler().getArgs().setThreads(args.getThreads());
        }

        return this.conanProcessService.execute(quastProcess, executionContextCopy);
    }


    @Override
    public void createSymbolicLink(File sourceFile, File destFile) throws InterruptedException, ProcessExecutionException {
        this.conanProcessService.createLocalSymbolicLink(sourceFile, destFile);
    }
}
