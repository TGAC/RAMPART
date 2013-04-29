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
import uk.ac.tgac.conan.process.asm.Assembler;
import uk.ac.tgac.conan.process.asm.AssemblerArgs;
import uk.ac.tgac.conan.process.asm.AssemblerFactory;
import uk.ac.tgac.conan.process.asm.stats.AscV10Args;
import uk.ac.tgac.conan.process.asm.stats.AscV10Process;
import uk.ac.tgac.rampart.tool.process.mass.MassArgs;

import java.io.File;
import java.io.IOException;

@Component
public class SingleMassProcess extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(SingleMassProcess.class);

    public SingleMassProcess() {
        this(new SingleMassArgs());
    }

    public SingleMassProcess(SingleMassArgs args) {
        super("", args, new SingleMassParams());
    }

    protected int getFirstValidKmer(int kmin) {

        if (kmin <= 11)
            return 11;

        String kminStr = String.valueOf(kmin);
        if (kminStr.charAt(kminStr.length() - 1) == '1') {
            return kmin;
        } else {
            char tensDigit = kminStr.charAt(kminStr.length() - 2);

            int tens = (Integer.parseInt(String.valueOf(tensDigit)) + 1) * 10;

            int rest = 0;

            if (kminStr.length() > 2) {
                String restStr = kminStr.substring(0, kminStr.length() - 3) + "00";
                rest = Integer.parseInt(restStr);
            }

            return rest + tens + 1;
        }
    }

    /**
     * Retrieves the next k-mer value in the sequence
     *
     * @param kmer The current k-mer value
     * @return The next kmer value
     */
    protected int nextKmer(int kmer) {

        int mod1 = (kmer - 1) % 10;
        int mod2 = (kmer - 5) % 10;

        if (mod1 == 0) {
            return kmer + 4;
        } else if (mod2 == 0) {
            return kmer + 6;
        } else {
            throw new IllegalArgumentException("Kmer values have somehow got out of step!!");
        }
    }

    /**
     * Determines whether or not the supplied k-mer value is valid
     *
     * @param kmer The k-mer value to validate
     * @return True if valid, false otherwise
     */
    public boolean validKmer(int kmer) {

        int mod1 = (kmer - 1) % 10;
        int mod2 = (kmer - 5) % 10;

        return (mod1 == 0 || mod2 == 0);
    }


    protected void createSupportDirectories() {

        SingleMassArgs args = (SingleMassArgs) this.getProcessArgs();

        Assembler assembler = AssemblerFactory.createAssembler(args.getAssembler());

        // Create directory for links to assembled unitigs
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

        // Create directory for logs
        args.getLogsDir().mkdir();
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

            log.info("Starting Single MASS run for " + args.getConfig().getAbsolutePath());

            // Check the range looks reasonable
            log.debug("Validating kmer range");
            args.validateKmers(args.getKmin(), args.getKmax());

            // Create any required directories for this job
            log.debug("Creating directories");
            this.createSupportDirectories();

            WaitCondition assemblerWait = null;

            if (!args.isStatsOnly()) {

                // Dispatch an assembly job for each requested kmer
                for (int k = getFirstValidKmer(args.getKmin()); k <= args.getKmax(); k = nextKmer(k)) {

                    File outputDir = new File(args.getOutputDir(), Integer.toString(k));

                    log.debug("Starting " + args.getAssembler() + " in " + outputDir.getAbsolutePath());

                    Assembler assembler = AssemblerFactory.createAssembler(args.getAssembler(), k, args.getLibs(), outputDir);

                    log.debug("Assembler: " + assembler != null ? assembler.getName() : "NULL");

                    AssemblerArgs asmArgs = assembler.getArgs();

                    log.debug("Assembler Args: " + asmArgs.toString());

                    asmArgs.setThreads(args.getThreads());
                    asmArgs.setCoverageCutoff(args.getCoverageCutoff());

                    this.executeAssembler(assembler, args.getJobPrefix() + "-k" + k, executionContext);

                    this.createLinks(assembler, k, executionContext);
                }

                // Create this wait job if we are using a scheduler and running in parallel.
                if (executionContext.usingScheduler()) {

                    if (args.getParallelismLevel() == MassArgs.ParallelismLevel.PARALLEL_ASSEMBLIES_ONLY) {

                        log.debug("Running assemblies in parallel, waiting for completion");
                        this.executeScheduledWait(args.getJobPrefix(), args.getOutputDir(), executionContext);
                    }
                    else if (args.getParallelismLevel().doParallelMass()) {

                        log.debug("Running MASS in parallel, so creating wait condition for stats job and continuing");
                        assemblerWait = executionContext.getScheduler().createWaitCondition(ExitStatus.Type.COMPLETED_SUCCESS, args.getJobPrefix() + "*");
                    }
                }
            }

            // Run analyser job using the original execution context
            log.debug("Analysing and comparing assemblies");
            this.dispatchStatsJob(assemblerWait, executionContext);

            log.info("Finished MASS run");

        } catch (IOException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        } catch (CommandExecutionException cee) {
            throw new ProcessExecutionException(-2, cee);
        }

        return true;
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

        if (assembler.makesUnitigs()) {
            compoundLinkCmdLine.add(makeLinkCmdLine(assembler.getUnitigsFile(), args.getUnitigsDir(), k));
        }

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

            executionContextCopy.setForegroundJob(!args.getParallelismLevel().doParallelAssemblies());
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


    protected void dispatchStatsJob(WaitCondition waitCondition, ExecutionContext executionContext) throws InterruptedException, ProcessExecutionException, IOException, CommandExecutionException {

        SingleMassArgs args = (SingleMassArgs) this.getProcessArgs();
        ExecutionContext executionContextCopy = executionContext.copy();

        // I think it's safe to assume we have at least one lib otherwise we wouldn't have got this far.

        String dataset = args.getLibs() != null && args.getLibs().size() > 0 ? args.getJobName() : "";
        String jobName = args.getJobPrefix() + "-analyser";

        if (executionContextCopy.usingScheduler()) {
            SchedulerArgs schedulerArgs = executionContextCopy.getScheduler().getArgs();

            schedulerArgs.setJobName(jobName);
            schedulerArgs.setThreads(1);
            schedulerArgs.setMemoryMB(0);
            schedulerArgs.setWaitCondition(waitCondition);

            executionContextCopy.setForegroundJob(args.getParallelismLevel() == MassArgs.ParallelismLevel.LINEAR);
        }

        // Build compound command for running a stat job for each assembly type
        Assembler assembler = AssemblerFactory.createAssembler(args.getAssembler());
        StringJoiner statCommands = new StringJoiner("; ");

        if (assembler.makesUnitigs()) {

            ExecutionContext executionContextCopyUnitigs = executionContextCopy.copy();

            if (executionContextCopyUnitigs.usingScheduler()) {

                SchedulerArgs schedulerArgs = executionContextCopyUnitigs.getScheduler().getArgs();

                String unitigJobName = jobName + "-unitigs";

                schedulerArgs.setJobName(unitigJobName);
                schedulerArgs.setMonitorFile(new File(args.getUnitigsDir(), unitigJobName + ".log"));
            }

            AscV10Args ascArgs = new AscV10Args();
            ascArgs.setInputDir(args.getUnitigsDir());
            ascArgs.setOutputDir(args.getUnitigsDir());

            AscV10Process ascProcess = new AscV10Process(ascArgs);

            try {
                this.conanProcessService.execute(ascProcess, executionContextCopyUnitigs);
            }
            catch(ProcessExecutionException pee) {
                // If an error occurs here it isn't critical so just log the error and continue
                log.error(pee.getMessage(), pee);
            }
        }

        if (assembler.makesContigs()) {

            ExecutionContext executionContextCopyContigs = executionContextCopy.copy();

            if (executionContextCopyContigs.usingScheduler()) {

                SchedulerArgs schedulerArgs = executionContextCopyContigs.getScheduler().getArgs();

                String contigJobName = jobName + "-contigs";

                schedulerArgs.setJobName(contigJobName);
                schedulerArgs.setMonitorFile(new File(args.getContigsDir(), contigJobName + ".log"));
            }

            AscV10Args ascArgs = new AscV10Args();
            ascArgs.setInputDir(args.getContigsDir());
            ascArgs.setOutputDir(args.getContigsDir());

            AscV10Process ascProcess = new AscV10Process(ascArgs);

            try {
                this.conanProcessService.execute(ascProcess, executionContextCopyContigs);
            }
            catch(ProcessExecutionException pee) {
                // If an error occurs here it isn't critical so just log the error and continue
                log.error(pee.getMessage(), pee);
            }
        }

        if (assembler.makesScaffolds()) {

            ExecutionContext executionContextCopyScaffolds = executionContextCopy.copy();

            if (executionContextCopyScaffolds.usingScheduler()) {

                SchedulerArgs schedulerArgs = executionContextCopyScaffolds.getScheduler().getArgs();

                String scaffoldJobName = jobName + "-scaffolds";

                schedulerArgs.setJobName(scaffoldJobName);
                schedulerArgs.setMonitorFile(new File(args.getScaffoldsDir(), scaffoldJobName + ".log"));
            }

            AscV10Args ascArgs = new AscV10Args();
            ascArgs.setInputDir(args.getScaffoldsDir());
            ascArgs.setOutputDir(args.getScaffoldsDir());

            AscV10Process ascProcess = new AscV10Process(ascArgs);

            try {
                this.conanProcessService.execute(ascProcess, executionContextCopyScaffolds);
            }
            catch(ProcessExecutionException pee) {
                // If an error occurs here it isn't critical so just log the error and continue
                log.error(pee.getMessage(), pee);
            }
        }

        // Create this wait job if we are using a scheduler and running in assemblies in parallel only.  If we are running
        // MASS in parallel we skip this wait and continue
        if (executionContext.usingScheduler() && args.getParallelismLevel() == MassArgs.ParallelismLevel.PARALLEL_ASSEMBLIES_ONLY) {

            log.debug("Running assembly analysis in parallel, waiting for completion");
            this.executeScheduledWait(jobName, args.getOutputDir(), executionContext);
        }
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
