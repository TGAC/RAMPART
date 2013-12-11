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
package uk.ac.tgac.rampart.tool.process.mecq;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.tool.pipeline.RampartStageArgs;

import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: maplesod
 * Date: 16/01/13
 * Time: 13:37
 */
public class MecqArgs implements RampartStageArgs {

    // Xml Config Keys
    public static final String KEY_ATTR_PARALLEL    = "parallel";
    public static final String KEY_ELEM_ECQ         = "ecq";


    public static final boolean DEFAULT_RUN_PARALLEL = false;

    private MecqParams params = new MecqParams();

    private File outputDir;
    private String jobPrefix;
    private List<Library> libraries;
    private boolean runParallel;
    private List<EcqArgs> eqcArgList;


    /**
     * Set defaults
     */
    public MecqArgs() {
        this.outputDir = new File("");
        this.eqcArgList = new ArrayList<>();
        this.libraries = new ArrayList<>();
        this.runParallel = DEFAULT_RUN_PARALLEL;

        Format formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateTime = formatter.format(new Date());
        this.jobPrefix = "qt-" + dateTime;
    }

    /**
     * Set from element and
     * @param ele
     */
    public MecqArgs(Element ele, File outputDir, String jobPrefix, List<Library> libraries) throws IOException {

        // Set defaults first
        this();

        // Set from parameters
        this.outputDir = outputDir;
        this.jobPrefix = jobPrefix;
        this.libraries = libraries;

        // Set from Xml
        this.runParallel = ele.hasAttribute(KEY_ATTR_PARALLEL) ? XmlHelper.getBooleanValue(ele, KEY_ATTR_PARALLEL) : DEFAULT_RUN_PARALLEL;

        // All libraries
        NodeList nodes = ele.getElementsByTagName(KEY_ELEM_ECQ);
        for(int i = 0; i < nodes.getLength(); i++) {
            this.eqcArgList.add(new EcqArgs((Element)nodes.item(i), libraries, outputDir, jobPrefix + "-ecq", this.runParallel));
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

    public boolean isRunParallel() {
        return runParallel;
    }

    public void setRunParallel(boolean runParallel) {
        this.runParallel = runParallel;
    }

    public List<EcqArgs> getEqcArgList() {
        return eqcArgList;
    }

    public void setEqcArgList(List<EcqArgs> eqcArgList) {
        this.eqcArgList = eqcArgList;
    }

    @Override
    public void parse(String args) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = new HashMap<>();

        if (this.outputDir != null)
            pvp.put(params.getOutputDir(), this.outputDir.getAbsolutePath());

        pvp.put(params.getRunParallel(), Boolean.toString(this.runParallel));

        if (this.jobPrefix != null) {
            pvp.put(params.getJobPrefix(), this.jobPrefix);
        }

        return pvp;
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {

        for (Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            }

            String param = entry.getKey().getName();

            if (param.equals(this.params.getOutputDir().getName())) {
                this.outputDir = new File(entry.getValue());
            } else if (param.equals(this.params.getJobPrefix().getName())) {
                this.jobPrefix = entry.getValue();
            } else if (param.equals(this.params.getRunParallel().getName())) {
                this.runParallel = Boolean.parseBoolean(entry.getValue());
            } else {
                throw new IllegalArgumentException("Unknown param found: " + param);
            }
        }
    }

    @Override
    public List<ConanProcess> getExternalProcesses() {
        return new ArrayList<>();
    }
}
