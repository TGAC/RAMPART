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
import uk.ac.tgac.rampart.tool.RampartConfiguration;
import uk.ac.tgac.rampart.tool.pipeline.RampartStage;
import uk.ac.tgac.rampart.tool.pipeline.amp.AmpProcess;
import uk.ac.tgac.rampart.tool.process.mass.MassProcess;
import uk.ac.tgac.rampart.tool.process.mecq.MecqProcess;

import java.io.IOException;
import java.util.ArrayList;
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




    public void configureProcesses() throws IOException {

        this.configureProcesses(this.args);
    }

    public void configureProcesses(RampartArgs args) throws IOException {

        // Capture the args
        this.args = args;

        // Create an object that maps expected RAMPART job directory structure based on specified output dir.
        // Probably processes an Xml configuration file to do this.
        RampartConfiguration config = new RampartConfiguration(this.args.getConfig(), this.args.getOutputDir(), this.args.getJobPrefix());

        // Shortcut to stages
        List<RampartStage> stages = this.args.getStages();

        // Configure pipeline
        if (stages.contains(RampartStage.MECQ) && config.getMecqSettings() != null) {
            this.processList.add(new MecqProcess(config.getMecqSettings()));
        }

        if (stages.contains(RampartStage.MASS) && config.getMassSettings() != null) {
            this.processList.add(new MassProcess(config.getMassSettings()));
        }

        if (stages.contains(RampartStage.AMP) && config.getAmpSettings() != null) {
            this.processList.add(new AmpProcess(config.getAmpSettings()));
        }


        // Spring autowiring might fail here to explicitly set conanProcessService for all processes
        for(AbstractConanProcess process : this.processList) {
            process.setConanProcessService(this.conanProcessService);
        }
    }

}
