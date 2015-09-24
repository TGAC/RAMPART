/*
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2015  Daniel Mapleson - TGAC
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
 */

package uk.ac.tgac.rampart.stage.analyse.asm.analysers;

import org.apache.commons.io.FileUtils;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.process.kmer.kat.KatCompV2;
import uk.ac.tgac.rampart.stage.Mecq;
import uk.ac.tgac.rampart.stage.analyse.asm.AnalyseAssembliesArgs;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStatsTable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 22/01/14
 * Time: 11:19
 * To change this template use File | Settings | File Templates.
 */
@MetaInfServices(AssemblyAnalyser.class)
public class KatAsmAnalyser extends AbstractConanProcess implements AssemblyAnalyser {

    private static Logger log = LoggerFactory.getLogger(KatAsmAnalyser.class);

    private AnalyseAssembliesArgs.ToolArgs args;

    @Override
    public boolean isOperational(ExecutionContext executionContext) {
        KatCompV2 katCompProc = new KatCompV2(this.conanExecutorService);
        return katCompProc.isOperational(executionContext);
    }

    @Override
    public void setArgs(AnalyseAssembliesArgs.ToolArgs args) {
        this.args = args;
    }

    @Override
    public List<ExecutionResult> execute(List<File> assemblies, File outputDir, String jobPrefix, ConanExecutorService ces)
            throws InterruptedException, ProcessExecutionException, ConanParameterException, IOException {

        List<ExecutionResult> jobResults = new ArrayList<>();

        if (outputDir.exists()) {
            FileUtils.deleteDirectory(outputDir);
        }

        outputDir.mkdirs();

        log.info("Running KAT comp on " + assemblies.size() + " assembly hashes.");

        // Loop through assemblies
        for(File asm : assemblies) {

            // Loop through each library and modified library
            for(Library lib : args.getSample().libraries) {
                // Execute jellyfish and add id to list of job ids
                ExecutionResult res = this.executeKatComp("raw", outputDir, lib, asm);
                jobResults.add(res);
            }

            if (args.getSample().ecqArgList != null) {
                for(Mecq.EcqArgs ecqArgs : args.getSample().ecqArgList) {
                    for(Library lib : ecqArgs.getOutputLibraries(args.getSample())) {
                        // Execute jellyfish and add id to list of job ids
                        ExecutionResult res = this.executeKatComp(ecqArgs.getName(), outputDir, lib, asm);
                        jobResults.add(res);
                    }
                }
            }
        }

        return jobResults;
    }

    private ExecutionResult executeKatComp(String mecq, File outputDir, Library lib, File assembly) throws ProcessExecutionException, InterruptedException {

        String prefix = "kat_comp-" + mecq + "_" + lib.getName() + "-vs-" + assembly.getName();

        File katOutDir = new File(outputDir, prefix);
        katOutDir.mkdirs();

        String input = lib.isPairedEnd() ? "'" + lib.getFile1().getAbsolutePath() + " " + lib.getFile2().getAbsolutePath() + "'" : lib.getFile1().getAbsolutePath();

        // Setup kat comp
        KatCompV2.Args katCompArgs = new KatCompV2.Args();
        katCompArgs.setInput1(input);
        katCompArgs.setInput2(assembly.getAbsolutePath());
        katCompArgs.setCanonical1(true);
        katCompArgs.setCanonical2(true);
        katCompArgs.setHashSize1(assembly.length() * 10);
        katCompArgs.setHashSize2(assembly.length() * 2);
        katCompArgs.setKmer(31);
        katCompArgs.setOutputPrefix(new File(katOutDir, prefix).getAbsolutePath());
        katCompArgs.setThreads(args.getThreads());

        KatCompV2 katCompProcess = new KatCompV2(this.conanExecutorService, katCompArgs);

        return this.conanExecutorService.executeProcess(
                katCompProcess,
                katOutDir,
                args.getJobPrefix() + "-" + prefix,
                args.getThreads(),
                args.getMemory(),
                args.isRunParallel());
    }

    @Override
    public void updateTable(AssemblyStatsTable table, File reportDir) {
        log.info("Currently KAT data cannot be used for updating the stats table");
    }

    @Override
    public boolean isFast() {
        return false;
    }

    @Override
    public void setConanExecutorService(ConanExecutorService ces) {
        this.conanExecutorService = ces;
    }

    @Override
    public String getName() {
        return "KAT";
    }
}
