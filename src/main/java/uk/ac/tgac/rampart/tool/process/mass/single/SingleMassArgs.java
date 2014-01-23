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
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.RampartJobFileSystem;
import uk.ac.tgac.rampart.tool.process.mass.MassArgs;
import uk.ac.tgac.rampart.tool.process.mass.ReadsInput;
import uk.ac.tgac.rampart.tool.process.mecq.EcqArgs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: maplesod
 * Date: 10/01/13
 * Time: 17:04
 */
public class SingleMassArgs extends AbstractProcessArgs {

    private static final String KEY_ELEM_INPUTS = "inputs";
    private static final String KEY_ELEM_SINGLE_INPUT = "input";
    private static final String KEY_ELEM_KMER_RANGE = "kmer";
    private static final String KEY_ELEM_CVG_RANGE = "coverage";

    private static final String KEY_ATTR_NAME = "name";
    private static final String KEY_ATTR_TOOL = "tool";
    private static final String KEY_ATTR_THREADS = "threads";
    private static final String KEY_ATTR_MEMORY = "memory";
    private static final String KEY_ATTR_PARALLEL = "parallel";

    public static final boolean DEFAULT_STATS_ONLY = false;
    public static final boolean DEFAULT_RUN_PARALLEL = false;
    public static final int DEFAULT_THREADS = 1;
    public static final int DEFAULT_MEMORY = 0;


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
    private File mecqDir;
    private List<ReadsInput> inputs;
    private List<Library> allLibraries;
    private List<EcqArgs> allMecqs;

    // System settings
    private int threads;
    private int memory;
    private boolean runParallel;


    public SingleMassArgs() {

        super(new SingleMassParams());

        this.outputDir = null;
        this.jobPrefix = "";

        this.tool = "ABYSS_V1_3_4";
        this.kmerRange = new KmerRange();
        this.coverageRange = new CoverageRange();
        this.coverageCutoff = -1;
        this.organism = null;

        this.mecqDir = null;
        this.inputs = new ArrayList<>();
        this.allLibraries = new ArrayList<>();
        this.allMecqs = new ArrayList<>();

        this.threads = 1;
        this.memory = 0;
        this.runParallel = DEFAULT_RUN_PARALLEL;
    }



    public SingleMassArgs(Element ele, File parentOutputDir, File mecqDir, String parentJobPrefix, List<Library> allLibraries,
                          List<EcqArgs> allMecqs, Organism organism) throws IOException {

        // Set defaults
        this();

        // Required Attributes
        if (!ele.hasAttribute(KEY_ATTR_NAME))
            throw new IOException("Could not find " + KEY_ATTR_NAME + " attribute in single mass element");

        if (!ele.hasAttribute(KEY_ATTR_TOOL))
            throw new IOException("Could not find " + KEY_ATTR_TOOL + " attribute in single mass element");

        this.name = XmlHelper.getTextValue(ele, KEY_ATTR_NAME);
        this.tool = XmlHelper.getTextValue(ele, KEY_ATTR_TOOL);

        // Required Elements
        Element inputElements = XmlHelper.getDistinctElementByName(ele, KEY_ELEM_INPUTS);
        NodeList actualInputs = inputElements.getElementsByTagName(KEY_ELEM_SINGLE_INPUT);
        for(int i = 0; i < actualInputs.getLength(); i++) {
            this.inputs.add(new ReadsInput((Element) actualInputs.item(i)));
        }

        // Optional
        this.threads = ele.hasAttribute(KEY_ATTR_THREADS) ?
                XmlHelper.getIntValue(ele, KEY_ATTR_THREADS) :
                DEFAULT_THREADS;

        this.memory = ele.hasAttribute(KEY_ATTR_MEMORY) ?
                XmlHelper.getIntValue(ele, KEY_ATTR_MEMORY) :
                DEFAULT_MEMORY;

        this.runParallel = ele.hasAttribute(KEY_ATTR_PARALLEL) ?
                XmlHelper.getBooleanValue(ele, KEY_ATTR_PARALLEL) :
                DEFAULT_RUN_PARALLEL;

        Element kmerElement = XmlHelper.getDistinctElementByName(ele, KEY_ELEM_KMER_RANGE);
        Element cvgElement = XmlHelper.getDistinctElementByName(ele, KEY_ELEM_CVG_RANGE);
        this.kmerRange = kmerElement != null ? new KmerRange(kmerElement) : new KmerRange();
        this.coverageRange = cvgElement != null ? new CoverageRange(cvgElement) : new CoverageRange();

        // Other args
        this.allLibraries = allLibraries;
        this.allMecqs = allMecqs;
        this.outputDir = new File(parentOutputDir, name);
        this.jobPrefix = parentJobPrefix + "-" + name;
        this.organism = organism;
        this.mecqDir = mecqDir;
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

    public List<ReadsInput> getInputs() {
        return inputs;
    }

    public void setInputs(List<ReadsInput> inputs) {
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

    public File getUnitigsDir() {
        return new File(this.getOutputDir(), "unitigs");
    }

    public File getContigsDir() {
        return new File(this.getOutputDir(), "contigs");
    }

    public File getScaffoldsDir() {
        return new File(this.getOutputDir(), "scaffolds");
    }


    public List<File> getInputKmers() {

        RampartJobFileSystem fs = new RampartJobFileSystem(this.getMecqDir().getParentFile());

        List<File> inputKmers = new ArrayList<>();

        for(ReadsInput ri : this.inputs) {
            inputKmers.add(new File(fs.getAnalyseReadsDir(), "jellyfish_" + ri.getEcq() + "_" + ri.getLib() + "_0"));
        }

        return inputKmers;
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
    protected void setOptionFromMapEntry(ConanParameter param, String value) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void setArgFromMapEntry(ConanParameter param, String value) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void parse(String args) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ParamMap getArgMap() {

        return null;
    }

}
