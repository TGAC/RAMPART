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

package uk.ac.tgac.citadel;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.tgac.citadel.stage.*;
import uk.ac.tgac.jellyswarm.CounterProcess;
import uk.ac.tgac.jellyswarm.StatsProcess;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 12/11/13
 * Time: 09:44
 * To change this template use File | Settings | File Templates.
 */
public enum CitadelStage {

    DECOMPRESS {

        @Override
        public List<ConanParameter> getParameters() {
            return new Decompress.Params().getConanParameters();
        }

        @Override
        public AbstractConanProcess create(ConanExecutorService ces, ProcessArgs processArgs) {
            return new Decompress(ces, (Decompress.Args)processArgs);
        }
    },
    JELLYSWARM {

        @Override
        public List<ConanParameter> getParameters() {
            return new Jellyswarm.Params().getConanParameters();
        }

        @Override
        public AbstractConanProcess create(ConanExecutorService ces, ProcessArgs processArgs) {
            return new Jellyswarm(ces, (Jellyswarm.Args)processArgs);
        }
    },
    RAMPART {

        @Override
        public List<ConanParameter> getParameters() {
            return new Rampart.Params().getConanParameters();
        }

        @Override
        public AbstractConanProcess create(ConanExecutorService ces, ProcessArgs processArgs) {
            return new Rampart(ces, (Rampart.Args)processArgs);
        }
    },
    ASSEMBLE {

        @Override
        public List<ConanParameter> getParameters() {
            return new Assemble.Params().getConanParameters();
        }

        @Override
        public AbstractConanProcess create(ConanExecutorService ces, ProcessArgs processArgs) {
            return new Assemble(ces, (Assemble.Args)processArgs);
        }
    },
    ANNOTATE {

        @Override
        public List<ConanParameter> getParameters() {
            return new Annotate.Params().getConanParameters();
        }

        @Override
        public AbstractConanProcess create(ConanExecutorService ces, ProcessArgs processArgs) {
            return new Annotate(ces, (Annotate.Args)processArgs);
        }
    },
    PACKAGE {

        @Override
        public List<ConanParameter> getParameters() {
            return new Annotate.Params().getConanParameters();
        }

        @Override
        public AbstractConanProcess create(ConanExecutorService ces, ProcessArgs processArgs) {
            return new Annotate(ces, (Annotate.Args)processArgs);
        }
    },

    ;

    public abstract List<ConanParameter> getParameters();

    public abstract AbstractConanProcess create(ConanExecutorService ces, ProcessArgs processArgs);

    public static String getFullListAsString() {

        List<String> stageNames = new ArrayList<>();

        for(CitadelStage stage : CitadelStage.values()) {
            stageNames.add(stage.toString());
        }

        return StringUtils.join(stageNames, ",");
    }

    public static List<CitadelStage> parse(String stages) {

        if (stages.trim().equalsIgnoreCase("ALL")) {
            stages = getFullListAsString();
        }

        String[] stageArray = stages.split(",");

        List<CitadelStage> stageList = new ArrayList<>();

        if (stageArray != null && stageArray.length != 0) {
            for(String stage : stageArray) {
                stageList.add(CitadelStage.valueOf(stage.trim().toUpperCase()));
            }
        }

        return stageList;
    }

    public static String toString(List<CitadelStage> rampartStages) {

        return StringUtils.join(rampartStages, ",");
    }
}
