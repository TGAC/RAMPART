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
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.process.ec.ErrorCorrector;
import uk.ac.tgac.conan.process.ec.ErrorCorrectorArgs;
import uk.ac.tgac.conan.process.ec.ErrorCorrectorFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 07/01/13
 * Time: 10:54
 * To change this template use File | Settings | File Templates.
 */
public class MecqProcess extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(MecqProcess.class);

    private MecqExecutor mecqExecutor;


    public MecqProcess() {
        this(new MecqArgs());
    }

    public MecqProcess(ProcessArgs args) {
        super("", args, new MecqParams());

        this.mecqExecutor = new MecqExecutorImpl();
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

        List<Integer> jobIds = new ArrayList<>();
        List<ErrorCorrector> errorCorrectors = new ArrayList<>();

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
                ErrorCorrector ec = this.makeErrorCorrector(ecqArgs, lib, args.getOutputDir());

                // Add this to the list in case we need it later
                errorCorrectors.add(ec);

                // Create symbolic links between file paths specified by the library and the working directory for this ECQ
                this.mecqExecutor.createInputLinks(lib, ec.getArgs());

                // Execute this error corrector
                this.mecqExecutor.executeEcq(ec, ecqLibDir, jobName, ecqArgs.isRunParallel());

                // The job id should be stored in the process if we are using a scheduler, add to the list regardless
                // in case we need it later
                jobIds.add(ec.getJobId());
            }

            // If we're using a scheduler, and we don't want to run separate ECQ in parallel, and we want to parallelise
            // each library processed by this ECQ, then wait here.
            if (executionContext.usingScheduler() && ecqArgs.isRunParallel() && !args.isRunParallel()) {
                log.debug("Waiting for completion of: " + ecqArgs.getName() + "; for all requested libraries");
                this.mecqExecutor.executeScheduledWait(
                        jobIds,
                        ecqArgs.getJobPrefix() + "*",
                        ExitStatus.Type.COMPLETED_SUCCESS,
                        args.getJobPrefix() + "-wait",
                        ecDir);

                jobIds.clear();
            }
        }

        // If we're using a scheduler and we have been asked to run each MECQ group for each library
        // in parallel, then we should wait for all those to complete before continueing.
        if (executionContext.usingScheduler() && args.isRunParallel() && !args.getEqcArgList().isEmpty()) {
            log.debug("Running all ECQ groups in parallel, waiting for completion");
            this.mecqExecutor.executeScheduledWait(
                    jobIds,
                    args.getJobPrefix() + "-ecq*",
                    ExitStatus.Type.COMPLETED_SUCCESS,
                    args.getJobPrefix() + "-wait",
                    args.getOutputDir());
        }

        log.info("MECQ Finished");

        return true;
    }



    /**
     * Using a set of ECQ specific args, creates an ErrorCorrector object for execution
     * @param ecqArgs
     * @param inputLib
     * @param mecqDir
     * @return
     */
    public ErrorCorrector makeErrorCorrector(EcqArgs ecqArgs, Library inputLib, File mecqDir) {

        File ecDir = new File(mecqDir, ecqArgs.getName());
        File ecqLibDir = new File(ecDir, inputLib.getName());

        ErrorCorrector ec = ErrorCorrectorFactory.valueOf(ecqArgs.getTool()).create();
        ErrorCorrectorArgs ecArgs = ec.getArgs();

        ecArgs.setMinLength(ecqArgs.getMinLen());
        ecArgs.setQualityThreshold(ecqArgs.getMinQual());
        ecArgs.setKmer(ecqArgs.getKmer());
        ecArgs.setThreads(ecqArgs.getThreads());
        ecArgs.setMemoryGb(ecqArgs.getMemory());
        ecArgs.setOutputDir(ecqLibDir);

        // Add files to ec (assumes ECQ tool and reads libraries are compatible with regards to Paired / Single End)
        List<File> altInputFiles = new ArrayList<>();
        if (inputLib.isPairedEnd()) {
            altInputFiles.add(new File(ecqLibDir, inputLib.getFile1().getName()));
            altInputFiles.add(new File(ecqLibDir, inputLib.getFile2().getName()));
        }
        else {
            altInputFiles.add(new File(ecqLibDir, inputLib.getFile1().getName()));
        }
        ec.getArgs().setFromLibrary(inputLib, altInputFiles);

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
