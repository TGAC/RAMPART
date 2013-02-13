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
package uk.ac.tgac.rampart.pipeline.tool.proc.external.qt.sickle;

import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.param.ProcessParams;
import uk.ac.tgac.rampart.pipeline.tool.proc.external.qt.QualityTrimmer;
import uk.ac.tgac.rampart.pipeline.tool.proc.external.qt.QualityTrimmerArgs;

/**
 * User: maplesod
 * Date: 23/01/13
 * Time: 14:36
 */
public class SickleV11Process extends AbstractConanProcess implements QualityTrimmer {


    public enum JobType {

        SINGLE_END {
            @Override
            public ProcessParams getParameters() {
                return new SickleSeV11Params();
            }

            @Override
            public String getExe() {
                return "sickle se";
            }
        },
        PAIRED_END {
            @Override
            public ProcessParams getParameters() {
                return new SicklePeV11Params();
            }

            @Override
            public String getExe() {
                return "sickle pe";
            }
        };

        public abstract String getExe();

        public abstract ProcessParams getParameters();
    }

    private JobType jobType;

    public SickleV11Process() {
        this(JobType.PAIRED_END, new SicklePeV11Args());
    }

    public SickleV11Process(JobType jobType, QualityTrimmerArgs args) {
        super(jobType.getExe(), args, jobType.getParameters());
        this.jobType = jobType;
    }

    @Override
    public QualityTrimmerArgs getArgs() {
        return (QualityTrimmerArgs) this.getProcessArgs();
    }

    @Override
    public String getCommand() {
        return this.getCommand(this.getProcessArgs(), false);
    }

    @Override
    public String getName() {
        return "Sickle_V1.1";
    }
}
