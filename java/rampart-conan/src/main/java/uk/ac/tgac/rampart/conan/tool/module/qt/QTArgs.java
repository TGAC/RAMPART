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
package uk.ac.tgac.rampart.conan.tool.module.qt;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.process.ProcessArgs;
import uk.ac.tgac.rampart.conan.tool.module.util.RampartConfig;
import uk.ac.tgac.rampart.conan.tool.process.qt.QualityTrimmer;
import uk.ac.tgac.rampart.conan.tool.process.qt.QualityTrimmerArgs;
import uk.ac.tgac.rampart.conan.tool.process.qt.QualityTrimmerFactory;
import uk.ac.tgac.rampart.conan.tool.process.qt.sickle.SicklePeV11Args;
import uk.ac.tgac.rampart.conan.tool.process.qt.sickle.SickleV11Process;
import uk.ac.tgac.rampart.core.data.Library;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: maplesod
 * Date: 16/01/13
 * Time: 13:37
 */
public class QTArgs implements ProcessArgs {

    private QTParams params = new QTParams();

    private RampartConfig config;
    private File outputDir;

    public QTArgs() {
        this.config = null;
        this.outputDir = null;
    }

    public RampartConfig getConfig() {
        return config;
    }

    public void setConfig(RampartConfig config) {
        this.config = config;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }


    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();

        if (this.config != null)
            pvp.put(params.getRampartConfig(), this.config.getConfigFile().getAbsolutePath());

        if (this.outputDir != null)
            pvp.put(params.getOutputDir(), this.outputDir.getAbsolutePath());

        return pvp;
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {

        for(Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            }

            String param = entry.getKey().getName();

            if (param.equals(this.params.getRampartConfig().getName())) {
                this.config = new RampartConfig(new File(entry.getValue()));
            }
            else if (param.equals(this.params.getOutputDir().getName())) {
                this.outputDir = new File(entry.getValue());
            }
            else {
                throw new IllegalArgumentException("Unknown parameter found: " + param);
            }
        }
    }
}
