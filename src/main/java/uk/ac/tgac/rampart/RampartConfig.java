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
    public static final String KEY_ELEM_ORGANISM        = "organism";
    public static final String KEY_ELEM_LIBRARY         = "library";
    public static final String KEY_ELEM_PIPELINE        = "pipeline";

    private RampartStageList stages;
    private List<Library> libs;
    private RampartJobFileSystem rampartJobFileSystem;
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
        this.rampartJobFileSystem = new RampartJobFileSystem(outputDir.getAbsoluteFile());
        this.doInitialChecks = doInitialChecks;
        this.ampInput = ampInput;
        this.ampBubble = ampBubble;
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
        NodeList libraries = librariesElement.getElementsByTagName(KEY_ELEM_LIBRARY);
        this.libs = new ArrayList<>();
        for(int i = 0; i < libraries.getLength(); i++) {
            this.libs.add(new Library((Element)libraries.item(i), this.getOutputDir().getAbsoluteFile()));
        }

        // Check all library files exist on the file system
        for(Library lib : this.libs) {
            if (!lib.getFile1().exists()) {
                throw new IOException("Could not locate file 1 from library: " + lib.getName() + "; " + lib.getFile1().getAbsolutePath());
            }

            if (lib.isPairedEnd() && !lib.getFile2().exists()) {
                throw new IOException("Could not locate file 2 from library: " + lib.getName() + "; " + lib.getFile2().getAbsolutePath());
            }
        }

        this.pipelineArgs = new RampartPipeline.Args(
                XmlHelper.getDistinctElementByName(element, KEY_ELEM_PIPELINE),
                this.libs,
                this.getOrganism(),
                this.rampartJobFileSystem,
                this.getJobPrefix(),
                this.getInstitution(),
                this.stages,
                this.doInitialChecks,
                this.ampInput,
                this.ampBubble);
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

