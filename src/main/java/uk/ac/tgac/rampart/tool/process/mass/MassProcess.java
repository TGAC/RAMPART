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
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.tool.process.mass.selector.MassSelectorArgs;
import uk.ac.tgac.rampart.tool.process.mass.selector.stats.AssemblyStats;
import uk.ac.tgac.rampart.tool.process.mass.selector.stats.AssemblyStatsTable;
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

            // Only run single mass groups if we're not in stats only mode.
            if (!args.isStatsOnly()) {

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


                log.info("Assemblies complete");
            }

            log.info("Comparing assemblies");

            // Compile results
            File statsDir = new File(args.getOutputDir(), "stats");

            AssemblyStatsTable results = this.compileMassResults(args);

            // Run MASS selector
            this.executeMassSelector(args, statsDir, results);

            log.info("Compared all assemblies");

            log.info("Predicted best assembly can be accessed from: " + new File(statsDir, "best.fa").getAbsolutePath());

            log.info("Multi MASS run complete");

        } catch (IOException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        }

        return true;
    }

    protected AssemblyStatsTable compileMassResults(MassArgs args) throws IOException {

        log.debug("Compiling results from each MASS group");

        AssemblyStatsTable merged = new AssemblyStatsTable();

        for (SingleMassArgs singleMassArgs : args.getSingleMassArgsList()) {

            AssemblyStatsTable singleMassResults = this.massExecutor.compileSingleMassResults(singleMassArgs);

            if (singleMassResults != null) {
                for(AssemblyStats stats : singleMassResults) {

                    merged.add(stats);
                }
            }
        }

        log.debug("Compiled " + merged.size() + " entries from " + args.getSingleMassArgsList().size() + " MASS groups");

        return merged;
    }

    protected void executeMassSelector(MassArgs args, File outputDir, AssemblyStatsTable results)
            throws IOException, ProcessExecutionException, InterruptedException {

        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                throw new IOException("Couldn't create directory for MASS analyser: " + outputDir.getAbsolutePath());
            }
        }

        MassSelectorArgs massSelectorArgs = new MassSelectorArgs();
        //massSelectorArgs.setStatsFiles(statsFiles);
        massSelectorArgs.setMergedTable(results);
        massSelectorArgs.setOutputDir(outputDir);
        massSelectorArgs.setOrganism(args.getOrganism());
        massSelectorArgs.setWeightings(args.getWeightings());

        this.massExecutor.executeMassSelector(massSelectorArgs);
    }

}
