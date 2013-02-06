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
package uk.ac.tgac.rampart.conan.tool.proc.qt;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.rampart.conan.conanx.exec.context.ExecutionContext;
import uk.ac.tgac.rampart.conan.conanx.exec.context.scheduler.ExitStatusType;
import uk.ac.tgac.rampart.conan.conanx.exec.context.scheduler.SchedulerArgs;
import uk.ac.tgac.rampart.conan.conanx.exec.task.TaskExecutionService;
import uk.ac.tgac.rampart.conan.tool.proc.RampartProcess;
import uk.ac.tgac.rampart.conan.tool.task.external.qt.QualityTrimmer;
import uk.ac.tgac.rampart.conan.tool.task.external.qt.QualityTrimmerFactory;
import uk.ac.tgac.rampart.core.data.Library;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 07/01/13
 * Time: 10:54
 * To change this template use File | Settings | File Templates.
 */
public class QTProcess implements ConanProcess, RampartProcess {

    @Autowired
    protected TaskExecutionService taskExecutionService;


    private QTArgs args;

    public QTProcess() {
        this(new QTArgs()) ;
    }

    public QTProcess(QTArgs args) {
        this.args = args;
    }

    @Override
    public void execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException, IOException, CommandExecutionException {

        // If necessary parse the rampart config file to set args
        if (    (this.args.getTool() == null || this.args.getTool().isEmpty()) &&
                (this.args.getConfig() != null && this.args.getConfig().exists())) {

            this.args = QTArgs.parseConfig(this.args.getConfig());
        }


        String qtType = this.args.getTool();
        List<Library> libs = this.args.getLibs();


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

        List<QualityTrimmer> qtList = this.createQualityTrimmers(this.args);


        int i = 1;
        for(QualityTrimmer qt : qtList) {

            if (executionContext.usingScheduler()) {
                executionContext.getScheduler().getArgs().setJobName(jobPrefix + "_" + i++);
            }

            this.taskExecutionService.execute(qt, executionContext);
        }

        if (executionContext.usingScheduler()) {

            this.taskExecutionService.waitFor(
                    executionContext.getScheduler().createWaitCondition(ExitStatusType.COMPLETED_SUCCESS, jobPrefix + "*"),
                    executionContext);
        }
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
        return "QT";
    }

    @Override
    public Collection<ConanParameter> getParameters() {
        return new QTParams().getConanParameters();
    }

    // ***** Construction methods *****

    public List<QualityTrimmer> createQualityTrimmers(QTArgs args) {

        List<QualityTrimmer> qtList = new ArrayList<QualityTrimmer>();

        for(Library lib : args.getLibs()) {

            if (lib.testUsage(Library.Usage.QUALITY_TRIMMING)) {

                QualityTrimmer qt = QualityTrimmerFactory.create(this.args.getTool(), lib, this.args.getOutputDir());

                qt.getArgs().setMinLength(this.args.getMinLen());
                qt.getArgs().setQualityThreshold(this.args.getMinQual());

                qtList.add(qt);
            }
        }

        return qtList;
    }

}
