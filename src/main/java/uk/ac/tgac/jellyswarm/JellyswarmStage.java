/*
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2015  Daniel Mapleson - TGAC
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
 */

package uk.ac.tgac.jellyswarm;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 12/11/13
 * Time: 09:44
 * To change this template use File | Settings | File Templates.
 */
public enum JellyswarmStage {

    COUNTER {

        @Override
        public List<ConanParameter> getParameters() {
            return new CounterProcess.Params().getConanParameters();
        }

        @Override
        public AbstractConanProcess create(ConanExecutorService ces, ProcessArgs processArgs) {
            return new CounterProcess(ces, (CounterProcess.Args)processArgs);
        }
    },
    STATS {

        @Override
        public List<ConanParameter> getParameters() {
            return new StatsProcess.Params().getConanParameters();
        }

        @Override
        public AbstractConanProcess create(ConanExecutorService ces, ProcessArgs processArgs) {
            return new StatsProcess(ces, (StatsProcess.Args)processArgs);
        }
    };

    public abstract List<ConanParameter> getParameters();

    public abstract AbstractConanProcess create(ConanExecutorService ces, ProcessArgs processArgs);

    public static String getFullListAsString() {

        List<String> stageNames = new ArrayList<>();

        for(JellyswarmStage stage : JellyswarmStage.values()) {
            stageNames.add(stage.toString());
        }

        return StringUtils.join(stageNames, ",");
    }

    public static List<JellyswarmStage> parse(String stages) {

        if (stages.trim().equalsIgnoreCase("ALL")) {
            stages = getFullListAsString();
        }

        String[] stageArray = stages.split(",");

        List<JellyswarmStage> stageList = new ArrayList<>();

        if (stageArray != null && stageArray.length != 0) {
            for(String stage : stageArray) {
                stageList.add(JellyswarmStage.valueOf(stage.trim().toUpperCase()));
            }
        }

        return stageList;
    }

    public static String toString(List<JellyswarmStage> rampartStages) {

        return StringUtils.join(rampartStages, ",");
    }
}
