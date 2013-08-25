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
import org.springframework.stereotype.Component;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.process.asm.Assembler;
import uk.ac.tgac.conan.process.asm.AssemblerFactory;
import uk.ac.tgac.rampart.tool.process.mass.selector.MassSelectorArgs;
import uk.ac.tgac.rampart.tool.process.mass.single.SingleMassArgs;

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
@Component
public class MassProcess extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(MassProcess.class);

    private MassExecutor massExecutor;

    public MassProcess() {
        this(new MassArgs());
    }

    public MassProcess(MassArgs args) {
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
    public String getCommand() {
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


            List<File> contigStatsFiles = new ArrayList<File>();
            List<File> scaffoldStatsFiles = new ArrayList<File>();

            for (SingleMassArgs singleMassArgs : args.getSingleMassArgsList()) {

                Assembler assembler = AssemblerFactory.createAssembler(singleMassArgs.getTool());

                if (assembler.makesUnitigs()) {
                    contigStatsFiles.add(new File(singleMassArgs.getUnitigsDir(), "stats.txt"));
                }

                if (assembler.makesContigs()) {
                    contigStatsFiles.add(new File(singleMassArgs.getContigsDir(), "stats.txt"));
                }

                if (assembler.makesScaffolds()) {
                    scaffoldStatsFiles.add(new File(singleMassArgs.getScaffoldsDir(), "stats.txt"));
                }

                // Ensure output directory for this MASS run exists
                if (!singleMassArgs.getOutputDir().exists() && !singleMassArgs.getOutputDir().mkdirs()) {
                    throw new IOException("Couldn't create directory for MASS");
                }

                // Execute this MASS group
                this.massExecutor.executeSingleMass(singleMassArgs);

                // Check to see if we should run each MASS group in parallel, if not wait here until each MASS group has completed
                if (!args.isRunParallel()) {
                    log.debug("Waiting for completion of: " + singleMassArgs.getName());
                    this.massExecutor.executeScheduledWait(singleMassArgs.getJobPrefix(), singleMassArgs.getOutputDir());
                }
            }

            // Wait for all assembly jobs to finish if they are running in parallel.
            if (args.isRunParallel()) {
                log.debug("Single MASS jobs were executed in parallel, waiting for all to complete");
                this.massExecutor.executeScheduledWait(args.getJobPrefix(), args.getOutputDir());
            }


            // Compile Single MASS results

            // Run MASS selector

            // Decide on best assembly


            /*log.info("Assemblies complete");

            // Execute the Mass Selector job
            log.info("Analysing and comparing assemblies");

            File statsDir = new File(args.getOutputDir(), "stats");

            if (assembler.makesUnitigs()) {
                executeMassSelector(args, new File(statsDir, "unitigs"), unitigStatsFiles, executionContext);
            }

            if (assembler.makesContigs()) {
                executeMassSelector(args, new File(statsDir, "contigs"), contigStatsFiles, executionContext);
            }

            if (assembler.makesScaffolds()) {
                executeMassSelector(args, new File(statsDir, "scaffolds"), scaffoldStatsFiles, executionContext);
            }                                   */

            log.info("Multi MASS run complete");

        } catch (IOException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        }

        return true;
    }

    protected void executeMassSelector(MassArgs args, File outputDir, List<File> statsFiles, ExecutionContext executionContext)
            throws IOException, ProcessExecutionException, InterruptedException {

        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                throw new IOException("Couldn't create directory for MASS analyser: " + outputDir.getAbsolutePath());
            }
        }

        MassSelectorArgs massSelectorArgs = new MassSelectorArgs();
        massSelectorArgs.setStatsFiles(statsFiles);
        massSelectorArgs.setOutputDir(outputDir);
        massSelectorArgs.setApproxGenomeSize(0L);
        massSelectorArgs.setWeightings(args.getWeightings());

        this.massExecutor.executeMassSelector(massSelectorArgs);
    }

}
