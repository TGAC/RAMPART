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
package uk.ac.tgac.rampart.tool.pipeline.rampart;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.tgac.rampart.tool.pipeline.amp.Amp;
import uk.ac.tgac.rampart.tool.process.Finalise;
import uk.ac.tgac.rampart.tool.process.Mecq;
import uk.ac.tgac.rampart.tool.process.analyse.asm.AnalyseAmpAssemblies;
import uk.ac.tgac.rampart.tool.process.analyse.asm.AnalyseMassAssemblies;
import uk.ac.tgac.rampart.tool.process.analyse.reads.AnalyseReads;
import uk.ac.tgac.rampart.tool.process.mass.Mass;

import java.util.ArrayList;
import java.util.List;

/**
 * This enum describes each possible stage within the RAMPART pipeline.  It is important that each stage is specified in
 * the order that it should be executed within the pipeline.
 */
public enum RampartStage {

    MECQ {

        @Override
        public List<ConanParameter> getParameters() {
            return new Mecq.Params().getConanParameters();
        }

        @Override
        public boolean checkArgs(RampartStageArgs args) {
            return classContains(args.getClass(), Mecq.Args.class);
        }

        @Override
        public AbstractConanProcess create(ConanExecutorService ces) {
            return new Mecq(ces, (Mecq.Args)this.getArgs());
        }

    },
    ANALYSE_READS {

        @Override
        public List<ConanParameter> getParameters() {
            return new AnalyseReads.Params().getConanParameters();
        }

        @Override
        public boolean checkArgs(RampartStageArgs args) {
            return classContains(args.getClass(), AnalyseReads.Args.class);
        }

        @Override
        public AbstractConanProcess create(ConanExecutorService ces) {
            return new AnalyseReads(ces, (AnalyseReads.Args)this.getArgs());
        }
    },
    MASS {

        @Override
        public List<ConanParameter> getParameters() {
            return new Mass.Params().getConanParameters();
        }

        @Override
        public boolean checkArgs(RampartStageArgs args) {
            return classContains(args.getClass(), Mass.Args.class);
        }

        @Override
        public AbstractConanProcess create(ConanExecutorService ces) {
            return new Mass(ces, (Mass.Args)this.getArgs());
        }
    },
    ANALYSE_MASS {

        @Override
        public List<ConanParameter> getParameters() {
            return new AnalyseMassAssemblies.Params().getConanParameters();
        }

        @Override
        public boolean checkArgs(RampartStageArgs args) {
            return classContains(args.getClass(), AnalyseMassAssemblies.Args.class);
        }

        @Override
        public AbstractConanProcess create(ConanExecutorService ces) {
            return new AnalyseMassAssemblies(ces, (AnalyseMassAssemblies.Args)this.getArgs());
        }
    },
    AMP {

        @Override
        public List<ConanParameter> getParameters() {
            return new Amp.Params().getConanParameters();
        }

        @Override
        public boolean checkArgs(RampartStageArgs args) {
            return classContains(args.getClass(), Amp.Args.class);
        }

        @Override
        public AbstractConanProcess create(ConanExecutorService ces) {
            return new Amp(ces, (Amp.Args)this.getArgs());
        }
    },
    ANALYSE_AMP {

        @Override
        public List<ConanParameter> getParameters() {
            return new AnalyseAmpAssemblies.Params().getConanParameters();
        }

        @Override
        public boolean checkArgs(RampartStageArgs args) {
            return classContains(args.getClass(), AnalyseAmpAssemblies.Args.class);
        }

        @Override
        public AbstractConanProcess create(ConanExecutorService ces) {
            return new AnalyseAmpAssemblies(ces, (AnalyseAmpAssemblies.Args)this.getArgs());
        }
    },
    FINALISE {

        @Override
        public List<ConanParameter> getParameters() {
            return new Finalise.Params().getConanParameters();
        }

        @Override
        public boolean checkArgs(RampartStageArgs args) {
            return classContains(args.getClass(), Finalise.Args.class);
        }

        @Override
        public AbstractConanProcess create(ConanExecutorService ces) {
            return new Finalise((Finalise.Args)this.getArgs());
        }
    },
    /*REPORT {

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
    }*/
    ;

    public abstract List<ConanParameter> getParameters();

    public abstract boolean checkArgs(RampartStageArgs args);

    public abstract AbstractConanProcess create(ConanExecutorService ces);


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
