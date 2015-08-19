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

package uk.ac.tgac.rampart;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.util.StringJoiner;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.util.AbstractXmlJobConfiguration;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.stage.RampartStageList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maplesod on 08/07/14.
 */
public class RampartConfig extends AbstractXmlJobConfiguration {

    public static final String KEY_ATTR_AUTHOR          = "author";
    public static final String KEY_ATTR_COLLABORATOR    = "collaborator";
    public static final String KEY_ATTR_INSTITUTION     = "institution";
    public static final String KEY_ATTR_TITLE           = "title";
    public static final String KEY_ELEM_LIBRARIES       = "libraries";
    public static final String KEY_ELEM_SAMPLES         = "samples";
    public static final String KEY_ELEM_ORGANISM        = "organism";
    public static final String KEY_ELEM_LIBRARY         = "library";
    public static final String KEY_ELEM_PIPELINE        = "pipeline";
    public static final String KEY_ATTR_SAMPLES_FILE    = "file";

    private RampartStageList stages;
    private List<Library> libs;
    private List<Library> samples;
    private File outputDir;
    private boolean multiSampleMode;
    private ExecutionContext executionContext;
    private boolean doInitialChecks;
    private RampartPipeline.Args pipelineArgs;
    private File ampInput;
    private File ampBubble;



    public RampartConfig(File configFile, File outputDir, String jobPrefix, RampartStageList stages, File ampInput, File ampBubble, boolean doInitialChecks)
            throws IOException {

        super(configFile, outputDir, jobPrefix);

        this.stages = stages;
        this.libs = new ArrayList<>();
        this.samples = new ArrayList<>();
        this.outputDir = outputDir;
        this.doInitialChecks = doInitialChecks;
        this.ampInput = ampInput;
        this.ampBubble = ampBubble;
        this.multiSampleMode = false;
    }



    @Override
    protected void internalParseXml(Element element) throws IOException {

        // Check there's nothing
        if (!XmlHelper.validate(element,
                new String[0],
                new String[]{
                        KEY_ATTR_AUTHOR,
                        KEY_ATTR_COLLABORATOR,
                        KEY_ATTR_INSTITUTION,
                        KEY_ATTR_TITLE
                },
                new String[]{
                        KEY_ELEM_LIBRARIES,
                        KEY_ELEM_PIPELINE,
                        KEY_ELEM_ORGANISM
                },
                new String[0])) {
            throw new IllegalArgumentException("Found unrecognised element or attribute in Library");
        }

        // All libraries
        Element librariesElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_LIBRARIES);
        Element samplesElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_SAMPLES);


        // Check to see if we run in single sample or multi-sample mode
        if (librariesElement != null) {
            NodeList libraries = librariesElement.getElementsByTagName(KEY_ELEM_LIBRARY);
            this.libs = new ArrayList<>();
            for (int i = 0; i < libraries.getLength(); i++) {
                this.libs.add(new Library((Element) libraries.item(i), this.getOutputDir().getAbsoluteFile()));
            }

            // Check all library files exist on the file system
            for (Library lib : this.libs) {
                if (!lib.getFile1().exists()) {
                    throw new IOException("Could not locate file 1 from library: " + lib.getName() + "; " + lib.getFile1().getAbsolutePath());
                }

                if (lib.isPairedEnd() && !lib.getFile2().exists()) {
                    throw new IOException("Could not locate file 2 from library: " + lib.getName() + "; " + lib.getFile2().getAbsolutePath());
                }
            }
            this.multiSampleMode = false;
        }
        else if (samplesElement != null) {
            String samplesFile = samplesElement.getAttribute(KEY_ATTR_SAMPLES_FILE);
            this.samples = parseSamplesFile(new File(samplesFile));
            this.multiSampleMode = true;
        }
        else {
            throw new IOException("Could not locate either a 'libraries' element or a 'samples' element in the config file.");
        }


        this.pipelineArgs = new RampartPipeline.Args(
                XmlHelper.getDistinctElementByName(element, KEY_ELEM_PIPELINE),
                this.multiSampleMode ? this.samples : this.libs,
                this.multiSampleMode,
                this.getOrganism(),
                this.outputDir,
                this.getJobPrefix(),
                this.getInstitution(),
                this.stages,
                this.doInitialChecks,
                this.ampInput,
                this.ampBubble);
    }

    protected List<Library> parseSamplesFile(File input) throws IOException {

        List<String> lines = FileUtils.readLines(input);
        List<Library> libs = new ArrayList<>();

        for(String line : lines) {
            String l = line.trim();

            if (l.isEmpty()) {
                continue;
            }

            String[] parts = l.split("\t");

            if (parts.length < 3 || parts.length > 4) {
                throw new IOException("Expected samples file to contain either 2 or 3 columns");
            }

            String sampleName = parts[0];
            String phred = parts[1];

            String file1 = parts[2];
            String file2 = (parts.length == 4) ? parts[3] : null;

            Library lib = new Library();
            lib.setName(sampleName);
            lib.setFiles(file1, file2);
            lib.setPhred(Library.Phred.valueOf(phred));

            libs.add(lib);
        }

        return libs;
    }

    public RampartStageList getStages() {
        return stages;
    }

    public void setStages(RampartStageList stages) {
        this.stages = stages;
    }


    public RampartPipeline.Args getPipelineArgs() {
        return pipelineArgs;
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



    public List<Library> getLibs() {
        return libs;
    }

    public void setLibs(List<Library> libs) {
        this.libs = libs;
    }

    public List<Library> getSamples() {
        return samples;
    }

    public void setSamples(List<Library> samples) {
        this.samples = samples;
    }

    public boolean isMultiSampleMode() {
        return multiSampleMode;
    }

    public void setMultiSampleMode(boolean multiSampleMode) {
        this.multiSampleMode = multiSampleMode;
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

    public boolean isDoInitialChecks() {
        return doInitialChecks;
    }

    public void setDoInitialChecks(boolean doInitialChecks) {
        this.doInitialChecks = doInitialChecks;
    }

    public void setPipelineArgs(RampartPipeline.Args pipelineArgs) {
        this.pipelineArgs = pipelineArgs;
    }

    public File getAmpInput() {
        return ampInput;
    }

    public void setAmpInput(File ampInput) {
        this.ampInput = ampInput;
    }

    public File getAmpBubble() {
        return ampBubble;
    }

    public void setAmpBubble(File ampBubble) {
        this.ampBubble = ampBubble;
    }
}

