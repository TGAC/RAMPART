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
package uk.ac.tgac.rampart.tool.process.mass.single;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.tool.process.mass.CoverageRange;
import uk.ac.tgac.rampart.tool.process.mass.KmerRange;
import uk.ac.tgac.rampart.tool.process.mass.MassArgs;
import uk.ac.tgac.rampart.tool.process.mass.MassInput;
import uk.ac.tgac.rampart.tool.process.mecq.MecqSingleArgs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: maplesod
 * Date: 10/01/13
 * Time: 17:04
 */
public class SingleMassArgs implements ProcessArgs {

    public static final String KEY_ELEM_INPUTS = "inputs";
    public static final String KEY_ELEM_SINGLE_INPUT = "input";

    public static final String KEY_ATTR_NAME = "name";
    public static final String KEY_ATTR_THREADS = "threads";
    public static final String KEY_ATTR_MEMORY = "memory";
    public static final String KEY_ATTR_PARALLEL = "parallel";


    // Need access to these
    private SingleMassParams params = new SingleMassParams();

    // Class vars
    private File outputDir;
    private String jobPrefix;

    private String name;
    private String tool;
    private KmerRange kmerRange;
    private CoverageRange coverageRange;
    private int coverageCutoff;
    private Organism organism;

    // Inputs
    private List<MassInput> inputs;
    private List<Library> allLibraries;
    private List<MecqSingleArgs> allMecqs;

    // System settings
    private int threads;
    private int memory;
    private boolean runParallel;



    public SingleMassArgs() {
        this.outputDir = null;
        this.jobPrefix = "";

        this.tool = "ABYSS_V1_3_4";
        this.kmerRange = new KmerRange();
        this.coverageRange = new CoverageRange();
        this.coverageCutoff = -1;
        this.runParallel = false;
        this.organism = null;
        this.inputs = new ArrayList<MassInput>();

        this.threads = 1;
        this.memory = 0;
    }

    public SingleMassArgs(Element ele, File parentOutputDir, String parentJobPrefix, List<Library> allLibraries, List<MecqSingleArgs> allMecqs, Organism organism) {

        // Set defaults
        this();

        this.name = XmlHelper.getTextValue(ele, KEY_ATTR_NAME);
        this.threads = XmlHelper.getIntValue(ele, KEY_ATTR_THREADS);
        this.memory = XmlHelper.getIntValue(ele, KEY_ATTR_MEMORY);
        this.runParallel = XmlHelper.getBooleanValue(ele, KEY_ATTR_PARALLEL);

        Element inputElements = XmlHelper.getDistinctElementByName(ele, KEY_ELEM_INPUTS);

        NodeList actualInputs = inputElements.getElementsByTagName(KEY_ELEM_SINGLE_INPUT);
        for(int i = 0; i < actualInputs.getLength(); i++) {
            this.inputs.add(new MassInput((Element) actualInputs.item(i)));
        }

        this.allLibraries = allLibraries;
        this.allMecqs = allMecqs;

        this.outputDir = new File(parentOutputDir, name);
        this.jobPrefix = parentJobPrefix + "-" + name;
        this.organism = organism;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public KmerRange getKmerRange() {
        return kmerRange;
    }

    public void setKmerRange(KmerRange kmerRange) {
        this.kmerRange = kmerRange;
    }

    public CoverageRange getCoverageRange() {
        return coverageRange;
    }

    public void setCoverageRange(CoverageRange coverageRange) {
        this.coverageRange = coverageRange;
    }

    public int getCoverageCutoff() {
        return coverageCutoff;
    }

    public void setCoverageCutoff(int coverageCutoff) {
        this.coverageCutoff = coverageCutoff;
    }

    public boolean isRunParallel() {
        return runParallel;
    }

    public void setRunParallel(boolean runParallel) {
        this.runParallel = runParallel;
    }

    public List<MassInput> getInputs() {
        return inputs;
    }

    public void setInputs(List<MassInput> inputs) {
        this.inputs = inputs;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
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

    public List<MecqSingleArgs> getAllMecqs() {
        return allMecqs;
    }

    public void setAllMecqs(List<MecqSingleArgs> allMecqs) {
        this.allMecqs = allMecqs;
    }

    public File getUnitigsDir() {
        return new File(this.getOutputDir(), "unitigs");
    }

    public File getContigsDir() {
        return new File(this.getOutputDir(), "contigs");
    }

    public File getScaffoldsDir() {
        return new File(this.getOutputDir(), "scaffolds");
    }


    public File getStatsFile(MassArgs.OutputLevel outputLevel) {

        File outputLevelStatsDir = null;

        if (outputLevel == MassArgs.OutputLevel.CONTIGS) {
            outputLevelStatsDir = this.getContigsDir();
        }
        else if (outputLevel == MassArgs.OutputLevel.SCAFFOLDS) {
            outputLevelStatsDir = this.getScaffoldsDir();
        }
        else {
            throw new IllegalArgumentException("Output Level not specified");
        }

        return new File(outputLevelStatsDir, "stats.txt");
    }


    @Override
    public void parse(String args) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<ConanParameter, String> getArgMap() {

        return null;
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {

        for (Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            }

            String param = entry.getKey().getName();

        }
    }
}
