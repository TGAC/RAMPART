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
package uk.ac.tgac.rampart.pipeline.tool.process.mass.single;

import org.ini4j.Profile;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.tgac.rampart.core.data.RampartConfiguration;
import uk.ac.tgac.rampart.pipeline.tool.process.mass.MassArgs;
import uk.ac.tgac.rampart.pipeline.tool.process.mass.StepSize;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * User: maplesod
 * Date: 10/01/13
 * Time: 17:04
 */
public class SingleMassArgs extends MassArgs {

    public static final String MASS_TOOL = "tool";
    public static final String MASS_KMIN = "kmin";
    public static final String MASS_KMAX = "kmax";
    public static final String MASS_STEP = "step";

    // Need access to these
    private SingleMassParams params = new SingleMassParams();

    // Class vars
    private File config;


    public SingleMassArgs() {
        this.config = null;
    }

    public File getConfig() {
        return config;
    }

    public void setConfig(File config) {
        this.config = config;
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
        return new File(this.getContigsDir(), "stats.txt");
    }

    public static SingleMassArgs parseConfig(File config) throws IOException {

        RampartConfiguration rampartConfig = new RampartConfiguration();

        SingleMassArgs args = new SingleMassArgs();

        rampartConfig.load(config);
        args.setLibs(rampartConfig.getLibs());
        Profile.Section section = rampartConfig.getMassSettings();

        if (section != null) {
            for (Map.Entry<String, String> entry : section.entrySet()) {

                if (entry.getKey().equalsIgnoreCase(MASS_TOOL)) {
                    args.setAssembler(entry.getValue());
                } else if (entry.getKey().equalsIgnoreCase(MASS_KMIN)) {
                    args.setKmin(Integer.parseInt(entry.getValue()));
                } else if (entry.getKey().equalsIgnoreCase(MASS_KMAX)) {
                    args.setKmax(Integer.parseInt(entry.getValue()));
                } else if (entry.getKey().equalsIgnoreCase(MASS_STEP)) {
                    args.setStepSize(StepSize.valueOf(entry.getValue().toUpperCase()));
                }
            }
        }

        return args;
    }

    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = super.getArgMap();

        if (this.config != null)
            pvp.put(params.getConfig(), this.config.getAbsolutePath());

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
            }
        }
    }
}
