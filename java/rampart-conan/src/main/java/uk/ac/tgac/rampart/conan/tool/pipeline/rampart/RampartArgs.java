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
package uk.ac.tgac.rampart.conan.tool.pipeline.rampart;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.param.ProcessArgs;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * User: maplesod
 * Date: 07/02/13
 * Time: 19:26
 */
public class RampartArgs implements ProcessArgs {

    private RampartParams params = new RampartParams();

    private File config;
    private File outputDir;

    public RampartArgs() {
    }

    public File getConfig() {
        return config;
    }

    public void setConfig(File config) {
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
            pvp.put(params.getConfig(), this.config.getAbsolutePath());

        if (this.outputDir != null)
            pvp.put(params.getOutputDir(), this.outputDir.getAbsolutePath());

        return pvp;
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {
        for (Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            }

            String param = entry.getKey().getName();

            if (param.equals(this.params.getConfig().getName())) {
                this.config = new File(entry.getValue());
            } else if (param.equals(this.params.getOutputDir().getName())) {
                this.outputDir = new File(entry.getValue());
            }

        }
    }
}
