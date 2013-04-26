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
package uk.ac.tgac.rampart.tool.process.mass.single;

import org.ini4j.Profile;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.tgac.rampart.data.RampartConfiguration;
import uk.ac.tgac.rampart.tool.process.mass.MassArgs;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * User: maplesod
 * Date: 10/01/13
 * Time: 17:04
 */
public class SingleMassArgs extends MassArgs {

    private static final String MASS_JOB_NAME = "job";

    // Need access to these
    private SingleMassParams params = new SingleMassParams();

    // Class vars
    private File config;
    private String jobName;

    public SingleMassArgs() {
        this.config = null;
        this.jobName = "raw";
    }

    public File getConfig() {
        return config;
    }

    public void setConfig(File config) {
        this.config = config;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public File getUnitigsDir() {
        return new File(this.getOutputDir(), "unitigs");
    }

    public File getContigsDir() {
        return new File(this.getOutputDir(), "contigs");
    }

    public File getScaffoldsDir() {
        return new File(this.getOutputDir(), "scaffolds");
    }

    public File getLogsDir() {
        return new File(this.getOutputDir(), "logs");
    }

    public File getStatsFile() {

        File outputLevelStatsDir = null;

        if (this.getOutputLevel() == OutputLevel.UNITIGS) {
            outputLevelStatsDir = this.getUnitigsDir();
        }
        else if (this.getOutputLevel() == OutputLevel.CONTIGS) {
            outputLevelStatsDir = this.getContigsDir();
        }
        else if (this.getOutputLevel() == OutputLevel.SCAFFOLDS) {
            outputLevelStatsDir = this.getContigsDir();
        }
        else {
            throw new IllegalArgumentException("Output Level not specified");
        }

        return new File(outputLevelStatsDir, "stats.txt");
    }

    @Override
    public void parseConfig(File config) throws IOException {

        super.parseConfig(config);

        RampartConfiguration rampartConfig = new RampartConfiguration();

        rampartConfig.load(config);
        this.setLibs(rampartConfig.getLibs());
        Profile.Section section = rampartConfig.getMassSettings();

        if (section != null) {
            for (Map.Entry<String, String> entry : section.entrySet()) {

                if (entry.getKey().equalsIgnoreCase(MASS_JOB_NAME)) {
                    this.jobName = entry.getValue().trim();
                }
            }
        }
    }


    @Override
    public void parse(String args) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = super.getArgMap();

        if (this.config != null)
            pvp.put(params.getConfig(), this.config.getAbsolutePath());

        if (this.jobName != null && !this.jobName.isEmpty())
            pvp.put(params.getJobName(), this.jobName);

        return pvp;
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {

        super.setFromArgMap(pvp);

        for (Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            }

            String param = entry.getKey().getName();

            if (param.equals(this.params.getConfig().getName())) {
                this.config = new File(entry.getValue());
            } else if (param.equals(this.params.getJobName().getName())) {
                this.jobName = entry.getValue().trim();
            }
        }
    }
}
