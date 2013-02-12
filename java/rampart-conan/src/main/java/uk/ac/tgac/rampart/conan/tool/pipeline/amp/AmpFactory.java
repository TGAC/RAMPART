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
package uk.ac.tgac.rampart.conan.tool.pipeline.amp;

import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.tgac.rampart.conan.tool.proc.external.degap.DegapperFactory;
import uk.ac.tgac.rampart.conan.tool.proc.external.scaffold.ScaffolderFactory;
import uk.ac.tgac.rampart.conan.tool.proc.internal.clip.ClipperFactory;
import uk.ac.tgac.rampart.conan.tool.proc.internal.clip.internal.RampartClipperProcess;
import uk.ac.tgac.rampart.conan.tool.proc.internal.dedup.Deduplicator;
import uk.ac.tgac.rampart.conan.tool.proc.internal.dedup.DeduplicatorFactory;

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
        public AmpStage create() {
            return ClipperFactory.createClipper();
        }

        @Override
        public AmpStage create(String ampTaskName) {
            return ClipperFactory.createClipper(ampTaskName);
        }
    },
    DEDUPLICATE {
        @Override
        public AmpStage create() {
            return DeduplicatorFactory.createDeduplicator();
        }

        @Override
        public AmpStage create(String ampTaskName) {
            return DeduplicatorFactory.createDeduplicator(ampTaskName);
        }
    },
    DEGAP {
        @Override
        public AmpStage create() {
            return DegapperFactory.createDegapper();
        }

        @Override
        public AmpStage create(String ampTaskName) {
            return DegapperFactory.createDegapper(ampTaskName);
        }
    },
    SCAFFOLD {
        @Override
        public AmpStage create() {
            return ScaffolderFactory.createScaffolder();
        }

        @Override
        public AmpStage create(String ampTaskName) {
            return ScaffolderFactory.createScaffolder(ampTaskName);
        }
    };

    public abstract AmpStage create();

    public abstract AmpStage create(String ampTaskName);


    public static AmpStage createAmpTask(String taskType) {
        return AmpFactory.valueOf(taskType.toUpperCase()).create();
    }

    public static AmpStage createAmpTask(String taskType, String taskName) {
        return AmpFactory.valueOf(taskType.toUpperCase()).create(taskName);
    }


    public static List<AmpStage> createFromList(String[] tasks) {

        List<AmpStage> stages = new ArrayList<AmpStage>();

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

    public static List<ConanProcess> createDefaultList() {
        return new ArrayList<ConanProcess>(Arrays.asList(
                new ConanProcess[]{
                        AmpFactory.SCAFFOLD.create(),
                        AmpFactory.DEGAP.create()
                }));
    }
}
