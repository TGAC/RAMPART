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
import uk.ac.ebi.fgpt.conan.core.param.DefaultParamMap;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.tool.pipeline.RampartStageArgs;
import uk.ac.tgac.rampart.tool.process.amp.AmpStage;
import uk.ac.tgac.rampart.tool.process.mecq.EcqArgs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: maplesod
 * Date: 12/02/13
 * Time: 16:55
 */
public class AmpArgs implements RampartStageArgs {

    private static final String INPUT_ASSEMBLY = "input";
    private static final String KEY_ELEM_AMP_STAGE = "stage";

    private static final String KEY_ATTR_THREADS = "threads";
    private static final String KEY_ATTR_MEMORY = "memory";
    private static final String KEY_ATTR_PARALLEL = "parallel";
    private static final String KEY_ATTR_STATS_ONLY = "stats_only";
    private static final String KEY_ATTR_STATS_LEVELS = "stats_levels";

    public static final boolean DEFAULT_STATS_ONLY = false;
    public static final boolean DEFAULT_RUN_PARALLEL = false;
    public static final int DEFAULT_THREADS = 1;
    public static final int DEFAULT_MEMORY = 0;

    // Need access to these
    private AmpParams params = new AmpParams();

    private File inputAssembly;
    private File outputDir;
    private List<Library> allLibraries;
    private List<EcqArgs> allMecqs;
    private List<AmpStage.Args> stageArgsList;
    private String jobPrefix;
    private Organism organism;
    private boolean statsOnly;
    private int threads;
    private int memory;
    private boolean runParallel;
    private ExecutionContext executionContext;


    public AmpArgs() {
        this.inputAssembly = null;
        this.outputDir = null;
        this.allLibraries = new ArrayList<>();
        this.allMecqs = new ArrayList<>();
        this.stageArgsList = new ArrayList<>();
        this.jobPrefix = "amp";
        this.statsOnly = DEFAULT_STATS_ONLY;
        this.threads = DEFAULT_THREADS;
        this.memory = DEFAULT_MEMORY;
        this.runParallel = DEFAULT_RUN_PARALLEL;
    }

    public AmpArgs(Element ele, File outputDir, String jobPrefix, File inputAssembly,
                   List<Library> allLibraries, List<EcqArgs> allMecqs, Organism organism)
            throws IOException {

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

            AmpStage.Args stage = new AmpStage.Args(
                    (Element)nodes.item(i-1), stageOutputDir, this.getAssembliesDir(), jobPrefix + "-" + stageName,
                    this.allLibraries, this.allMecqs, this.organism,
                    inputFile, i);

            this.stageArgsList.add(stage);

            inputFile = stage.getOutputFile();
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

    public List<AmpStage.Args> getStageArgsList() {
        return stageArgsList;
    }

    public void setStageArgsList(List<AmpStage.Args> stageArgsList) {
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

    public boolean isStatsOnly() {
        return statsOnly;
    }

    public void setStatsOnly(boolean statsOnly) {
        this.statsOnly = statsOnly;
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

    public boolean isRunParallel() {
        return runParallel;
    }

    public void setRunParallel(boolean runParallel) {
        this.runParallel = runParallel;
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    @Override
    public void parse(String args) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ParamMap getArgMap() {

        ParamMap pvp = new DefaultParamMap();

        if (this.inputAssembly != null)
            pvp.put(params.getInputAssembly(), this.inputAssembly.getAbsolutePath());

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

            if (param.equals(this.params.getInputAssembly())) {
                this.inputAssembly = new File(entry.getValue());
            } else if (param.equals(this.params.getOutputDir())) {
                this.outputDir = new File(entry.getValue());
            } else if (param.equals(this.params.getJobPrefix())) {
                this.jobPrefix = entry.getValue();
            }
        }
    }

    public File getFinalAssembly() {
        return new File(this.getOutputDir(), "final.fa");
    }

    @Override
    public List<ConanProcess> getExternalProcesses() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
