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

import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.SchedulerArgs;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.util.StringJoiner;
import uk.ac.tgac.conan.core.data.FilePair;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.process.ec.AbstractErrorCorrector;
import uk.ac.tgac.conan.process.ec.AbstractErrorCorrectorArgs;
import uk.ac.tgac.conan.process.ec.AbstractErrorCorrectorPairedEndArgs;
import uk.ac.tgac.conan.process.ec.AbstractErrorCorrectorSingleEndArgs;
import uk.ac.tgac.rampart.tool.RampartExecutorImpl;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * User: maplesod
 * Date: 22/08/13
 * Time: 15:04
 */
public class MecqExecutorImpl extends RampartExecutorImpl implements MecqExecutor {

    @Override
    public void executeEcq(AbstractErrorCorrector errorCorrector, File outputDir, String jobName, boolean runInParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException {

        // Ensure downstream process has access to the process service
        errorCorrector.initialise();

        // Duplicate the execution context so we don't modify the original accidentally.
        ExecutionContext executionContextCopy = this.executionContext.copy();
        executionContextCopy.setContext(
                jobName,
                executionContextCopy.usingScheduler() ? !runInParallel : true,
                new File(outputDir, jobName + ".log"));

        if (this.executionContext.usingScheduler()) {

            SchedulerArgs schedulerArgs = executionContextCopy.getScheduler().getArgs();
            AbstractErrorCorrectorArgs ecArgs = errorCorrector.getArgs();

            schedulerArgs.setThreads(ecArgs.getThreads());
            schedulerArgs.setMemoryMB(ecArgs.getMemoryGb() * 1000);
        }

        this.conanProcessService.execute(errorCorrector, executionContextCopy);
    }

    @Override
    public void createInputLinks(Library library, AbstractErrorCorrectorArgs args)
            throws ProcessExecutionException, InterruptedException {

        // Modify execution context so we execute these instructions straight away (i.e. no scheduling)
        ExecutionContext linkingExecutionContext = new DefaultExecutionContext(this.executionContext.getLocality(), null, null);

        if (library.isPairedEnd()) {

            FilePair pairedEndFiles = ((AbstractErrorCorrectorPairedEndArgs)args).getPairedEndInputFiles();

            StringJoiner compoundLinkCmdLine = new StringJoiner(";");

            compoundLinkCmdLine.add(this.conanProcessService.makeLinkCommand(library.getFile1(), pairedEndFiles.getFile1()));
            compoundLinkCmdLine.add(this.conanProcessService.makeLinkCommand(library.getFile2(), pairedEndFiles.getFile2()));

            this.conanProcessService.execute(compoundLinkCmdLine.toString(), linkingExecutionContext);
        }
        else {
            conanProcessService.execute(this.conanProcessService.makeLinkCommand(library.getFile1(),
                    ((AbstractErrorCorrectorSingleEndArgs)args).getSingleEndInputFile()), linkingExecutionContext);
        }
    }

    @Override
    public void createOutputLinks(File outputDir, AbstractErrorCorrector ec, EcqArgs ecqArgs, Library library)
            throws ProcessExecutionException, InterruptedException {

        // Make sure the output directory exists
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        ExecutionContext linkingExecutionContext = new DefaultExecutionContext(executionContext.getLocality(), null, null);

        StringJoiner compoundLinkCmdLine = new StringJoiner(";");

        Library modLib = ec == null ? library : ec.getArgs().getOutputLibrary(library);

        for(File file : modLib.getFiles()) {
            compoundLinkCmdLine.add(
                    this.conanProcessService.makeLinkCommand(file,
                            new File(outputDir, file.getName())));
        }

        this.conanProcessService.execute(compoundLinkCmdLine.toString(), linkingExecutionContext);
    }


    /**
     * Modifies the libraries so that they contain they point to the files generated by the error corrector, rather than
     * the input files.
     * @param ec
     * @param lib
     * @param ecqArgs
     * @return
     * @throws ProcessExecutionException
     */
    protected Library modifyLib(AbstractErrorCorrector ec, Library lib, EcqArgs ecqArgs) throws ProcessExecutionException {

        Library modLib = lib.copy();

        List<File> files = ec.getArgs().getCorrectedFiles();

        try {
            if (modLib.isPairedEnd()) {
                if (files.size() < 2 || files.size() > 3) {
                    throw new IOException("Paired end library: " + modLib.getName() + " from " + ecqArgs.getName() + " does not have two or three files");
                }

                modLib.setFiles(files.get(0), files.get(1));
            }
            else {
                if (files.size() != 1) {
                    throw new IOException("Single end library: " + modLib.getName() + " from " + ecqArgs.getName() + " does not have one file");
                }

                modLib.setFiles(files.get(0), null);
            }

            return modLib;
        }
        catch(IOException ioe) {
            throw new ProcessExecutionException(3, ioe);
        }
    }
}
