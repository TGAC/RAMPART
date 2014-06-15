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

import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uk.ac.ebi.fgpt.conan.core.param.DefaultParamMap;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.util.StringJoiner;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.util.AbstractXmlJobConfiguration;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.RampartJobFileSystem;
import uk.ac.tgac.rampart.tool.pipeline.amp.Amp;
import uk.ac.tgac.rampart.tool.process.Finalise;
import uk.ac.tgac.rampart.tool.process.Mecq;
import uk.ac.tgac.rampart.tool.process.analyse.asm.AnalyseAmpAssemblies;
import uk.ac.tgac.rampart.tool.process.analyse.asm.AnalyseMassAssemblies;
import uk.ac.tgac.rampart.tool.process.analyse.reads.AnalyseReads;
import uk.ac.tgac.rampart.tool.process.mass.Mass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: maplesod
 * Date: 07/02/13
 * Time: 19:26
 */
public class RampartArgs extends AbstractXmlJobConfiguration implements ProcessArgs {

    public static final String KEY_ELEM_LIBRARIES       = "libraries";
    public static final String KEY_ELEM_LIBRARY         = "library";
    public static final String KEY_ELEM_PIPELINE        = "pipeline";
    public static final String KEY_ELEM_MECQ            = "mecq";
    public static final String KEY_ELEM_ANALYSE_READS   = "analyse_reads";
    public static final String KEY_ELEM_MASS            = "mass";
    public static final String KEY_ELEM_ANALYSE_ASMS    = "analyse_asms";
    public static final String KEY_ELEM_AMP             = "amp";
    public static final String KEY_ELEM_FINALISE        = "finalise";


    private RampartParams params = new RampartParams();

    private RampartStageList stages;
    private List<Library> libs;
    private Mecq.Args mecqArgs;
    private AnalyseReads.Args analyseReadsArgs;
    private Mass.Args massArgs;
    private AnalyseMassAssemblies.Args analyseMassArgs;
    private File ampInput;
    private Amp.Args ampArgs;
    private AnalyseAmpAssemblies.Args analyseAmpArgs;
    private Finalise.Args finaliseArgs;
    private RampartJobFileSystem rampartJobFileSystem;
    private ExecutionContext executionContext;



    public RampartArgs(File configFile, File outputDir, String jobPrefix, RampartStageList stages, File ampInput)
            throws IOException {

        super(configFile, outputDir, jobPrefix);

        this.stages = stages;
        this.libs = new ArrayList<>();
        this.mecqArgs = null;
        this.analyseReadsArgs = null;
        this.massArgs = null;
        this.analyseMassArgs = null;
        this.ampInput = ampInput;
        this.ampArgs = null;
        this.analyseAmpArgs = null;
        this.finaliseArgs = null;
        this.rampartJobFileSystem = new RampartJobFileSystem(outputDir.getAbsoluteFile());
    }



    @Override
    protected void internalParseXml(Element element) throws IOException {

        // All libraries
        Element librariesElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_LIBRARIES);
        NodeList libraries = librariesElement.getElementsByTagName(KEY_ELEM_LIBRARY);
        this.libs = new ArrayList<>();
        for(int i = 0; i < libraries.getLength(); i++) {
            this.libs.add(new Library((Element)libraries.item(i), this.getOutputDir().getAbsoluteFile()));
        }

        Element pipelineElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_PIPELINE);

        // MECQ
        Element mecqElement = XmlHelper.getDistinctElementByName(pipelineElement, KEY_ELEM_MECQ);
        this.mecqArgs = mecqElement == null ? null :
                new Mecq.Args(
                        mecqElement,
                        this.rampartJobFileSystem.getMeqcDir(),
                        this.getJobPrefix() + "-mecq",
                        this.libs);

        this.stages.setArgsIfPresent(RampartStage.MECQ, this.mecqArgs);


        // Analyse reads
        Element analyseReadsElement = XmlHelper.getDistinctElementByName(pipelineElement, KEY_ELEM_ANALYSE_READS);
        this.analyseReadsArgs = analyseReadsElement == null ? null :
                new AnalyseReads.Args(
                        analyseReadsElement,
                        this.libs,
                        this.mecqArgs == null ? new ArrayList<Mecq.EcqArgs>() : this.mecqArgs.getEqcArgList(),
                        this.getJobPrefix() + "-analyse_reads",
                        this.rampartJobFileSystem.getMeqcDir(),
                        this.rampartJobFileSystem.getAnalyseReadsDir(),
                        this.getOrganism());

        this.stages.setArgsIfPresent(RampartStage.ANALYSE_READS, this.analyseReadsArgs);

        // MASS
        Element massElement = XmlHelper.getDistinctElementByName(pipelineElement, KEY_ELEM_MASS);
        this.massArgs = massElement == null ? null :
                new Mass.Args(
                        massElement,
                        this.rampartJobFileSystem.getMassDir(),
                        this.rampartJobFileSystem.getMeqcDir(),
                        this.getJobPrefix() + "-mass",
                        this.libs,
                        this.mecqArgs == null ? new ArrayList<Mecq.EcqArgs>() : this.mecqArgs.getEqcArgList(),
                        this.getOrganism());

        this.stages.setArgsIfPresent(RampartStage.MASS, this.massArgs);

        // Analyse assemblies
        Element analyseMassElement = XmlHelper.getDistinctElementByName(pipelineElement, KEY_ELEM_ANALYSE_ASMS);
        this.analyseMassArgs = analyseMassElement == null ? null :
                new AnalyseMassAssemblies.Args(
                        analyseMassElement,
                        this.rampartJobFileSystem.getMassDir(),
                        this.analyseReadsArgs != null ? this.rampartJobFileSystem.getAnalyseReadsDir() : null,
                        this.rampartJobFileSystem.getAnalyseMassDir(),
                        this.massArgs == null ? null : this.massArgs.getSingleMassArgsList(),
                        this.getOrganism(),
                        this.getJobPrefix() + "-analyse_mass");

        this.stages.setArgsIfPresent(RampartStage.ANALYSE_MASS, this.analyseMassArgs);

        // AMP
        Element ampElement = XmlHelper.getDistinctElementByName(pipelineElement, KEY_ELEM_AMP);
        this.ampArgs = ampElement == null ? null :
                new Amp.Args(
                        ampElement,
                        this.rampartJobFileSystem.getAmpDir(),
                        this.getJobPrefix() + "-amp",
                        ampInput != null ? ampInput : this.rampartJobFileSystem.getSelectedAssemblyFile(),
                        this.libs,
                        this.mecqArgs == null ? new ArrayList<Mecq.EcqArgs>() : this.mecqArgs.getEqcArgList(),
                        this.getOrganism());

        this.stages.setArgsIfPresent(RampartStage.AMP, this.ampArgs);

        // AMP
        Element analyseAmpElement = XmlHelper.getDistinctElementByName(pipelineElement, KEY_ELEM_AMP);
        this.analyseAmpArgs = analyseAmpElement == null ? null :
                new AnalyseAmpAssemblies.Args(
                        analyseAmpElement,
                        this.analyseReadsArgs != null ? this.rampartJobFileSystem.getAnalyseReadsDir() : null,
                        this.rampartJobFileSystem.getAnalyseAmpDir(),
                        this.ampArgs == null ? null : this.ampArgs.getStageArgsList(),
                        this.getOrganism(),
                        this.getJobPrefix() + "-analyse_amp");

        this.stages.setArgsIfPresent(RampartStage.ANALYSE_AMP, this.analyseAmpArgs);


        File finalAssembly = this.ampArgs == null ? this.rampartJobFileSystem.getSelectedAssemblyFile(): this.ampArgs.getFinalAssembly();

        Element finaliseElement = XmlHelper.getDistinctElementByName(pipelineElement, KEY_ELEM_FINALISE);
        this.finaliseArgs = finaliseElement == null ? null :
                new Finalise.Args(
                        finaliseElement,
                        finalAssembly,
                        this.rampartJobFileSystem.getFinalDir(),
                        this.getOrganism(),
                        this.getInstitution());

        this.stages.setArgsIfPresent(RampartStage.FINALISE, this.finaliseArgs);
    }

    public RampartStageList getStages() {
        return stages;
    }

    public void setStages(RampartStageList stages) {
        this.stages = stages;
    }

    @Override
    public void parse(String args) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ParamMap getArgMap() {

        ParamMap pvp = new DefaultParamMap();

        if (this.getConfigFile() != null) {
            pvp.put(params.getConfig(), this.getConfigFile().getAbsolutePath());
        }

        if (this.getOutputDir() != null) {
            pvp.put(params.getOutputDir(), this.getOutputDir().getAbsolutePath());
        }

        if (this.stages != null && this.stages.size() > 0) {
            pvp.put(params.getStageList(), this.stages.toString());
        }

        return pvp;
    }

    @Override
    public void setFromArgMap(ParamMap pvp) throws IOException, ConanParameterException {
        for (Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            }

            ConanParameter param = entry.getKey();

            if (param.equals(this.params.getConfig())) {
                this.setConfigFile(new File(entry.getValue()));
            }
            else if (param.equals(this.params.getOutputDir())) {
                this.setOutputDir(new File(entry.getValue()));
            }
            else if (param.equals(this.params.getStageList())) {
                this.stages = RampartStageList.parse(entry.getValue());
            }
        }

        this.rampartJobFileSystem = new RampartJobFileSystem(this.getOutputDir());

        this.parseXml();
    }

    @Override
    public String toString() {

        StringJoiner sj = new StringJoiner("\n");
        sj.add("Job Configuration File: " + this.getConfigFile().getAbsolutePath());
        sj.add("Output Directory: " + this.getOutputDir().getAbsolutePath());
        sj.add("Job Prefix: " + this.getJobPrefix());
        sj.add("Stages: " + ArrayUtils.toString(stages));

        return sj.toString();
    }

    public Amp.Args getAmpArgs() {
        return ampArgs;
    }

    public void setAmpArgs(Amp.Args ampArgs) {
        this.ampArgs = ampArgs;
    }

    public Mecq.Args getMecqArgs() {
        return mecqArgs;
    }

    public void setMecqArgs(Mecq.Args mecqArgs) {
        this.mecqArgs = mecqArgs;
    }

    public AnalyseReads.Args getAnalyseReadsArgs() {
        return analyseReadsArgs;
    }

    public void setAnalyseReadsArgs(AnalyseReads.Args analyseReadsArgs) {
        this.analyseReadsArgs = analyseReadsArgs;
    }

    public Mass.Args getMassArgs() {
        return massArgs;
    }

    public void setMassArgs(Mass.Args massArgs) {
        this.massArgs = massArgs;
    }

    public AnalyseMassAssemblies.Args getAnalyseMassArgs() {
        return analyseMassArgs;
    }

    public void setAnalyseMassArgs(AnalyseMassAssemblies.Args analyseMassArgs) {
        this.analyseMassArgs = analyseMassArgs;
    }

    public AnalyseAmpAssemblies.Args getAnalyseAmpArgs() {
        return analyseAmpArgs;
    }

    public void setAnalyseAmpArgs(AnalyseAmpAssemblies.Args analyseAmpArgs) {
        this.analyseAmpArgs = analyseAmpArgs;
    }

    public Finalise.Args getFinaliseArgs() {
        return finaliseArgs;
    }

    public void setFinaliseArgs(Finalise.Args finaliseArgs) {
        this.finaliseArgs = finaliseArgs;
    }

    public List<Library> getLibs() {
        return libs;
    }

    public void setLibs(List<Library> libs) {
        this.libs = libs;
    }

    public List<ConanProcess> getRequestedTools() {
        return this.stages.getExternalTools();
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }
}
