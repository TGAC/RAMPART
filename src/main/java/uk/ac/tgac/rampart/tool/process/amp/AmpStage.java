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
package uk.ac.tgac.rampart.tool.process.amp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uk.ac.ebi.fgpt.conan.core.param.*;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.conan.process.asmIO.AssemblyEnhancer;
import uk.ac.tgac.conan.process.asmIO.AssemblyEnhancerFactory;
import uk.ac.tgac.conan.process.ec.AbstractErrorCorrector;
import uk.ac.tgac.rampart.tool.process.ReadsInput;
import uk.ac.tgac.rampart.tool.process.mecq.EcqArgs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: maplesod
 * Date: 16/08/13
 * Time: 15:52
 */
public class AmpStage extends AbstractConanProcess {


    private static Logger log = LoggerFactory.getLogger(AmpStage.class);

    private AmpStageExecutor ampStageExecutor;

    public AmpStage(Args args) {
        super("", args, new Params());
        this.ampStageExecutor = new AmpStageExecutorImpl();
    }

    public Args getAmpArgs() {
        return (Args)this.getProcessArgs();
    }

    /**
     * Dispatches amp stage to the specified environments
     *
     * @param executionContext The environment to dispatch jobs too
     * @throws IllegalArgumentException
     * @throws uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException
     * @throws InterruptedException
     */
    @Override
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        try {

            // Make a shortcut to the args
            Args args = (Args) this.getProcessArgs();

            // Initialise the object that makes system calls
            this.ampStageExecutor.initialise(this.getConanProcessService(), executionContext);


            log.info("Starting AMP stage " + args.getIndex());

            // Make sure reads file exists
            if (args.getInputAssembly() == null || !args.getInputAssembly().exists()) {
                throw new IOException("Input file for stage: " + args.getIndex() + " does not exist: " +
                        (args.getInputAssembly() == null ? "null" : args.getInputAssembly().getAbsolutePath()));
            }

            // Make sure the inputs are reasonable
            List<Library> selectedLibs = this.validateInputs(args.getIndex(), args.getInputs(), args.getAllLibraries(),
                    args.getAllMecqs());

            // Create output directory
            if (!args.getOutputDir().exists()) {
                args.getOutputDir().mkdir();
            }

            // Create the configuration for this stage
            AssemblyEnhancer ampProc = this.makeStage(args, selectedLibs);

            // Execute the AMP stage
            this.ampStageExecutor.executeAmpStage(ampProc, "amp-" + args.getIndex());

            // Create links for outputs from this assembler to known locations
            this.getConanProcessService().createLocalSymbolicLink(ampProc.getOutputFile(), args.getOutputFile());

            log.info("Finished AMP stage " + args.getIndex());
        }
        catch (IOException | ConanParameterException e) {
            throw new ProcessExecutionException(-1, e);
        }

        return true;
    }

    protected AssemblyEnhancer makeStage(Args args, List<Library> libs) throws IOException {

        AssemblyEnhancer proc = AssemblyEnhancerFactory.create(args.getTool(), args.getInputAssembly(), args.getOutputDir(), "amp-" + args.getIndex(),
                libs, args.getThreads(), args.getMemory(), args.getOtherArgs(), this.conanExecutorService);

        if (proc == null)
            throw new IOException("Could not find requested tool: " + args.getTool());

        return proc;
    }


    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public String getName() {
        // Make a shortcut to the args
        Args args = (Args) this.getProcessArgs();

        return args != null ? "AMP-" + args.getIndex() + " - " + args.getTool() : "Undefined-AMP-stage";
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        Args args = (Args)this.getProcessArgs();

        AssemblyEnhancer proc = null;

        try {
            proc = this.makeStage(args, null);
        } catch (IOException e) {
            log.warn("Could not create AMP stage");
            return false;
        }

        if (proc == null) {
            log.warn("Could not create AMP stage for tool: " + args.getTool() + "; check tool exists.");
            return false;
        }

        return proc.isOperational(executionContext);
    }

    protected List<Library> validateInputs(int ampIndex, List<ReadsInput> inputs, List<Library> allLibraries, List<EcqArgs> allMecqs) throws IOException {

        List<Library> selectedLibs = new ArrayList<>();

        for(ReadsInput mi : inputs) {
            Library lib = mi.findLibrary(allLibraries);
            EcqArgs ecqArgs = mi.findMecq(allMecqs);

            if (lib == null) {
                throw new IOException("Unrecognised library: " + mi.getLib() + "; not processing AMP stage: " + ampIndex);
            }

            if (ecqArgs == null) {
                if (mi.getEcq().equalsIgnoreCase(EcqArgs.RAW)) {
                    selectedLibs.add(lib);
                }
                else {
                    throw new IOException("Unrecognised MECQ dataset requested: " + mi.getEcq() + "; not processing AMP stage: " + ampIndex);
                }
            }
            else {
                Library modLib = lib.copy();

                AbstractErrorCorrector ec = ecqArgs.makeErrorCorrector(modLib);
                List<File> files = ec.getArgs().getCorrectedFiles();

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

                selectedLibs.add(modLib);
            }

            log.info("Found library.  Lib name: " + mi.getLib() + "; ECQ name: " + mi.getEcq() + "; Single MASS name: " + ampIndex);
        }

        return selectedLibs;
    }

    public static class Args extends AbstractProcessArgs {

        private static final String KEY_ELEM_TOOL = "tool";
        private static final String KEY_ATTR_THREADS = "threads";
        private static final String KEY_ATTR_MEMORY = "memory";
        private static final String KEY_ELEM_OTHER_ARGS = "args";
        private static final String KEY_ATTR_SELECTED_ASSEMBLY = "selected_assembly";
        private static final String KEY_ELEM_INPUTS = "inputs";
        private static final String KEY_ELEM_SINGLE_INPUT = "input";


        // Defaults
        public static final int DEFAULT_THREADS = 1;
        public static final int DEFAULT_MEMORY = 0;
        public static final String DEFAULT_OTHER_ARGS = "";


        // Common stuff
        private File outputDir;
        private File assembliesDir;
        private String jobPrefix;
        private List<Library> allLibraries;
        private List<EcqArgs> allMecqs;
        private Organism organism;

        // Specifics
        private String tool;
        private File inputAssembly;
        private List<ReadsInput> inputs;
        private int index;
        private int threads;
        private int memory;
        private String otherArgs;


        public Args() {

            super(new Params());

            this.tool = "";
            this.inputAssembly = null;
            this.index = 0;
            this.threads = DEFAULT_THREADS;
            this.memory = DEFAULT_MEMORY;
            this.otherArgs = DEFAULT_OTHER_ARGS;

            this.outputDir = new File("");
            this.jobPrefix = "AMP-" + this.index;
            this.inputs = null;
            this.allLibraries = null;
            this.allMecqs = null;
            this.organism = null;
        }

        public Args(Element ele, File outputDir, File assembliesDir, String jobPrefix, List<Library> allLibraries,
                            List<EcqArgs> allMecqs, Organism organism, File inputAssembly, int index) throws IOException {

            // Set defaults
            this();

            // Required
            if (!ele.hasAttribute(KEY_ELEM_TOOL))
                throw new IOException("Could not find " + KEY_ELEM_TOOL + " attribute in AMP stage element");

            this.tool = XmlHelper.getTextValue(ele, KEY_ELEM_TOOL);
            this.inputAssembly = ele.hasAttribute(KEY_ATTR_SELECTED_ASSEMBLY) ?
                    new File(XmlHelper.getTextValue(ele, KEY_ATTR_SELECTED_ASSEMBLY)) :
                    inputAssembly;

            // Required Elements
            Element inputElements = XmlHelper.getDistinctElementByName(ele, KEY_ELEM_INPUTS);
            NodeList actualInputs = inputElements.getElementsByTagName(KEY_ELEM_SINGLE_INPUT);
            for(int i = 0; i < actualInputs.getLength(); i++) {
                this.inputs.add(new ReadsInput((Element) actualInputs.item(i)));
            }

            // Optional
            this.threads = ele.hasAttribute(KEY_ATTR_THREADS) ? XmlHelper.getIntValue(ele, KEY_ATTR_THREADS) : DEFAULT_THREADS;
            this.memory = ele.hasAttribute(KEY_ATTR_MEMORY) ? XmlHelper.getIntValue(ele, KEY_ATTR_MEMORY) : DEFAULT_MEMORY;
            this.otherArgs = ele.hasAttribute(KEY_ELEM_OTHER_ARGS) ? XmlHelper.getTextValue(ele, KEY_ELEM_OTHER_ARGS) : DEFAULT_OTHER_ARGS;

            // Other args
            this.outputDir = outputDir;
            this.assembliesDir = assembliesDir;
            this.jobPrefix = jobPrefix;
            this.allLibraries = allLibraries;
            this.allMecqs = allMecqs;
            this.organism = organism;
            this.index = index;
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

        public String getJobPrefix() {
            return jobPrefix;
        }

        public void setJobPrefix(String jobPrefix) {
            this.jobPrefix = jobPrefix;
        }

        public List<Library> getAllLibraries() {
            return allLibraries;
        }

        public void setAllLibraries(List<Library> allLibraries) {
            this.allLibraries = allLibraries;
        }

        public List<EcqArgs> getAllMecqs() {
            return allMecqs;
        }

        public void setAllMecqs(List<EcqArgs> allMecqs) {
            this.allMecqs = allMecqs;
        }

        public Organism getOrganism() {
            return organism;
        }

        public void setOrganism(Organism organism) {
            this.organism = organism;
        }

        public String getTool() {
            return tool;
        }

        public void setTool(String tool) {
            this.tool = tool;
        }

        public File getInputAssembly() {
            return inputAssembly;
        }

        public void setInputAssembly(File inputAssembly) {
            this.inputAssembly = inputAssembly;
        }

        public File getOutputFile() {
            return new File(this.assembliesDir, "amp-stage-" + this.index + "-scaffolds.fa");
        }


        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public File getAssembliesDir() {
            return assembliesDir;
        }

        public void setAssembliesDir(File assembliesDir) {
            this.assembliesDir = assembliesDir;
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

        public String getOtherArgs() {
            return otherArgs;
        }

        public void setOtherArgs(String otherArgs) {
            this.otherArgs = otherArgs;
        }

        public List<ReadsInput> getInputs() {
            return inputs;
        }

        @Override
        public void parse(String args) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public ParamMap getArgMap() {

            Params params = this.getParams();

            ParamMap pvp = new DefaultParamMap();

            if (this.inputAssembly != null)
                pvp.put(params.getInput(), this.inputAssembly.getAbsolutePath());

            if (this.outputDir != null)
                pvp.put(params.getOutputDir(), this.outputDir.getAbsolutePath());

            if (this.jobPrefix != null)
                pvp.put(params.getJobPrefix(), this.jobPrefix);

            pvp.put(params.getThreads(), Integer.toString(this.threads));
            pvp.put(params.getMemory(), Integer.toString(this.memory));

            if (this.otherArgs != null)
                pvp.put(params.getOtherArgs(), this.otherArgs);

            return pvp;
        }

        @Override
        protected void setOptionFromMapEntry(ConanParameter param, String value) {

            Params params = this.getParams();

            if (param.equals(params.getInput())) {
                this.inputAssembly = new File(value);
            } else if (param.equals(params.getOutputDir())) {
                this.outputDir = new File(value);
            } else if (param.equals(params.getJobPrefix())) {
                this.jobPrefix = value;
            } else if (param.equals(params.getThreads())) {
                this.threads = Integer.parseInt(value);
            } else if (param.equals(params.getMemory())) {
                this.memory = Integer.parseInt(value);
            } else if (param.equals(params.getOtherArgs())) {
                this.otherArgs = value;
            }
        }

        @Override
        protected void setArgFromMapEntry(ConanParameter param, String value) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public static class Params extends AbstractProcessParams {

        private ConanParameter input;
        private ConanParameter outputDir;
        private ConanParameter jobPrefix;
        private ConanParameter threads;
        private ConanParameter memory;
        private ConanParameter otherArgs;

        public Params() {

            this.input = new PathParameter(
                    "input",
                    "The input assembly containing the assembly to enhance",
                    true
            );

            this.outputDir = new PathParameter(
                    "output",
                    "The output directory which should contain the enhancement steps",
                    true
            );

            this.jobPrefix = new ParameterBuilder()
                    .longName("job_prefix")
                    .description("Describes the jobs that will be executed as part of this pipeline")
                    .create();

            this.threads = new NumericParameter(
                    "threads",
                    "The number of threads to use for this AMP stage",
                    true
            );

            this.memory = new NumericParameter(
                    "memory",
                    "The amount of memory to request for this AMP stage",
                    true
            );

            this.otherArgs = new ParameterBuilder()
                    .longName("other_args")
                    .description("Any additional arguments to provide to this specific process")
                    .argValidator(ArgValidator.OFF)
                    .create();
        }

        public ConanParameter getInput() {
            return input;
        }

        public ConanParameter getOutputDir() {
            return outputDir;
        }

        public ConanParameter getJobPrefix() {
            return jobPrefix;
        }

        public ConanParameter getThreads() {
            return threads;
        }

        public ConanParameter getMemory() {
            return memory;
        }

        public ConanParameter getOtherArgs() {
            return otherArgs;
        }

        @Override
        public ConanParameter[] getConanParametersAsArray() {
            return new ConanParameter[]{
                    this.input,
                    this.outputDir,
                    this.jobPrefix,
                    this.threads,
                    this.memory,
                    this.otherArgs
            };
        }

    }

}
