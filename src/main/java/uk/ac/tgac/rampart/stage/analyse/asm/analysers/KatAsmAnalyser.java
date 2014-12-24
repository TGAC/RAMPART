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
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishCountV11;
import uk.ac.tgac.conan.process.kmer.kat.KatCompV1;
import uk.ac.tgac.conan.process.kmer.kat.KatPlotSpectraCnV1;
import uk.ac.tgac.rampart.stage.analyse.asm.AnalyseAssembliesArgs;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStatsTable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
        boolean jellyfish = new JellyfishCountV11(this.conanExecutorService).isOperational(executionContext);

        KatCompV1 katCompProc = new KatCompV1();
        katCompProc.setConanProcessService(this.getConanProcessService());
        boolean katComp = katCompProc.isOperational(executionContext);

        KatPlotSpectraCnV1 katPlotSpectraCn = new KatPlotSpectraCnV1();
        katPlotSpectraCn.setConanProcessService(this.getConanProcessService());
        boolean katPlot = katPlotSpectraCn.isOperational(executionContext);

        return jellyfish && katComp && katPlot;
    }

    @Override
    public void setArgs(AnalyseAssembliesArgs.ToolArgs args) {
        this.args = args;
    }

    @Override
    public List<ExecutionResult> execute(List<File> assemblies, File outputDir, String jobPrefix, ConanExecutorService ces)
            throws InterruptedException, ProcessExecutionException, ConanParameterException, IOException {

        List<ExecutionResult> jobResults = new ArrayList<>();
        List<ExecutionResult> allJobResults = new ArrayList<>();
        List<File> jellyfishHashes = new ArrayList<>();


        if (outputDir.exists()) {
            FileUtils.deleteDirectory(outputDir);
        }

        outputDir.mkdirs();

        int i = 1;
        for(File f : assemblies) {

            String jfJobName = jobPrefix + "-jfcount-" + i++;

            // Should be ok to assume that the extension is .fa as we should be working with symbolic links that we
            // generated ourselves
            File katOutputDir = new File(outputDir, f.getName().substring(0, f.getName().length() - 3));

            if (katOutputDir.exists()) {
                FileUtils.deleteDirectory(katOutputDir);
            }
            katOutputDir.mkdirs();

            String outputPrefix = katOutputDir.getAbsolutePath() + "/" + jfJobName + ".jf31";

            // Setup Jellyfish for counting the assembly
            JellyfishCountV11.Args jellyfishArgs = new JellyfishCountV11.Args();
            jellyfishArgs.setOutputPrefix(outputPrefix);
            jellyfishArgs.setLowerCount(0);  // Count everything!
            jellyfishArgs.setHashSize(args.getOrganism().getEstGenomeSize() * args.getOrganism().getPloidy() * 10);
            jellyfishArgs.setMerLength(31);       // 31 should be more than sufficient for all organisms (even wheat)
            jellyfishArgs.setBothStrands(true);
            jellyfishArgs.setThreads(args.getThreads());
            jellyfishArgs.setCounterLength(32); // This is probably overkill... consider changing this later (or using jellyfish
            // 2 which auto adjusts this figure)
            jellyfishArgs.setInputFile(f.getAbsolutePath());

            JellyfishCountV11 jellyfishProcess = new JellyfishCountV11(this.conanExecutorService, jellyfishArgs);

            // Execute kmer count and comparison
            ExecutionResult result = ces.executeProcess(
                    jellyfishProcess,
                    new File(outputPrefix).getParentFile(),
                    jfJobName,
                    args.getThreads(),
                    args.getMemory(),
                    args.isRunParallel());

            jellyfishHashes.add(new File(outputPrefix + "_0"));

            // Add job id to list
            jobResults.add(result);
            allJobResults.add(result);
        }

        // If we're using a scheduler and we have been asked to run each MECQ group for each library
        // in parallel, then we should wait for all those to complete before continueing.
        if (ces.usingScheduler() && args.isRunParallel()) {
            log.debug("Analysing assemblies using kmers in parallel, waiting for completion");
            ces.executeScheduledWait(
                    jobResults,
                    args.getJobPrefix() + "-kmer-count-*",
                    ExitStatus.Type.COMPLETED_ANY,
                    args.getJobPrefix() + "-k-count-wait",
                    args.getOutputDir());
        }

        jobResults.clear();

        Collection<File> readCounts = FileUtils.listFiles(args.getReadsAnalysisDir(), new String[] {"jf31_0"}, true);

        // Loop through jellyfish hashes
        i = 1;
        for(File jfHash : jellyfishHashes) {

            // Loop through each read count for this assembly
            int j = 1;
            for(File readCount : readCounts) {

                String outputPrefix = "katcomp-" + i + "-" + j;
                String jobName = jobPrefix + "-" + outputPrefix;

                // Setup kat comp
                KatCompV1.Args katCompArgs = new KatCompV1.Args();
                katCompArgs.setJellyfishHash1(readCount);
                katCompArgs.setJellyfishHash2(jfHash);
                katCompArgs.setOutputPrefix(new File(jfHash.getParentFile(), outputPrefix).getAbsolutePath());
                katCompArgs.setThreads(args.getThreads());

                KatCompV1 katCompProcess = new KatCompV1(katCompArgs);

                File matrixFile = new File(katCompArgs.getOutputPrefix() + "_main.mx");

                // Setup kat comp
                KatPlotSpectraCnV1.Args katPlotSpectraCnArgs = new KatPlotSpectraCnV1.Args();
                katPlotSpectraCnArgs.setInput(matrixFile);
                katPlotSpectraCnArgs.setOutput(new File(matrixFile.getAbsolutePath() + ".png"));
                katPlotSpectraCnArgs.setUncheckedArgs("--x_max=200");

                KatPlotSpectraCnV1 katPlotSpectraCnProcess = new KatPlotSpectraCnV1(katPlotSpectraCnArgs);

                // Add the plot command to the kat-comp command, to save some scheduling hassle
                katCompProcess.addPostCommand(katPlotSpectraCnProcess.getCommand());

                ExecutionResult result = ces.executeProcess(
                        katCompProcess,
                        jfHash.getParentFile(),
                        jobName,
                        args.getThreads(),
                        args.getMemory(),
                        args.isRunParallel());

                // Add job id to list
                jobResults.add(result);
                allJobResults.add(result);
                j++;
            }
            i++;
        }


        // If we're using a scheduler and we have been asked to run each MECQ group for each library
        // in parallel, then we should wait for all those to complete before continueing.
        if (ces.usingScheduler() && args.isRunParallel()) {
            log.debug("Analysing assemblies using kmers in parallel, waiting for completion");
            ces.executeScheduledWait(
                    jobResults,
                    args.getJobPrefix() + "-katcomp*",
                    ExitStatus.Type.COMPLETED_ANY,
                    args.getJobPrefix() + "-katcomp-wait",
                    args.getOutputDir());
        }

        return allJobResults;
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
