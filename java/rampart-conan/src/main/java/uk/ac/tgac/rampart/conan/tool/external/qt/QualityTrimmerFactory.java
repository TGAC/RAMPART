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
package uk.ac.tgac.rampart.conan.tool.external.qt;

import uk.ac.tgac.rampart.conan.tool.external.qt.sickle.SicklePeV11Args;
import uk.ac.tgac.rampart.conan.tool.external.qt.sickle.SickleSeV11Args;
import uk.ac.tgac.rampart.conan.tool.external.qt.sickle.SickleV11Process;
import uk.ac.tgac.rampart.conan.tool.external.scaffold.Scaffolder;

/**
 * User: maplesod
 * Date: 30/01/13
 * Time: 18:42
 */
public enum QualityTrimmerFactory {

    SICKLE_SE_V1_1 {
        @Override
        public QualityTrimmer create() {
            return new SickleV11Process(SickleV11Process.JobType.SINGLE_END, new SickleSeV11Args());
        }
    },
    SICKLE_PE_V1_1 {
        @Override
        public QualityTrimmer create() {
            return new SickleV11Process(SickleV11Process.JobType.PAIRED_END, new SicklePeV11Args());
        }
    };

    public abstract QualityTrimmer create();

    public static QualityTrimmer createQualityTrimmer() {
        return SICKLE_PE_V1_1.create();
    }

    public static QualityTrimmer createQualityTrimmer(String name) {
        return QualityTrimmerFactory.valueOf(name.toUpperCase()).create();
    }
}
