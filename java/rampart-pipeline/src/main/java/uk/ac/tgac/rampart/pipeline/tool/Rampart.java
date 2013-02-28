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
package uk.ac.tgac.rampart.pipeline.tool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.fgpt.conan.core.user.GuestUser;
import uk.ac.ebi.fgpt.conan.factory.ConanTaskFactory;
import uk.ac.ebi.fgpt.conan.factory.DefaultTaskFactory;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExternalProcessConfiguration;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.service.exception.TaskExecutionException;
import uk.ac.tgac.rampart.core.data.RampartJobFileStructure;
import uk.ac.tgac.rampart.pipeline.cli.RampartOptions;
import uk.ac.tgac.rampart.pipeline.tool.pipeline.rampart.RampartArgs;
import uk.ac.tgac.rampart.pipeline.tool.pipeline.rampart.RampartPipeline;
import uk.ac.tgac.rampart.pipeline.tool.proc.mass.multi.MultiMassArgs;
import uk.ac.tgac.rampart.pipeline.tool.proc.qt.QTArgs;
import uk.ac.tgac.rampart.pipeline.tool.proc.util.clean.CleanJobArgs;
import uk.ac.tgac.rampart.pipeline.tool.proc.util.clean.CleanJobProcess;

import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * User: maplesod
 * Date: 12/02/13
 * Time: 14:55
 */
@Component
public class Rampart {

    @Autowired
    private ConanProcessService conanProcessService;

    @Autowired
    private RampartPipeline rampartPipeline;



    private RampartOptions options;
    private ExecutionContext executionContext;
    private ExternalProcessConfiguration externalProcessConfiguration;

    public Rampart() {
        this(null);
    }



    public Rampart(RampartOptions options) {
        this.options = options;
    }

    public RampartOptions getOptions() {
        return this.options;
    }

    public void setOptions(RampartOptions options) {
        this.options = options;
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public ExternalProcessConfiguration getExternalProcessConfiguration() {
        return externalProcessConfiguration;
    }

    public void setExternalProcessConfiguration(ExternalProcessConfiguration externalProcessConfiguration) {
        this.externalProcessConfiguration = externalProcessConfiguration;
    }


    /**
     * TBH this probably shouldn't be something that we want Conan to control.  Probably should extract this for quick
     * execution
     * @throws ProcessExecutionException
     * @throws InterruptedException
     */
    protected void cleanJob() throws ProcessExecutionException, InterruptedException {

        CleanJobArgs cleanJobArgs = new CleanJobArgs();
        cleanJobArgs.setJobDir(this.options.getClean());

        CleanJobProcess cleanJobProcess = new CleanJobProcess(cleanJobArgs);

        this.conanProcessService.execute(cleanJobProcess, this.executionContext);
    }

    protected String createJobPrefix() {
        Format formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateTime = formatter.format(new Date());
        String jobPrefix = "rampart-" + dateTime;

        return jobPrefix;
    }

    protected void startJob() throws InterruptedException, TaskExecutionException, IOException {

        // Configure RAMPART args based on CLI input.
        RampartArgs args = new RampartArgs();
        args.setConfig(this.options.getConfig());
        args.setOutputDir(this.options.getOutput());

        // Ensure the output directory exists
        if (!args.getOutputDir().exists()) {
            args.getOutputDir().mkdirs();
        }

        // Create an object that maps expected RAMPART job directory structure based on specified output dir.
        RampartJobFileStructure jobFS = new RampartJobFileStructure(this.options.getOutput());

        // Create job prefix
        String jobPrefix = createJobPrefix();

        // Create QT args
        QTArgs qtArgs = QTArgs.parseConfig(this.options.getConfig());
        qtArgs.setOutputDir(jobFS.getReadsDir());
        qtArgs.setJobPrefix(jobPrefix + "-qt");
        qtArgs.setCreateConfigs(true);
        qtArgs.setRunParallel(true);

        // Create MASS args
        MultiMassArgs multiMassArgs = new MultiMassArgs();
        multiMassArgs.setConfigs(new ArrayList<File>(Arrays.asList(
                new File[]{
                        jobFS.getConfigRawFile(),
                        jobFS.getConfigQtFile()
                })));
        multiMassArgs.setOutputDir(jobFS.getMassDir());
        multiMassArgs.setJobPrefix(jobPrefix + "-mass");
        multiMassArgs.setRunParallel(true);

        // Create AMP args
        // TODO Amp args

        // Configure pipeline
        this.rampartPipeline.setStages(this.options.getStages());
        this.rampartPipeline.getQtProcess().setProcessArgs(qtArgs);
        this.rampartPipeline.getMultiMassProcess().setProcessArgs(multiMassArgs);
        //this.rampartPipeline.getAmpPipeline().se

        // Create a guest user
        ConanUser rampartUser = new GuestUser("daniel.mapleson@tgac.ac.uk");

        // Create the RAMPART proc
        ConanTaskFactory conanTaskFactory = new DefaultTaskFactory();
        ConanTask<RampartPipeline> rampartTask = conanTaskFactory.createTask(
                this.rampartPipeline,
                0,
                args.getArgMap(),
                ConanTask.Priority.HIGHEST,
                rampartUser);
        rampartTask.setId("");
        rampartTask.submit();
        rampartTask.execute(this.executionContext);
    }

    public void process() throws ProcessExecutionException, InterruptedException, TaskExecutionException, IOException {

        if (this.options.getClean() != null) {
            cleanJob();
        }
        else {
            startJob();
        }
    }


}
