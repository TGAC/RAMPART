/*
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
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.stage.*;
import uk.ac.tgac.rampart.stage.analyse.asm.AnalyseAmpAssemblies;
import uk.ac.tgac.rampart.stage.analyse.asm.AnalyseMassAssemblies;
import uk.ac.tgac.rampart.stage.analyse.reads.AnalyseReads;

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
        public static final String KEY_ELEM_ANALYSE_READS   = "analyse_reads";
        public static final String KEY_ELEM_KMER_CALC       = "kmer_calc";
        public static final String KEY_ELEM_MASS            = "mass";
        public static final String KEY_ELEM_ANALYSE_MASS    = "analyse_mass";
        public static final String KEY_ELEM_ANALYSE_AMP     = "analyse_amp";
        public static final String KEY_ELEM_AMP             = "amp";
        public static final String KEY_ELEM_FINALISE        = "finalise";
        public static final String KEY_ELEM_SELECT_MASS     = "select_mass";


        private List<Library> libs;
        private Organism organism;
        private RampartJobFileSystem rampartJobFileSystem;
        private String jobPrefix;
        private String institution;
        private RampartStageList stages;
        private boolean doInitialChecks;

        private Mecq.Args mecqArgs;
        private AnalyseReads.Args analyseReadsArgs;
        private CalcOptimalKmer.Args kmerCalcArgs;
        private Mass.Args massArgs;
        private AnalyseMassAssemblies.Args analyseMassArgs;
        private Select.Args selectMassArgs;
        private File ampInput;
        private File ampBubble;
        private Amp.Args ampArgs;
        private AnalyseAmpAssemblies.Args analyseAmpArgs;
        private Finalise.Args finaliseArgs;

        public Args() {
            super(new Params());

            this.libs = null;
            this.organism = null;
            this.rampartJobFileSystem = null;
            this.jobPrefix = "rampart-pipeline";
            this.institution = "";
            this.stages = null;
            this.doInitialChecks = true;

            this.mecqArgs = null;
            this.analyseReadsArgs = null;
            this.kmerCalcArgs = null;
            this.massArgs = null;
            this.analyseMassArgs = null;
            this.selectMassArgs = null;
            this.ampInput = null;
            this.ampBubble = null;
            this.ampArgs = null;
            this.analyseAmpArgs = null;
            this.finaliseArgs = null;
        }

        public Args(Element element, List<Library> libs, Organism organism, RampartJobFileSystem rjfs, String jobPrefix,
                    String institution, RampartStageList stages, boolean doInitialChecks, File ampInput, File ampBubble)
                throws IOException {

            // Set defaults
            this();

            // Check the pipeline element is valid
            if (!XmlHelper.validate(element,
                    new String[0],
                    new String[0],
                    new String[0],
                    new String[]{
                            KEY_ELEM_MECQ,
                            KEY_ELEM_ANALYSE_READS,
                            KEY_ELEM_KMER_CALC,
                            KEY_ELEM_MASS,
                            KEY_ELEM_ANALYSE_MASS,
                            KEY_ELEM_SELECT_MASS,
                            KEY_ELEM_AMP,
                            KEY_ELEM_ANALYSE_AMP,
                            KEY_ELEM_FINALISE
                    }
            )) {
                throw new IllegalArgumentException("Found unrecognised element or attribute in Library");
            }

            this.libs = libs;
            this.organism = organism;
            this.rampartJobFileSystem = rjfs;
            this.jobPrefix = jobPrefix;
            this.institution = institution;
            this.stages = stages;
            this.doInitialChecks = doInitialChecks;
            this.ampInput = ampInput;
            this.ampBubble = ampBubble;

            // MECQ
            Element mecqElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_MECQ);
            this.mecqArgs = mecqElement == null ? null :
                    new Mecq.Args(
                            mecqElement,
                            this.rampartJobFileSystem.getMeqcDir(),
                            this.jobPrefix + "-mecq",
                            this.libs);

            this.stages.setArgsIfPresent(RampartStage.MECQ, this.mecqArgs);


            // Analyse reads
            Element analyseReadsElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_ANALYSE_READS);
            this.analyseReadsArgs = analyseReadsElement == null ? null :
                    new AnalyseReads.Args(
                            analyseReadsElement,
                            this.libs,
                            this.mecqArgs == null ? new ArrayList<Mecq.EcqArgs>() : this.mecqArgs.getEqcArgList(),
                            this.jobPrefix + "-analyse_reads",
                            this.rampartJobFileSystem.getMeqcDir(),
                            this.rampartJobFileSystem.getAnalyseReadsDir(),
                            this.organism);

            this.stages.setArgsIfPresent(RampartStage.ANALYSE_READS, this.analyseReadsArgs);

            // Kmer calc
            Element kmerCalcElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_KMER_CALC);
            this.kmerCalcArgs = kmerCalcElement == null ? null :
                    new CalcOptimalKmer.Args(
                            kmerCalcElement,
                            this.rampartJobFileSystem.getKmerCalcDir(),
                            this.jobPrefix + "-kmer_calc",
                            this.organism.getPloidy());

            this.stages.setArgsIfPresent(RampartStage.KMER_CALC, this.kmerCalcArgs);

            // MASS
            Element massElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_MASS);
            this.massArgs = massElement == null ? null :
                    new Mass.Args(
                            massElement,
                            this.rampartJobFileSystem.getMassDir(),
                            this.rampartJobFileSystem.getMeqcDir(),
                            this.jobPrefix + "-mass",
                            this.libs,
                            this.mecqArgs == null ? new ArrayList<Mecq.EcqArgs>() : this.mecqArgs.getEqcArgList(),
                            this.organism);

            this.stages.setArgsIfPresent(RampartStage.MASS, this.massArgs);

            // Retrospectively update kmer calc and MASS with info from both
            if (this.kmerCalcArgs != null && this.massArgs != null) {
                this.kmerCalcArgs.setMassJobArgList(this.massArgs.getMassJobArgList());
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
            Element analyseMassElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_ANALYSE_MASS);
            this.analyseMassArgs = analyseMassElement == null ? null :
                    new AnalyseMassAssemblies.Args(
                            analyseMassElement,
                            this.rampartJobFileSystem.getMassDir(),
                            this.analyseReadsArgs != null ? this.rampartJobFileSystem.getAnalyseReadsDir() : null,
                            this.rampartJobFileSystem.getAnalyseMassDir(),
                            this.massArgs == null ? null : this.massArgs.getMassJobArgList(),
                            this.organism,
                            this.jobPrefix + "-analyse_mass",
                            this.kmerCalcArgs == null ? null : this.kmerCalcArgs.getResultFile());

            this.stages.setArgsIfPresent(RampartStage.ANALYSE_MASS, this.analyseMassArgs);

            // Select MASS assembly
            Element selectMassElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_SELECT_MASS);
            this.selectMassArgs = selectMassElement == null ? null :
                    new Select.Args(
                            selectMassElement,
                            this.rampartJobFileSystem.getAnalyseMassDir(),
                            this.analyseMassArgs.getAssemblyLinkageFile(),
                            this.rampartJobFileSystem.getSelectMassDir(),
                            this.massArgs == null ? null : this.massArgs.getMassJobArgList(),
                            new ArrayList<>(this.analyseMassArgs.getAssemblyAnalysers()),
                            this.organism,
                            this.jobPrefix + "-select_mass");

            this.stages.setArgsIfPresent(RampartStage.SELECT_MASS, this.selectMassArgs);

            // AMP
            Element ampElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_AMP);
            this.ampArgs = ampElement == null ? null :
                    new Amp.Args(
                            ampElement,
                            this.rampartJobFileSystem.getAmpDir(),
                            this.jobPrefix + "-amp",
                            this.ampInput != null ?
                                    this.ampInput :
                                    this.rampartJobFileSystem.getSelectedAssemblyFile(),
                            this.organism.getPloidy() > 1 ?
                                    this.ampBubble != null ?
                                            this.ampBubble :
                                            this.rampartJobFileSystem.getSelectedBubbleFile() :
                                    null,
                            this.libs,
                            this.mecqArgs == null ? new ArrayList<Mecq.EcqArgs>() : this.mecqArgs.getEqcArgList(),
                            this.organism);

            this.stages.setArgsIfPresent(RampartStage.AMP, this.ampArgs);

            // Analyse AMP assemblies
            Element analyseAmpElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_ANALYSE_AMP);
            this.analyseAmpArgs = analyseAmpElement == null ? null :
                    new AnalyseAmpAssemblies.Args(
                            analyseAmpElement,
                            this.analyseReadsArgs != null ? this.rampartJobFileSystem.getAnalyseReadsDir() : null,
                            this.rampartJobFileSystem.getAnalyseAmpDir(),
                            this.ampArgs == null ? null : this.ampArgs.getStageArgsList(),
                            this.organism,
                            this.jobPrefix + "-analyse_amp");

            this.stages.setArgsIfPresent(RampartStage.ANALYSE_AMP, this.analyseAmpArgs);


            File finalAssembly = this.ampArgs == null ? this.rampartJobFileSystem.getSelectedAssemblyFile(): this.ampArgs.getFinalAssembly();

            Element finaliseElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_FINALISE);
            this.finaliseArgs = finaliseElement == null ? null :
                    new Finalise.Args(
                            finaliseElement,
                            finalAssembly,
                            this.rampartJobFileSystem.getFinalDir(),
                            this.jobPrefix + "-finalise",
                            this.organism,
                            this.institution);

            this.stages.setArgsIfPresent(RampartStage.FINALISE, this.finaliseArgs);

        }

        public Params getParams() {
            return (Params)this.params;
        }

        public Amp.Args getAmpArgs() {
            return ampArgs;
        }

        public void setAmpArgs(Amp.Args ampArgs) {
            this.ampArgs = ampArgs;
        }

        public Mecq.Args getMecqArgs() {
            return mecqArgs;
        }

        public void setMecqArgs(Mecq.Args mecqArgs) {
            this.mecqArgs = mecqArgs;
        }

        public AnalyseReads.Args getAnalyseReadsArgs() {
            return analyseReadsArgs;
        }

        public void setAnalyseReadsArgs(AnalyseReads.Args analyseReadsArgs) {
            this.analyseReadsArgs = analyseReadsArgs;
        }

        public CalcOptimalKmer.Args getKmerCalcArgs() {
            return kmerCalcArgs;
        }

        public void setKmerCalcArgs(CalcOptimalKmer.Args kmerCalcArgs) {
            this.kmerCalcArgs = kmerCalcArgs;
        }

        public Mass.Args getMassArgs() {
            return massArgs;
        }

        public void setMassArgs(Mass.Args massArgs) {
            this.massArgs = massArgs;
        }

        public AnalyseMassAssemblies.Args getAnalyseMassArgs() {
            return analyseMassArgs;
        }

        public void setAnalyseMassArgs(AnalyseMassAssemblies.Args analyseMassArgs) {
            this.analyseMassArgs = analyseMassArgs;
        }

        public AnalyseAmpAssemblies.Args getAnalyseAmpArgs() {
            return analyseAmpArgs;
        }

        public void setAnalyseAmpArgs(AnalyseAmpAssemblies.Args analyseAmpArgs) {
            this.analyseAmpArgs = analyseAmpArgs;
        }

        public Finalise.Args getFinaliseArgs() {
            return finaliseArgs;
        }

        public void setFinaliseArgs(Finalise.Args finaliseArgs) {
            this.finaliseArgs = finaliseArgs;
        }

        public List<Library> getLibs() {
            return libs;
        }

        public void setLibs(List<Library> libs) {
            this.libs = libs;
        }

        public Organism getOrganism() {
            return organism;
        }

        public void setOrganism(Organism organism) {
            this.organism = organism;
        }

        public RampartJobFileSystem getRampartJobFileSystem() {
            return rampartJobFileSystem;
        }

        public void setRampartJobFileSystem(RampartJobFileSystem rampartJobFileSystem) {
            this.rampartJobFileSystem = rampartJobFileSystem;
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

        public Select.Args getSelectMassArgs() {
            return selectMassArgs;
        }

        public void setSelectMassArgs(Select.Args selectMassArgs) {
            this.selectMassArgs = selectMassArgs;
        }

        public File getAmpInput() {
            return ampInput;
        }

        public void setAmpInput(File ampInput) {
            this.ampInput = ampInput;
        }

        public File getAmpBubble() {
            return ampBubble;
        }

        public void setAmpBubble(File ampBubble) {
            this.ampBubble = ampBubble;
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

            if (this.rampartJobFileSystem != null) {
                pvp.put(params.getOutputDir(), this.rampartJobFileSystem.getMeqcDir().getParentFile().getAbsolutePath());
            }

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