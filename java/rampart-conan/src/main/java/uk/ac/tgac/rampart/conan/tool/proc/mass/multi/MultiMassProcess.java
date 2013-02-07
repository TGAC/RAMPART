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
package uk.ac.tgac.rampart.conan.tool.proc.mass.multi;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.rampart.conan.conanx.exec.context.ExecutionContext;
import uk.ac.tgac.rampart.conan.conanx.exec.context.scheduler.ExitStatusType;
import uk.ac.tgac.rampart.conan.conanx.exec.process.ProcessExecutionService;
import uk.ac.tgac.rampart.conan.conanx.exec.task.TaskExecutionService;
import uk.ac.tgac.rampart.conan.tool.proc.RampartProcess;
import uk.ac.tgac.rampart.conan.tool.proc.mass.MassArgs;
import uk.ac.tgac.rampart.conan.tool.proc.mass.single.SingleMassArgs;
import uk.ac.tgac.rampart.conan.tool.proc.mass.single.SingleMassProcess;
import uk.ac.tgac.rampart.conan.tool.proc.mass.stats.MassSelector;
import uk.ac.tgac.rampart.core.data.RampartConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * User: maplesod
 * Date: 01/02/13
 * Time: 11:10
 */
public class MultiMassProcess  implements ConanProcess, RampartProcess {

    @Autowired
    private TaskExecutionService taskExecutionService;

    @Autowired
    private ProcessExecutionService processExecutionService;


    private MultiMassArgs args;

    public MultiMassProcess() {
        this(new MultiMassArgs());
    }

    public MultiMassProcess(MultiMassArgs args) {
        this.args = args;
    }

    @Override
    public boolean execute(Map<ConanParameter, String> parameters) throws ProcessExecutionException, IllegalArgumentException, InterruptedException {

        this.args.setFromArgMap(parameters);

        ExecutionContext env = new ExecutionContext();

        try {
            this.execute(env);
        } catch (IOException e) {
            throw new ProcessExecutionException(-1, e);
        } catch (CommandExecutionException e) {
            throw new ProcessExecutionException(-1, e);
        }

        return true;
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
    public void execute(ExecutionContext executionContext) throws IOException, ProcessExecutionException, InterruptedException, CommandExecutionException {

        List<File> statsFiles = new ArrayList<File>();

        List<SingleMassArgs> singleMassArgsList = createSingleMassArgsList(this.args);

        for(SingleMassArgs singleMassArgs : singleMassArgsList) {

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

            this.taskExecutionService.waitFor(
                    executionContext.getScheduler().createWaitCondition(
                            ExitStatusType.COMPLETED_SUCCESS,
                            this.args.getJobPrefix() + "*"),
                    executionContext);
        }

        File statsDir = new File(this.args.getOutputDir(), "stats");
        if (!statsDir.mkdirs()) {
            throw new IOException("Couldn't create directory for MASS stats");
        }

        // Execute the Mass Selector job
        this.processExecutionService.execute(new MassSelector(statsFiles, this.args.getConfigs(), statsDir, -1, null), executionContext);
    }

    private List<SingleMassArgs> createSingleMassArgsList(MultiMassArgs args) throws IOException {

        List<SingleMassArgs> singleMassArgsList = new ArrayList<SingleMassArgs>();

        List<RampartConfiguration> configs = RampartConfiguration.createList(this.args.getConfigs(), true);

        for(RampartConfiguration config : configs) {

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
