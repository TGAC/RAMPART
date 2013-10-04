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
package uk.ac.tgac.rampart.tool.process.mass.single;

import org.apache.commons.io.FileUtils;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.SchedulerArgs;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.util.StringJoiner;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.conan.process.asm.Assembler;
import uk.ac.tgac.conan.process.asm.AssemblerArgs;
import uk.ac.tgac.conan.process.asm.stats.CegmaV2_4Args;
import uk.ac.tgac.conan.process.asm.stats.CegmaV2_4Process;
import uk.ac.tgac.conan.process.asm.stats.QuastV2_2Args;
import uk.ac.tgac.conan.process.asm.stats.QuastV2_2Process;
import uk.ac.tgac.conan.process.subsampler.SubsamplerV1_0Args;
import uk.ac.tgac.conan.process.subsampler.SubsamplerV1_0Process;
import uk.ac.tgac.rampart.tool.RampartExecutorImpl;
import uk.ac.tgac.rampart.tool.process.mass.MassArgs;

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
public class SingleMassExecutorImpl extends RampartExecutorImpl implements SingleMassExecutor {

    private List<Integer> jobIds;

    public SingleMassExecutorImpl() {
        this.jobIds = new ArrayList<>();
    }

    @Override
    public ExecutionResult executeAssembler(Assembler assembler, String jobName, boolean runParallel)
            throws ProcessExecutionException, InterruptedException, IOException {

        ExecutionContext executionContextCopy = executionContext.copy();

        // Important that this happens after directory cleaning.
        assembler.initialise();

        // Modify the scheduler jobname is present
        if (executionContextCopy.usingScheduler()) {

            SchedulerArgs schArgs = executionContextCopy.getScheduler().getArgs();

            AssemblerArgs asmArgs = assembler.getArgs();

            schArgs.setJobName(jobName);
            schArgs.setMonitorFile(new File(assembler.getArgs().getOutputDir(), jobName + ".log"));
            schArgs.setThreads(asmArgs.getThreads());
            schArgs.setMemoryMB(asmArgs.getMemory());

            if (assembler.usesOpenMpi() && asmArgs.getThreads() > 1) {
                schArgs.setOpenmpi(true);
            }

            executionContextCopy.setForegroundJob(!runParallel);
        }

        // Create process
        return this.conanProcessService.execute(assembler, executionContextCopy);
    }

    @Override
    public void createAssemblyLinks(Assembler assembler, SingleMassArgs smArgs, String jobName)
            throws ProcessExecutionException, InterruptedException {

        ExecutionContext linkingExecutionContext = new DefaultExecutionContext(executionContext.getLocality(), null, null, true);

        StringJoiner compoundLinkCmdLine = new StringJoiner(";");

        if (assembler.makesUnitigs()) {
            compoundLinkCmdLine.add(this.makeLinkCommand(assembler.getUnitigsFile(), new File(smArgs.getUnitigsDir(), jobName + "-unitigs.fa")));
        }

        if (assembler.makesContigs()) {
            compoundLinkCmdLine.add(this.makeLinkCommand(assembler.getContigsFile(), new File(smArgs.getContigsDir(), jobName + "-contigs.fa")));
        }

        if (assembler.makesScaffolds()) {
            compoundLinkCmdLine.add(this.makeLinkCommand(assembler.getScaffoldsFile(), new File(smArgs.getScaffoldsDir(), jobName + "-scaffolds.fa")));
        }

        this.conanProcessService.execute(compoundLinkCmdLine.toString(), linkingExecutionContext);
    }

    @Override
    public void dispatchAnalyserJobs(Assembler assembler, SingleMassArgs args, String waitCondition, String jobName)
            throws InterruptedException, ProcessExecutionException, IOException, CommandExecutionException {

        // Make a copy of the execution context so we can make modifications
        ExecutionContext executionContextCopy = executionContext.copy();

        if (executionContextCopy.usingScheduler()) {
            SchedulerArgs schedulerArgs = executionContextCopy.getScheduler().getArgs();

            schedulerArgs.setJobName(jobName);
            schedulerArgs.setThreads(args.getThreads());
            schedulerArgs.setMemoryMB(0);
            schedulerArgs.setWaitCondition(waitCondition);

            // Always going to want to run these jobs in parallel if we have access to a scheduler!
            executionContextCopy.setForegroundJob(!args.isRunParallel());
        }

        // Kick off the quast jobs if requested
        if (args.getStatsLevels().contains(StatsLevel.CONTIGUITY)) {
            this.executeQuastJobs(assembler, args, jobName + "-quast", executionContextCopy);
        }

        // Kick off the cegma jobs if requested
        if (args.getStatsLevels().contains(StatsLevel.COMPLETENESS)) {
            this.executeCegmaJobs(assembler, args, jobName + "-cegma", executionContextCopy);
        }
    }

    @Override
    public void executeSubsampler(double probability, long timestamp, File input, File output, String jobName)
            throws ProcessExecutionException, InterruptedException, IOException {

        ExecutionContext executionContextCopy = executionContext.copy();

        // Modify the scheduler jobname is present
        if (executionContextCopy.usingScheduler()) {

            SchedulerArgs schArgs = executionContextCopy.getScheduler().getArgs();

            schArgs.setJobName(jobName);
            schArgs.setMonitorFile(new File(output.getParentFile(), jobName + ".log"));

            executionContextCopy.setForegroundJob(true);
        }

        SubsamplerV1_0Args ssArgs = new SubsamplerV1_0Args();
        ssArgs.setInputFile(input);
        ssArgs.setOutputFile(output);
        ssArgs.setLogFile(new File(output.getParentFile(), output.getName() + ".log"));
        ssArgs.setSeed(timestamp);
        ssArgs.setProbability(probability);

        SubsamplerV1_0Process ssProc = new SubsamplerV1_0Process(ssArgs);

        // Create process
        this.conanProcessService.execute(ssProc, executionContextCopy);
    }

    @Override
    public long getNbEntries(File seqFile, File outputDir, String jobName) throws ProcessExecutionException, InterruptedException, IOException {

        return getCount(seqFile, outputDir, jobName, true);
    }

    @Override
    public long getNbBases(File seqFile, File outputDir, String jobName) throws IOException, ProcessExecutionException, InterruptedException {

        return getCount(seqFile, outputDir, jobName, false);
    }

    @Override
    public List<Integer> getJobIds() {
        return this.jobIds;
    }

    protected long getCount(File seqFile, File outputDir, String jobName, boolean linesOnly) throws ProcessExecutionException, InterruptedException, IOException {

        ExecutionContext executionContextCopy = executionContext.copy();

        // Modify the scheduler jobname is present
        if (executionContextCopy.usingScheduler()) {

            SchedulerArgs schArgs = executionContextCopy.getScheduler().getArgs();

            schArgs.setJobName(jobName);
            schArgs.setMonitorFile(new File(outputDir, jobName + ".log"));

            executionContextCopy.setForegroundJob(true);
        }

        File outputFile = new File(outputDir, seqFile.getName() + ".nb_entries.out");

        String wcOption = linesOnly ? "-l" : "-m";
        String command = "awk '/^@/{getline; print}' " + seqFile.getAbsolutePath() + " | wc " + wcOption + " > " + outputFile.getAbsolutePath();

        this.conanProcessService.execute(command, executionContextCopy);

        List<String> lines = FileUtils.readLines(outputFile);

        if (lines == null || lines.isEmpty()) {
            throw new IOException("Failed to retrieve number of lines in file: " + seqFile.getAbsolutePath());
        }

        return Long.parseLong(lines.get(0).trim());
    }


    protected void executeCegmaJobs(Assembler assembler, SingleMassArgs args, String jobName, ExecutionContext executionContext)
            throws IOException, ProcessExecutionException, InterruptedException {

        File inputDir = null;

        // We only do one level of Cegma jobs (the highest), technically there shouldn't be much / any different between different levels
        if (assembler.makesScaffolds()) {
            inputDir = args.getScaffoldsDir();
        }
        else if (assembler.makesContigs()) {
            inputDir = args.getContigsDir();
        }
        else if (assembler.makesUnitigs()) {
            inputDir = args.getUnitigsDir();
        }
        else {
            throw new IOException("Couldn't run CEGMA because assembler does not support any recognised output types: " + MassArgs.OutputLevel.getListAsString());
        }

        List<File> files = filesFromDir(inputDir);

        File rootOutputDir = new File(inputDir, "cegma");
        if (rootOutputDir.exists()) {
            FileUtils.deleteDirectory(rootOutputDir);
        }
        rootOutputDir.mkdir();

        int i = 1;
        for(File f : files) {
            ExecutionContext executionContextCopy = executionContext.copy();

            if (executionContextCopy.usingScheduler()) {

                SchedulerArgs schedulerArgs = executionContextCopy.getScheduler().getArgs();

                String cegmaJobName = jobName + "-" + i + ".log";

                schedulerArgs.setJobName(cegmaJobName);
                schedulerArgs.setMonitorFile(new File(inputDir, cegmaJobName));
                i++;
            }

            File outputDir = new File(rootOutputDir, f.getName());
            if (outputDir.exists()) {
                FileUtils.deleteDirectory(outputDir);
            }
            outputDir.mkdir();

            // Setup CEGMA
            CegmaV2_4Args cegmaArgs = new CegmaV2_4Args();
            cegmaArgs.setGenomeFile(f);
            cegmaArgs.setOutputPrefix(new File(outputDir, f.getName()));
            cegmaArgs.setThreads(args.getThreads());

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
            this.conanProcessService.execute(linkCmd, new DefaultExecutionContext(executionContext.getLocality(), null, null, true));

            // Add cegma job id to list
            this.jobIds.add(result.getJobId());
        }


    }

    protected void executeQuastJobs(Assembler assembler, SingleMassArgs args, String jobName, ExecutionContext executionContext)
            throws InterruptedException, ProcessExecutionException {

        if (assembler.makesUnitigs()) {
            this.executeSingleQuastJob(args.getUnitigsDir(), args, jobName + "-unitigs", executionContext, false);
        }

        if (assembler.makesContigs()) {
            this.executeSingleQuastJob(args.getContigsDir(), args, jobName + "-contigs", executionContext, false);
        }

        if (assembler.makesScaffolds()) {
            this.executeSingleQuastJob(args.getScaffoldsDir(), args, jobName + "-scaffolds", executionContext, true);
        }
    }

    protected void executeSingleQuastJob(File inputDir, SingleMassArgs args, String jobName, ExecutionContext executionContext, boolean scaffolds)
            throws InterruptedException, ProcessExecutionException {

        ExecutionContext executionContextCopy = executionContext.copy();

        if (executionContextCopy.usingScheduler()) {

            SchedulerArgs schedulerArgs = executionContextCopy.getScheduler().getArgs();

            schedulerArgs.setJobName(jobName);
            schedulerArgs.setMonitorFile(new File(inputDir, jobName + ".log"));
        }

        QuastV2_2Args quastArgs = new QuastV2_2Args();
        quastArgs.setInputFiles(filesFromDir(inputDir));
        quastArgs.setOutputDir(new File(inputDir, "quast"));   // No need to create this directory first... quast will take care of that
        quastArgs.setEstimatedGenomeSize(args.getOrganism() == null ? 0 : args.getOrganism().getEstGenomeSize());
        quastArgs.setThreads(args.getThreads());
        quastArgs.setScaffolds(scaffolds);

        QuastV2_2Process quastProcess = new QuastV2_2Process(quastArgs);

        ExecutionResult result = this.conanProcessService.execute(quastProcess, executionContextCopy);

        // Add quast job id to list
        this.jobIds.add(result.getJobId());
    }

    /**
     * Gets all the FastA files in the directory specified by the user.
     * @param inputDir
     * @return A list of fasta files in the user specified directory
     */
    protected List<File> filesFromDir(File inputDir) {

        if (inputDir == null || !inputDir.exists())
            return null;

        List<File> fileList = new ArrayList<File>();

        Collection<File> fileCollection = FileUtils.listFiles(inputDir, new String[]{"fa", "fasta"}, true);

        for(File file : fileCollection) {
            fileList.add(file);
        }

        Collections.sort(fileList);

        return fileList;
    }

}
