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
package uk.ac.tgac.rampart.stage;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.tgac.rampart.stage.analyse.asm.AnalyseAmpAssemblies;
import uk.ac.tgac.rampart.stage.analyse.asm.AnalyseMassAssemblies;

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
        public RampartProcess create(ConanExecutorService ces) {
            return new Mecq(ces, (Mecq.Args)this.getArgs());
        }

    },
    MECQ_ANALYSIS {

        @Override
        public List<ConanParameter> getParameters() {
            return new MecqAnalysis.Params().getConanParameters();
        }

        @Override
        public boolean checkArgs(RampartStageArgs args) {
            return classContains(args.getClass(), MecqAnalysis.Args.class);
        }

        @Override
        public RampartProcess create(ConanExecutorService ces) {
            return new MecqAnalysis(ces, (MecqAnalysis.Args)this.getArgs());
        }

    },
    KMER_CALC {

        @Override
        public List<ConanParameter> getParameters() {
            return new CalcOptimalKmer.Params().getConanParameters();
        }

        @Override
        public boolean checkArgs(RampartStageArgs args) {
            return classContains(args.getClass(), CalcOptimalKmer.Args.class);
        }

        @Override
        public RampartProcess create(ConanExecutorService ces) {
            return new CalcOptimalKmer(ces, (CalcOptimalKmer.Args)this.getArgs());
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
        public RampartProcess create(ConanExecutorService ces) {
            return new Mass(ces, (Mass.Args)this.getArgs());
        }

    },
    MASS_ANALYSIS {

        @Override
        public List<ConanParameter> getParameters() {
            return new AnalyseMassAssemblies.Params().getConanParameters();
        }

        @Override
        public boolean checkArgs(RampartStageArgs args) {
            return classContains(args.getClass(), AnalyseMassAssemblies.Args.class);
        }

        @Override
        public RampartProcess create(ConanExecutorService ces) {
            return new AnalyseMassAssemblies(ces, (AnalyseMassAssemblies.Args)this.getArgs());
        }

    },
    MASS_SELECT {
        @Override
        public List<ConanParameter> getParameters() {
            return new Select.Params().getConanParameters();
        }

        @Override
        public boolean checkArgs(RampartStageArgs args) {
            return classContains(args.getClass(), Select.Args.class);
        }

        @Override
        public RampartProcess create(ConanExecutorService ces) {
            return new Select(ces, (Select.Args)this.getArgs());
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
        public RampartProcess create(ConanExecutorService ces) {
            return new Amp(ces, (Amp.Args)this.getArgs());
        }

    },
    AMP_ANALYSIS {

        @Override
        public List<ConanParameter> getParameters() {
            return new AnalyseAmpAssemblies.Params().getConanParameters();
        }

        @Override
        public boolean checkArgs(RampartStageArgs args) {
            return classContains(args.getClass(), AnalyseAmpAssemblies.Args.class);
        }

        @Override
        public RampartProcess create(ConanExecutorService ces) {
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
        public RampartProcess create(ConanExecutorService ces) {
            return new Finalise(ces, (Finalise.Args)this.getArgs());
        }
    },
    COLLECT {

        @Override
        public List<ConanParameter> getParameters() {
            return new Collect.Params().getConanParameters();
        }

        @Override
        public boolean checkArgs(RampartStageArgs args) {
            return classContains(args.getClass(), Collect.Args.class);
        }

        @Override
        public RampartProcess create(ConanExecutorService ces) {
            return new Collect(ces, (Collect.Args)this.getArgs());
        }
    }
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

    public abstract RampartProcess create(ConanExecutorService ces);

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

    public String getOutputDirName() {
        return (this.ordinal() + 1) + "_" + this.name().toLowerCase();
    }


    public static String getFullListAsString() {

        List<String> stageNames = new ArrayList<>();

        for(RampartStage stage : RampartStage.values()) {
            stageNames.add(stage.toString());
        }

        return StringUtils.join(stageNames, ",");
    }

}
