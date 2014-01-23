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
package uk.ac.tgac.rampart.tool.process.mass;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uk.ac.ebi.fgpt.conan.core.param.DefaultParamMap;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.Rampart;
import uk.ac.tgac.rampart.tool.pipeline.RampartStageArgs;
import uk.ac.tgac.rampart.tool.process.mass.single.SingleMassArgs;
import uk.ac.tgac.rampart.tool.process.mass.single.SingleMassParams;
import uk.ac.tgac.rampart.tool.process.mecq.EcqArgs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: maplesod
 * Date: 10/01/13
 * Time: 17:04
 */
public class MassArgs implements RampartStageArgs {

    // Keys for config file
    private static final String KEY_ATTR_PARALLEL = "parallel";
    private static final String KEY_ELEM_SINGLE_MASS = "single_mass";

    // Constants
    public static final int DEFAULT_CVG_CUTOFF = -1;
    public static final OutputLevel DEFAULT_OUTPUT_LEVEL = OutputLevel.CONTIGS;
    public static final boolean DEFAULT_RUN_PARALLEL = false;


    // Need access to these
    private SingleMassParams params = new SingleMassParams();

    // Rampart vars
    private String jobPrefix;
    private File outputDir;
    private List<SingleMassArgs> singleMassArgsList;    // List of Single MASS groups to run separately
    private List<Library> allLibraries;                    // All allLibraries available in this job
    private List<EcqArgs> allMecqs;                 // All mecq configurations
    private File mecqDir;
    private boolean runParallel;                // Whether to run MASS groups in parallel
    private Organism organism;


    private OutputLevel outputLevel;

    @Override
    public List<ConanProcess> getExternalProcesses() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public enum OutputLevel {
        UNITIGS,
        CONTIGS,
        SCAFFOLDS;

        public static String getListAsString() {

            List<String> levels = new ArrayList<>();

            for(OutputLevel level : OutputLevel.values()) {
                levels.add(level.toString());
            }

            return StringUtils.join(levels, ",");
        }
    }

    public MassArgs() {
        this.jobPrefix = "";
        this.outputDir = null;

        this.allLibraries = new ArrayList<>();
        this.allMecqs = new ArrayList<>();
        this.mecqDir = null;

        this.outputLevel = DEFAULT_OUTPUT_LEVEL;

        this.organism = null;
        this.runParallel = DEFAULT_RUN_PARALLEL;
        this.singleMassArgsList = new ArrayList<>();
    }

    public MassArgs(Element ele, File outputDir, File mecqDir, String jobPrefix, List<Library> allLibraries, List<EcqArgs> allMecqs, Organism organism)
            throws IOException {

        // Set defaults first
        this();

        // Set from parameters
        this.outputDir = outputDir;
        this.jobPrefix = jobPrefix;
        this.allLibraries = allLibraries;
        this.allMecqs = allMecqs;
        this.organism = organism;
        this.mecqDir = mecqDir;


        // From Xml (optional)
        this.runParallel = ele.hasAttribute(KEY_ATTR_PARALLEL) ?
                XmlHelper.getBooleanValue(ele, KEY_ATTR_PARALLEL) :
                DEFAULT_RUN_PARALLEL;

        // All single mass args
        NodeList nodes = ele.getElementsByTagName(KEY_ELEM_SINGLE_MASS);
        for(int i = 0; i < nodes.getLength(); i++) {
            this.singleMassArgsList.add(
                    new SingleMassArgs(
                            (Element)nodes.item(i), outputDir, mecqDir, jobPrefix + "-group",
                            this.allLibraries, this.allMecqs, this.organism));
        }
    }


    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public String getJobPrefix() {
        return jobPrefix;
    }

    public void setJobPrefix(String jobPrefix) {
        this.jobPrefix = jobPrefix;
    }

    public OutputLevel getOutputLevel() {
        return outputLevel;
    }

    public void setOutputLevel(OutputLevel outputLevel) {
        this.outputLevel = outputLevel;
    }

    public boolean isRunParallel() {
        return runParallel;
    }

    public void setRunParallel(boolean runParallel) {
        this.runParallel = runParallel;
    }

    public Organism getOrganism() {
        return organism;
    }

    public void setOrganism(Organism organism) {
        this.organism = organism;
    }

    public List<Library> getAllLibraries() {
        return allLibraries;
    }

    public void setAllLibraries(List<Library> allLibraries) {
        this.allLibraries = allLibraries;
    }

    public List<SingleMassArgs> getSingleMassArgsList() {
        return singleMassArgsList;
    }

    public void setSingleMassArgsList(List<SingleMassArgs> singleMassArgsList) {
        this.singleMassArgsList = singleMassArgsList;
    }

    public List<EcqArgs> getAllMecqs() {
        return allMecqs;
    }

    public void setAllMecqs(List<EcqArgs> allMecqs) {
        this.allMecqs = allMecqs;
    }

    public File getMecqDir() {
        return mecqDir;
    }

    public void setMecqDir(File mecqDir) {
        this.mecqDir = mecqDir;
    }

    @Override
    public void parse(String args) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ParamMap getArgMap() {

        ParamMap pvp = new DefaultParamMap();


        if (this.outputLevel != null) {
            pvp.put(params.getOutputLevel(), this.outputLevel.toString());
        }

        if (this.outputDir != null)
            pvp.put(params.getOutputDir(), this.outputDir.getAbsolutePath());

        if (this.jobPrefix != null)
            pvp.put(params.getJobPrefix(), this.jobPrefix);


        return pvp;
    }

    @Override
    public void setFromArgMap(ParamMap pvp) {
        for (Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            }

            ConanParameter param = entry.getKey();

            if (param.equals(this.params.getJobPrefix())) {
                this.jobPrefix = entry.getValue();
            } else if (param.equals(this.params.getOutputDir())) {
                this.outputDir = new File(entry.getValue());
            } else if (param.equals(this.params.getOutputLevel())) {
                this.outputLevel = OutputLevel.valueOf(entry.getValue());
            }
        }
    }



}
