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
package uk.ac.tgac.rampart.pipeline.tool.proc.internal.mass.stats;

import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.tgac.rampart.core.data.AssemblyStatsMatrixRow;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * User: maplesod
 * Date: 11/02/13
 * Time: 15:55
 */
public class MassSelectorArgs implements ProcessArgs {

    private List<File> statsFiles;
    private List<File> configs;
    private File outputDir;
    private long approxGenomeSize;
    private AssemblyStatsMatrixRow weightings;

    public MassSelectorArgs() {
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

    public long getApproxGenomeSize() {
        return approxGenomeSize;
    }

    public void setApproxGenomeSize(long approxGenomeSize) {
        this.approxGenomeSize = approxGenomeSize;
    }

    public AssemblyStatsMatrixRow getWeightings() {
        return weightings;
    }

    public void setWeightings(AssemblyStatsMatrixRow weightings) {
        this.weightings = weightings;
    }


    @Override
    public Map<ConanParameter, String> getArgMap() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
