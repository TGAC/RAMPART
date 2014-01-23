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
package uk.ac.tgac.rampart.tool.process.mass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.tool.process.analyse.asm.selector.DefaultAssemblySelector;
import uk.ac.tgac.rampart.tool.process.analyse.asm.stats.AssemblyStats;
import uk.ac.tgac.rampart.tool.process.analyse.asm.stats.AssemblyStatsTable;
import uk.ac.tgac.rampart.tool.process.mass.single.SingleMassArgs;
import uk.ac.tgac.rampart.tool.process.mass.single.SingleMassProcess;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: maplesod
 * Date: 01/02/13
 * Time: 11:10
 */
public class MassProcess extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(MassProcess.class);

    private MassExecutor massExecutor;

    public MassProcess() {
        this(new MassArgs());
    }

    public MassProcess(ProcessArgs args) {
        super("", args, new MassParams());
        this.massExecutor = new MassExecutorImpl();
    }

    @Override
    public String getName() {
        return "MASS";
    }

    @Override
    public Collection<ConanParameter> getParameters() {
        return new MassParams().getConanParameters();
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        MassArgs args = (MassArgs) this.getProcessArgs();

        for(SingleMassArgs singleMassArgs : args.getSingleMassArgsList()) {
            if (!new SingleMassProcess(singleMassArgs, this.conanProcessService).isOperational(executionContext)) {
                log.warn("MASS stage is NOT operational.");
                return false;
            }
        }

        log.info("MASS stage is operational.");

        return true;
    }

    @Override
    public String getCommand() throws ConanParameterException {
        return this.getFullCommand();
    }

    @Override
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        try {
            log.info("Starting MASS");

            // Get shortcut to the args
            MassArgs args = (MassArgs) this.getProcessArgs();

            // Initialise executor
            this.massExecutor.initialise(this.conanProcessService, executionContext);

            List<Integer> jobIds = new ArrayList<>();

            for (SingleMassArgs singleMassArgs : args.getSingleMassArgsList()) {

                // Ensure output directory for this MASS run exists
                if (!singleMassArgs.getOutputDir().exists() && !singleMassArgs.getOutputDir().mkdirs()) {
                    throw new IOException("Couldn't create directory for MASS");
                }

                // Execute this MASS group
                this.massExecutor.executeSingleMass(singleMassArgs);

                // Collect job ids
                jobIds.addAll(this.massExecutor.getJobIds());

                // Check to see if we should run each MASS group in parallel, if not wait here until each MASS group has completed
                if (executionContext.usingScheduler() && !args.isRunParallel()) {
                    log.debug("Waiting for completion of: " + singleMassArgs.getName());
                    this.massExecutor.executeScheduledWait(
                            jobIds,
                            singleMassArgs.getJobPrefix() + "-analyser-*",
                            ExitStatus.Type.COMPLETED_ANY,
                            args.getJobPrefix() + "-wait",
                            singleMassArgs.getOutputDir());

                    jobIds.clear();
                }
            }

            // Wait for all assembly jobs to finish if they are running in parallel.
            if (executionContext.usingScheduler() && args.isRunParallel()) {
                log.debug("Single MASS jobs were executed in parallel, waiting for all to complete");
                this.massExecutor.executeScheduledWait(
                        jobIds,
                        args.getJobPrefix() + "-group*",
                        ExitStatus.Type.COMPLETED_ANY,
                        args.getJobPrefix() + "-wait",
                        args.getOutputDir());

                jobIds.clear();
            }

            log.info("MASS complete");

        } catch (IOException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        }

        return true;
    }


}
