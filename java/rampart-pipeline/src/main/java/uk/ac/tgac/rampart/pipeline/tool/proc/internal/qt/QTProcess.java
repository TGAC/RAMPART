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
package uk.ac.tgac.rampart.pipeline.tool.proc.internal.qt;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.model.context.SchedulerArgs;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.core.data.Library;
import uk.ac.tgac.rampart.core.data.RampartConfiguration;
import uk.ac.tgac.rampart.pipeline.tool.proc.external.qt.QualityTrimmer;

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
public class QTProcess extends AbstractConanProcess {

    @Autowired
    protected ConanProcessService conanProcessService;

    public QTProcess() {
        this(new QTArgs());
    }

    public QTProcess(QTArgs args) {
        super("", args, new QTParams());
    }

    @Override
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        try {

            QTArgs args = (QTArgs) this.getProcessArgs();

            // If necessary parse the rampart config file to set args
            if ((args.getTool() == null || args.getTool().isEmpty()) &&
                    (args.getConfig() != null && args.getConfig().exists())) {

                args = QTArgs.parseConfig(args.getConfig());
            }


            String qtType = args.getTool();
            List<Library> libs = args.getLibs();


            SchedulerArgs backupArgs = null;
            SchedulerArgs copyArgs = null;
            String jobPrefix = "";

            if (executionContext.usingScheduler()) {

                backupArgs = executionContext.getScheduler().getArgs();
                jobPrefix = backupArgs.getJobName();
                copyArgs = executionContext.getScheduler().getArgs().copy();
                copyArgs.setBackgroundTask(true);
                executionContext.getScheduler().setArgs(copyArgs);
            }

            List<QualityTrimmer> qtList = args.createQualityTrimmers();


            int i = 1;
            for (QualityTrimmer qt : qtList) {

                if (executionContext.usingScheduler()) {
                    executionContext.getScheduler().getArgs().setJobName(jobPrefix + "_" + i++);
                }

                this.conanProcessService.execute(qt, executionContext);
            }

            if (executionContext.usingScheduler()) {

                this.conanProcessService.waitFor(
                        executionContext.getScheduler().createWaitCondition(ExitStatus.Type.COMPLETED_SUCCESS, jobPrefix + "*"),
                        executionContext);
            }

            createConfigs(qtList, args);
        } catch (IOException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        }

        return true;
    }

    private void createConfigs(List<QualityTrimmer> qtList, QTArgs args) throws IOException {

        RampartConfiguration rawConfig = null;
        RampartConfiguration qtConfig = null;

        // Create the basics of the configuration object, preferably using the original config file if present
        if (args.getConfig() != null && args.getConfig().exists()) {
            rawConfig = RampartConfiguration.loadFile(args.getConfig());
            qtConfig = RampartConfiguration.loadFile(args.getConfig());
        } else {
            rawConfig = args.createRampartConfiguration();
            qtConfig = args.createRampartConfiguration();
        }

        // Modify the dataset names
        rawConfig.getJob().setName("RAW");
        qtConfig.getJob().setName("QT");

        // TODO Modify the files


        // Save configs to disk
        File rawConfigFile = new File(args.getOutputDir(), "raw.cfg");
        File qtConfigFile = new File(args.getOutputDir(), "qt.cfg");

        rawConfig.save(rawConfigFile);
        qtConfig.save(qtConfigFile);
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
