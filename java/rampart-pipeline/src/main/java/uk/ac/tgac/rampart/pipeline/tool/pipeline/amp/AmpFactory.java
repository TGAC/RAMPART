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

import uk.ac.tgac.rampart.conan.process.AbstractAmpArgs;
import uk.ac.tgac.rampart.conan.process.AbstractAmpProcess;
import uk.ac.tgac.rampart.conan.process.clip.AbstractClipperArgs;
import uk.ac.tgac.rampart.conan.process.clip.ClipperFactory;
import uk.ac.tgac.rampart.conan.process.dedup.AbstractDeduplicatorArgs;
import uk.ac.tgac.rampart.conan.process.dedup.DeduplicatorFactory;
import uk.ac.tgac.rampart.conan.process.degap.AbstractDegapperArgs;
import uk.ac.tgac.rampart.conan.process.degap.DegapperFactory;
import uk.ac.tgac.rampart.conan.process.scaffold.AbstractScaffolderArgs;
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
        public AbstractAmpProcess create(AbstractAmpArgs abstractAmpArgs) {
            return ClipperFactory.createClipper((AbstractClipperArgs)abstractAmpArgs);
        }

        @Override
        public AbstractAmpProcess create(String ampTaskName) {
            return ClipperFactory.createClipper(ampTaskName);
        }
    },
    DEDUPLICATE {
        @Override
        public AbstractAmpProcess create(AbstractAmpArgs abstractAmpArgs) {
            return DeduplicatorFactory.createDeduplicator((AbstractDeduplicatorArgs)abstractAmpArgs);
        }

        @Override
        public AbstractAmpProcess create(String ampTaskName) {
            return DeduplicatorFactory.createDeduplicator(ampTaskName);
        }
    },
    DEGAP {
        @Override
        public AbstractAmpProcess create(AbstractAmpArgs abstractAmpArgs) {
            return DegapperFactory.createDegapper((AbstractDegapperArgs)abstractAmpArgs);
        }

        @Override
        public AbstractAmpProcess create(String ampTaskName) {
            return DegapperFactory.createDegapper(ampTaskName);
        }
    },
    SCAFFOLD {
        @Override
        public AbstractAmpProcess create(AbstractAmpArgs abstractAmpArgs) {
            return ScaffolderFactory.createScaffolder((AbstractScaffolderArgs)abstractAmpArgs);
        }

        @Override
        public AbstractAmpProcess create(String ampTaskName) {
            return ScaffolderFactory.createScaffolder(ampTaskName);
        }
    };

    public abstract AbstractAmpProcess create(AbstractAmpArgs abstractAmpArgs);

    public abstract AbstractAmpProcess create(String ampTaskName);


    public static AbstractAmpProcess createAmpTask(String taskType) {
        return AmpFactory.valueOf(taskType.toUpperCase())..create();
    }

    public static AbstractAmpProcess createAmpTask(String taskType, String taskName) {
        return AmpFactory.valueOf(taskType.toUpperCase()).create(taskName);
    }

    public static AbstractAmpProcess createFromString(String taskString) {

        if (taskString.contains(" ")) {
            String[] parts = taskString.split(" ");
            String taskType = parts[0];
            String taskName = parts[1];

            return AmpFactory.createAmpTask(taskType, taskName);
        } else {
            return AmpFactory.createAmpTask(taskString);
        }
    }

    public static List<AbstractAmpProcess> createFromList(String taskString) {

        String[] tasks = taskString.split(",");

        return createFromList(tasks);
    }

    public static List<AbstractAmpProcess> createFromList(String[] tasks) {

        List<AbstractAmpProcess> stages = new ArrayList<AbstractAmpProcess>();

        for (String task : tasks) {
            stages.add(createFromString(task));
        }

        return stages;
    }

}
