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
import uk.ac.tgac.rampart.tool.pipeline.amp.AmpArgs;
import uk.ac.tgac.rampart.tool.pipeline.amp.AmpParams;
import uk.ac.tgac.rampart.tool.pipeline.amp.AmpProcess;
import uk.ac.tgac.rampart.tool.process.finalise.FinaliseArgs;
import uk.ac.tgac.rampart.tool.process.finalise.FinaliseParams;
import uk.ac.tgac.rampart.tool.process.finalise.FinaliseProcess;
import uk.ac.tgac.rampart.tool.process.kmercount.reads.KmerCountReadsArgs;
import uk.ac.tgac.rampart.tool.process.kmercount.reads.KmerCountReadsParams;
import uk.ac.tgac.rampart.tool.process.kmercount.reads.KmerCountReadsProcess;
import uk.ac.tgac.rampart.tool.process.mass.MassArgs;
import uk.ac.tgac.rampart.tool.process.mass.MassParams;
import uk.ac.tgac.rampart.tool.process.mass.MassProcess;
import uk.ac.tgac.rampart.tool.process.mecq.MecqArgs;
import uk.ac.tgac.rampart.tool.process.mecq.MecqParams;
import uk.ac.tgac.rampart.tool.process.mecq.MecqProcess;
import uk.ac.tgac.rampart.tool.process.report.ReportParams;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This enum describes each possible stage within the RAMPART pipeline.  It is important that each stage is specified in
 * the order that it should be executed within the pipeline.
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
        public boolean checkArgs(RampartStageArgs args) {
            return classContains(args.getClass(), MecqArgs.class);
        }

        @Override
        public AbstractConanProcess create() {
            return new MecqProcess(this.getArgs());
        }

    },
    KMER_READS {
        @Override
        public String translateFilenameToKey(String filename) {
            return null;
        }

        @Override
        public List<ConanParameter> getParameters() {
            return new KmerCountReadsParams().getConanParameters();
        }

        @Override
        public boolean checkArgs(RampartStageArgs args) {
            return classContains(args.getClass(), KmerCountReadsArgs.class);
        }

        @Override
        public AbstractConanProcess create() {
            return new KmerCountReadsProcess(this.getArgs());
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
        public boolean checkArgs(RampartStageArgs args) {
            return classContains(args.getClass(), MassArgs.class);
        }

        @Override
        public AbstractConanProcess create() {
            return new MassProcess(this.getArgs());
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
        public boolean checkArgs(RampartStageArgs args) {
            return classContains(args.getClass(), AmpArgs.class);
        }

        @Override
        public AbstractConanProcess create() {
            return new AmpProcess(this.getArgs());
        }
    },
    /*ANALYSE {

        @Override
        public String translateFilenameToKey(String filename) {
            return null;
        }

        @Override
        public List<ConanParameter> getParameters() {
            return null;
        }

        @Override
        public boolean checkArgs(RampartStageArgs args) {
            return false;
        }

        @Override
        public AbstractConanProcess create() {
            return null;
        }
    },
    REPORT {

        @Override
        public String translateFilenameToKey(String filename) {
            return null;
        }

        @Override
        public List<ConanParameter> getParameters() {
            return new ReportParams().getConanParameters();
        }

        @Override
        public boolean checkArgs(RampartStageArgs args) {
            return false;
        }

        @Override
        public AbstractConanProcess create() {
            return null;
        }
    },*/
    FINALISE {
        @Override
        public String translateFilenameToKey(String filename) {
            return null;
        }

        @Override
        public List<ConanParameter> getParameters() {
            return new FinaliseParams().getConanParameters();
        }

        @Override
        public boolean checkArgs(RampartStageArgs args) {
            return classContains(args.getClass(), FinaliseArgs.class);
        }

        @Override
        public AbstractConanProcess create() {
            return new FinaliseProcess(this.getArgs());
        }
    };

    public abstract String translateFilenameToKey(String filename);

    public abstract List<ConanParameter> getParameters();

    public abstract boolean checkArgs(RampartStageArgs args);

    public abstract AbstractConanProcess create();


    private RampartStageArgs args;



    protected static boolean classContains(Class query, Class target) {

        return query.getSimpleName().equals(target.getSimpleName());
    }

    public RampartStageArgs getArgs() {
        return this.args;
    }

    public void setArgs(RampartStageArgs args) {

        boolean validArgsType = this.checkArgs(args);

        if (!validArgsType)
            throw new IllegalArgumentException("Cannot assign args to this stage.  Invalid args type: " + args.getClass().getSimpleName());

        this.args = args;
    }


    public static String getFullListAsString() {

        List<String> stageNames = new ArrayList<>();

        for(RampartStage stage : RampartStage.values()) {
            stageNames.add(stage.toString());
        }

        return StringUtils.join(stageNames, ",");
    }

}
