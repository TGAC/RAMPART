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
package uk.ac.tgac.rampart.stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uk.ac.ebi.fgpt.conan.core.param.*;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.conan.process.re.GenericReadEnhancerArgs;
import uk.ac.tgac.conan.process.re.ReadEnhancer;
import uk.ac.tgac.conan.process.re.ReadEnhancerFactory;

import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 07/01/13
 * Time: 10:54
 * To change this template use File | Settings | File Templates.
 */
public class Mecq extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(Mecq.class);

    public Mecq() {
        this(null);
    }

    public Mecq(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public Mecq(ConanExecutorService ces, Args args) {
        super("", args, new Params(), ces);
    }


    public Args getArgs() {
        return (Args)this.getProcessArgs();
    }

    @Override
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {


        log.info("Starting MECQ Process");

        // Create shortcut to args for convienience
        Args args = this.getArgs();

        // If the output directory doesn't exist then make it
        if (!args.getMecqDir().exists()) {
            log.debug("Creating MECQ directory");
            args.getMecqDir().mkdirs();
            args.getOutputDir().mkdirs();
        }

        // Passthrough links for raw libraries to output
        for(Library lib : args.getLibraries()) {
            this.createOutputLinks(new File(args.getOutputDir(), "raw"), null, null, lib);
        }

        List<Integer> jobIds = new ArrayList<>();
        List<ReadEnhancer> readEnhancers = new ArrayList<>();

        // For each ecq process all libraries
        for(EcqArgs ecqArgs : args.getEqcArgList()) {

            // Process each lib
            for(Library lib : ecqArgs.getLibraries()) {

                // Create the output directory
                File ecqLibDir = new File(ecqArgs.getOutputDir(), lib.getName());
                ecqLibDir.mkdirs();

                // Create a job name
                String jobName = ecqArgs.getJobPrefix() + "_" + ecqArgs.getName() + "_" + lib.getName();

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

                // The job id should be stored in the process if we are using a scheduler, add to the list regardless
                // in case we need it later
                jobIds.add(result.getJobId());

                // Create links for outputs from this assembler to known locations
                this.createOutputLinks(new File(args.getOutputDir(), ecqArgs.getName()), readEnhancer, ecqArgs, lib);
            }

            // If we're using a scheduler, and we don't want to run separate ECQ in parallel, and we want to parallelise
            // each library processed by this ECQ, then wait here.
            if (executionContext.usingScheduler() && ecqArgs.isRunParallel() && !args.isRunParallel()) {
                log.debug("Waiting for completion of: " + ecqArgs.getName() + "; for all requested libraries");
                this.conanExecutorService.executeScheduledWait(
                        jobIds,
                        ecqArgs.getJobPrefix() + "*",
                        ExitStatus.Type.COMPLETED_SUCCESS,
                        args.getJobPrefix() + "-wait",
                        ecqArgs.getOutputDir());

                jobIds.clear();
            }
        }

        // If we're using a scheduler and we have been asked to run each MECQ group for each library
        // in parallel, then we should wait for all those to complete before continueing.
        if (executionContext.usingScheduler() && args.isRunParallel() && !args.getEqcArgList().isEmpty()) {
            log.debug("Running all ECQ groups in parallel, waiting for completion");
            this.conanExecutorService.executeScheduledWait(
                    jobIds,
                    args.getJobPrefix() + "-ecq*",
                    ExitStatus.Type.COMPLETED_SUCCESS,
                    args.getJobPrefix() + "-wait",
                    args.getMecqDir());
        }

        log.info("MECQ Finished");


        return true;
    }


    @Override
    public String getName() {
        return "MECQ";
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        Args args = this.getArgs();

        for(EcqArgs ecqArgs : args.getEqcArgList()) {

            ReadEnhancer ec = ReadEnhancerFactory.create(ecqArgs.getTool(), this.conanExecutorService);

            if (ec == null) {
                throw new NullPointerException("Unidentified tool requested for MECQ run: " + ecqArgs.getTool());
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


    /**
     * Modifies the libraries so that they contain they point to the files generated by the error corrector, rather than
     * the input files.
     * @param ec
     * @param lib
     * @param ecqArgs
     * @return
     * @throws ProcessExecutionException
     */
    protected Library modifyLib(ReadEnhancer ec, Library lib, EcqArgs ecqArgs) throws ProcessExecutionException {

        Library modLib = lib.copy();

        List<File> files = ec.getEnhancedFiles();

        try {
            if (modLib.isPairedEnd()) {
                if (files.size() < 2 || files.size() > 3) {
                    throw new IOException("Paired end library: " + modLib.getName() + " from " + ecqArgs.getName() + " does not have two or three files");
                }

                modLib.setFiles(files.get(0), files.get(1));
            }
            else {
                if (files.size() != 1) {
                    throw new IOException("Single end library: " + modLib.getName() + " from " + ecqArgs.getName() + " does not have one file");
                }

                modLib.setFiles(files.get(0), null);
            }

            return modLib;
        }
        catch(IOException ioe) {
            throw new ProcessExecutionException(3, ioe);
        }
    }


    public static class Args extends AbstractProcessArgs implements RampartStageArgs {

        // Xml Config Keys
        public static final String KEY_ATTR_PARALLEL    = "parallel";
        public static final String KEY_ELEM_ECQ         = "ecq";


        public static final boolean DEFAULT_RUN_PARALLEL = false;

        private File mecqDir;
        private String jobPrefix;
        private List<Library> libraries;
        private boolean runParallel;
        private List<EcqArgs> eqcArgList;


        /**
         * Set defaults
         */
        public Args() {

            super(new Params());

            this.mecqDir = new File("");
            this.eqcArgList = new ArrayList<>();
            this.libraries = new ArrayList<>();
            this.runParallel = DEFAULT_RUN_PARALLEL;

            Format formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String dateTime = formatter.format(new Date());
            this.jobPrefix = "qt-" + dateTime;
        }

        /**
         * Set from element and
         * @param ele
         */
        public Args(Element ele, File mecqDir, String jobPrefix, List<Library> libraries) throws IOException {

            // Set defaults first
            this();

            // Set from parameters
            this.mecqDir = mecqDir;
            this.jobPrefix = jobPrefix;
            this.libraries = libraries;

            // Set from Xml
            this.runParallel = ele.hasAttribute(KEY_ATTR_PARALLEL) ? XmlHelper.getBooleanValue(ele, KEY_ATTR_PARALLEL) : DEFAULT_RUN_PARALLEL;

            // All libraries
            NodeList nodes = ele.getElementsByTagName(KEY_ELEM_ECQ);
            for(int i = 0; i < nodes.getLength(); i++) {
                this.eqcArgList.add(new EcqArgs((Element)nodes.item(i), libraries, mecqDir, jobPrefix + "-ecq", this.runParallel));
            }
        }

        protected Params getParams() {
            return (Params)this.params;
        }

        public File getOutputDir() {
            return new File(this.mecqDir, "output");
        }

        public File getMecqDir() {
            return mecqDir;
        }

        public void setMecqDir(File mecqDir) {
            this.mecqDir = mecqDir;
        }

        public List<Library> getLibraries() {
            return libraries;
        }

        public void setLibraries(List<Library> libraries) {
            this.libraries = libraries;
        }

        public String getJobPrefix() {
            return jobPrefix;
        }

        public void setJobPrefix(String jobPrefix) {
            this.jobPrefix = jobPrefix;
        }

        public boolean isRunParallel() {
            return runParallel;
        }

        public void setRunParallel(boolean runParallel) {
            this.runParallel = runParallel;
        }

        public List<EcqArgs> getEqcArgList() {
            return eqcArgList;
        }

        public void setEqcArgList(List<EcqArgs> eqcArgList) {
            this.eqcArgList = eqcArgList;
        }

        @Override
        public void parse(String args) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public ParamMap getArgMap() {

            Params params = this.getParams();
            ParamMap pvp = new DefaultParamMap();

            if (this.mecqDir != null)
                pvp.put(params.getOutputDir(), this.mecqDir.getAbsolutePath());

            pvp.put(params.getRunParallel(), Boolean.toString(this.runParallel));

            if (this.jobPrefix != null) {
                pvp.put(params.getJobPrefix(), this.jobPrefix);
            }

            return pvp;
        }

        @Override
        protected void setOptionFromMapEntry(ConanParameter param, String value) {

            Params params = this.getParams();

            if (param.equals(params.getOutputDir())) {
                this.mecqDir = new File(value);
            } else if (param.equals(params.getJobPrefix())) {
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

        public static final String KEY_ELEM_LIBS = "libs";

        public static final String KEY_ATTR_NAME = "name";
        public static final String KEY_ATTR_TOOL = "tool";
        public static final String KEY_ATTR_THREADS = "threads";
        public static final String KEY_ATTR_MEMORY = "memory";
        public static final String KEY_ATTR_PARALLEL = "parallel";


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

        public EcqArgs() {
            this.name = "";
            this.threads = DEFAULT_THREADS;
            this.memory = DEFAULT_MEMORY;
            this.runParallel = DEFAULT_RUN_PARALLEL;
            this.libraries = new ArrayList<>();
        }


        public EcqArgs(Element ele, List<Library> allLibraries, File parentOutputDir, String parentJobPrefix, boolean forceParallel)
                throws IOException {

            // Set defaults
            this();

            // Required
            if (!ele.hasAttribute(KEY_ATTR_NAME))
                throw new IOException("Could not find " + KEY_ATTR_NAME + " attribute in single mass element");

            if (!ele.hasAttribute(KEY_ATTR_TOOL))
                throw new IOException("Could not find " + KEY_ATTR_TOOL + " attribute in single mass element");

            this.name = XmlHelper.getTextValue(ele, KEY_ATTR_NAME);
            this.tool = XmlHelper.getTextValue(ele, KEY_ATTR_TOOL);

            // Optional
            this.threads = ele.hasAttribute(KEY_ATTR_THREADS) ? XmlHelper.getIntValue(ele, KEY_ATTR_THREADS) : DEFAULT_THREADS;
            this.memory = ele.hasAttribute(KEY_ATTR_MEMORY) ? XmlHelper.getIntValue(ele, KEY_ATTR_MEMORY) : DEFAULT_MEMORY;
            this.runParallel = forceParallel ||
                    ele.hasAttribute(KEY_ATTR_PARALLEL) ? XmlHelper.getBooleanValue(ele, KEY_ATTR_PARALLEL) : DEFAULT_RUN_PARALLEL;

            // Filter the provided libs
            String libList = XmlHelper.getTextValue(ele, KEY_ELEM_LIBS);
            String[] libIds = libList.split(",");

            for(String libId : libIds) {
                for(Library lib : allLibraries) {
                    if (lib.getName().equalsIgnoreCase(libId.trim())) {
                        this.libraries.add(lib);
                        break;
                    }
                }
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

        public Library getOutputLibrary(Library lib) {

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
                if (files.size() < 2 || files.size() > 3) {
                    throw new IllegalArgumentException("Paired end library: " + modLib.getName() + " from " + this.name + " does not have two or three files");
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

        public List<Library> getOutputLibraries() {

            List<Library> modLibs = new ArrayList<>();

            for(Library lib : this.getLibraries()) {
                modLibs.add(this.getOutputLibrary(lib));
            }

            return modLibs;
        }

    }


    public static class Params extends AbstractProcessParams {

        private ConanParameter rampartConfig;
        private ConanParameter tool;
        private ConanParameter libs;
        private ConanParameter outputDir;
        private ConanParameter minLength;
        private ConanParameter minQuality;
        private ConanParameter kmer;
        private ConanParameter threads;
        private ConanParameter memoryGb;
        private ConanParameter createConfigs;
        private ConanParameter jobPrefix;
        private ConanParameter runParallel;
        private ConanParameter noQT;

        public Params() {

            this.rampartConfig = new PathParameter(
                    "qtConfig",
                    "The rampart configuration file describing the libraries to quality trim",
                    true);

            this.tool = new ParameterBuilder()
                    .longName("tool")
                    .description("The quality trimming tool to be used")
                    .isOptional(false)
                    .create();

            this.libs = new ParameterBuilder()
                    .longName("libs")
                    .description("The libraries to be quality trimmed")
                    .isOptional(false)
                    .argValidator(ArgValidator.OFF)
                    .create();

            this.outputDir = new PathParameter(
                    "qtOutput",
                    "The directory to place the quality trimmed libraries",
                    false);

            this.minLength = new NumericParameter(
                    "minLength",
                    "The minimum length for trimmed reads.  Any reads shorter than this value after trimming are discarded",
                    true);

            this.minQuality = new NumericParameter(
                    "minQuality",
                    "The minimum quality for trimmed reads.  Any reads with quality scores lower than this value will be trimmed.",
                    true);

            this.kmer = new NumericParameter(
                    "kmer",
                    "The kmer value to use for Kmer Frequency Spectrum based correction.  This is often a different value to that used in genome assembly.  See individual correction tool for details.  Default: 17",
                    true);

            this.threads = new NumericParameter(
                    "threads",
                    "The number of threads to use to process data.  Default: 8",
                    true);

            this.memoryGb = new NumericParameter(
                    "memory_gb",
                    "A figure used as a guide to run process in an efficient manner.  Normally it is sensible to slightly overestimate requirements if resources allow it.  Default: 20GB",
                    true);

            this.createConfigs = new FlagParameter(
                    "createConfigs",
                    "Whether or not to create separate RAMPART configuration files for RAW and QT datasets in the output directory");

            this.jobPrefix = new ParameterBuilder()
                    .longName("jobPrefix")
                    .description("If using a scheduler this prefix is applied to the job names of all child QT processes")
                    .create();

            this.runParallel = new FlagParameter(
                    "runParallel",
                    "If set to true, and we want to run QT in a scheduled execution context, then each library provided to this " +
                            "QT process will be executed in parallel.  A wait job will be executed in the foreground which will " +
                            "complete after all libraries have been quality trimmed");

            this.noQT = new FlagParameter(
                    "noQT",
                    "If set to true then we don't actually do any Quality trimming.  We still do everything else though, which " +
                            "includes creating the output directory and the RAW configuration file and symbolic links");
        }

        public ConanParameter getRampartConfig() {
            return rampartConfig;
        }

        public ConanParameter getTool() {
            return tool;
        }

        public ConanParameter getLibs() {
            return libs;
        }

        public ConanParameter getMinLength() {
            return minLength;
        }

        public ConanParameter getMinQuality() {
            return minQuality;
        }

        public ConanParameter getKmer() {
            return kmer;
        }

        public ConanParameter getThreads() {
            return threads;
        }

        public ConanParameter getMemoryGb() {
            return memoryGb;
        }

        public ConanParameter getNoQT() {
            return noQT;
        }

        public ConanParameter getOutputDir() {
            return outputDir;
        }

        public ConanParameter getCreateConfigs() {
            return createConfigs;
        }

        public ConanParameter getJobPrefix() {
            return jobPrefix;
        }

        public ConanParameter getRunParallel() {
            return runParallel;
        }


        @Override
        public ConanParameter[] getConanParametersAsArray() {
            return new ConanParameter[]{
                    this.rampartConfig,
                    this.tool,
                    this.libs,
                    this.minLength,
                    this.minQuality,
                    this.kmer,
                    this.threads,
                    this.memoryGb,
                    this.noQT,
                    this.outputDir,
                    this.createConfigs,
                    this.jobPrefix,
                    this.runParallel
            };
        }

    }

}
