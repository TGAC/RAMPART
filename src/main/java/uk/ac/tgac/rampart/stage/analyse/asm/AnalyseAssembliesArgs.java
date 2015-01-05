/*
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2015  Daniel Mapleson - TGAC
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
 */

package uk.ac.tgac.rampart.stage.analyse.asm;

import org.apache.commons.cli.CommandLine;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.stage.RampartStageArgs;
import uk.ac.tgac.rampart.stage.analyse.asm.analysers.AssemblyAnalyser;
import uk.ac.tgac.rampart.util.SpiFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by maplesod on 15/06/14.
 */
public abstract class AnalyseAssembliesArgs extends AbstractProcessArgs implements RampartStageArgs {

    private static final String KEY_ATTR_PARALLEL = "parallel";

    private static final String KEY_ELEM_TOOL = "tool";

    public static final boolean DEFAULT_RUN_PARALLEL = false;

    private SpiFactory<AssemblyAnalyser> assemblyAnalyserFactory;
    private Set<AssemblyAnalyser> assemblyAnalysers;

    private List<ToolArgs> tools;
    private File analyseReadsDir;
    private File outputDir;
    private Organism organism;
    private int threadsPerProcess;
    private int memory;
    private boolean runParallel;
    private String jobPrefix;

    public AnalyseAssembliesArgs(AnalyseAssembliesParams params) {

        super(params);

        this.tools = new ArrayList<>();
        this.analyseReadsDir = null;
        this.outputDir = null;
        this.organism = null;
        this.threadsPerProcess = 1;
        this.memory = 0;
        this.runParallel = false;
        this.jobPrefix = "assembly-analyses";

        this.assemblyAnalyserFactory = new SpiFactory<>(AssemblyAnalyser.class);
        this.assemblyAnalysers = new HashSet<>();
    }

    public AnalyseAssembliesArgs(AnalyseAssembliesParams params, Element element, File analyseReadsDir, File outputDir,
                                 Organism organism, String jobPrefix, boolean doingReadKmerAnalysis) throws IOException {

        this(params);

        // Check there's nothing
        if (!XmlHelper.validate(element,
                new String[]{},
                new String[]{
                        KEY_ATTR_PARALLEL,
                },
                new String[]{
                        KEY_ELEM_TOOL
                },
                new String[0]
        )) {
            throw new IOException("Found unrecognised element or attribute in \"analyse_mass\"");
        }

        this.analyseReadsDir = analyseReadsDir;
        this.outputDir = outputDir;
        this.organism = organism;
        this.jobPrefix = jobPrefix;

        this.runParallel = element.hasAttribute(KEY_ATTR_PARALLEL) ?
                XmlHelper.getBooleanValue(element, KEY_ATTR_PARALLEL) :
                DEFAULT_RUN_PARALLEL;

        // All libraries
        NodeList nodes = element.getElementsByTagName(KEY_ELEM_TOOL);
        for (int i = 0; i < nodes.getLength(); i++) {
            this.tools.add(new ToolArgs((Element) nodes.item(i), outputDir, analyseReadsDir, organism, jobPrefix, this.runParallel, doingReadKmerAnalysis));
        }

        for(AnalyseAssembliesArgs.ToolArgs requestedService : this.tools) {
            AssemblyAnalyser aa = this.assemblyAnalyserFactory.create(requestedService.getName());
            aa.setArgs(requestedService);
            this.assemblyAnalysers.add(aa);
        }
    }

    public File getAssembliesDir() {
        return new File(this.outputDir, "assemblies");
    }

    public Set<AssemblyAnalyser> getAssemblyAnalysers() {
        return assemblyAnalysers;
    }

    public List<ToolArgs> getTools() {
        return tools;
    }

    public void setTools(List<ToolArgs> tools) {
        this.tools = tools;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public File getAnalyseReadsDir() {
        return analyseReadsDir;
    }

    public void setAnalyseReadsDir(File analyseReadsDir) {
        this.analyseReadsDir = analyseReadsDir;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public Organism getOrganism() {
        return organism;
    }

    public void setOrganism(Organism organism) {
        this.organism = organism;
    }

    public int getThreadsPerProcess() {
        return threadsPerProcess;
    }

    public void setThreadsPerProcess(int threadsPerProcess) {
        this.threadsPerProcess = threadsPerProcess;
    }

    public boolean isRunParallel() {
        return runParallel;
    }

    public void setRunParallel(boolean runParallel) {
        this.runParallel = runParallel;
    }

    public String getJobPrefix() {
        return jobPrefix;
    }

    public void setJobPrefix(String jobPrefix) {
        this.jobPrefix = jobPrefix;
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
    public void parseCommandLine(CommandLine cmdLine) {
    }

    @Override
    public ParamMap getArgMap() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ConanProcess> getExternalProcesses() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public static class ToolArgs {

        // **** Xml Config file property keys ****

        private static final String KEY_ATTR_NAME = "name";
        private static final String KEY_ATTR_THREADS = "threads";
        private static final String KEY_ATTR_MEMORY = "memory";
        private static final String KEY_ATTR_PARALLEL = "parallel";


        // **** Default values ****

        public static final int DEFAULT_THREADS = 1;
        public static final int DEFAULT_MEMORY = 0;
        public static final boolean DEFAULT_RUN_PARALLEL = false;


        // **** Class vars ****

        private String name;
        private int threads;
        private int memory;
        private boolean runParallel;
        private String jobPrefix;
        private Organism organism;
        private File outputDir;
        private File readsAnalysisDir;

        public ToolArgs() {
            this.name = "";
            this.threads = DEFAULT_THREADS;
            this.memory = DEFAULT_MEMORY;
            this.runParallel = DEFAULT_RUN_PARALLEL;
            this.jobPrefix = "analyse_mass_tool";
            this.outputDir = null;
            this.readsAnalysisDir = null;
        }


        public ToolArgs(Element ele, File outputDir, File readsAnalysisDir, Organism organism, String jobPrefix, boolean forceParallel,
                        boolean doingReadKmerAnalysis)
                throws IOException {

            // Set defaults
            this();

            // Check there's nothing
            if (!XmlHelper.validate(ele,
                    new String[] {
                            KEY_ATTR_NAME
                    },
                    new String[] {
                            KEY_ATTR_THREADS,
                            KEY_ATTR_MEMORY,
                            KEY_ATTR_PARALLEL
                    },
                    new String[0],
                    new String[0])) {
                throw new IOException("Found unrecognised element or attribute in analyse_mass tool");
            }

            // Required
            if (!ele.hasAttribute(KEY_ATTR_NAME))
                throw new IOException("Could not find " + KEY_ATTR_NAME + " attribute in analyse_mass tool.");

            this.name = XmlHelper.getTextValue(ele, KEY_ATTR_NAME);

            if (this.name.equalsIgnoreCase("KAT") && !doingReadKmerAnalysis) {
                throw new IOException("You have requested to do a KAT analysis of your assemblies but have not requested a kmer analysis of your reads.  Either remove the KAT assembly analysis request or add a read kmer analysis request to your configuration file.");
            }

            // Optional
            this.threads = ele.hasAttribute(KEY_ATTR_THREADS) ? XmlHelper.getIntValue(ele, KEY_ATTR_THREADS) : DEFAULT_THREADS;
            this.memory = ele.hasAttribute(KEY_ATTR_MEMORY) ? XmlHelper.getIntValue(ele, KEY_ATTR_MEMORY) : DEFAULT_MEMORY;
            this.runParallel = forceParallel ||
                    (ele.hasAttribute(KEY_ATTR_PARALLEL) ? XmlHelper.getBooleanValue(ele, KEY_ATTR_PARALLEL) : DEFAULT_RUN_PARALLEL);

            this.jobPrefix = jobPrefix;
            this.organism = organism;
            this.outputDir = outputDir;
            this.readsAnalysisDir = readsAnalysisDir;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

        public String getJobPrefix() {
            return jobPrefix;
        }

        public void setJobPrefix(String jobPrefix) {
            this.jobPrefix = jobPrefix;
        }

        public Organism getOrganism() {
            return organism;
        }

        public void setOrganism(Organism organism) {
            this.organism = organism;
        }

        public File getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(File outputDir) {
            this.outputDir = outputDir;
        }

        public File getReadsAnalysisDir() {
            return readsAnalysisDir;
        }

        public void setReadsAnalysisDir(File readsAnalysisDir) {
            this.readsAnalysisDir = readsAnalysisDir;
        }
    }
}
