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
package uk.ac.tgac.rampart.tool.pipeline.amp;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.rampart.tool.process.amp.AmpStageArgs;
import uk.ac.tgac.rampart.tool.process.mecq.EcqArgs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: maplesod
 * Date: 12/02/13
 * Time: 16:55
 */
public class AmpArgs implements ProcessArgs {

    private static final String INPUT_ASSEMBLY = "input";
    private static final String KEY_ELEM_AMP_STAGE = "stage";

    // Need access to these
    private AmpParams params = new AmpParams();

    private File inputAssembly;
    private File outputDir;
    private File config;
    private List<Library> allLibraries;
    private List<EcqArgs> allMecqs;
    private List<AmpStageArgs> stageArgsList;
    private String jobPrefix;
    private Organism organism;


    public AmpArgs() {
        this.inputAssembly = null;
        this.outputDir = null;
        this.allLibraries = new ArrayList<Library>();
        this.allMecqs = new ArrayList<EcqArgs>();
        this.config = null;
        this.stageArgsList = new ArrayList<AmpStageArgs>();
        this.jobPrefix = "amp";
    }

    public AmpArgs(Element ele, File outputDir, String jobPrefix, File inputAssembly, List<Library> allLibraries, List<EcqArgs> allMecqs, Organism organism) {

        // Set defaults
        this();

        // Set args
        this.outputDir = outputDir;
        this.jobPrefix = jobPrefix;
        this.inputAssembly = inputAssembly;
        this.allLibraries = allLibraries;
        this.allMecqs = allMecqs;
        this.organism = organism;

        // Parse Xml for AMP stages
        // All single mass args
        File inputFile = this.inputAssembly;
        NodeList nodes = ele.getElementsByTagName(KEY_ELEM_AMP_STAGE);
        for(int i = 1; i <= nodes.getLength(); i++) {

            String stageName = "amp-" + Integer.toString(i);
            File stageOutputDir = new File(this.getOutputDir(), stageName);

            AmpStageArgs stage = new AmpStageArgs(
                    (Element)nodes.item(i-1), stageOutputDir, this.getAssembliesDir(), jobPrefix + "-" + stageName,
                    this.allLibraries, this.allMecqs, this.organism,
                    inputFile, i);

            this.stageArgsList.add(stage);

            inputFile = stage.getOutputFile();
        }
    }


    public AmpParams getParams() {
        return params;
    }

    public void setParams(AmpParams params) {
        this.params = params;
    }

    public File getInputAssembly() {
        return inputAssembly;
    }

    public void setInputAssembly(File inputAssembly) {
        this.inputAssembly = inputAssembly;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public List<Library> getAllLibraries() {
        return allLibraries;
    }

    public void setAllLibraries(List<Library> allLibraries) {
        this.allLibraries = allLibraries;
    }

    public File getConfig() {
        return config;
    }

    public void setConfig(File config) {
        this.config = config;
    }

    public String getJobPrefix() {
        return jobPrefix;
    }

    public void setJobPrefix(String jobPrefix) {
        this.jobPrefix = jobPrefix;
    }

    public List<EcqArgs> getAllMecqs() {
        return allMecqs;
    }

    public void setAllMecqs(List<EcqArgs> allMecqs) {
        this.allMecqs = allMecqs;
    }

    public List<AmpStageArgs> getStageArgsList() {
        return stageArgsList;
    }

    public void setStageArgsList(List<AmpStageArgs> stageArgsList) {
        this.stageArgsList = stageArgsList;
    }

    public Organism getOrganism() {
        return organism;
    }

    public void setOrganism(Organism organism) {
        this.organism = organism;
    }

    public File getAssembliesDir() {
        return new File(this.getOutputDir(), "assemblies");
    }


    @Override
    public void parse(String args) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();

        if (this.inputAssembly != null)
            pvp.put(params.getInputAssembly(), this.inputAssembly.getAbsolutePath());

        if (this.outputDir != null)
            pvp.put(params.getOutputDir(), this.outputDir.getAbsolutePath());

        if (this.config != null)
            pvp.put(params.getConfig(), this.config.getAbsolutePath());

        if (this.jobPrefix != null)
            pvp.put(params.getJobPrefix(), this.jobPrefix);

        return pvp;
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {

        for (Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            }

            String param = entry.getKey().getName();

            if (param.equals(this.params.getInputAssembly().getName())) {
                this.inputAssembly = new File(entry.getValue());
            } else if (param.equals(this.params.getOutputDir().getName())) {
                this.outputDir = new File(entry.getValue());
            } else if (param.equals(this.params.getConfig().getName())) {
                this.config = new File(entry.getValue());
            } else if (param.equals(this.params.getJobPrefix().getName())) {
                this.jobPrefix = entry.getValue();
            }
        }
    }
}
