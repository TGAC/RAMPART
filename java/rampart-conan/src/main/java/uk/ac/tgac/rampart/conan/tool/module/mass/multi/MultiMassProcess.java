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
package uk.ac.tgac.rampart.conan.tool.module.mass.multi;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.rampart.conan.conanx.env.DefaultEnvironment;
import uk.ac.tgac.rampart.conan.conanx.env.Environment;
import uk.ac.tgac.rampart.conan.tool.module.RampartProcess;
import uk.ac.tgac.rampart.conan.tool.module.mass.single.SingleMassArgs;
import uk.ac.tgac.rampart.conan.tool.module.mass.single.SingleMassProcess;
import uk.ac.tgac.rampart.conan.tool.module.mass.stats.MassSelector;
import uk.ac.tgac.rampart.conan.tool.module.util.RampartConfig;

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

        Environment env = new DefaultEnvironment();

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
    public void execute(Environment env) throws IOException, ProcessExecutionException, InterruptedException, CommandExecutionException {

        List<File> statsFiles = new ArrayList<File>();

        // Create directories to each MASS run.
        for(RampartConfig config : this.args.getConfigs()) {

            File singleMassOutputDir = new File(this.args.getOutputDir(), config.getName());
            if (!singleMassOutputDir.mkdirs()) {
                throw new IOException("Couldn't create directory for MASS");
            }

            SingleMassArgs singleMassArgs = new SingleMassArgs();
            singleMassArgs.setAssembler(this.args.getAssembler());
            singleMassArgs.setKmin(this.args.getKmin());
            singleMassArgs.setKmax(this.args.getKmax());
            singleMassArgs.setStepSize(this.args.getStepSize());
            singleMassArgs.setOutputDir(singleMassOutputDir);
            singleMassArgs.setJobPrefix(this.args.getJobPrefix() + "-" + config.getName());
            singleMassArgs.setConfig(config);

            // Add the predicted stats file to the list for processing later.
            statsFiles.add(singleMassArgs.getStatsFile());

            // Create the single mass process
            SingleMassProcess singleMassProcess = new SingleMassProcess(singleMassArgs);

            //TODO do we need to modify the environment here?
            // Execute ALL MASS jobs run in background
            singleMassProcess.execute(env);
        }

        // Wait for all MASS jobs to complete
        //TODO execute a wait command here if single mass was run as a background job

        File statsDir = new File(this.args.getOutputDir(), "stats");
        if (!statsDir.mkdirs()) {
            throw new IOException("Couldn't create directory for MASS stats");
        }

        // Do stats
        MassSelector ms = new MassSelector(statsFiles, this.args.getConfigs(), statsDir, -1, null);
        ms.execute(env);

        // Make links to best files



        /*# Put the best assembly and config in a known location
        mkdir $job_fs->getMassBestDir();

        my @gb_args = grep {$_} (
                $GETBEST_PATH,
                "--best_assembly_in " . $job_fs->getBestPathFile(),
                "--best_dataset_in " . $job_fs->getBestDatasetFile(),
                "--raw_config " . $job_fs->getRawConfigFile(),
                "--qt_config " . $job_fs->getQtConfigFile(),
                "--best_assembly_out " . $job_fs->getBestAssemblyFile(),
                "--best_config_out " . $job_fs->getBestConfigFile(),
                $qst->isVerboseAsParam()
        );

        my $get_best_job = new QsOptions();
        $get_best_job->setGridEngine($qst->getGridEngine());
        $get_best_job->setProjectName($qst->getProjectName());
        $get_best_job->setJobName($get_best_job_name);
        $get_best_job->setQueue($qst->getQueue());
        $get_best_job->setWaitCondition("ended(" . $ms_job_name . ")");
        $get_best_job->setVerbose($qst->isVerbose());
        SubmitJob::submit($get_best_job, join " ", @gb_args);  */
    }
}
