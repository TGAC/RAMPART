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

import org.apache.commons.lang.StringUtils;
import org.ini4j.Profile;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.tgac.rampart.conan.process.AbstractAmpArgs;
import uk.ac.tgac.rampart.conan.process.AbstractAmpProcess;
import uk.ac.tgac.rampart.core.data.Library;
import uk.ac.tgac.rampart.core.data.RampartConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * User: maplesod
 * Date: 12/02/13
 * Time: 16:55
 */
public class AmpArgs implements ProcessArgs {

    private static final String INPUT_ASSEMBLY = "input";

    // Need access to these
    private AmpParams params = new AmpParams();

    private File inputAssembly;
    private File outputDir;
    private File config;
    private List<Library> libs;
    private List<AbstractAmpProcess> processes;
    private String jobPrefix;


    public AmpArgs() {
        this.inputAssembly = null;
        this.outputDir = null;
        this.libs = new ArrayList<Library>();
        this.config = null;
        this.processes = new ArrayList<AbstractAmpProcess>();
        this.jobPrefix = "amp";
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

    public List<Library> getLibs() {
        return libs;
    }

    public void setLibs(List<Library> libs) {
        this.libs = libs;
    }

    public File getConfig() {
        return config;
    }

    public void setConfig(File config) {
        this.config = config;
    }

    public List<AbstractAmpProcess> getProcesses() {
        return processes;
    }

    public void setProcesses(List<AbstractAmpProcess> processes) {
        this.processes = processes;
    }

    public String getJobPrefix() {
        return jobPrefix;
    }

    public void setJobPrefix(String jobPrefix) {
        this.jobPrefix = jobPrefix;
    }

    public void linkProcesses() {

        File inputFile = this.inputAssembly;

        for(int i = 0; i < this.processes.size(); i++) {

            AbstractAmpProcess ampProcess = this.processes.get(i);
            AbstractAmpArgs ampArgs = ampProcess.getAmpArgs();

            ampArgs.setInputFile(inputFile);
            ampArgs.setOutputDir(new File(this.getOutputDir(), Integer.toString(i)));
            ampArgs.setLibraries(this.getLibs());

            inputFile = ampArgs.getOutputFile();
        }
    }


    private static class IndexedAmpStage implements Comparable<IndexedAmpStage> {
        private int index;
        private AbstractAmpProcess stage;

        private IndexedAmpStage(int index, AbstractAmpProcess stage) {
            this.index = index;
            this.stage = stage;
        }

        public AbstractAmpProcess getStage() {
            return stage;
        }

        public void setStage(AbstractAmpProcess stage) {
            this.stage = stage;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        @Override
        public int compareTo(IndexedAmpStage o) {
            return index - o.getIndex();
        }
    }

    public static AmpArgs parseConfig(File config) throws IOException {

        RampartConfiguration rampartConfig = new RampartConfiguration();

        AmpArgs args = new AmpArgs();
        args.getProcesses().clear();

        rampartConfig.load(config);

        // Add the libs from the config file
        args.setLibs(rampartConfig.getLibs());

        Profile.Section section = rampartConfig.getAmpSettings();

        List<IndexedAmpStage> stageList = new ArrayList<IndexedAmpStage>();

        if (section != null) {
            for (Map.Entry<String, String> entry : section.entrySet()) {

                try {
                    int index = Integer.parseInt(entry.getKey());

                    AbstractAmpProcess ampStage = AmpFactory.createFromString(entry.getValue());

                    stageList.add(new IndexedAmpStage(index, ampStage));

                } catch(NumberFormatException e) {

                    // Not a process index so assume it's another AMP arg
                    if (entry.getKey().equalsIgnoreCase(INPUT_ASSEMBLY)) {
                        args.setInputAssembly(new File(entry.getValue()));
                    }
                }
            }
        }

        // Add processes in correct order
        Collections.sort(stageList);
        for(IndexedAmpStage stage : stageList) {

            args.processes.add(stage.getStage());
        }

        return args;
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

        if (this.processes != null)
            pvp.put(params.getProcesses(), StringUtils.join(this.processes, ","));

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
