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
package uk.ac.tgac.rampart.pipeline.tool.pipeline.rampart;

import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.tgac.rampart.pipeline.tool.pipeline.amp.AmpProcess;
import uk.ac.tgac.rampart.pipeline.tool.process.analyser.length.LengthAnalysisProcess;
import uk.ac.tgac.rampart.pipeline.tool.process.mass.multi.MultiMassProcess;
import uk.ac.tgac.rampart.pipeline.tool.process.qt.QTProcess;
import uk.ac.tgac.rampart.pipeline.tool.process.report.ReportProcess;

import java.util.ArrayList;
import java.util.List;

/**
 * User: maplesod
 * Date: 12/02/13
 * Time: 16:24
 */
public enum RampartFactory {

    QT {
        @Override
        public ConanProcess create() {
            return new QTProcess();
        }

    },
    MASS {
        @Override
        public ConanProcess create() {
            return new MultiMassProcess();
        }

    },
    AMP {
        @Override
        public ConanProcess create() {
            return new AmpProcess();
        }

    },
    ANALYSE {
        @Override
        public ConanProcess create() {
            return new LengthAnalysisProcess();
        }

    },
    REPORT {
        @Override
        public ConanProcess create() {
            return new ReportProcess();
        }

    };

    public abstract ConanProcess create();


    public static List<ConanProcess> createFrom(RampartFactory startStage) {

        List<ConanProcess> list = new ArrayList<ConanProcess>();
        boolean add = false;
        for(RampartFactory stage : RampartFactory.values()) {

            if (startStage == stage) {
                add = true;
            }

            if (add) {
                list.add(stage.create());
            }
        }

        return list;
    }

    public static List<ConanProcess> createAfterAndIncluding(RampartFactory startStage) {

        List<ConanProcess> list = new ArrayList<ConanProcess>();
        boolean add = true;
        for(RampartFactory stage : RampartFactory.values()) {

            if (add) {
                list.add(stage.create());
            }

            if (startStage == stage) {
                break;
            }
        }

        return list;
    }

    public static List<ConanProcess> createAll() {

        List<ConanProcess> list = new ArrayList<ConanProcess>();
        for(RampartFactory stage : RampartFactory.values()) {
             list.add(stage.create());
        }

        return list;
    }
}
