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
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.tgac.citadel.stage.*;
import uk.ac.tgac.citadel.stage.Package;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.RampartJobFileSystem;
import uk.ac.tgac.rampart.RampartPipeline;
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
public class CitadelPipeline extends AbstractConanPipeline {

    private Args args;
    private ConanExecutorService conanExecutorService;

    private static final String NAME = "citadel-pipeline";
    private static final ConanUser USER = new GuestUser("rampart@tgac.ac.uk");

    public CitadelPipeline(Args args, ConanExecutorService ces) throws IOException {

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

        Args args = this.getArgs();

        // Clear pipeline
        this.clearProcessList();

        // Stage 1: Alignment
        File decompressDir = new File(this.args.getOutputDir(), "1-decompress");
        Decompress.Args decompressArgs = new Decompress.Args();
        decompressArgs.setOutputDir(decompressDir);
        decompressArgs.setJobPrefix(args.getJobPrefix() + "-decompress");
        decompressArgs.setRunParallel(args.isRunParallel());
        decompressArgs.setPaired(args.isPaired());
        decompressArgs.setRecursive(args.isRecursive());

        if (this.args.stages.contains(CitadelStage.DECOMPRESS)) {
            this.addProcess(new Decompress(this.conanExecutorService, decompressArgs));
        }

        // Stage 2: Jellyswarm
        File jellyswarmDir = new File(this.args.getOutputDir(), "2-jellyswarm");
        Jellyswarm.Args jellyswarmArgs = new Jellyswarm.Args();
        jellyswarmArgs.setOutputDir(jellyswarmDir);
        jellyswarmArgs.setJobPrefix(args.getJobPrefix() + "-jellyswarm");
        jellyswarmArgs.setThreads(args.getThreadsPerProcess());
        jellyswarmArgs.setRunParallel(args.isRunParallel());
        jellyswarmArgs.setInputDir(decompressDir);
        jellyswarmArgs.setLowerCount(2);
        jellyswarmArgs.setHashSize(400000000L);
        jellyswarmArgs.setPaired(args.isPaired());

        if (this.args.stages.contains(CitadelStage.JELLYSWARM)) {
            this.addProcess(new Jellyswarm(this.conanExecutorService, jellyswarmArgs));
        }

        // Stage 3: RAMPART (works out which samples to analyse by itself)
        File rampartDir = new File(this.args.getOutputDir(), "3-rampart");
        Rampart.Args rampartArgs = new Rampart.Args();
        rampartArgs.setOutputDir(rampartDir);
        rampartArgs.setJobPrefix(args.getJobPrefix() + "-rampart");
        rampartArgs.setOrganism(args.getOrganism());

        if (this.args.stages.contains(CitadelStage.RAMPART)) {
            this.addProcess(new Rampart(this.conanExecutorService, rampartArgs));
        }

        // Stage 4: Run transdecoder on cufflinks and pasa output

        File assembleDir = new File(this.args.getOutputDir(), "4-assemble");
        Assemble.Args assembleArgs = new Assemble.Args();
        assembleArgs.setOutputDir(assembleDir);
        assembleArgs.setJobPrefix(args.getJobPrefix() + "-assemble");
        assembleArgs.setRunParallel(args.isRunParallel());

        if (this.args.stages.contains(CitadelStage.ASSEMBLE)) {
            this.addProcess(new Assemble(this.conanExecutorService, assembleArgs));
        }

        // Stage 5: Annotate assemblies
        File annotateDir = new File(this.args.getOutputDir(), "5-annotate");
        Annotate.Args annotateArgs = new Annotate.Args();
        annotateArgs.setOutputDir(annotateDir);
        annotateArgs.setJobPrefix(args.getJobPrefix() + "-annotate");
        annotateArgs.setRunParallel(args.isRunParallel());

        if (this.args.stages.contains(CitadelStage.ANNOTATE)) {
            this.addProcess(new Annotate(this.conanExecutorService, annotateArgs));
        }

        // Stage 5: Annotate assemblies
        File packageDir = new File(this.args.getOutputDir(), "6-package");
        Package.Args packageArgs = new Package.Args();
        packageArgs.setOutputDir(packageDir);
        packageArgs.setJobPrefix(args.getJobPrefix() + "-package");
        packageArgs.setRunParallel(args.isRunParallel());

        if (this.args.stages.contains(CitadelStage.PACKAGE)) {
            this.addProcess(new Package(this.conanExecutorService, packageArgs));
        }

        // Check all processes in the pipeline are operational, modify execution context to execute unscheduled locally
        ExecutionContext localContext = new DefaultExecutionContext(new Local(), null, this.conanExecutorService.getExecutionContext().getExternalProcessConfiguration());

        if (!this.isOperational(localContext)) {
            throw new IOException("The pipeline contains one or more processes that are not currently operational.  " +
                    "Please fix before restarting pipeline.");
        }
    }


    public static class Args extends AbstractProcessArgs {

        private File outputDir;

        private List<Library> libs;
        private Organism organism;
        private RampartJobFileSystem rampartJobFileSystem;
        private String jobPrefix;
        private boolean runParallel;
        private String institution;
        private RampartStageList stages;
        private boolean doInitialChecks;
        private boolean paired;
        private boolean recursive;
        private int threadsPerProcess;

        public Args() {
            super(new Params());

            this.outputDir = null;
            this.libs = null;
            this.organism = null;
            this.rampartJobFileSystem = null;
            this.jobPrefix = "rampart-pipeline";
            this.institution = "";
            this.stages = null;
            this.doInitialChecks = true;
            this.runParallel = false;
            this.paired = true;
            this.recursive = false;
            this.threadsPerProcess = 1;
        }



        public Params getParams() {
            return (Params)this.params;
        }

        public File getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(File outputDir) {
            this.outputDir = outputDir;
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

        public boolean isDoInitialChecks() {
            return doInitialChecks;
        }

        public void setDoInitialChecks(boolean doInitialChecks) {
            this.doInitialChecks = doInitialChecks;
        }

        public boolean isRunParallel() {
            return runParallel;
        }

        public void setRunParallel(boolean runParallel) {
            this.runParallel = runParallel;
        }

        public boolean isPaired() {
            return paired;
        }

        public void setPaired(boolean paired) {
            this.paired = paired;
        }

        public boolean isRecursive() {
            return recursive;
        }

        public void setRecursive(boolean recursive) {
            this.recursive = recursive;
        }

        public int getThreadsPerProcess() {
            return threadsPerProcess;
        }

        public void setThreadsPerProcess(int threadsPerProcess) {
            this.threadsPerProcess = threadsPerProcess;
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

            if (this.outputDir != null) {
                pvp.put(params.getOutputDir(), this.outputDir.getAbsolutePath());
            }

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