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
package uk.ac.tgac.rampart.conan.tool.proc.internal.mass.multi;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.exec.context.ExecutionContext;
import uk.ac.tgac.rampart.conan.conanx.exec.context.scheduler.ExitStatusType;
import uk.ac.tgac.rampart.conan.conanx.exec.process.AbstractConanXProcess;
import uk.ac.tgac.rampart.conan.conanx.exec.process.ProcessExecutionService;
import uk.ac.tgac.rampart.conan.tool.proc.internal.mass.MassArgs;
import uk.ac.tgac.rampart.conan.tool.proc.internal.mass.single.SingleMassArgs;
import uk.ac.tgac.rampart.conan.tool.proc.internal.mass.single.SingleMassProcess;
import uk.ac.tgac.rampart.conan.tool.proc.internal.mass.stats.MassSelectorArgs;
import uk.ac.tgac.rampart.conan.tool.proc.internal.mass.stats.MassSelectorProcess;
import uk.ac.tgac.rampart.core.data.RampartConfiguration;

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
public class MultiMassProcess extends AbstractConanXProcess {

    @Autowired
    private ProcessExecutionService processExecutionService;

    public MultiMassProcess() {
        this(new MultiMassArgs());
    }

    public MultiMassProcess(MultiMassArgs args) {
        super("", args, new MultiMassParams());
    }

    @Override
    public String getName() {
        return "MultiMASS";
    }

    @Override
    public Collection<ConanParameter> getParameters() {
        return new MultiMassParams().getConanParameters();
    }

    @Override
    public String getCommand() {
        return this.getFullCommand();
    }

    @Override
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        try {
            MultiMassArgs args = (MultiMassArgs) this.getProcessArgs();

            List<File> statsFiles = new ArrayList<File>();

            List<SingleMassArgs> singleMassArgsList = createSingleMassArgsList(args);

            for (SingleMassArgs singleMassArgs : singleMassArgsList) {

                // Create output directory for this MASS run
                if (!singleMassArgs.getOutputDir().mkdirs()) {
                    throw new IOException("Couldn't create directory for MASS");
                }

                // Add the predicted stats file to the list for processing later.
                statsFiles.add(singleMassArgs.getStatsFile());

                // Execute the single MASS run
                this.processExecutionService.execute(new SingleMassProcess(singleMassArgs), executionContext);
            }

            // Wait for all assembly jobs to finish if they are running as background tasks.
            if (executionContext.usingScheduler() && !executionContext.isForegroundJob()) {

                this.processExecutionService.waitFor(
                        executionContext.getScheduler().createWaitCondition(
                                ExitStatusType.COMPLETED_SUCCESS,
                                args.getJobPrefix() + "*"),
                        executionContext);
            }

            // Execute the Mass Selector job
            executeMassSelector(args, statsFiles, executionContext);
        } catch (IOException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        }

        return true;
    }

    protected void executeMassSelector(MultiMassArgs args, List<File> statsFiles, ExecutionContext executionContext)
            throws IOException, ProcessExecutionException, InterruptedException {

        File statsDir = new File(args.getOutputDir(), "stats");
        if (!statsDir.mkdirs()) {
            throw new IOException("Couldn't create directory for MASS stats");
        }

        MassSelectorArgs massSelectorArgs = new MassSelectorArgs();
        massSelectorArgs.setStatsFiles(statsFiles);
        massSelectorArgs.setConfigs(args.getConfigs());
        massSelectorArgs.setOutputDir(statsDir);
        massSelectorArgs.setApproxGenomeSize(-1);
        massSelectorArgs.setWeightings(null);

        MassSelectorProcess massSelectorProcess = new MassSelectorProcess(massSelectorArgs);

        this.processExecutionService.execute(massSelectorProcess, executionContext);
    }

    private List<SingleMassArgs> createSingleMassArgsList(MultiMassArgs args) throws IOException {

        List<SingleMassArgs> singleMassArgsList = new ArrayList<SingleMassArgs>();

        List<RampartConfiguration> configs = RampartConfiguration.createList(args.getConfigs(), true);

        for (RampartConfiguration config : configs) {

            // Assume we're using args gathered from the config file for now
            SingleMassArgs singleMassArgs = SingleMassArgs.parseConfig(config.getFile());

            // Override args loaded from file if explicitly specified by the MultiMassArgs
            if (args.getAssembler() != null) {
                singleMassArgs.setAssembler(args.getAssembler());
            }

            if (args.getKmin() != MassArgs.DEFAULT_KMER_MIN) {
                singleMassArgs.setKmin(args.getKmin());
            }

            if (args.getKmax() != MassArgs.DEFAULT_KMER_MAX) {
                singleMassArgs.setKmax(args.getKmax());
            }

            if (args.getStepSize() != MassArgs.DEFAULT_STEP_SIZE) {
                singleMassArgs.setStepSize(args.getStepSize());
            }

            // These args are automatically set.
            singleMassArgs.setOutputDir(new File(args.getOutputDir(), config.getJob().getName()));
            singleMassArgs.setJobPrefix(args.getJobPrefix() + "-" + config.getJob().getName());
            singleMassArgs.setConfig(config.getFile());

            // Add to list
            singleMassArgsList.add(singleMassArgs);
        }

        return singleMassArgsList;
    }
}
