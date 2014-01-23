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
import uk.ac.tgac.conan.process.asm.stats.CegmaV2_4Process;
import uk.ac.tgac.conan.process.asm.stats.QuastV2_2Process;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishCountV11;
import uk.ac.tgac.conan.process.kmer.kat.KatCompV1;
import uk.ac.tgac.conan.process.kmer.kat.KatPlotSpectraCnV1;
import uk.ac.tgac.rampart.tool.RampartExecutor;

import java.io.File;

/**
 * User: maplesod
 * Date: 23/08/13
 * Time: 11:18
 */
public interface AnalyseAsmsExecutor extends RampartExecutor {

    ExecutionResult executeQuast(QuastV2_2Process quast, ExecutionContext executionContext, String jobName, boolean runParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException;

    ExecutionResult executeCegma(CegmaV2_4Process cegma, ExecutionContext executionContext, String jobName, boolean runParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException;

    ExecutionResult executeKmerCount(JellyfishCountV11 jellyfishProcess, ExecutionContext executionContext, String jobName, boolean runParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException;

    ExecutionResult executeKatComp(KatCompV1 katCompProcess, ExecutionContext executionContext, String jobName, boolean runParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException;

    ExecutionResult executeKatPlotSpectraCn(KatPlotSpectraCnV1 katPlotSpectraCnProcess, ExecutionContext executionContext, String jobName, boolean runParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException;;

    void createSymbolicLink(File sourceFile, File destFile) throws InterruptedException, ProcessExecutionException;

}
