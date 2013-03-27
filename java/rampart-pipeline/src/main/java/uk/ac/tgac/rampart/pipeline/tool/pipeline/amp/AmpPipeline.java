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
package uk.ac.tgac.rampart.pipeline.tool.pipeline.amp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.process.AbstractAmpArgs;
import uk.ac.tgac.rampart.conan.process.AbstractAmpProcess;
import uk.ac.tgac.rampart.core.utils.StringJoiner;
import uk.ac.tgac.rampart.pipeline.tool.pipeline.RampartStage;
import uk.ac.tgac.rampart.pipeline.tool.process.analyser.length.LengthAnalysisArgs;
import uk.ac.tgac.rampart.pipeline.tool.process.analyser.length.LengthAnalysisProcess;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 07/01/13
 * Time: 10:55
 * To change this template use File | Settings | File Templates.
 */
@Component
public class AmpPipeline implements ConanPipeline {

    @Autowired
    private ConanProcessService conanProcessService;

    private List<ConanProcess> processList;
    private AmpParams params = new AmpParams();
    private AmpArgs args;

    public AmpPipeline() {
        this(new AmpArgs());
    }

    public AmpPipeline(AmpArgs ampArgs) {
        this.args = ampArgs;
        this.processList = new ArrayList<ConanProcess>();
    }

    public AmpArgs getArgs() {
        return args;
    }

    public void setArgs(AmpArgs args) {
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
        return "Assembly iMProver (AMP)";
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

        List<ConanProcess> cProcs = new ArrayList<ConanProcess>();

        for(ConanProcess ampProcess : this.processList) {
            cProcs.add(ampProcess);
        }

        return cProcs;
    }

    @Override
    public List<ConanParameter> getAllRequiredParameters() {
        return this.params.getConanParameters();
    }

    public void configureProcesses() {
        this.configureProcesses(this.args);
    }

    public void configureProcesses(AmpArgs args) {

        args.linkProcesses();

        for(AbstractAmpProcess proc : args.getProcesses()) {

            proc.setConanProcessService(this.conanProcessService);
            proc.initialise();
            this.processList.add(proc);
        }

        this.processList.add(createStatsJob());

    }

    private ConanProcess createStatsJob() {

        File assembliesDir = new File(args.getOutputDir(), "assemblies");

        LengthAnalysisArgs laArgs = new LengthAnalysisArgs();
        laArgs.setInputDir(assembliesDir);
        laArgs.setOutputDir(assembliesDir);
        laArgs.setRampartStage(RampartStage.AMP);

        LengthAnalysisProcess laProc = new LengthAnalysisProcess(laArgs);
        laProc.setConanProcessService(this.conanProcessService);

        return laProc;
    }

    protected String makeLinkCommand(File source, File target) {
        return "ln -s -f " + source.getAbsolutePath() + " " + target.getAbsolutePath();
    }

    public void createLinks(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        StringJoiner linkCmds = new StringJoiner(";");

        File scaffoldsDir = new File(args.getOutputDir(), "assemblies");

        scaffoldsDir.mkdirs();

        for(int i = 0; i < args.getProcesses().size(); i++) {

            AbstractAmpArgs ampArgs = args.getProcesses().get(i).getAmpArgs();

            linkCmds.add(makeLinkCommand(ampArgs.getOutputFile(), new File(scaffoldsDir, "AMP-" + i + ".fa")));
        }

        ExecutionContext linkingExecutionContext = new DefaultExecutionContext(executionContext.getLocality(), null, null, true);

        this.conanProcessService.execute(linkCmds.toString(), linkingExecutionContext);
    }
}
