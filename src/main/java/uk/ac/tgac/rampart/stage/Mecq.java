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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uk.ac.ebi.fgpt.conan.core.context.DefaultTaskResult;
import uk.ac.ebi.fgpt.conan.core.param.DefaultParamMap;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.*;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.conan.process.re.GenericReadEnhancerArgs;
import uk.ac.tgac.conan.process.re.ReadEnhancer;
import uk.ac.tgac.conan.process.re.ReadEnhancerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 07/01/13
 * Time: 10:54
 * To change this template use File | Settings | File Templates.
 */
public class Mecq extends RampartProcess {

    private static Logger log = LoggerFactory.getLogger(Mecq.class);

    public Mecq(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public Mecq(ConanExecutorService ces, Args args) {
        super(ces, args);
    }


    public Args getArgs() {
        return (Args)this.getProcessArgs();
    }

    @Override
    public TaskResult executeSample(Mecq.Sample sample, ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException, IOException {

        Args args = this.getArgs();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<ReadEnhancer> readEnhancers = new ArrayList<>();
        List<ExecutionResult> results = new ArrayList<>();

        File stageOutputDir = args.getStageDir(sample);

        // Passthrough links for raw libraries to output
        for (Library lib : sample.libraries) {
            this.createOutputLinks(new File(stageOutputDir, "raw"), null, null, lib);
        }

        // For each ecq process all libraries
        for (EcqArgs ecqArgs : sample.ecqArgList) {

            // Process each lib
            for (Library lib : ecqArgs.getLibraries()) {

                // Create the output directory
                File ecqLibDir = new File(ecqArgs.getOutputDir(), lib.getName());

                if (ecqLibDir.exists()) {
                    try {
                        FileUtils.deleteDirectory(ecqLibDir);
                    } catch (IOException e) {
                        throw new ProcessExecutionException(2, "Could not delete ecqDir: " + ecqLibDir.getAbsolutePath(), e);
                    }
                }
                ecqLibDir.mkdirs();

                // Create a job name
                String title = ecqArgs.getName() + "_" + lib.getName();
                String jobName = ecqArgs.getJobPrefix() + "_" + title;

                GenericReadEnhancerArgs genericArgs = new GenericReadEnhancerArgs();
                genericArgs.setInput(lib);
                genericArgs.setOutputDir(ecqLibDir);
                genericArgs.setThreads(ecqArgs.getThreads());
                genericArgs.setMemoryGb(ecqArgs.getMemory());

                // Create the actual error corrector from the user provided EcqArgs
                ReadEnhancer readEnhancer = ReadEnhancerFactory.create(
                        ecqArgs.getTool(),
                        genericArgs,
                        this.conanExecutorService);

                // Configure read enhancer
                if (ecqArgs.getCheckedArgs() != null && !ecqArgs.getCheckedArgs().trim().isEmpty()) {
                    readEnhancer.getReadEnchancerArgs().parse(ecqArgs.getCheckedArgs());
                }
                readEnhancer.getReadEnchancerArgs().setUncheckedArgs(ecqArgs.getUncheckedArgs());
                readEnhancer.setup();

                // Add this to the list in case we need it later
                readEnhancers.add(readEnhancer);

                // Execute this error corrector
                ExecutionResult result = this.conanExecutorService.executeProcess(
                        readEnhancer.toConanProcess(),
                        ecqLibDir,
                        jobName,
                        ecqArgs.getThreads(),
                        ecqArgs.getMemory(),
                        ecqArgs.isRunParallel() || args.isRunParallel());

                result.setName(title);
                results.add(result);

                // Create links for outputs from this assembler to known locations
                File finishedDir = new File(stageOutputDir, "output");
                this.createOutputLinks(new File(finishedDir, ecqArgs.getName()), readEnhancer, ecqArgs, lib);
            }

            // If we're using a scheduler, and we don't want to run separate ECQ in parallel, and we want to parallelise
            // each library processed by this ECQ, then wait here.
            if (executionContext.usingScheduler() && ecqArgs.isRunParallel() && !args.isRunParallel()) {
                log.info("Waiting for completion of: " + ecqArgs.getName() + "; for all requested libraries");
                MultiWaitResult mrw = this.conanExecutorService.executeScheduledWait(
                        results,
                        ecqArgs.getJobPrefix() + "*",
                        ExitStatus.Type.COMPLETED_SUCCESS,
                        args.getJobPrefix() + "-wait",
                        ecqArgs.getOutputDir());

                results.clear();
            }
        }

        stopWatch.stop();

        return new DefaultTaskResult(sample.name + "-" + args.stage.getOutputDirName(), true, results, stopWatch.getTime() / 1000L);
    }

    /**
     * For each ecq check all output files exist
     * @throws IOException Thrown if expected output files do not exist
     */
    @Override
    public boolean validateOutput(Mecq.Sample sample) throws IOException {

        for(EcqArgs ecqArgs : sample.ecqArgList) {
            for (Library lib : ecqArgs.getOutputLibraries(sample)) {
                for (File file : lib.getFiles()) {
                    if (!file.exists()) {
                        log.error("MECQ job \"" + ecqArgs.name + "\" for sample \"" + sample.name + "\" did not produce the expected output file: " + file.getAbsolutePath());
                        return false;
                    }
                }
            }
        }

        return true;
    }


    public static class Sample {

        public List<EcqArgs> ecqArgList;
        public List<Library> libraries;
        public String name;
        public int failedAtStage;

        public Sample(List<EcqArgs> ecqArgList, List<Library> libraries, String name) {
            this.ecqArgList = ecqArgList;
            this.libraries = libraries;
            this.name = name;
            this.failedAtStage = -1;
        }
    }

    @Override
    public String getName() {
        return "MECQ";
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        Args args = this.getArgs();

        for(EcqArgs ecqArgs : args.samples.get(0).ecqArgList) {

            ReadEnhancer ec = ReadEnhancerFactory.create(ecqArgs.getTool(), this.conanExecutorService);

            if (ec == null) {
                throw new NullPointerException("Unidentified tool requested for MECQ run: " + ecqArgs.getTool());
            }

            if (ecqArgs.getCheckedArgs() != null && !ecqArgs.getCheckedArgs().trim().isEmpty()) {
                try {
                    ec.getReadEnchancerArgs().parse(ecqArgs.getCheckedArgs());
                } catch (IOException e) {
                    throw new IllegalArgumentException("Invalid or unrecognised checked arguments provided to " + ecqArgs.tool + " in " + ecqArgs.name, e);
                }
            }

            if (!ec.toConanProcess().isOperational(executionContext)) {
                log.warn("MECQ stage is NOT operational.");
                return false;
            }
        }

        log.info("MECQ stage is operational.");

        return true;
    }

    @Override
    public String getCommand() {
        return null;
    }

    public void createOutputLinks(File outputDir, ReadEnhancer ec, EcqArgs ecqArgs, Library library)
            throws ProcessExecutionException, InterruptedException {

        // Make sure the output directory exists
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        final ConanProcessService cps = this.conanExecutorService.getConanProcessService();

        List<File> outputs = ec == null ? library.getFiles() : ec.getEnhancedFiles();

        for(File file : outputs) {
            cps.createLocalSymbolicLink(file, new File(outputDir, file.getName()));
        }
    }


    public static class Args extends RampartProcessArgs {

        // Xml Config Keys
        public static final String KEY_ELEM_ECQ         = "ecq";


        /**
         * Set defaults
         */
        public Args() {
            super(RampartStage.MECQ);
        }

        public Args(Element ele, File outputDir, String jobPrefix, List<Mecq.Sample> samples, Organism organism, boolean runParallel) throws IOException {

            // Set defaults first
            super(RampartStage.MECQ, outputDir, jobPrefix, samples, organism, runParallel);

            // Check there's nothing
            if (!XmlHelper.validate(ele,
                    new String[0],
                    new String[]{
                            KEY_ATTR_PARALLEL
                    },
                    new String[]{
                            KEY_ELEM_ECQ
                    },
                    new String[0])) {
                throw new IOException("Found unrecognised element or attribute in MECQ");
            }

            // Set from Xml
            this.runParallel = ele.hasAttribute(KEY_ATTR_PARALLEL) ? XmlHelper.getBooleanValue(ele, KEY_ATTR_PARALLEL) : runParallel;

            // All libraries
            NodeList nodes = ele.getElementsByTagName(KEY_ELEM_ECQ);

            // Add ECQ information into samples list
            for(int i = 0; i < this.samples.size(); i++) {
                Mecq.Sample sample = this.samples.get(i);
                List<EcqArgs> ecqs = new ArrayList<>();

                for (int j = 0; j < nodes.getLength(); j++) {
                    File sampleDir = new File(this.outputDir, sample.name);
                    File stageDir = new File(sampleDir, this.stage.getOutputDirName());
                    String mecqJobPrefix = this.jobPrefix + "-" + sample.name + "-" + this.stage.getOutputDirName();
                    ecqs.add(new EcqArgs((Element) nodes.item(j), sample.libraries, stageDir, mecqJobPrefix, this.runParallel, j + 1));
                }
                sample.ecqArgList = ecqs;
            }
        }


        @Override
        public ParamMap getArgMap() {

            Params params = this.getParams();
            ParamMap pvp = new DefaultParamMap();

            pvp.put(params.getRunParallel(), Boolean.toString(this.runParallel));

            if (this.jobPrefix != null) {
                pvp.put(params.getJobPrefix(), this.jobPrefix);
            }

            return pvp;
        }

        @Override
        protected void setOptionFromMapEntry(ConanParameter param, String value) {

            Params params = this.getParams();

            if (param.equals(params.getJobPrefix())) {
                this.jobPrefix = value;
            } else if (param.equals(params.getRunParallel())) {
                this.runParallel = Boolean.parseBoolean(value);
            } else {
                throw new IllegalArgumentException("Unknown param found: " + param);
            }
        }

        @Override
        protected void setArgFromMapEntry(ConanParameter param, String value) {

        }

        @Override
        public List<ConanProcess> getExternalProcesses() {
            return new ArrayList<>();
        }

    }

    public static class EcqArgs {

        // **** Xml Config file property keys ****

        private static final String KEY_ATTR_LIBS = "libs";

        private static final String KEY_ATTR_NAME = "name";
        private static final String KEY_ATTR_TOOL = "tool";
        private static final String KEY_ATTR_THREADS = "threads";
        private static final String KEY_ATTR_MEMORY = "memory";
        private static final String KEY_ATTR_PARALLEL = "parallel";
        private static final String KEY_ATTR_CHECKED_ARGS = "checked_args";
        private static final String KEY_ATTR_UNCHECKED_ARGS = "unchecked_args";


        // **** Default values ****

        public static final int DEFAULT_THREADS = 1;
        public static final int DEFAULT_MEMORY = 0;
        public static final boolean DEFAULT_RUN_PARALLEL = false;

        public static final String RAW = "raw";


        // **** Class vars ****

        private String name;
        private String tool;
        private int threads;
        private int memory;
        private boolean runParallel;
        private List<Library> libraries;
        private File outputDir;
        private String jobPrefix;
        private String checkedArgs;
        private String uncheckedArgs;

        public EcqArgs() {
            this.name = "";
            this.tool = "";
            this.threads = DEFAULT_THREADS;
            this.memory = DEFAULT_MEMORY;
            this.runParallel = DEFAULT_RUN_PARALLEL;
            this.libraries = new ArrayList<>();
            this.outputDir = null;
            this.jobPrefix = "";
            this.checkedArgs = null;
            this.uncheckedArgs = null;
        }


        public EcqArgs(Element ele, List<Library> allLibraries, File parentOutputDir, String parentJobPrefix, boolean forceParallel, int index)
                throws IOException {

            // Set defaults
            this();

            // Check there's nothing
            if (!XmlHelper.validate(ele,
                    new String[] {
                            KEY_ATTR_NAME,
                            KEY_ATTR_TOOL
                    },
                    new String[] {
                            KEY_ATTR_LIBS,
                            KEY_ATTR_THREADS,
                            KEY_ATTR_MEMORY,
                            KEY_ATTR_PARALLEL,
                            KEY_ATTR_CHECKED_ARGS,
                            KEY_ATTR_UNCHECKED_ARGS
                    },
                    new String[0],
                    new String[0])) {
                throw new IOException("Found unrecognised element or attribute in MECQ job: " + index);
            }

            // Required
            if (!ele.hasAttribute(KEY_ATTR_NAME))
                throw new IOException("Could not find " + KEY_ATTR_NAME + " attribute in single mass element");

            if (!ele.hasAttribute(KEY_ATTR_TOOL))
                throw new IOException("Could not find " + KEY_ATTR_TOOL + " attribute in single mass element");

            this.name = XmlHelper.getTextValue(ele, KEY_ATTR_NAME);
            this.tool = XmlHelper.getTextValue(ele, KEY_ATTR_TOOL);

            // Check tool is recognised
            ReadEnhancer ec = ReadEnhancerFactory.create(this.tool, null);

            if (ec == null) {
                throw new IOException("Did not recognise tool name: " + this.tool + "; in ecq: " + this.name);
            }

            // Optional
            this.threads = ele.hasAttribute(KEY_ATTR_THREADS) ? XmlHelper.getIntValue(ele, KEY_ATTR_THREADS) : DEFAULT_THREADS;
            this.memory = ele.hasAttribute(KEY_ATTR_MEMORY) ? XmlHelper.getIntValue(ele, KEY_ATTR_MEMORY) : DEFAULT_MEMORY;
            this.runParallel = forceParallel ||
                    ele.hasAttribute(KEY_ATTR_PARALLEL) ? XmlHelper.getBooleanValue(ele, KEY_ATTR_PARALLEL) : DEFAULT_RUN_PARALLEL;
            this.checkedArgs = ele.hasAttribute(KEY_ATTR_CHECKED_ARGS) ?
                    XmlHelper.getTextValue(ele, KEY_ATTR_CHECKED_ARGS) :
                    null;

            this.uncheckedArgs = ele.hasAttribute(KEY_ATTR_UNCHECKED_ARGS) ?
                    XmlHelper.getTextValue(ele, KEY_ATTR_UNCHECKED_ARGS) :
                    null;

            // Filter the provided libs
            if (ele.hasAttribute(KEY_ATTR_LIBS)) {
                String libList = XmlHelper.getTextValue(ele, KEY_ATTR_LIBS);
                String[] libIds = libList.split(",");

                for (String libId : libIds) {
                    for (Library lib : allLibraries) {
                        if (lib.getName().equalsIgnoreCase(libId.trim())) {
                            this.libraries.add(lib);
                            break;
                        }
                    }
                }

                if (libIds.length != this.libraries.size()) {
                    throw new IllegalArgumentException("Could not find all the requested libraries for MECQ job: " + this.name);
                }
            }
            else if (allLibraries.size() == 1){    // In multi-sample mode or only one library available
                this.libraries.addAll(allLibraries);
            }
            else {
                throw new IllegalArgumentException("Didn't find \"libs\" attribute in ecq (required for single sample mode), and found multiple (" + allLibraries.size() + ") libraries (require exactly 1 library per sample for multi-sample mode).");
            }


            // Other args
            this.outputDir = new File(parentOutputDir, name);
            this.jobPrefix = parentJobPrefix + "-name";
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTool() {
            return tool;
        }

        public void setTool(String tool) {
            this.tool = tool;
        }

        public int getThreads() {
            return threads;
        }

        public void setThreads(int threads) {
            this.threads = threads;
        }

        public int getMemory() {
            return memory;
        }

        public void setMemory(int memory) {
            this.memory = memory;
        }

        public boolean isRunParallel() {
            return runParallel;
        }

        public void setRunParallel(boolean runParallel) {
            this.runParallel = runParallel;
        }

        public List<Library> getLibraries() {
            return libraries;
        }

        public void setLibraries(List<Library> libraries) {
            this.libraries = libraries;
        }

        public File getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(File outputDir) {
            this.outputDir = outputDir;
        }

        public String getJobPrefix() {
            return jobPrefix;
        }

        public void setJobPrefix(String jobPrefix) {
            this.jobPrefix = jobPrefix;
        }

        public String getCheckedArgs() {
            return checkedArgs;
        }

        public void setCheckedArgs(String checkedArgs) {
            this.checkedArgs = checkedArgs;
        }

        public String getUncheckedArgs() {
            return uncheckedArgs;
        }

        public void setUncheckedArgs(String uncheckedArgs) {
            this.uncheckedArgs = uncheckedArgs;
        }

        public Library getOutputLibrary(Sample sample, Library lib) {

            GenericReadEnhancerArgs genericArgs = new GenericReadEnhancerArgs();
            genericArgs.setInput(lib);
            genericArgs.setOutputDir(new File(this.outputDir, lib.getName()));
            genericArgs.setThreads(this.threads);
            genericArgs.setMemoryGb(this.memory);

            ReadEnhancer re = ReadEnhancerFactory.create(
                    this.tool,
                    genericArgs,
                    null);

            Library modLib = lib.copy();

            List<File> files = re.getEnhancedFiles();

            if (modLib.isPairedEnd()) {
                if (files.size() < 2) {
                    throw new IllegalArgumentException("Paired end library: " + modLib.getName() + " from " + this.name + " does not have at least two files");
                }

                modLib.setFiles(files.get(0), files.get(1));
            }
            else {
                if (files.size() != 1) {
                    throw new IllegalArgumentException("Single end library: " + modLib.getName() + " from " + this.name + " does not have one file");
                }

                modLib.setFiles(files.get(0), null);
            }

            return modLib;
        }

        public List<Library> getOutputLibraries(Sample sample) {

            List<Library> modLibs = new ArrayList<>();

            for(Library lib : this.getLibraries()) {
                modLibs.add(this.getOutputLibrary(sample, lib));
            }

            return modLibs;
        }

    }

}
