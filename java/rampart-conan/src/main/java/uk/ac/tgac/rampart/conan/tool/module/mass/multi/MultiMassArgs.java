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
package uk.ac.tgac.rampart.conan.tool.module.mass.multi;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.process.ProcessArgs;
import uk.ac.tgac.rampart.conan.tool.module.mass.MassArgs;
import uk.ac.tgac.rampart.conan.tool.module.mass.StepSize;
import uk.ac.tgac.rampart.conan.tool.module.mass.single.SingleMassParams;
import uk.ac.tgac.rampart.conan.tool.module.util.RampartConfig;
import uk.ac.tgac.rampart.conan.tool.process.asm.Assembler;
import uk.ac.tgac.rampart.conan.tool.process.asm.AssemblerFactory;
import uk.ac.tgac.rampart.core.data.Library;

import java.io.File;
import java.util.*;

/**
 * User: maplesod
 * Date: 10/01/13
 * Time: 17:04
 */
public class MultiMassArgs extends MassArgs {

    // Need access to these
    private MultiMassParams params = new MultiMassParams();

    // Class vars
    private List<RampartConfig> configs;
    private String jobPrefix;
    private File outputDir;

    public MultiMassArgs() {
        this.configs = new ArrayList<RampartConfig>();
        this.jobPrefix = "";
        this.outputDir = null;
    }

    public List<RampartConfig> getConfigs() {
        return configs;
    }

    public void setConfigs(List<RampartConfig> configs) {
        this.configs = configs;
    }

    public void setConfigs(String configList) {
        this.configs = RampartConfig.parseList(configList);
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


    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = super.getArgMap();

        if (this.configs!= null && this.configs.size() > 0)
            pvp.put(params.getConfigs(), this.getConfigs().toString());

        if (this.outputDir != null)
            pvp.put(params.getOutputDir(), this.outputDir.getAbsolutePath());

        if (this.jobPrefix != null)
            pvp.put(params.getJobPrefix(), this.jobPrefix);


        return pvp;
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {

        super.setFromArgMap(pvp);

        for(Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            };

            String param = entry.getKey().getName();

            if (param.equals((this.params.getConfigs().getName()))) {
                this.configs = RampartConfig.parseList(entry.getValue());
            }
            else if (param.equals(this.params.getJobPrefix().getName())) {
                this.jobPrefix = entry.getValue();
            }
            else if (param.equals(this.params.getOutputDir())) {
                this.outputDir = new File(entry.getValue());
            }
        }
    }
}
