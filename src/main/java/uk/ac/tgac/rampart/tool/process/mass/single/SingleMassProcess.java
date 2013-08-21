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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.model.context.SchedulerArgs;
import uk.ac.ebi.fgpt.conan.model.context.WaitCondition;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.util.StringJoiner;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.process.asm.Assembler;
import uk.ac.tgac.conan.process.asm.AssemblerArgs;
import uk.ac.tgac.conan.process.asm.AssemblerFactory;
import uk.ac.tgac.conan.process.asm.stats.CegmaV2_4Args;
import uk.ac.tgac.conan.process.asm.stats.CegmaV2_4Process;
import uk.ac.tgac.conan.process.asm.stats.QuastV2_2Args;
import uk.ac.tgac.conan.process.asm.stats.QuastV2_2Process;
import uk.ac.tgac.rampart.tool.process.mass.CoverageRange;
import uk.ac.tgac.rampart.tool.process.mass.KmerRange;
import uk.ac.tgac.rampart.tool.process.mass.MassInput;
import uk.ac.tgac.rampart.tool.process.mecq.MecqSingleArgs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class SingleMassProcess extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(SingleMassProcess.class);

    public SingleMassProcess() {
        this(new SingleMassArgs());
    }

    public SingleMassProcess(SingleMassArgs args) {
        super("", args, new SingleMassParams());
    }


    protected void createSupportDirectories(Assembler assembler, SingleMassArgs args) {

        // Create directory for links to assembled contigs
        if (assembler.makesUnitigs()) {
            args.getUnitigsDir().mkdir();
        }

        // Create directory for links to assembled contigs
        if (assembler.makesContigs()) {
            args.getContigsDir().mkdir();
        }

        // Create dir for scaffold links if this asm creates them
        if (assembler.makesScaffolds()) {
            args.getScaffoldsDir().mkdir();
        }
    }

    /**
     * Dispatches assembly jobs to the specified environments
     *
     * @param executionContext The environment to dispatch jobs too
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws ProcessExecutionException
     * @throws InterruptedException
     */
    @Override
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        try {

            // Make a shortcut to the args
            SingleMassArgs args = (SingleMassArgs) this.getProcessArgs();

            // Create an assembly object used for other stages in the MASS pipeline.. note this does not include
            // specific kmer settings
            Assembler genericAssembler = AssemblerFactory.createAssembler(args.getTool());

            log.info("Starting Single MASS run for " + args.getName());

            // Make sure the kmer range is reasonable (if it's not already)
            KmerRange validatedKmerRange = this.validateKmerRange(args.getName(), genericAssembler.hasKParam(), args.getKmerRange());

            // Make sure the coverage range reasonable (if it's not already)
            CoverageRange validatedCoverageRange = this.validateCoverageRange(args.getName(), args.getCoverageRange());

            // Make sure the inputs are reasonable
            List<Library> selectedLibs = this.validateInputs(args.getName(), args.getInputs(), args.getAllLibraries(), args.getAllMecqs());

            // Create any required directories for this job
            log.debug("Creating directories");
            this.createSupportDirectories(genericAssembler, args);

            WaitCondition assemblerWait = null;

            //if (!args.isStatsOnly()) {

                // Dispatch an assembly job for coverage and kmer value
                for (Integer cvg : validatedCoverageRange) {

                    for (Integer k : validatedKmerRange) {

                        String cvgString = CoverageRange.toString(cvg);

                        String dirName = genericAssembler.hasKParam() ? ("cvg-" + cvgString + "_k-" + k) : "cvg-" + cvgString;

                        File outputDir = new File(args.getOutputDir(), dirName);

                        log.debug("Starting " + args.getTool() + " in " + outputDir.getAbsolutePath());

                        // Create the actual assembler for these settings
                        Assembler assembler = this.makeAssembler(args, k, cvg, selectedLibs, outputDir);

                        // Execute the assembler
                        this.executeAssembler(assembler, args.getJobPrefix() + "-" + dirName, executionContext);

                        // Create links for outputs from this assembler to known locations
                        this.createLinks(assembler, k, executionContext);
                    }
                }

                // If using a scheduler create a wait condition that will be observed by the stats job
                assemblerWait = executionContext.usingScheduler() ?
                        executionContext.getScheduler().createWaitCondition(ExitStatus.Type.COMPLETED_SUCCESS, args.getJobPrefix() + "*") :
                        null;
            //}

            // Run analyser job using the original execution context
            log.debug("Analysing and comparing assemblies for MASS group: " + args.getName());
            this.dispatchStatsJobs(genericAssembler, assemblerWait, executionContext);

            log.info("Finished Single MASS run");

        } catch (IOException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        } catch (CommandExecutionException cee) {
            throw new ProcessExecutionException(-2, cee);
        }

        return true;
    }


    protected Assembler makeAssembler(SingleMassArgs massArgs, int k, int cvg, List<Library> selectedLibs, File outputDir) {

        Assembler asm = AssemblerFactory.valueOf(massArgs.getTool()).create();
        AssemblerArgs asmArgs = asm.getArgs();

        asmArgs.setLibraries(selectedLibs);
        asmArgs.setDesiredCoverage(cvg);
        asmArgs.setKmer(k);
        asmArgs.setThreads(massArgs.getThreads());
        asmArgs.setMemory(massArgs.getMemory());
        asmArgs.setOrganism(massArgs.getOrganism());
        asmArgs.setOutputDir(outputDir);

        return asm;
    }

    protected List<Library> validateInputs(String massName, List<MassInput> inputs, List<Library> allLibraries, List<MecqSingleArgs> allMecqs) throws IOException {

        List<Library> selectedLibs = new ArrayList<Library>();

        for(MassInput mi : inputs) {
            Library lib = mi.findLibrary(allLibraries);
            MecqSingleArgs mecqArgs = mi.findMecq(allMecqs);

            if (lib == null) {
                throw new IOException("Unrecognised library: " + mi.getLib() + "; not processing MASS run: " + massName);
            }

            if (mecqArgs == null) {
                if (mi.getMecq().equalsIgnoreCase(MecqSingleArgs.RAW)) {
                    selectedLibs.add(lib);
                }
                else {
                    throw new IOException("Unrecognised MECQ dataset requested: " + mi.getMecq() + "; not processing MASS run: " + massName);
                }
            }
            else {
                Library modLib = lib.copy();

                List<File> files = mecqArgs.getOutputFiles(lib.getName());

                if (modLib.isPairedEnd()) {
                    if (files.size() != 2) {
                        throw new IOException("Paired end library: " + modLib.getName() + " from " + mecqArgs.getName() + " does not have two files");
                    }

                    modLib.setFiles(files.get(0), files.get(1));
                }
                else {
                    if (files.size() != 1) {
                        throw new IOException("Single end library: " + modLib.getName() + " from " + mecqArgs.getName() + " does not have one file");
                    }

                    modLib.setFiles(files.get(0), null);
                }

                selectedLibs.add(modLib);
            }
        }

        return selectedLibs;
    }

    protected KmerRange validateKmerRange(String massName, boolean assemblerSupportsK, KmerRange kmerRange) throws CommandExecutionException {

        if (!assemblerSupportsK) {
            log.info("Selected assembler for: " + massName + " does not support K parameter");
            return new KmerRange();
        }
        else if (kmerRange == null) {
            KmerRange defaultKmerRange = new KmerRange();
            log.info("No K-mer range specified for " + massName + " running assembler with default range: " + defaultKmerRange.toString());
            return defaultKmerRange;
        }
        else if (kmerRange.validate()) {
            log.info("K-mer range for " + massName + " validated: " + kmerRange.toString());
            return kmerRange;
        }
        else {
            throw new CommandExecutionException("Invalid K-mer range: " + kmerRange.toString() + " Not processing MASS run: " + massName);
        }
    }

    protected CoverageRange validateCoverageRange(String massName, CoverageRange coverageRange) throws CommandExecutionException {

        if (coverageRange == null) {
            CoverageRange defaultCoverageRange = new CoverageRange();
            log.info("No coverage range specified for " + massName + " running assembler with default range: " + defaultCoverageRange.toString());
            return defaultCoverageRange;
        }
        else if (coverageRange.validate()) {
            log.info("Coverage range for " + massName + " validated: " + coverageRange.toString());
            return coverageRange;
        }
        else {
            throw new CommandExecutionException("Invalid coverage range: " + coverageRange.toString() + " Not processing MASS run: " + massName);
        }
    }

    protected String makeLinkCmdLine(File sourceFile, File outputDir, int k) {

        String targetFileName = outputDir.getParentFile().getName() + "-k-" + Integer.toString(k) + "-" + outputDir.getName() + ".fa";
        return "ln -s -f " + sourceFile.getAbsolutePath() + " " + new File(outputDir, targetFileName).getAbsolutePath();
    }

    protected void createLinks(Assembler assembler, int k, ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        ExecutionContext linkingExecutionContext = new DefaultExecutionContext(executionContext.getLocality(), null, null, true);

        StringJoiner compoundLinkCmdLine = new StringJoiner(";");

        // Make a shortcut to the args
        SingleMassArgs args = (SingleMassArgs) this.getProcessArgs();

        if (assembler.makesContigs()) {
            compoundLinkCmdLine.add(makeLinkCmdLine(assembler.getContigsFile(), args.getContigsDir(), k));
        }

        if (assembler.makesScaffolds()) {
            compoundLinkCmdLine.add(makeLinkCmdLine(assembler.getScaffoldsFile(), args.getScaffoldsDir(), k));
        }

        this.conanProcessService.execute(compoundLinkCmdLine.toString(), linkingExecutionContext);
    }

    protected void executeAssembler(Assembler assembler, String jobName, ExecutionContext executionContext)
            throws IOException, ProcessExecutionException, InterruptedException {

        ExecutionContext executionContextCopy = executionContext.copy();

        SingleMassArgs args = (SingleMassArgs)this.getProcessArgs();

        File outputDir = assembler.getArgs().getOutputDir();

        // Make the output directory for this child job (delete the directory if it exists)
        if (outputDir.exists()) {
            FileUtils.deleteDirectory(outputDir);
        }
        outputDir.mkdir();

        // Important that this happens after directory cleaning.
        assembler.initialise();


        // Modify the scheduler jobname is present
        if (executionContextCopy.usingScheduler()) {

            SchedulerArgs schArgs = executionContextCopy.getScheduler().getArgs();

            schArgs.setJobName(jobName);
            schArgs.setMonitorFile(new File(outputDir, jobName + ".log"));
            schArgs.setThreads(args.getThreads());
            schArgs.setMemoryMB(args.getMemory());

            if (assembler.usesOpenMpi() && args.getThreads() > 1) {
                schArgs.setOpenmpi(true);
            }

            executionContextCopy.setForegroundJob(!args.isRunParallel());
        }

        // Create process
        this.conanProcessService.execute(assembler, executionContextCopy);
    }

    protected void executeScheduledWait(String jobPrefix, File outputDir, ExecutionContext executionContext)
            throws ProcessExecutionException, InterruptedException {

        // Duplicate the execution context so we don't modify the original accidentally.
        ExecutionContext executionContextCopy = executionContext.copy();

        if (executionContext.usingScheduler()) {

            String jobName = jobPrefix + "_wait";
            executionContextCopy.getScheduler().getArgs().setJobName(jobName);
            executionContextCopy.getScheduler().getArgs().setMonitorFile(new File(outputDir, jobName + ".log"));
            executionContextCopy.setForegroundJob(true);
        }

        this.conanProcessService.waitFor(
                executionContextCopy.getScheduler().createWaitCondition(ExitStatus.Type.COMPLETED_SUCCESS, jobPrefix + "*"),
                executionContextCopy);
    }


    protected void dispatchStatsJobs(Assembler assembler, WaitCondition waitCondition, ExecutionContext executionContext) throws InterruptedException, ProcessExecutionException, IOException, CommandExecutionException {

        SingleMassArgs args = (SingleMassArgs) this.getProcessArgs();
        ExecutionContext executionContextCopy = executionContext.copy();

        String jobName = args.getJobPrefix() + "-analyser";

        if (executionContextCopy.usingScheduler()) {
            SchedulerArgs schedulerArgs = executionContextCopy.getScheduler().getArgs();

            schedulerArgs.setJobName(jobName);
            schedulerArgs.setThreads(args.getThreads());
            schedulerArgs.setMemoryMB(0);
            schedulerArgs.setWaitCondition(waitCondition);

            // Always going to want to run these jobs in parallel if we have access to a scheduler!
            executionContextCopy.setForegroundJob(false);
        }

        // Kick off the quast jobs
        this.executeQuastJobs(assembler, args, jobName + "-quast", executionContextCopy);

        // Kick off the cegma jobs
        this.executeCegmaJobs(assembler, args, jobName + "-cegma", executionContextCopy);
    }

    protected void executeCegmaJobs(Assembler assembler, SingleMassArgs args, String jobName, ExecutionContext executionContext)
            throws IOException, ProcessExecutionException, InterruptedException {

        File inputDir = null;

        // We only do one level of Cegma jobs, technically there shouldn't be much / any different between different levels
        if (assembler.makesScaffolds()) {
            inputDir = args.getScaffoldsDir();
        }
        else if (assembler.makesContigs()) {
            inputDir = args.getContigsDir();
        }
        else {
            log.warn("Couldn't run CEGMA because assembler does not support any recognised output types (contigs, scaffods).");
            return;
        }

        File[] files = inputDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("fa") || name.endsWith("fasta");
            }
        });

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
            try {
                this.conanProcessService.execute(cegmaProcess, executionContextCopy);
            }
            catch(ProcessExecutionException pee) {
                // If an error occurs here it isn't critical so just log the error and continue
                log.error(pee.getMessage(), pee);
            }

            // Create symbolic links to completeness_reports
            File sourceFile = new File(cegmaArgs.getOutputPrefix().getAbsolutePath() + ".completeness_report");
            File destFile = new File(rootOutputDir, f.getName() + ".cegma");
            String linkCmd = "ln -s -f " + sourceFile.getAbsolutePath() + " " + destFile.getAbsolutePath();
            this.conanProcessService.execute(linkCmd, new DefaultExecutionContext(executionContext.getLocality(), null, null, true));
        }


    }

    protected void executeQuastJobs(Assembler assembler, SingleMassArgs args, String jobName, ExecutionContext executionContext)
            throws InterruptedException {

        if (assembler.makesUnitigs()) {
            this.executeSingleQuastJob(args.getUnitigsDir(), jobName + "-unitigs", executionContext, false);
        }

        if (assembler.makesContigs()) {
            this.executeSingleQuastJob(args.getContigsDir(), jobName + "-contigs", executionContext, false);
        }

        if (assembler.makesScaffolds()) {
            this.executeSingleQuastJob(args.getScaffoldsDir(), jobName + "-scaffolds", executionContext, true);
        }
    }

    protected void executeSingleQuastJob(File inputDir, String jobName, ExecutionContext executionContext, boolean scaffolds)
            throws InterruptedException {

        SingleMassArgs args = (SingleMassArgs) this.getProcessArgs();
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

        try {
            this.conanProcessService.execute(quastProcess, executionContextCopy);
        }
        catch(ProcessExecutionException pee) {
            // If an error occurs here it isn't critical so just log the error and continue
            log.error(pee.getMessage(), pee);
        }
    }

    /**
     * Gets all the FastA files in the directory specified by the user.
     * @param inputDir
     * @return
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

    @Override
    public String getName() {
        return "MASS";
    }

    @Override
    public String getCommand() {
        return null;
    }

}
