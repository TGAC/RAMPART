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
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.process.asmIO.AbstractAssemblyIOProcess;
import uk.ac.tgac.conan.process.asmIO.AssemblyIOFactory;

import java.io.File;
import java.io.IOException;

/**
 * User: maplesod
 * Date: 16/08/13
 * Time: 15:52
 */
public class AmpStageProcess extends AbstractConanProcess {


    private static Logger log = LoggerFactory.getLogger(AmpStageProcess.class);


    public AmpStageProcess(AmpStageArgs args) {
        super("", args, new AmpStageParams());
    }

    public AmpStageArgs getAmpArgs() {
        return (AmpStageArgs)this.getProcessArgs();
    }

    /**
     * Dispatches amp stage to the specified environments
     *
     * @param executionContext The environment to dispatch jobs too
     * @throws java.io.IOException
     * @throws IllegalArgumentException
     * @throws uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException
     * @throws InterruptedException
     */
    @Override
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        try {

            // Make a shortcut to the args
            AmpStageArgs args = (AmpStageArgs) this.getProcessArgs();

            log.info("Starting AMP stage " + args.getIndex());

            // Make sure input file exists
            if (args.getInput() == null || !args.getInput().exists()) {
                throw new IOException("Input file for stage: " + args.getIndex() + " does not exist");
            }

            // Create output directory
            if (!args.getOutputDir().exists()) {
                args.getOutputDir().mkdir();
            }

            // Create the actual assembler for these settings
            AbstractAssemblyIOProcess ampProc = this.makeStage(args);

            // Execute the AMP stage
            this.executeStage(ampProc, args.getJobPrefix(), executionContext);

            // Create links for outputs from this assembler to known locations
            this.createLink(args.getOutputFile(), args.getAssembliesDir(), args.getIndex(), executionContext);

            log.info("Finished AMP stage " + args.getIndex());

        } catch (IOException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        }

        return true;
    }

    protected AbstractAssemblyIOProcess makeStage(AmpStageArgs args) {

        AbstractAssemblyIOProcess proc = AssemblyIOFactory.create(args.getTool(), args.getInput(), args.getOutputDir(), "amp-" + args.getIndex(),
                args.getAllLibraries(), args.getThreads(), args.getMemory(), args.getOtherArgs());

        return proc;
    }

    protected void executeStage(AbstractAssemblyIOProcess ampProc, String jobPrefix, ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        this.conanProcessService.execute(ampProc, executionContext);
    }

    protected String makeLinkCommand(File source, File target) {
        return "ln -s -f " + source.getAbsolutePath() + " " + target.getAbsolutePath();
    }


    protected void createLink(File sourceFile, File outputDir, int stageNumber, ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        String linkCommand = makeLinkCommand(sourceFile, new File(outputDir, "amp-stage-" + stageNumber + "-scaffolds.fa"));

        ExecutionContext linkingExecutionContext = new DefaultExecutionContext(executionContext.getLocality(), null, null, true);

        this.conanProcessService.execute(linkCommand, linkingExecutionContext);
    }

    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
