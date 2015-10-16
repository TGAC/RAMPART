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

package uk.ac.tgac.rampart;

import org.apache.commons.cli.CommandLine;
import org.w3c.dom.Element;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.core.context.locality.Local;
import uk.ac.ebi.fgpt.conan.core.param.ArgValidator;
import uk.ac.ebi.fgpt.conan.core.param.DefaultParamMap;
import uk.ac.ebi.fgpt.conan.core.param.ParameterBuilder;
import uk.ac.ebi.fgpt.conan.core.param.PathParameter;
import uk.ac.ebi.fgpt.conan.core.pipeline.AbstractConanPipeline;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.core.user.GuestUser;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.stage.*;
import uk.ac.tgac.rampart.stage.analyse.asm.AnalyseAmpAssemblies;
import uk.ac.tgac.rampart.stage.analyse.asm.AnalyseMassAssemblies;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maplesod on 27/10/14.
 */
public class RampartPipeline extends AbstractConanPipeline {

    private Args args;
    private ConanExecutorService conanExecutorService;

    private static final String NAME = "rampart-pipeline";
    private static final ConanUser USER = new GuestUser("rampart@tgac.ac.uk");

    public RampartPipeline(Args args, ConanExecutorService ces) throws IOException {

        super(NAME, USER, false, false, ces.getConanProcessService());

        this.conanExecutorService = ces;
        this.args = args;

        this.init();
    }

    public Args getArgs() {
        return args;
    }

    public void setArgs(Args args) throws IOException {
        this.args = args;

        this.init();
    }


    public void init() throws IOException {

        // Configure pipeline
        this.clearProcessList();

        // Create all the processes
        this.addProcesses(this.args.getStages().createProcesses(this.conanExecutorService));

        // Check all processes in the pipeline are operational, modify execution context to execute unscheduled locally
        if (this.args.isDoInitialChecks() && !this.isOperational(new DefaultExecutionContext(new Local(), null,
                this.conanExecutorService.getExecutionContext().getExternalProcessConfiguration()))) {
            throw new IOException("RAMPART pipeline contains one or more processes that are not currently operational.  " +
                    "Please fix before restarting pipeline.");
        }
    }


    public static class Args extends AbstractProcessArgs {

        public static final String KEY_ELEM_MECQ            = "mecq";
        public static final String KEY_ELEM_MECQ_ANALYSIS   = "mecq_analysis";
        public static final String KEY_ELEM_KMER_CALC       = "kmer_calc";
        public static final String KEY_ELEM_MASS            = "mass";
        public static final String KEY_ELEM_MASS_ANALYSIS   = "mass_analysis";
        public static final String KEY_ELEM_MASS_SELECT     = "mass_select";
        public static final String KEY_ELEM_AMP             = "amp";
        public static final String KEY_ELEM_AMP_ANALYSIS    = "amp_analysis";
        public static final String KEY_ELEM_FINALISE        = "finalise";
        public static final String KEY_ELEM_COLLECT         = "collect";

        public static final String KEY_ATTR_PARALLEL        = "parallel";



        private List<Mecq.Sample> samples;
        private Organism organism;
        private File outputDir;
        private String jobPrefix;
        private String institution;
        private RampartStageList stages;
        private boolean doInitialChecks;
        private boolean runParallel;

        private Mecq.Args mecqArgs;
        private MecqAnalysis.Args mecqAnalysisArgs;
        private CalcOptimalKmer.Args kmerCalcArgs;
        private Mass.Args massArgs;
        private AnalyseMassAssemblies.Args analyseMassArgs;
        private Select.Args selectMassArgs;
        private File ampInput;
        private File ampBubble;
        private Amp.Args ampArgs;
        private AnalyseAmpAssemblies.Args analyseAmpArgs;
        private Finalise.Args finaliseArgs;
        private Collect.Args collectArgs;

        public Args() {
            super(new Params());

            this.samples = null;
            this.organism = null;
            this.outputDir = null;
            this.jobPrefix = "rampart-pipeline";
            this.institution = "";
            this.stages = null;
            this.doInitialChecks = true;
            this.runParallel = false;

            this.mecqArgs = null;
            this.mecqAnalysisArgs = null;
            this.kmerCalcArgs = null;
            this.massArgs = null;
            this.analyseMassArgs = null;
            this.selectMassArgs = null;
            this.ampInput = null;
            this.ampBubble = null;
            this.ampArgs = null;
            this.analyseAmpArgs = null;
            this.finaliseArgs = null;
            this.collectArgs = null;
        }

        public Args(Element element, List<Mecq.Sample> samples, Organism organism, File outputDir, String jobPrefix,
                    String institution, RampartStageList stages, boolean doInitialChecks, File ampInput, File ampBubble)
                throws IOException {

            // Set defaults
            this();

            // Check the pipeline element is valid
            if (!XmlHelper.validate(element,
                    new String[0],
                    new String[] {
                            KEY_ATTR_PARALLEL
                    },
                    new String[0],
                    new String[]{
                            KEY_ELEM_MECQ,
                            KEY_ELEM_MECQ_ANALYSIS,
                            KEY_ELEM_KMER_CALC,
                            KEY_ELEM_MASS,
                            KEY_ELEM_MASS_ANALYSIS,
                            KEY_ELEM_MASS_SELECT,
                            KEY_ELEM_AMP,
                            KEY_ELEM_AMP_ANALYSIS,
                            KEY_ELEM_FINALISE,
                            KEY_ELEM_COLLECT
                    }
            )) {
                throw new IllegalArgumentException("Found unrecognised element or attribute in Library");
            }

            this.samples = samples;
            this.organism = organism;
            this.outputDir = outputDir;
            this.jobPrefix = jobPrefix;
            this.institution = institution;
            this.stages = stages;
            this.doInitialChecks = doInitialChecks;
            this.ampInput = ampInput;
            this.ampBubble = ampBubble;

            this.runParallel = element.hasAttribute(KEY_ATTR_PARALLEL) ? XmlHelper.getBooleanValue(element, KEY_ATTR_PARALLEL) : false;

            if (this.organism == null) {
                throw new IOException("Organism details not found.");
            }

            // MECQ
            Element mecqElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_MECQ);
            this.mecqArgs = mecqElement == null ? null :
                    new Mecq.Args(
                            mecqElement,
                            this.outputDir,
                            this.jobPrefix,
                            samples,
                            this.organism,
                            this.runParallel);

            this.stages.setArgsIfPresent(RampartStage.MECQ, this.mecqArgs);

            if (this.mecqArgs == null) {
                // Add ECQ information into samples list
                for (int i = 0; i < samples.size(); i++) {
                    samples.get(i).ecqArgList = new ArrayList<>();
                }
            }


            // Analyse reads
            Element mecqAnalysisElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_MECQ_ANALYSIS);
            this.mecqAnalysisArgs = mecqAnalysisElement == null ? null :
                    new MecqAnalysis.Args(
                            mecqAnalysisElement,
                            this.outputDir,
                            this.jobPrefix,
                            samples,
                            this.organism,
                            this.runParallel);

            this.stages.setArgsIfPresent(RampartStage.MECQ_ANALYSIS, this.mecqAnalysisArgs);

            // Kmer calc
            Element kmerCalcElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_KMER_CALC);
            this.kmerCalcArgs = kmerCalcElement == null ? null :
                    new CalcOptimalKmer.Args(
                            kmerCalcElement,
                            this.outputDir,
                            this.jobPrefix,
                            samples,
                            this.organism,
                            this.runParallel);

            this.stages.setArgsIfPresent(RampartStage.KMER_CALC, this.kmerCalcArgs);

            // MASS
            Element massElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_MASS);
            this.massArgs = massElement == null ? null :
                    new Mass.Args(
                            massElement,
                            this.outputDir,
                            this.jobPrefix,
                            samples,
                            this.organism,
                            this.kmerCalcArgs,
                            this.runParallel);

            this.stages.setArgsIfPresent(RampartStage.MASS, this.massArgs);

            // Retrospectively update kmer calc and MASS with info from both
            if (this.kmerCalcArgs != null && this.massArgs != null) {
                this.kmerCalcArgs.setMassJobArgMap(this.massArgs.getMassJobArgMap());
                this.massArgs.setKmerCalcArgs(this.kmerCalcArgs);
            }

            // Do extra initialisation and validation
            if (this.kmerCalcArgs != null) {
                this.kmerCalcArgs.initialise();
            }

            if (this.massArgs != null) {
                this.massArgs.initialise();
            }

            // Analyse MASS assemblies
            Element analyseMassElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_MASS_ANALYSIS);
            this.analyseMassArgs = analyseMassElement == null ? null :
                    new AnalyseMassAssemblies.Args(
                            analyseMassElement,
                            this.outputDir,
                            this.massArgs.getMassJobArgMap(),
                            this.organism,
                            this.jobPrefix,
                            this.kmerCalcArgs,
                            this.runParallel);

            this.stages.setArgsIfPresent(RampartStage.MASS_ANALYSIS, this.analyseMassArgs);

            // Select MASS assembly
            Element selectMassElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_MASS_SELECT);
            this.selectMassArgs = selectMassElement == null ? null :
                    new Select.Args(
                            selectMassElement,
                            this.outputDir,
                            this.jobPrefix,
                            this.massArgs == null ? null : this.massArgs.getMassJobArgMap(),
                            this.organism,
                            new ArrayList<>(this.analyseMassArgs.getAssemblyAnalysers()),
                            this.runParallel
                            );

            this.stages.setArgsIfPresent(RampartStage.MASS_SELECT, this.selectMassArgs);


            // AMP
            Element ampElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_AMP);
            this.ampArgs = ampElement == null ? null :
                    new Amp.Args(
                            ampElement,
                            this.outputDir,
                            this.jobPrefix,
                            samples,
                            this.organism,
                            this.ampInput != null && samples.size() == 1 ?
                                    this.ampInput :
                                    null,
                            this.organism.getPloidy() > 1 ?
                                    this.ampBubble != null && samples.size() == 1 ?
                                            this.ampBubble :
                                            null :
                                    null,
                            this.runParallel
                    );

            this.stages.setArgsIfPresent(RampartStage.AMP, this.ampArgs);

            // Analyse AMP assemblies
            Element analyseAmpElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_AMP_ANALYSIS);
            this.analyseAmpArgs = analyseAmpElement == null ? null :
                    new AnalyseAmpAssemblies.Args(
                            analyseAmpElement,
                            this.outputDir,
                            this.jobPrefix,
                            samples,
                            this.organism,
                            this.ampArgs == null ? null : this.ampArgs.getStageArgsList(),
                            this.runParallel
                    );

            this.stages.setArgsIfPresent(RampartStage.AMP_ANALYSIS, this.analyseAmpArgs);


            boolean inputFromMass = this.ampArgs == null;

            Element finaliseElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_FINALISE);
            this.finaliseArgs = finaliseElement == null ? null :
                    new Finalise.Args(
                            finaliseElement,
                            this.outputDir,
                            this.jobPrefix,
                            samples,
                            this.organism,
                            this.institution,
                            inputFromMass,
                            this.runParallel);

            if (this.finaliseArgs == null && this.mecqArgs.getSamples().size() > 1) {
                throw new IOException("You have not requested finalise stage in the pipeline but this is required in multisample mode");
            }

            this.stages.setArgsIfPresent(RampartStage.FINALISE, this.finaliseArgs);

            if (samples.size() > 1) {
                Element collectElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_COLLECT);
                this.collectArgs = new Collect.Args(
                        collectElement,
                        this.outputDir,
                        this.jobPrefix,
                        samples,
                        this.organism,
                        this.finaliseArgs,
                        this.runParallel);

                this.stages.setArgsIfPresent(RampartStage.COLLECT, this.collectArgs);
            }

        }

        public Params getParams() {
            return (Params)this.params;
        }

        public List<Mecq.Sample> getSamples() {
            return samples;
        }

        public void setSamples(List<Mecq.Sample> samples) {
            this.samples = samples;
        }

        public File getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(File outputDir) {
            this.outputDir = outputDir;
        }

        public Organism getOrganism() {
            return organism;
        }

        public void setOrganism(Organism organism) {
            this.organism = organism;
        }

        public String getJobPrefix() {
            return jobPrefix;
        }

        public void setJobPrefix(String jobPrefix) {
            this.jobPrefix = jobPrefix;
        }

        public String getInstitution() {
            return institution;
        }

        public void setInstitution(String institution) {
            this.institution = institution;
        }

        public RampartStageList getStages() {
            return stages;
        }

        public void setStages(RampartStageList stages) {
            this.stages = stages;
        }

        public boolean isDoInitialChecks() {
            return doInitialChecks;
        }

        public void setDoInitialChecks(boolean doInitialChecks) {
            this.doInitialChecks = doInitialChecks;
        }

        @Override
        protected void setOptionFromMapEntry(ConanParameter param, String value) {

        }

        @Override
        protected void setArgFromMapEntry(ConanParameter param, String value) {

        }

        @Override
        protected void parseCommandLine(CommandLine commandLine) {

        }

        @Override
        public ParamMap getArgMap() {
            Params params = this.getParams();
            ParamMap pvp = new DefaultParamMap();

            if (this.jobPrefix != null && !this.jobPrefix.isEmpty())
                pvp.put(params.getJobPrefix(), this.jobPrefix);

            return pvp;
        }
    }

    public static class Params extends AbstractProcessParams {

        private ConanParameter jobPrefix;
        private ConanParameter outputDir;
        private ConanParameter stageList;

        public Params() {

            this.jobPrefix = new PathParameter(
                    "jobPrefix",
                    "The job Prefix for this pipeline task",
                    false);

            this.outputDir = new PathParameter(
                    "output",
                    "The path to the folder where all RAMPART output should be stored",
                    true);

            this.stageList = new ParameterBuilder()
                    .longName("stages")
                    .description("The RAMPART stages to execute: " + RampartStage.getFullListAsString() + ", ALL.  Default: ALL.")
                    .argValidator(ArgValidator.OFF)
                    .create();
        }



        public ConanParameter getJobPrefix() {
            return jobPrefix;
        }

        public ConanParameter getOutputDir() {
            return outputDir;
        }

        public ConanParameter getStageList() {
            return stageList;
        }

        @Override
        public ConanParameter[] getConanParametersAsArray() {
            return new ConanParameter[]{
                    this.jobPrefix,
                    this.outputDir,
                    this.stageList
            };
        }
    }
}