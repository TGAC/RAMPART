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
package uk.ac.tgac.rampart.conan.process.asm.abyss;

import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.tgac.rampart.conan.process.asm.AssemblerArgs;

import java.util.HashMap;
import java.util.Map;

public class AbyssV134Args extends AssemblerArgs {

    private AbyssV134Params params = new AbyssV134Params();

    private String name;

    public AbyssV134Args() {
        super();
        this.name = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();

        if (this.name != null) {
            pvp.put(params.getName(), params.getName().getName() + "=" + this.name);
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

            if (param.equals(this.params.getName().getName())) {
                this.name = entry.getValue();
            } else {
                throw new IllegalArgumentException("Unknown param found: " + param);
            }
        }
    }

    @Override
    public AssemblerArgs copy() {

        AbyssV134Args copy = new AbyssV134Args();
        copy.setName(this.getName());
        copy.setKmer(this.getKmer());
        copy.setThreads(this.getThreads());
        copy.setNbContigPairs(this.getNbContigPairs());
        copy.setLibraries(this.getLibraries());  // Not really copying this!!

        return copy;
    }

}
