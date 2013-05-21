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
package uk.ac.tgac.rampart.tool.pipeline.rampart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.tgac.rampart.RampartConfig;
import uk.ac.tgac.rampart.data.RampartJobFileStructure;
import uk.ac.tgac.rampart.tool.pipeline.RampartStage;
import uk.ac.tgac.rampart.tool.pipeline.amp.AmpArgs;
import uk.ac.tgac.rampart.tool.pipeline.amp.AmpProcess;
import uk.ac.tgac.rampart.tool.process.mass.multi.MultiMassArgs;
import uk.ac.tgac.rampart.tool.process.mass.multi.MultiMassProcess;
import uk.ac.tgac.rampart.tool.process.mecq.MecqArgs;
import uk.ac.tgac.rampart.tool.process.mecq.MecqProcess;

import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 07/01/13
 * Time: 10:48
 * To change this template use File | Settings | File Templates.
 */
@Component
public class RampartPipeline implements ConanPipeline {

    private List<AbstractConanProcess> processList;
    private RampartArgs args;

    @Autowired
    private ConanProcessService conanProcessService;

    public RampartPipeline() {
        this(new RampartArgs());
    }

    public RampartPipeline(RampartArgs args) {
        this.args = args;
        this.processList = new ArrayList<AbstractConanProcess>();
    }

    public RampartArgs getArgs() {
        return args;
    }

    public void setArgs(RampartArgs args) {
        this.args = args;
    }

    public ConanProcessService getConanProcessService() {
        return conanProcessService;
    }

    public void setConanProcessService(ConanProcessService conanProcessService) {
        this.conanProcessService = conanProcessService;
    }

    @Override
    public String getName() {
        return "RAMPART";
    }

    @Override
    public ConanUser getCreator() {
        return null;
    }

    @Override
    public boolean isPrivate() {
        return false;
    }

    @Override
    public boolean isDaemonized() {
        return false;
    }


    @Override
    public List<ConanProcess> getProcesses() {

        List<ConanProcess> conanProcessList = new ArrayList<ConanProcess>();

        for(AbstractConanProcess process : this.processList) {
            conanProcessList.add(process);
        }

        return conanProcessList;
    }

    @Override
    public List<ConanParameter> getAllRequiredParameters() {

        List<ConanParameter> params = new ArrayList<ConanParameter>();

        for(AbstractConanProcess process : this.processList) {
            params.addAll(process.getParameters());
        }

        return params;
    }


    protected String createJobPrefix() {
        Format formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateTime = formatter.format(new Date());
        String jobPrefix = "rampart-" + dateTime;

        return jobPrefix;
    }

    public void configureProcesses() throws IOException {

        this.configureProcesses(this.args);
    }

    public void configureProcesses(RampartArgs args) throws IOException {

        this.args = args;

        // Create an object that maps expected RAMPART job directory structure based on specified output dir.
        RampartJobFileStructure jobFS = new RampartJobFileStructure(this.args.getOutputDir());

        // Create job prefix
        String jobPrefix = createJobPrefix();

        // Create QT args
        MecqArgs mecqArgs = MecqArgs.parseConfig(this.args.getConfig());
        mecqArgs.setOutputDir(jobFS.getMeqcDir());
        mecqArgs.setJobPrefix(jobPrefix + "-mecq");
        mecqArgs.setCreateConfigs(true);
        mecqArgs.setRunParallel(true);

        // Create MASS args
        MultiMassArgs multiMassArgs = new MultiMassArgs();
        multiMassArgs.parseConfig(this.args.getConfig());
        multiMassArgs.setConfigDir(jobFS.getMeqcConfigDir());
        multiMassArgs.setWeightingsFile(new File(RampartConfig.DATA_DIR, "weightings.tab"));
        multiMassArgs.setOutputDir(jobFS.getMassDir());
        multiMassArgs.setJobPrefix(jobPrefix + "-mass");

        // Create AMP args (AMP stage is optional)
        AmpArgs ampArgs = AmpArgs.parseConfig(args.getConfig());
        if (ampArgs != null) {
            ampArgs.setInputAssembly(jobFS.getMassOutFile());
            ampArgs.setJobPrefix(jobPrefix + "-amp");
            ampArgs.setOutputDir(jobFS.getAmpDir());
        }


        // Shortcut to stages
        List<RampartStage> stages = this.args.getStages();

        // Configure pipeline
        if (stages.contains(RampartStage.MECQ)) {
            this.processList.add(new MecqProcess(mecqArgs));
        }

        if (stages.contains(RampartStage.MASS)) {
            this.processList.add(new MultiMassProcess(multiMassArgs));
        }

        if (stages.contains(RampartStage.AMP) && ampArgs != null) {
            this.processList.add(new AmpProcess(ampArgs));
        }


        // this.rampartPipeline.getAmpProcess().setProcessArgs(ampArgs);



        // Spring autowiring might fail here to explicitly set conanProcessService for all processes
        for(AbstractConanProcess process : this.processList) {
            process.setConanProcessService(this.conanProcessService);
        }
    }

}
