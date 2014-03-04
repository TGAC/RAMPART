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
package uk.ac.tgac.rampart.tool.process.amp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.process.asmIO.AbstractAssemblyIOProcess;
import uk.ac.tgac.conan.process.asmIO.AssemblyIOFactory;

import java.io.IOException;

/**
 * User: maplesod
 * Date: 16/08/13
 * Time: 15:52
 */
public class AmpStageProcess extends AbstractConanProcess {


    private static Logger log = LoggerFactory.getLogger(AmpStageProcess.class);

    private AmpStageExecutor ampStageExecutor;

    public AmpStageProcess(AmpStageArgs args) {
        super("", args, new AmpStageParams());
        this.ampStageExecutor = new AmpStageExecutorImpl();
    }

    public AmpStageArgs getAmpArgs() {
        return (AmpStageArgs)this.getProcessArgs();
    }

    /**
     * Dispatches amp stage to the specified environments
     *
     * @param executionContext The environment to dispatch jobs too
     * @throws IllegalArgumentException
     * @throws uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException
     * @throws InterruptedException
     */
    @Override
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        try {

            // Make a shortcut to the args
            AmpStageArgs args = (AmpStageArgs) this.getProcessArgs();

            // Initialise the object that makes system calls
            this.ampStageExecutor.initialise(this.getConanProcessService(), executionContext);


            log.info("Starting AMP stage " + args.getIndex());

            // Make sure reads file exists
            if (args.getInput() == null || !args.getInput().exists()) {
                throw new IOException("Input file for stage: " + args.getIndex() + " does not exist: " +
                        (args.getInput() == null ? "null" : args.getInput().getAbsolutePath()));
            }

            // Create output directory
            if (!args.getOutputDir().exists()) {
                args.getOutputDir().mkdir();
            }

            // Create the configuration for this stage
            AbstractAssemblyIOProcess ampProc = this.makeStage(args);

            // Execute the AMP stage
            this.ampStageExecutor.executeAmpStage(ampProc, "amp-" + args.getIndex());

            // Create links for outputs from this assembler to known locations
            this.getConanProcessService().createLocalSymbolicLink(ampProc.getAmpArgs().getOutputFile(), args.getOutputFile());

            log.info("Finished AMP stage " + args.getIndex());
        }
        catch (IOException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        }

        return true;
    }

    protected AbstractAssemblyIOProcess makeStage(AmpStageArgs args) throws IOException {

        AbstractAssemblyIOProcess proc = AssemblyIOFactory.create(args.getTool(), args.getInput(), args.getOutputDir(), "amp-" + args.getIndex(),
                args.getAllLibraries(), args.getThreads(), args.getMemory(), args.getOtherArgs());

        if (proc == null)
            throw new IOException("Could not find requested tool: " + args.getTool());

        return proc;
    }


    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public String getName() {
        // Make a shortcut to the args
        AmpStageArgs args = (AmpStageArgs) this.getProcessArgs();

        return args != null ? "AMP-" + args.getIndex() + " - " + args.getTool() : "Undefined-AMP-stage";
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        AmpStageArgs args = (AmpStageArgs)this.getProcessArgs();

        AbstractAssemblyIOProcess proc = null;

        try {
            proc = this.makeStage(args);
        } catch (IOException e) {
            log.warn("Could not create AMP stage");
            return false;
        }

        if (proc == null) {
            log.warn("Could not create AMP stage for tool: " + args.getTool() + "; check tool exists.");
            return false;
        }

        proc.setConanProcessService(this.getConanProcessService());

        return proc.isOperational(executionContext);
    }
}
