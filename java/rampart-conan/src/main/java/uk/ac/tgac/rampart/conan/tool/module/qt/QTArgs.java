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
import uk.ac.tgac.rampart.conan.tool.process.qt.QualityTrimmer;
import uk.ac.tgac.rampart.conan.tool.process.qt.QualityTrimmerFactory;
import uk.ac.tgac.rampart.conan.tool.process.qt.sickle.SicklePeV11Args;
import uk.ac.tgac.rampart.conan.tool.process.qt.sickle.SickleV11Process;

import java.util.HashMap;
import java.util.Map;

/**
 * User: maplesod
 * Date: 16/01/13
 * Time: 13:37
 */
public class QTArgs implements ProcessArgs {

    private QTParams params = new QTParams();
    private QualityTrimmer qualityTrimmer;

    public QTArgs() {
        this.qualityTrimmer = new SickleV11Process(SickleV11Process.JobType.PAIRED_END, new SicklePeV11Args());
    }

    public QualityTrimmer getQualityTrimmer() {
        return qualityTrimmer;
    }

    public void setQualityTrimmer(QualityTrimmer qualityTrimmer) {
        this.qualityTrimmer = qualityTrimmer;
    }

    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();

        if (this.qualityTrimmer != null)
            pvp.put(params.getQualityTrimmer(), this.qualityTrimmer.getName());

        return pvp;
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {

        for(Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            };

            String param = entry.getKey().getName();

            if (param.equals(this.params.getQualityTrimmer().getName())) {
                this.qualityTrimmer = QualityTrimmerFactory.valueOf(entry.getValue()).create();
            }
            else {
                throw new IllegalArgumentException("Unknown parameter found: " + param);
            }
        }
    }
}
