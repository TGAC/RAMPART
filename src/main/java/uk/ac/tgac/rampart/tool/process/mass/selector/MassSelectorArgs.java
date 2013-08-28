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
package uk.ac.tgac.rampart.tool.process.mass.selector;

import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.rampart.tool.process.mass.selector.stats.AssemblyStatsTable;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User: maplesod
 * Date: 11/02/13
 * Time: 15:55
 */
public class MassSelectorArgs implements ProcessArgs {

    // Need access to these
    private MassSelectorParams params = new MassSelectorParams();

    private List<File> statsFiles;
    private List<File> configs;
    private File outputDir;
    private Organism organism;
    private File weightings;
    private AssemblyStatsTable mergedTable;
    private File mergedFile;

    public MassSelectorArgs() {
        this.statsFiles = new ArrayList<File>();
        this.configs = new ArrayList<File>();
        this.outputDir = new File("");
        this.organism = null;
        this.weightings = null;
        this.mergedTable = null;
        this.mergedFile = null;
    }

    public List<File> getStatsFiles() {
        return statsFiles;
    }

    public void setStatsFiles(List<File> statsFiles) {
        this.statsFiles = statsFiles;
    }

    public List<File> getConfigs() {
        return configs;
    }

    public void setConfigs(List<File> configs) {
        this.configs = configs;
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

    public long getEstimatedGenomeSize() {
        return this.organism != null ? this.organism.getEstGenomeSize() : 0;
    }

    public double getEstimatedGcPercentage() {
        return this.organism != null ? this.organism.getEstGcPercentage() : 0.0;
    }

    public File getWeightings() {
        return weightings;
    }

    public void setWeightings(File weightings) {
        this.weightings = weightings;
    }

    public File getMergedFile() {
        return mergedFile;
    }

    public void setMergedFile(File mergedFile) {
        this.mergedFile = mergedFile;
    }

    @Override
    public void parse(String args) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = new LinkedHashMap<ConanParameter, String>();

        if (this.statsFiles != null && this.statsFiles.size() > 0) {
            pvp.put(params.getStatsFiles(), this.getStatsFiles().toString());
        }

        if (this.configs != null && this.configs.size() > 0) {
            pvp.put(params.getConfigFiles(), this.getConfigs().toString());
        }

        if (this.outputDir != null) {
            pvp.put(params.getOutputDir(), this.getOutputDir().getAbsolutePath());
        }

        /*if (this.organism != null) {
            pvp.put(params.getOrganism(), Long.toString(this.approxGenomeSize));
        }            */

        return pvp;
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {

        for (Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            }

            String param = entry.getKey().getName();

            if (param.equals(this.params.getStatsFiles().getName())) {
                this.statsFiles = createFileList(entry.getValue());
            } else if (param.equals(this.params.getConfigFiles().getName())) {
                this.configs = createFileList(entry.getValue());
            } else if (param.equals(this.params.getOutputDir().getName())) {
                this.outputDir = new File(entry.getValue());
            } /*else if (param.equals(this.params.getApproxGenomeSize().getName())) {
                this.approxGenomeSize = Long.parseLong(entry.getValue());
            } */else if (param.equals(this.params.getWeightings().getName())) {
                //TODO This still needs parsing!
                this.weightings = null;
            }
        }
    }

    private List<File> createFileList(String value) {

        List<File> fileList = new ArrayList<File>();

        String[] files = value.split(",");

        for(String filePath : files) {
            fileList.add(new File(filePath.trim()));
        }

        return fileList;
    }

    public void setMergedTable(AssemblyStatsTable mergedTable) {
        this.mergedTable = mergedTable;
    }

    public AssemblyStatsTable getMergedTable() {
        return mergedTable;
    }
}
