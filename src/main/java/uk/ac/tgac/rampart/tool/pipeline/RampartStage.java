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
package uk.ac.tgac.rampart.tool.pipeline;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.tgac.rampart.tool.pipeline.amp.AmpParams;
import uk.ac.tgac.rampart.tool.pipeline.amp.AmpProcess;
import uk.ac.tgac.rampart.tool.process.mass.MassParams;
import uk.ac.tgac.rampart.tool.process.mass.MassProcess;
import uk.ac.tgac.rampart.tool.process.mecq.MecqParams;
import uk.ac.tgac.rampart.tool.process.mecq.MecqProcess;
import uk.ac.tgac.rampart.tool.process.report.ReportParams;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: maplesod
 * Date: 30/01/13
 * Time: 16:45
 */
public enum RampartStage {

    MECQ {
        @Override
        public String translateFilenameToKey(String filename) {
            return null;
        }

        @Override
        public List<ConanParameter> getParameters() {
            return new MecqParams().getConanParameters();
        }

        @Override
        public AbstractConanProcess create(ProcessArgs processArgs) {
            return new MecqProcess(processArgs);
        }
    },
    MASS {
        @Override
        public String translateFilenameToKey(String filename) {

            Pattern pattern = Pattern.compile("^.*k(\\d+).*$");
            Matcher matcher = pattern.matcher(filename);

            if (matcher.matches()) {

                return matcher.group(1);
            } else {
                return null;
            }
        }

        @Override
        public List<ConanParameter> getParameters() {
            return new MassParams().getConanParameters();
        }

        @Override
        public AbstractConanProcess create(ProcessArgs processArgs) {
            return new MassProcess(processArgs);
        }
    },
    AMP {
        @Override
        public String translateFilenameToKey(String filename) {

            Pattern pattern = Pattern.compile("^.*-(\\d+).*$");
            Matcher matcher = pattern.matcher(filename);

            if (matcher.matches()) {
                return matcher.group(1);
            } else {
                return null;
            }
        }

        @Override
        public List<ConanParameter> getParameters() {
            return new AmpParams().getConanParameters();
        }

        @Override
        public AbstractConanProcess create(ProcessArgs processArgs) {
            return new AmpProcess(processArgs);
        }
    },
    ANALYSE {

        @Override
        public String translateFilenameToKey(String filename) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public List<ConanParameter> getParameters() {
            return null;
        }

        @Override
        public AbstractConanProcess create(ProcessArgs processArgs) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    },
    REPORT {

        @Override
        public String translateFilenameToKey(String filename) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public List<ConanParameter> getParameters() {
            return new ReportParams().getConanParameters();
        }

        @Override
        public AbstractConanProcess create(ProcessArgs processArgs) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    };

    public abstract String translateFilenameToKey(String filename);

    public abstract List<ConanParameter> getParameters();

    public abstract AbstractConanProcess create(ProcessArgs processArgs);

    public static String getFullListAsString() {

        List<String> stageNames = new ArrayList<>();

        for(RampartStage stage : RampartStage.values()) {
            stageNames.add(stage.toString());
        }

        return StringUtils.join(stageNames, ",");
    }

    public static List<RampartStage> parse(String stages) {

        if (stages.trim().equalsIgnoreCase("ALL")) {
            stages = getFullListAsString();
        }

        String[] stageArray = stages.split(",");

        List<RampartStage> stageList = new ArrayList<>();

        if (stageArray != null && stageArray.length != 0) {
            for(String stage : stageArray) {
                stageList.add(RampartStage.valueOf(stage.trim().toUpperCase()));
            }
        }

        return stageList;
    }

    public static String toString(List<RampartStage> rampartStages) {

        return StringUtils.join(rampartStages, ",");
    }
}
