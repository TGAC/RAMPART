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
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.SeqFile;
import uk.ac.tgac.conan.process.ec.ErrorCorrector;
import uk.ac.tgac.conan.process.ec.ErrorCorrectorPairedEndArgs;
import uk.ac.tgac.conan.process.ec.ErrorCorrectorSingleEndArgs;
import uk.ac.tgac.rampart.data.RampartConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

        try {

            log.info("Starting MECQ Process");

            // Create shortcut to args for convienience
            MecqArgs args = (MecqArgs) this.getProcessArgs();

            // Create the QualityTrimmer processes to execute
            List<ErrorCorrector> ecqs = args.createMecqs();
            List<Library> libs = args.getLibsToProcess();

            // If the output directory doesn't exist then make it
            if (!args.getOutputDir().exists()) {
                log.debug("Creating output directory");
                args.getOutputDir().mkdirs();
            }

            // Create configs directory
            File configDir = new File(args.getOutputDir(), "configs");
            configDir.mkdirs();

            // Essentially copy the file and rename as raw.cfg
            createConfig(args.getConfig(), null, configDir);

            // For each ecq process all libraries
            for(ErrorCorrector ec : ecqs) {

                // Get the name to call this error corrector
                String ecName = ec.getName().toLowerCase();

                // Create an output dir for this error corrector
                File ecDir = new File(args.getOutputDir(), ecName);
                ecDir.mkdirs();

                // Process each lib
                for(Library lib : libs) {

                    String libName = lib.getName().toLowerCase();

                    File ecqLibDir = new File(ecDir, libName);
                    ecqLibDir.mkdirs();

                    String jobName = args.getJobPrefix() + "_" + ecName + "_" + libName;
                    this.executeEcq(ec, jobName, args.isRunParallel(), ecqLibDir, executionContext);
                }

                createConfig(args.getConfig(), ec, configDir);
            }

            // If we're using a scheduler and we have been asked to run the quality trimming processes for each library
            // in parallel, then we should wait for all those to complete before continueing.
            if (executionContext.usingScheduler() && args.isRunParallel()) {
                log.debug("Running Quality trimming step in parallel, waiting for completion");
                this.executeScheduledWait(args.getJobPrefix(), args.getOutputDir(), executionContext);
            }

            log.info("MECQ complete");

        } catch (IOException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        }

        return true;
    }


    protected void executeEcq(ErrorCorrector errorCorrector, String jobName, boolean runInParallel,
                              File outputDir, ExecutionContext executionContext)
            throws ProcessExecutionException, InterruptedException {

        // Duplicate the execution context so we don't modify the original accidentally.
        ExecutionContext executionContextCopy = executionContext.copy();

        // Ensure downstream process has access to the process service
        errorCorrector.configure(this.getConanProcessService());
        errorCorrector.getArgs().setOutputDir(outputDir);

        if (executionContext.usingScheduler()) {

            executionContextCopy.getScheduler().getArgs().setJobName(jobName);
            executionContextCopy.getScheduler().getArgs().setMonitorFile(new File(outputDir, jobName + ".log"));
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



    protected void createConfig(File baseConfigFile, ErrorCorrector errorCorrector, File configDir) throws IOException {

        RampartConfiguration baseConfig = RampartConfiguration.loadFile(baseConfigFile);

        String name = errorCorrector == null ? "raw" : errorCorrector.getName().toLowerCase();
        baseConfig.getMassSettings().add("job", name);

        for(Library lib : baseConfig.getLibs()) {
            if (lib.testUsage(Library.Usage.QUALITY_TRIMMING)) {

                if (errorCorrector.getArgs().isSingleEndOnly()) {

                    ErrorCorrectorSingleEndArgs ecPairedEndArgs = (ErrorCorrectorSingleEndArgs)errorCorrector.getArgs();

                    lib.setSeFile(new SeqFile(ecPairedEndArgs.getCorrectedFile()));
                }
                else {

                    ErrorCorrectorPairedEndArgs ecPairedEndArgs = (ErrorCorrectorPairedEndArgs)errorCorrector.getArgs();

                    lib.setFilePaired1(new SeqFile(ecPairedEndArgs.getPairedEndCorrectedFiles().getFile1()));
                    lib.setFilePaired2(new SeqFile(ecPairedEndArgs.getPairedEndCorrectedFiles().getFile2()));
                    lib.setSeFile(new SeqFile(ecPairedEndArgs.getSingleEndCorrectedFiles().get(0)));
                }
            }
        }
        baseConfig.save(new File(configDir, name + ".cfg"));
    }


    @Override
    public String getName() {
        return "QT";
    }

    @Override
    public String getCommand() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
