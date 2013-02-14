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
package uk.ac.tgac.rampart.pipeline.tool.proc.internal.mass.single;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.model.context.SchedulerArgs;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.rampart.core.data.RampartConfiguration;
import uk.ac.tgac.rampart.core.utils.StringJoiner;
import uk.ac.tgac.rampart.pipeline.tool.proc.external.asm.Assembler;
import uk.ac.tgac.rampart.pipeline.tool.proc.internal.util.PerlHelper;

import java.io.File;
import java.io.IOException;

@Component
public class SingleMassProcess extends AbstractConanProcess {

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

        Assembler assembler = args.getAssembler();

        // Create directory for links to assembled contigs
        if (assembler.makesUnitigs()) {
            args.getUnitigsDir().mkdir();
        }

        if (assembler.makesContigs()) {
            args.getContigsDir().mkdir();
        }

        // Create dir for scaffold links if this asm creates them
        if (args.getAssembler().makesScaffolds()) {
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
            SingleMassArgs args = (SingleMassArgs) this.getProcessArgs();

            RampartConfiguration config = new RampartConfiguration();
            config.load(args.getConfig());

            // Check the range looks reasonable
            args.validateKmers(args.getKmin(), args.getKmax());

            // Input the assembler's libraries
            args.getAssembler().getArgs().setLibraries(args.getLibs());

            ExecutionContext asmExeCtx = createAssemblerExecutionContext(executionContext);

            // Create any required directories for this job
            createSupportDirectories();

            // Dispatch an assembly job for each requested kmer
            for (int k = getFirstValidKmer(args.getKmin()); k <= args.getKmax(); k = nextKmer(k)) {

                File kDir = new File(args.getOutputDir(), String.valueOf(k));

                // Make the output directory for this child job (delete the directory if it exists)
                if (kDir.exists()) {
                    FileUtils.deleteDirectory(kDir);
                }
                kDir.mkdir();

                // Modify the ProcessArgs kmer value
                args.getAssembler().getArgs().setKmer(k);

                // Modify the scheduler jobname is present
                if (asmExeCtx.usingScheduler()) {
                    asmExeCtx.getScheduler().getArgs().setJobName(args.getJobPrefix() + "-k" + k);
                }

                // Create proc
                this.conanProcessService.execute(args.getAssembler(), asmExeCtx);
            }

            // Wait for all assembly jobs to finish if they are running as background tasks.
            if (asmExeCtx.usingScheduler() && !asmExeCtx.isForegroundJob()) {

                this.conanProcessService.waitFor(
                        asmExeCtx.getScheduler().createWaitCondition(
                                ExitStatus.Type.COMPLETED_SUCCESS,
                                args.getJobPrefix() + "*"),
                        asmExeCtx);
            }

            // Run stats job using the original execution context
            this.dispatchStatsJob(executionContext);     // Load the config file
        } catch (IOException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        } catch (CommandExecutionException cee) {
            throw new ProcessExecutionException(-2, cee);
        }

        return true;
    }


    protected ExecutionContext createAssemblerExecutionContext(ExecutionContext executionContext) {

        ExecutionContext newExecutionContext = executionContext.copy();

        if (newExecutionContext.usingScheduler()) {
            SchedulerArgs schArgs = newExecutionContext.getScheduler().getArgs().copy();

            // Set mem required to 60GB if no memory requested
            if (schArgs.getMemoryMB() == 0) {
                schArgs.setMemoryMB(60000);
            }

            // Set num threads to 8 if none specified
            if (schArgs.getThreads() == 0) {
                schArgs.setThreads(8);
            }

            // If we're using a job scheduler then make sure the assembly jobs run in the background
            executionContext.setForegroundJob(false);
        }


        return newExecutionContext;
    }


    protected String buildStatCmdLine(File statDir) {

        StringJoiner mgpArgs = new StringJoiner(" ");

        mgpArgs.add(PerlHelper.MASS_GP.getPath());
        mgpArgs.add("--grid_engine NONE");
        mgpArgs.add("--input " + statDir.getPath());
        mgpArgs.add("--output " + statDir.getPath());
        //mgpArgs.add(this.isVerbose(), "", "--verbose");

        return mgpArgs.toString();
    }


    protected void dispatchStatsJob(ExecutionContext env) throws InterruptedException, ProcessExecutionException, IOException, CommandExecutionException {

        SingleMassArgs args = (SingleMassArgs) this.getProcessArgs();

        // Alter the environment for this job if using a scheduler
        SchedulerArgs schArgsCopy = null;
        SchedulerArgs schArgsBackup = null;
        if (env.usingScheduler()) {
            schArgsBackup = env.getScheduler().getArgs();
            schArgsCopy = env.getScheduler().getArgs().copy();

            schArgsCopy.setJobName(args.getJobPrefix() + "-stats");
            schArgsCopy.setThreads(0);
            schArgsCopy.setMemoryMB(0);
            schArgsCopy.setBackgroundTask(false);
            env.getScheduler().setArgs(schArgsCopy);
        }

        StringJoiner statCommands = new StringJoiner("; ");

        statCommands.add(buildStatCmdLine(args.getUnitigsDir()));
        statCommands.add(buildStatCmdLine(args.getContigsDir()));
        statCommands.add(buildStatCmdLine(args.getScaffoldsDir()));

        // Create proc
        this.conanProcessService.execute(statCommands.toString(), env);

        if (env.usingScheduler()) {
            env.getScheduler().setArgs(schArgsBackup);
        }
    }

    @Override
    public String getName() {
        return "MASS";
    }

    @Override
    public String getCommand() {
        return this.getFullCommand();
    }

}
