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
package uk.ac.tgac.rampart.tool.process.mecq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.model.context.SchedulerArgs;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.util.StringJoiner;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.process.ec.*;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 07/01/13
 * Time: 10:54
 * To change this template use File | Settings | File Templates.
 */
@Component
public class MecqProcess extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(MecqProcess.class);


    public MecqProcess() {
        this(new MecqArgs());
    }

    public MecqProcess(MecqArgs args) {
        super("", args, new MecqParams());
    }


    @Override
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        // Create this for situations where we need to create a symbolic link straight away
        ExecutionContext linkingExecutionContext = new DefaultExecutionContext(executionContext.getLocality(), null, null, true);

        log.info("Starting MECQ Process");

        // Create shortcut to args for convienience
        MecqArgs args = (MecqArgs) this.getProcessArgs();

        // If the output directory doesn't exist then make it
        if (!args.getOutputDir().exists()) {
            log.debug("Creating output directory");
            args.getOutputDir().mkdirs();
        }

        // For each ecq process all libraries
        for(MecqSingleArgs singleMecqArgs : args.getEqcArgList()) {

            // Create an output dir for this error corrector
            File ecDir = new File(args.getOutputDir(), singleMecqArgs.getName());
            ecDir.mkdirs();

            // Process each lib
            for(Library lib : singleMecqArgs.getLibraries()) {

                String libName = lib.getName().toLowerCase();

                File ecqLibDir = new File(ecDir, libName);
                ecqLibDir.mkdirs();

                // Create the actual error corrector
                ErrorCorrector ec = makeErrorCorrector(singleMecqArgs, ecqLibDir);

                if (lib.isPairedEnd()) {

                    // Create links to files in output dir (makes things simpler if we need to auto-gen config files)
                    StringJoiner compoundLinkCmdLine = new StringJoiner(";");

                    compoundLinkCmdLine.add(makeLinkCmdLine(lib.getFile1(), ecqLibDir));
                    compoundLinkCmdLine.add(makeLinkCmdLine(lib.getFile2(), ecqLibDir));

                    this.conanProcessService.execute(compoundLinkCmdLine.toString(), linkingExecutionContext);

                    // Add libs to ec
                    ((ErrorCorrectorPairedEndArgs)ec.getArgs()).setFromLibrary(lib,
                            new File(ecqLibDir, lib.getFile1().getName()),
                            new File(ecqLibDir, lib.getFile2().getName()));
                }
                else {
                    this.conanProcessService.execute(makeLinkCmdLine(lib.getFile1(), ecqLibDir), linkingExecutionContext);

                    // Add libs to ec
                    ((ErrorCorrectorSingleEndArgs)ec.getArgs()).setFromLibrary(lib,
                            new File(ecqLibDir, lib.getFile1().getName()));
                }


                String jobName = args.getJobPrefix() + "_" + singleMecqArgs.getName() + "_" + libName;
                this.executeEcq(ec, jobName, args.isRunParallel(), ecqLibDir, executionContext);
            }
        }

        // If we're using a scheduler and we have been asked to run the mecq processes for each library
        // in parallel, then we should wait for all those to complete before continueing.
        if (executionContext.usingScheduler() && args.isRunParallel() && !args.getEqcArgList().isEmpty()) {
            log.debug("Running Quality trimming step in parallel, waiting for completion");
            this.executeScheduledWait(args.getJobPrefix(), args.getOutputDir(), executionContext);
        }

        log.info("MECQ complete");

        return true;
    }

    protected String makeLinkCmdLine(File sourceFile, File outputDir) {

        return "ln -s -f " + sourceFile.getAbsolutePath() + " " + new File(outputDir, sourceFile.getName()).getAbsolutePath();
    }


    public ErrorCorrector makeErrorCorrector(MecqSingleArgs mecqArgs, File outputDir) {

        ErrorCorrector ec = ErrorCorrectorFactory.valueOf(mecqArgs.getTool()).create();
        ErrorCorrectorArgs ecArgs = ec.getArgs();

        ecArgs.setMinLength(mecqArgs.getMinLen());
        ecArgs.setQualityThreshold(mecqArgs.getMinQual());
        ecArgs.setKmer(mecqArgs.getKmer());
        ecArgs.setThreads(mecqArgs.getThreads());
        ecArgs.setMemoryGb(mecqArgs.getMemory());
        ecArgs.setOutputDir(outputDir);

        return ec;
    }


    protected void executeEcq(ErrorCorrector errorCorrector, String jobName, boolean runInParallel,
                              File outputDir, ExecutionContext executionContext)
            throws ProcessExecutionException, InterruptedException {

        // Duplicate the execution context so we don't modify the original accidentally.
        ExecutionContext executionContextCopy = executionContext.copy();

        // Ensure downstream process has access to the process service
        errorCorrector.configure(this.getConanProcessService());
        errorCorrector.initialise();

        if (executionContext.usingScheduler()) {

            SchedulerArgs schedulerArgs = executionContextCopy.getScheduler().getArgs();
            ErrorCorrectorArgs ecArgs = errorCorrector.getArgs();

            schedulerArgs.setJobName(jobName);
            schedulerArgs.setMonitorFile(new File(outputDir, jobName + ".log"));
            schedulerArgs.setThreads(ecArgs.getThreads());
            schedulerArgs.setMemoryMB(ecArgs.getMemoryGb() * 1000);

            executionContextCopy.setForegroundJob(!runInParallel);
        }

        this.conanProcessService.execute(errorCorrector, executionContextCopy);
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


    @Override
    public String getName() {
        return "MECQ";
    }

    @Override
    public String getCommand() {
        return null;
    }

}
