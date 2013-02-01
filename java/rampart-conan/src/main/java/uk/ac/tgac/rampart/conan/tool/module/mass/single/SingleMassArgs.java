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
package uk.ac.tgac.rampart.conan.tool.module.mass.single;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.tool.module.mass.MassArgs;
import uk.ac.tgac.rampart.conan.tool.module.util.RampartConfig;

import java.io.File;
import java.util.Map;

/**
 * User: maplesod
 * Date: 10/01/13
 * Time: 17:04
 */
public class SingleMassArgs extends MassArgs {

    // Need access to these
    private SingleMassParams params = new SingleMassParams();

    // Class vars
    private RampartConfig config;
    private File outputDir;
    private String jobPrefix;


    public SingleMassArgs() {
        this.config = null;
        this.outputDir = null;
        this.jobPrefix = "";
    }

    public RampartConfig getConfig() {
        return config;
    }

    public void setConfig(RampartConfig config) {
        this.config = config;
    }

    public void setConfig(File configFile) {
        this.config = new RampartConfig(configFile);
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

    public File getUnitigsDir() {
        return new File(this.outputDir, "unitigs");
    }

    public File getContigsDir() {
        return new File(this.outputDir, "contigs");
    }

    public File getScaffoldsDir() {
        return new File(this.outputDir, "scaffolds");
    }

    public File getLogsDir() {
        return new File(this.outputDir, "logs");
    }

    public File getStatsFile() {
        return new File(this.getContigsDir(), "stats.txt");
    }

    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = super.getArgMap();

        if (this.config != null)
            pvp.put(params.getConfig(), this.config.getConfigFile().getAbsolutePath());

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

            if (param.equals(this.params.getJobPrefix().getName())) {
                this.jobPrefix = entry.getValue();
            }
            else if (param.equals(this.params.getOutputDir())) {
                this.outputDir = new File(entry.getValue());
            }
            else if (param.equals(this.params.getConfig().getName())) {
                this.config = new RampartConfig(new File(entry.getValue()));
            }
        }
    }
}
