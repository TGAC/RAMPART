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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
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

    @Autowired
    private MecqExecutor mecqExecutor = new MecqExecutorImpl();


    public MecqProcess() {
        this(new MecqArgs());
    }

    public MecqProcess(MecqArgs args) {
        super("", args, new MecqParams());
    }


    @Override
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        log.info("Starting MECQ Process");

        // Create shortcut to args for convienience
        MecqArgs args = (MecqArgs) this.getProcessArgs();

        // Initialise executor
        this.mecqExecutor.initialise(this.conanProcessService, executionContext);

        // If the output directory doesn't exist then make it
        if (!args.getOutputDir().exists()) {
            log.debug("Creating output directory");
            args.getOutputDir().mkdirs();
        }

        // For each ecq process all libraries
        for(EcqArgs ecqArgs : args.getEqcArgList()) {

            // Create an output dir for this error corrector
            File ecDir = new File(args.getOutputDir(), ecqArgs.getName());
            ecDir.mkdirs();

            // Process each lib
            for(Library lib : ecqArgs.getLibraries()) {

                // Create the output directory
                File ecqLibDir = new File(ecDir, lib.getName());
                ecqLibDir.mkdirs();

                // Create a job name
                String jobName = ecqArgs.getJobPrefix() + "_" + ecqArgs.getName() + "_" + lib.getName();

                // Create the actual error corrector from the user provided EcqArgs
                ErrorCorrector ec = makeErrorCorrector(ecqArgs, lib, ecqLibDir);

                // Create symbolic links between file paths specified by the library and the working directory for this ECQ
                this.mecqExecutor.createInputLinks(lib, ec.getArgs());

                // Execute this error corrector
                this.mecqExecutor.executeEcq(ec, ecqLibDir, jobName, ecqArgs.isRunParallel());

                // Check to see if we should run each ECQ in parallel, if not wait here until each ECQ has completed
                /*if (executionContext.usingScheduler() && !ecqArgs.isRunParallel()) {
                    log.debug("Waiting for completion of: " + ecqArgs.getName() + "; for library: " + lib.getName());
                    this.mecqExecutor.executeScheduledWait(
                            ecqArgs.getJobPrefix() + "*",
                            args.getJobPrefix() + "-wait",
                            ecqLibDir);
                } */
            }

            // If we're using a scheduler, and we don't want to run separate ECQ in parallel, and we want to parallelise
            // each library processed by this ECQ, then wait here.
            if (executionContext.usingScheduler() && ecqArgs.isRunParallel() && !args.isRunParallel()) {
                log.debug("Waiting for completion of: " + ecqArgs.getName() + "; for all requested libraries");
                this.mecqExecutor.executeScheduledWait(
                        ecqArgs.getJobPrefix() + "*",
                        ExitStatus.Type.COMPLETED_SUCCESS,
                        args.getJobPrefix() + "-wait",
                        ecDir);
            }
        }

        // If we're using a scheduler and we have been asked to run each MECQ group for each library
        // in parallel, then we should wait for all those to complete before continueing.
        if (executionContext.usingScheduler() && args.isRunParallel() && !args.getEqcArgList().isEmpty()) {
            log.debug("Running all ECQ groups in parallel, waiting for completion");
            this.mecqExecutor.executeScheduledWait(
                    args.getJobPrefix() + "-ecq*",
                    ExitStatus.Type.COMPLETED_SUCCESS,
                    args.getJobPrefix() + "-wait",
                    args.getOutputDir());
        }

        log.info("MECQ complete");

        return true;
    }


    /**
     * Using a set of ECQ specific args, creates an ErrorCorrector object for execution
     * @param mecqArgs
     * @param inputLib
     * @param outputDir
     * @return
     */
    public ErrorCorrector makeErrorCorrector(EcqArgs mecqArgs, Library inputLib, File outputDir) {

        ErrorCorrector ec = ErrorCorrectorFactory.valueOf(mecqArgs.getTool()).create();
        ErrorCorrectorArgs ecArgs = ec.getArgs();

        ecArgs.setMinLength(mecqArgs.getMinLen());
        ecArgs.setQualityThreshold(mecqArgs.getMinQual());
        ecArgs.setKmer(mecqArgs.getKmer());
        ecArgs.setThreads(mecqArgs.getThreads());
        ecArgs.setMemoryGb(mecqArgs.getMemory());
        ecArgs.setOutputDir(outputDir);

        // Add files to ec (assumes ECQ tool and input libraries are compatible with regards to Paired / Single End)
        if (inputLib.isPairedEnd()) {
            ((ErrorCorrectorPairedEndArgs)ec.getArgs()).setFromLibrary(inputLib,
                    new File(outputDir, inputLib.getFile1().getName()),
                    new File(outputDir, inputLib.getFile2().getName()));
        }
        else {
            ((ErrorCorrectorSingleEndArgs)ec.getArgs()).setFromLibrary(inputLib,
                    new File(outputDir, inputLib.getFile1().getName()));
        }

        return ec;
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
