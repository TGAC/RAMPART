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
package uk.ac.tgac.rampart.conan.tool.proc.amp;

import uk.ac.tgac.rampart.conan.tool.task.internal.clip.internal.RampartClipperProcess;
import uk.ac.tgac.rampart.conan.tool.task.internal.dedup.Deduplicator;
import uk.ac.tgac.rampart.conan.tool.task.external.degap.DegapperFactory;
import uk.ac.tgac.rampart.conan.tool.task.external.scaffold.ScaffolderFactory;

/**
 * User: maplesod
 * Date: 31/01/13
 * Time: 13:48
 */
public enum AmpFactory {

    CLIP {
        @Override
        public AmpTask create() {
            return new RampartClipperProcess();
        }

        @Override
        public AmpTask create(String ampTaskName) {
            return new RampartClipperProcess();
        }
    },
    DEDUPLICATE {
        @Override
        public AmpTask create() {
            return new Deduplicator();
        }

        @Override
        public AmpTask create(String ampTaskName) {
            return new Deduplicator();
        }
    },
    DEGAP {
        @Override
        public AmpTask create() {
            return DegapperFactory.createDegapper();
        }

        @Override
        public AmpTask create(String ampTaskName) {
            return DegapperFactory.createDegapper(ampTaskName);
        }
    },
    SCAFFOLD {
        @Override
        public AmpTask create() {
            return ScaffolderFactory.createScaffolder();
        }

        @Override
        public AmpTask create(String ampTaskName) {
            return ScaffolderFactory.createScaffolder(ampTaskName);
        }
    };

    public abstract AmpTask create();
    public abstract AmpTask create(String ampTaskName);


    public static AmpTask createAmpTask(String taskType) {
        return AmpFactory.valueOf(taskType.toUpperCase()).create();
    }

    public static AmpTask createAmpTask(String taskType, String taskName) {
        return AmpFactory.valueOf(taskType.toUpperCase()).create(taskName);
    }
}
