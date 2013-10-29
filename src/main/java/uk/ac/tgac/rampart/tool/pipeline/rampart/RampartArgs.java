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
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.ebi.fgpt.conan.util.StringJoiner;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.util.AbstractXmlJobConfiguration;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.RampartJobFileSystem;
import uk.ac.tgac.rampart.tool.pipeline.RampartStage;
import uk.ac.tgac.rampart.tool.pipeline.amp.AmpArgs;
import uk.ac.tgac.rampart.tool.process.mass.MassArgs;
import uk.ac.tgac.rampart.tool.process.mecq.MecqArgs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    public static final String KEY_ELEM_MASS            = "mass";
    public static final String KEY_ELEM_AMP             = "amp";


    private RampartParams params = new RampartParams();

    private List<RampartStage> stages;
    private List<Library> libs;
    private MecqArgs mecqSettings;
    private MassArgs massSettings;
    private AmpArgs ampSettings;
    private RampartJobFileSystem rampartJobFileSystem;



    public RampartArgs(File configFile, File outputDir, String jobPrefix, List<RampartStage> stages) throws IOException {

        super(configFile, outputDir, jobPrefix);

        this.stages = new ArrayList<>();

        this.rampartJobFileSystem = new RampartJobFileSystem(outputDir);
    }



    @Override
    protected void internalParseXml(Element element) throws IOException {

        // All libraries
        Element librariesElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_LIBRARIES);
        NodeList libraries = librariesElement.getElementsByTagName(KEY_ELEM_LIBRARY);
        for(int i = 0; i < libraries.getLength(); i++) {
            this.libs.add(new Library((Element)libraries.item(i)));
        }

        Element pipelineElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_PIPELINE);

        // MECQ
        Element mecqElement = XmlHelper.getDistinctElementByName(pipelineElement, KEY_ELEM_MECQ);
        this.mecqSettings = mecqElement == null ? null :
                new MecqArgs(
                        mecqElement,
                        this.rampartJobFileSystem.getMeqcDir(),
                        this.getJobPrefix() + "-mecq",
                        this.libs);

        // MASS
        Element massElement = XmlHelper.getDistinctElementByName(pipelineElement, KEY_ELEM_MASS);
        this.massSettings = massElement == null ? null :
                new MassArgs(
                        massElement,
                        this.rampartJobFileSystem.getMassDir(),
                        this.rampartJobFileSystem.getMeqcDir(),
                        this.getJobPrefix() + "-mass",
                        this.libs,
                        this.mecqSettings == null ? null : this.mecqSettings.getEqcArgList(),
                        this.getOrganism());

        // AMP
        Element ampElement = XmlHelper.getDistinctElementByName(pipelineElement, KEY_ELEM_AMP);
        this.ampSettings = ampElement == null ? null :
                new AmpArgs(
                        ampElement,
                        this.rampartJobFileSystem.getAmpDir(),
                        this.getJobPrefix() + "-amp",
                        this.rampartJobFileSystem.getMassOutFile(),
                        this.libs,
                        this.mecqSettings.getEqcArgList(),
                        this.getOrganism());

    }

    public List<RampartStage> getStages() {
        return stages;
    }

    public void setStages(List<RampartStage> stages) {
        this.stages = stages;
    }

    @Override
    public void parse(String args) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = new HashMap<>();

        if (this.getConfigFile() != null) {
            pvp.put(params.getConfig(), this.getConfigFile().getAbsolutePath());
        }

        if (this.getOutputDir() != null) {
            pvp.put(params.getOutputDir(), this.getOutputDir().getAbsolutePath());
        }

        if (this.stages != null && this.stages.size() > 0) {
            pvp.put(params.getStageList(), RampartStage.toString(this.stages));
        }

        return pvp;
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) throws IOException {
        for (Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            }

            String param = entry.getKey().getName();

            if (param.equals(this.params.getConfig().getName())) {
                this.setConfigFile(new File(entry.getValue()));
            }
            else if (param.equals(this.params.getOutputDir().getName())) {
                this.setOutputDir(new File(entry.getValue()));
            }
            else if (param.equals(this.params.getStageList().getName())) {
                this.stages = RampartStage.parse(entry.getValue());
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

    public AmpArgs getAmpSettings() {
        return ampSettings;
    }

    public void setAmpSettings(AmpArgs ampSettings) {
        this.ampSettings = ampSettings;
    }

    public MecqArgs getMecqSettings() {
        return mecqSettings;
    }

    public void setMecqSettings(MecqArgs mecqSettings) {
        this.mecqSettings = mecqSettings;
    }

    public MassArgs getMassSettings() {
        return massSettings;
    }

    public void setMassSettings(MassArgs massSettings) {
        this.massSettings = massSettings;
    }

    public List<Library> getLibs() {
        return libs;
    }

    public void setLibs(List<Library> libs) {
        this.libs = libs;
    }



}
