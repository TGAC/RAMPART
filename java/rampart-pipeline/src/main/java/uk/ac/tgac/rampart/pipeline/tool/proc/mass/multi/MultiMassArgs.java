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
package uk.ac.tgac.rampart.pipeline.tool.proc.mass.multi;

import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.tgac.rampart.pipeline.tool.proc.mass.MassArgs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: maplesod
 * Date: 10/01/13
 * Time: 17:04
 */
public class MultiMassArgs extends MassArgs {

    // Need access to these
    private MultiMassParams params = new MultiMassParams();

    // Class vars
    private List<File> configs;


    public MultiMassArgs() {
        this.configs = null;
    }

    public List<File> getConfigs() {
        return configs;
    }

    public void setConfigs(List<File> configs) {
        this.configs = configs;
    }

    public void setConfigs(String configList) {

        String[] parts = configList.split(",");

        List<File> cfgs = new ArrayList<File>();

        for (String part : parts) {
            cfgs.add(new File(part.trim()));
        }

        this.configs = cfgs;
    }


    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = super.getArgMap();

        if (this.configs != null && this.configs.size() > 0)
            pvp.put(params.getConfigs(), this.getConfigs().toString());

        pvp.put(params.getParallelMass(), Boolean.toString(this.isRunParallel()));

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

            if (param.equals((this.params.getConfigs().getName()))) {
                this.setConfigs(entry.getValue());
            } else if (param.equalsIgnoreCase(this.params.getParallelMass().getName())) {
                this.setRunParallel(Boolean.parseBoolean(entry.getValue()));
            }

        }
    }
}
