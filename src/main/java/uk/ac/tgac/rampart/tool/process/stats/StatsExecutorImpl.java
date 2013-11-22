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
package uk.ac.tgac.rampart.tool.process.stats;

import org.apache.commons.io.FileUtils;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.model.context.SchedulerArgs;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.process.asm.stats.CegmaV2_4Args;
import uk.ac.tgac.conan.process.asm.stats.CegmaV2_4Process;
import uk.ac.tgac.conan.process.asm.stats.QuastV2_2Args;
import uk.ac.tgac.conan.process.asm.stats.QuastV2_2Process;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishCountV11Args;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishCountV11Process;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishMergeV11Args;
import uk.ac.tgac.conan.process.kmer.jellyfish.JellyfishMergeV11Process;
import uk.ac.tgac.conan.process.kmer.kat.KatCompV1Args;
import uk.ac.tgac.conan.process.kmer.kat.KatCompV1Process;
import uk.ac.tgac.rampart.tool.RampartExecutorImpl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User: maplesod
 * Date: 23/08/13
 * Time: 11:18
 */
public class StatsExecutorImpl extends RampartExecutorImpl implements StatsExecutor {

    @Override
    public List<Integer> dispatchAnalyserJobs(List<StatsLevel> statsLevels, File inputDir, List<File> readKmerCounts,
                                              int threadsPerProcess, int memoryPerProcess, Organism organism,
                                              boolean scaffolds, boolean runParallel, String waitCondition, String jobName)
            throws InterruptedException, ProcessExecutionException, IOException, CommandExecutionException {

        // Make a copy of the execution context so we can make modifications
        ExecutionContext executionContextCopy = this.executionContext.copy();
        executionContextCopy.setContext(
                jobName,
                executionContextCopy.usingScheduler() ? !runParallel : true,
                null);

        if (executionContextCopy.usingScheduler()) {
            SchedulerArgs schedulerArgs = executionContextCopy.getScheduler().getArgs();

            schedulerArgs.setThreads(threadsPerProcess);
            schedulerArgs.setMemoryMB(0);
            schedulerArgs.setWaitCondition(waitCondition);
        }

        List<Integer> jobIds = new ArrayList<>();

        // Kick off the quast jobs if requested
        if (statsLevels.contains(StatsLevel.CONTIGUITY)) {
            jobIds.addAll(this.executeQuastJob(inputDir, threadsPerProcess, organism.getEstGenomeSize(), jobName + "-quast",
                    executionContextCopy, scaffolds));
        }

        // Kick off the cegma jobs if requested
        if (statsLevels.contains(StatsLevel.COMPLETENESS)) {
            jobIds.addAll(this.executeCegmaJobs(inputDir, threadsPerProcess, jobName + "-cegma", executionContextCopy));
        }

        // Kick off jellyfish and KAT if requested
        if (statsLevels.contains(StatsLevel.KMER)) {
            jobIds.addAll(this.executeKmerJobs(inputDir, readKmerCounts, threadsPerProcess, memoryPerProcess, organism, jobName + "-kmer",
                    executionContextCopy));
        }

        return jobIds;
    }

    private List<Integer> executeKmerJobs(File inputDir, List<File> readKmerCounts, int threadsPerProcess, int memoryPerProcess,
                                          Organism organism, String jobName, ExecutionContext executionContext)
            throws IOException, ProcessExecutionException, InterruptedException {

        List<Integer> jobIds = new ArrayList<>();

        List<File> files = filesFromDir(inputDir);

        File rootOutputDir = new File(inputDir, "kmer");
        if (rootOutputDir.exists()) {
            FileUtils.deleteDirectory(rootOutputDir);
        }
        rootOutputDir.mkdir();

        File mergedReadKmerCounts = new File(rootOutputDir, "merged_reads.jf31_0");

        String kmerWait = null;

        if (readKmerCounts.size() > 1) {

            // Setup jellyfish for merging all the reads for this mass run
            JellyfishMergeV11Args mergeArgs = new JellyfishMergeV11Args();
            mergeArgs.setBufferSize(20000000);
            mergeArgs.setOutputFile(mergedReadKmerCounts);
            mergeArgs.setInputFiles(readKmerCounts);

            JellyfishMergeV11Process jellyfishMerge = new JellyfishMergeV11Process(mergeArgs);

            String mergeJobName = jobName + "-merging_read_kmers";

            ExecutionContext executionContextMerge = executionContext.copy();
            executionContextMerge.setContext(mergeJobName, executionContext.isForegroundJob(),
                    new File(inputDir, mergeJobName + ".log"));

            ExecutionResult result = this.conanProcessService.execute(jellyfishMerge, executionContextMerge);

            List<Integer> mergeId = new ArrayList<>();
            jobIds.add(result.getJobId());

            // If using a scheduler create a wait condition that will be observed by the stats job if running in parallel
            kmerWait = executionContext.usingScheduler() ?
                    executionContext.getScheduler().generatesJobIdFromOutput() ?
                            executionContext.getScheduler().createWaitCondition(ExitStatus.Type.COMPLETED_SUCCESS, mergeId) :
                            executionContext.getScheduler().createWaitCondition(ExitStatus.Type.COMPLETED_SUCCESS, mergeJobName) :
                    null;
        }
        else if (readKmerCounts.size() == 1) {
            // If there's only one library just create a symbolic link
            this.conanProcessService.createLocalSymbolicLink(readKmerCounts.get(0), mergedReadKmerCounts);
        }
        else {
            throw new IOException("Kmer analysis for the assembly was requested but no kmer counts for the reads could be found.");
        }

        int i = 1;
        for(File f : files) {

            String kmerJobName = jobName + "-" + i++;

            ExecutionContext executionContextCopy = executionContext.copy();
            executionContextCopy.setContext(kmerJobName, executionContext.isForegroundJob(), new File(inputDir, kmerJobName + ".log"));

            if (executionContextCopy.usingScheduler() && kmerWait != null) {

                if (executionContextCopy.getScheduler().getArgs().getWaitCondition().isEmpty()) {
                    executionContextCopy.getScheduler().getArgs().setWaitCondition(kmerWait);
                }
                else {
                    executionContextCopy.getScheduler().getArgs().setWaitCondition(
                            executionContextCopy.getScheduler().getArgs().getWaitCondition() + " " + kmerWait
                    );
                }
            }

            File outputDir = new File(rootOutputDir, f.getName().substring(0, f.getName().length() - 3));
            if (outputDir.exists()) {
                FileUtils.deleteDirectory(outputDir);
            }
            outputDir.mkdir();

            // Setup Jellyfish for counting the assembly
            JellyfishCountV11Args jellyfishArgs = new JellyfishCountV11Args();
            jellyfishArgs.setOutputPrefix(outputDir.getAbsolutePath() + "/" + jobName);
            jellyfishArgs.setLowerCount(0);
            jellyfishArgs.setHashSize(organism.getEstGenomeSize() * organism.getPloidy() * 2);
            jellyfishArgs.setMerLength(31);       // 31 should be sufficient for all organisms
            jellyfishArgs.setBothStrands(true);
            jellyfishArgs.setThreads(threadsPerProcess);
            jellyfishArgs.setMemoryMb(memoryPerProcess);
            jellyfishArgs.setCounterLength(32); // This is probably overkill... consider changing this later (or using jellyfish
                                                // 2 which auto adjusts this figure)
            jellyfishArgs.setInputFile(f.getAbsolutePath());

            JellyfishCountV11Process jellyfishProcess = new JellyfishCountV11Process(jellyfishArgs);

            // Setup kat comp
            KatCompV1Args katCompArgs = new KatCompV1Args();
            katCompArgs.setJellyfishHash1(mergedReadKmerCounts);
            katCompArgs.setJellyfishHash2(new File(outputDir.getAbsolutePath() + "/" + jobName + "_0"));
            katCompArgs.setOutputPrefix(outputDir.getAbsolutePath() + "/" + jobName + "-kat-comp");
            katCompArgs.setThreads(threadsPerProcess);
            katCompArgs.setMemoryMb(memoryPerProcess);

            KatCompV1Process katCompProcess = new KatCompV1Process(katCompArgs);

            // Combine the commands so they run in sequence
            String compoundCommand = jellyfishProcess.getCommand() + "; " + katCompProcess.getCommand();

            // Execute kmer count and comparison
            ExecutionResult resultCount = this.conanProcessService.execute(compoundCommand, executionContextCopy);

            // Add job id to list
            jobIds.add(resultCount.getJobId());
        }

        return jobIds;
    }



    protected List<Integer> executeCegmaJobs(File inputDir, int threads, String jobName, ExecutionContext executionContext)
            throws IOException, ProcessExecutionException, InterruptedException {

        List<Integer> jobIds = new ArrayList<>();

        List<File> files = filesFromDir(inputDir);

        File rootOutputDir = new File(inputDir, "cegma");
        if (rootOutputDir.exists()) {
            FileUtils.deleteDirectory(rootOutputDir);
        }
        rootOutputDir.mkdir();

        int i = 1;
        for(File f : files) {

            String cegmaJobName = jobName + "-" + i++;

            ExecutionContext executionContextCopy = executionContext.copy();
            executionContextCopy.setContext(cegmaJobName, executionContext.isForegroundJob(), new File(inputDir, cegmaJobName + ".log"));

            File outputDir = new File(rootOutputDir, f.getName());
            if (outputDir.exists()) {
                FileUtils.deleteDirectory(outputDir);
            }
            outputDir.mkdir();

            // Setup CEGMA
            CegmaV2_4Args cegmaArgs = new CegmaV2_4Args();
            cegmaArgs.setGenomeFile(f);
            cegmaArgs.setOutputPrefix(new File(outputDir, f.getName()));
            cegmaArgs.setThreads(threads);

            CegmaV2_4Process cegmaProcess = new CegmaV2_4Process(cegmaArgs);

            // Creates output and temp directories
            // Also creates a modified genome file that's BLAST tolerant.
            cegmaProcess.initialise();

            // Execute CEGMA
            ExecutionResult result = this.conanProcessService.execute(cegmaProcess, executionContextCopy);

            // Create symbolic links to completeness_reports
            File sourceFile = new File(cegmaArgs.getOutputPrefix().getAbsolutePath() + ".completeness_report");
            File destFile = new File(rootOutputDir, f.getName() + ".cegma");
            String linkCmd = "ln -s -f " + sourceFile.getAbsolutePath() + " " + destFile.getAbsolutePath();
            this.conanProcessService.execute(linkCmd, new DefaultExecutionContext(executionContext.getLocality(), null, null));

            // Add cegma job id to list
            jobIds.add(result.getJobId());
        }

        return jobIds;
    }

    protected List<Integer> executeQuastJob(File inputDir, int threads, int estGenomeSize, String jobName, ExecutionContext executionContext, boolean scaffolds)
            throws InterruptedException, ProcessExecutionException {

        ExecutionContext executionContextCopy = executionContext.copy();
        executionContextCopy.setContext(jobName, executionContext.isForegroundJob(), new File(inputDir, jobName + ".log"));

        QuastV2_2Args quastArgs = new QuastV2_2Args();
        quastArgs.setInputFiles(filesFromDir(inputDir));
        quastArgs.setOutputDir(new File(inputDir, "quast"));   // No need to create this directory first... quast will take care of that
        quastArgs.setEstimatedGenomeSize(estGenomeSize);
        quastArgs.setThreads(threads);
        quastArgs.setScaffolds(scaffolds);

        QuastV2_2Process quastProcess = new QuastV2_2Process(quastArgs);

        ExecutionResult result = this.conanProcessService.execute(quastProcess, executionContextCopy);

        // Add quast job id to list
        List<Integer> jobIds = new ArrayList<>();
        jobIds.add(result.getJobId());

        return jobIds;
    }

    /**
     * Gets all the FastA files in the directory specified by the user.
     * @param inputDir
     * @return A list of fasta files in the user specified directory
     */
    protected List<File> filesFromDir(File inputDir) {

        if (inputDir == null || !inputDir.exists())
            return null;

        List<File> fileList = new ArrayList<>();

        Collection<File> fileCollection = FileUtils.listFiles(inputDir, new String[]{"fa", "fasta"}, true);

        for(File file : fileCollection) {
            fileList.add(file);
        }

        Collections.sort(fileList);

        return fileList;
    }

}
