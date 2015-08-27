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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionResult;
import uk.ac.ebi.fgpt.conan.core.context.DefaultTaskResult;
import uk.ac.ebi.fgpt.conan.core.context.locality.Local;
import uk.ac.ebi.fgpt.conan.core.param.ArgValidator;
import uk.ac.ebi.fgpt.conan.core.param.DefaultParamMap;
import uk.ac.ebi.fgpt.conan.core.param.ParameterBuilder;
import uk.ac.ebi.fgpt.conan.core.param.PathParameter;
import uk.ac.ebi.fgpt.conan.core.pipeline.AbstractConanPipeline;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.core.user.GuestUser;
import uk.ac.ebi.fgpt.conan.factory.DefaultTaskFactory;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.ResourceUsage;
import uk.ac.ebi.fgpt.conan.model.context.TaskResult;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.service.exception.TaskExecutionException;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class wraps a Pipeline to manage each AMP stage
 *
 * User: maplesod
 * Date: 12/02/13
 * Time: 16:30
 */
public class Amp extends RampartProcess {

    private static Logger log = LoggerFactory.getLogger(Amp.class);

    private Pipeline ampPipeline;

    public Amp(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public Amp(ConanExecutorService ces, Args args) {
        super(ces, args);
        this.ampPipeline = new Pipeline(this.conanExecutorService, this.getArgs());
    }

    public Args getArgs() {
        return (Args)this.getProcessArgs();
    }

    @Override
    public String getName() {
        return "AMP (Assembly iMProver)";
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        // Create AMP Pipeline
        if (!ampPipeline.isOperational(executionContext)) {
            log.warn("AMP stage is NOT operational.");
            return false;
        }

        log.info("AMP stage is operational.");
        return true;
    }

    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public TaskResult executeSample(Mecq.Sample sample, ExecutionContext executionContext)
            throws ProcessExecutionException, InterruptedException, IOException {

        // Short cut to arguments
        Args args = this.getArgs();

        // Create AMP Pipeline
        Pipeline ampPipeline = new Pipeline(this.conanExecutorService, args, sample);

        log.debug("Found " + ampPipeline.getProcesses().size() + " AMP stages in pipeline to process");

        // Make sure the output directory exists
        args.getAssembliesDir(sample).mkdirs();

        // Create link for the initial input file
        this.getConanProcessService().createLocalSymbolicLink(
                args.getInputAssembly(sample),
                new File(args.getAssembliesDir(sample), "amp-stage-0.fa"));

        // Create a guest user
        ConanUser rampartUser = new GuestUser("daniel.mapleson@tgac.ac.uk");

        // Create the AMP task
        ConanTask<Pipeline> ampTask = new DefaultTaskFactory().createTask(
                ampPipeline,
                0,
                ampPipeline.getArgs().getArgMap(),
                ConanTask.Priority.HIGHEST,
                rampartUser);

        ampTask.setId("AMP");
        ampTask.submit();

        // Run the AMP pipeline
        TaskResult result;
        try {
            result = ampTask.execute(executionContext);
        } catch (TaskExecutionException e) {
            throw new ProcessExecutionException(-1, e);
        }

        // Create a symbolic link for the final assembly from the final stage
        this.getConanProcessService().createLocalSymbolicLink(
                new File(args.getAssembliesDir(sample), "amp-stage-" + ampPipeline.getProcesses().size() + ".fa"),
                args.getFinalAssembly(sample));


        return result;

    }

    public static class Pipeline extends AbstractConanPipeline {

        private static final String NAME = "AMP";
        private static final ConanUser USER = new GuestUser("rampart@tgac.ac.uk");


        private Params params = new Params();
        private ConanExecutorService conanExecutorService;
        private Mecq.Sample sample;

        private Args args;

        public Pipeline(ConanExecutorService ces, Args ampArgs) {
            this(ces, ampArgs, null);
        }

        public Pipeline(ConanExecutorService ces, Args ampArgs, Mecq.Sample sample) {

            super(NAME, USER, false, false, ces.getConanProcessService());

            this.conanExecutorService = ces;
            this.args = ampArgs;
            this.args.setExecutionContext(ces.getExecutionContext());
            this.sample = sample;

            this.init();
        }


        public Args getArgs() {
            return args;
        }

        public void setArgs(Args args) {
            this.args = args;

            this.init();
        }


        public void init() {

            List<AmpStage.Args> ampArgsList = this.sample == null ?
                    this.args.getStageArgsList().values().iterator().next() :
                    this.args.getStageArgsList().get(this.sample);

            for(AmpStage.Args ampStageArgs : ampArgsList) {

                AmpStage proc = new AmpStage(this.conanExecutorService, ampStageArgs, this.sample);
                proc.setConanProcessService(getConanProcessService());
                this.addProcess(proc);
            }

            // Check all processes in the pipeline are operational, modify execution context to execute unscheduled locally
            if (!this.isOperational(new DefaultExecutionContext(new Local(), null,
                    this.args.getExecutionContext().getExternalProcessConfiguration()))) {
                throw new UnsupportedOperationException("AMP pipeline contains one or more processes that are not currently operational.  " +
                        "Please fix before restarting pipeline.");
            }
        }

    }


    public static class Args extends RampartProcessArgs {

        private static final String INPUT_ASSEMBLY = "input";
        private static final String KEY_ELEM_AMP_STAGE = "stage";

        private File inputAssembly;
        private File bubbleFile;
        private Map<Mecq.Sample, List<AmpStage.Args>> stageArgsList;
        private ExecutionContext executionContext;


        public Args() {
            super(RampartStage.AMP);
            this.inputAssembly = null;
            this.bubbleFile = null;
            this.stageArgsList = new HashMap<>();
        }

        public Args(Element ele, File outputDir, String jobPrefix, List<Mecq.Sample> samples, Organism organism,
                    File inputAssembly, File bubbleFile)
                throws IOException {

            // Set defaults
            super(RampartStage.AMP, outputDir, jobPrefix, samples, organism);

            // Check there's nothing
            if (!XmlHelper.validate(ele,
                    new String[0],
                    new String[0],
                    new String[] {
                            KEY_ELEM_AMP_STAGE
                    },
                    new String[0])) {
                throw new IOException("Found unrecognised element or attribute in AMP");
            }

            // Set args
            this.inputAssembly = inputAssembly;
            this.bubbleFile = bubbleFile;
            this.stageArgsList = new HashMap<>();

            for(Mecq.Sample sample : samples) {

                // Parse Xml for AMP stages
                // All single mass args
                List<AmpStage.Args> stageList = new ArrayList<>();
                NodeList nodes = ele.getElementsByTagName(KEY_ELEM_AMP_STAGE);
                for (int i = 1; i <= nodes.getLength(); i++) {

                    String stageName = "amp-" + Integer.toString(i);
                    File ampStageOutputDir = new File(this.getStageDir(sample), stageName);

                    AmpStage.Args stage = new AmpStage.Args(
                            (Element) nodes.item(i - 1),
                            ampStageOutputDir,
                            this.getAssembliesDir(sample),
                            jobPrefix + "-" + stageName,
                            sample,
                            this.organism,
                            this.getInputAssembly(sample),
                            this.bubbleFile,
                            i);

                    stageList.add(stage);

                    this.inputAssembly = stage.getOutputFile();
                }

                this.stageArgsList.put(sample, stageList);
            }
        }

        public File getInputAssembly(Mecq.Sample sample) {
            return this.inputAssembly == null ?
                    new File(new File(this.getSampleDir(sample), RampartStage.MASS_SELECT.getOutputDirName()), "best.fa") :
                    this.inputAssembly;
        }

        public void setInputAssembly(File inputAssembly) {
            this.inputAssembly = inputAssembly;
        }

        public File getBubbleFile() {
            return bubbleFile;
        }

        public void setBubbleFile(File bubbleFile) {
            this.bubbleFile = bubbleFile;
        }

        public File getAssembliesDir(Mecq.Sample sample) {
            return new File(this.getStageDir(sample), "assemblies");
        }

        public Map<Mecq.Sample, List<AmpStage.Args>> getStageArgsList() {
            return stageArgsList;
        }

        public void setStageArgsList(Map<Mecq.Sample, List<AmpStage.Args>> stageArgsList) {
            this.stageArgsList = stageArgsList;
        }

        public ExecutionContext getExecutionContext() {
            return executionContext;
        }

        public void setExecutionContext(ExecutionContext executionContext) {
            this.executionContext = executionContext;
        }

        @Override
        public void parseCommandLine(CommandLine cmdLine) {

        }

        @Override
        public ParamMap getArgMap() {

            ParamMap pvp = new DefaultParamMap();
            return pvp;
        }

        @Override
        protected void setOptionFromMapEntry(ConanParameter param, String value) {
        }

        @Override
        protected void setArgFromMapEntry(ConanParameter param, String value) {

        }

        public File getFinalAssembly(Mecq.Sample sample) {
            return new File(this.getStageDir(sample), "final.fa");
        }

        @Override
        public List<ConanProcess> getExternalProcesses() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

}
