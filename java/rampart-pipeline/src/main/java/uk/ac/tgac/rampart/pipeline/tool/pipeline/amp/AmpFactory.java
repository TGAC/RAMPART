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
package uk.ac.tgac.rampart.pipeline.tool.pipeline.amp;

import uk.ac.tgac.rampart.conan.process.SimpleIOProcess;
import uk.ac.tgac.rampart.conan.process.clip.ClipperFactory;
import uk.ac.tgac.rampart.conan.process.dedup.DeduplicatorFactory;
import uk.ac.tgac.rampart.conan.process.degap.DegapperFactory;
import uk.ac.tgac.rampart.conan.process.scaffold.ScaffolderFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: maplesod
 * Date: 31/01/13
 * Time: 13:48
 */
public enum AmpFactory {

    CLIP {
        @Override
        public SimpleIOProcess create() {
            return ClipperFactory.createClipper();
        }

        @Override
        public SimpleIOProcess create(String ampTaskName) {
            return ClipperFactory.createClipper(ampTaskName);
        }
    },
    DEDUPLICATE {
        @Override
        public SimpleIOProcess create() {
            return DeduplicatorFactory.createDeduplicator();
        }

        @Override
        public SimpleIOProcess create(String ampTaskName) {
            return DeduplicatorFactory.createDeduplicator(ampTaskName);
        }
    },
    DEGAP {
        @Override
        public SimpleIOProcess create() {
            return DegapperFactory.createDegapper();
        }

        @Override
        public SimpleIOProcess create(String ampTaskName) {
            return DegapperFactory.createDegapper(ampTaskName);
        }
    },
    SCAFFOLD {
        @Override
        public SimpleIOProcess create() {
            return ScaffolderFactory.createScaffolder();
        }

        @Override
        public SimpleIOProcess create(String ampTaskName) {
            return ScaffolderFactory.createScaffolder(ampTaskName);
        }
    };

    public abstract SimpleIOProcess create();

    public abstract SimpleIOProcess create(String ampTaskName);


    public static SimpleIOProcess createAmpTask(String taskType) {
        return AmpFactory.valueOf(taskType.toUpperCase()).create();
    }

    public static SimpleIOProcess createAmpTask(String taskType, String taskName) {
        return AmpFactory.valueOf(taskType.toUpperCase()).create(taskName);
    }


    public static List<SimpleIOProcess> createFromList(String[] tasks) {

        List<SimpleIOProcess> stages = new ArrayList<SimpleIOProcess>();

        for (String task : tasks) {
            if (task.contains(" ")) {
                String[] parts = task.split(" ");
                String taskType = parts[0];
                String taskName = parts[1];

                stages.add(AmpFactory.createAmpTask(taskType, taskName));
            } else {
                stages.add(AmpFactory.createAmpTask(task));
            }
        }

        return stages;
    }

    public static List<SimpleIOProcess> createDefaultList() {
        return new ArrayList<SimpleIOProcess>(Arrays.asList(
                new SimpleIOProcess[]{
                        AmpFactory.SCAFFOLD.create(),
                        AmpFactory.DEGAP.create()
                }));
    }
}
