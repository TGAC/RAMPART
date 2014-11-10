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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionResult;
import uk.ac.ebi.fgpt.conan.core.param.*;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.ResourceUsage;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.conan.process.asmIO.AssemblyEnhancer;
import uk.ac.tgac.conan.process.asmIO.AssemblyEnhancerFactory;
import uk.ac.tgac.rampart.stage.util.ReadsInput;

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

    public AmpStage() {
        this(null);
    }

    public AmpStage(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public AmpStage(ConanExecutorService ces, Args args) {
        super("", args, new Params(), ces);
    }

    public Args getArgs() {
        return (Args)this.getProcessArgs();
    }

    /**
     * Dispatches amp stage to the specified environments
     *
     * @param executionContext The environment to dispatch jobs to
     * @throws ProcessExecutionException Thrown if there is an issue during execution of an external process
     * @throws InterruptedException Thrown if user has interrupted the process during execution
     */
    @Override
    public ExecutionResult execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        try {

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            // Make a shortcut to the args
            Args args = this.getArgs();

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

            // Set a suitable execution context
            ExecutionContext ecCopy = executionContext.copy();
            ecCopy.setContext("AMP-" + args.getIndex(), true, new File(args.getOutputDir(), "amp-" + args.getIndex() + ".log"));
            if (ecCopy.usingScheduler()) {
                ecCopy.getScheduler().getArgs().setThreads(args.getThreads());
                ecCopy.getScheduler().getArgs().setMemoryMB(args.getMemory());
            }

            // Do any setup for this process
            ampProc.setup();

            // Execute the AMP stage
            ExecutionResult result = ampProc.execute(ecCopy);

            if (!ampProc.getOutputFile().exists()) {
                throw new ProcessExecutionException(2, "AMP stage " + args.index + "\" did not produce an output file");
            }

            // Create links for outputs from this assembler to known locations
            this.getConanProcessService().createLocalSymbolicLink(ampProc.getOutputFile(), args.getOutputFile());

            stopWatch.stop();

            log.info("Finished AMP stage " + args.getIndex());

            return new DefaultExecutionResult(
                    Integer.toString(args.index) + "-" + args.tool,
                    0,
                    result.getOutput(),
                    null,
                    -1,
                    new ResourceUsage(result.getResourceUsage() == null ? 0 : result.getResourceUsage().getMaxMem(),
                            stopWatch.getTime() / 1000,
                            result.getResourceUsage() == null ? 0 : result.getResourceUsage().getCpuTime()));
        }
        catch (IOException | ConanParameterException e) {
            throw new ProcessExecutionException(-1, e);
        }
    }

    protected AssemblyEnhancer makeStage(Args args, List<Library> libs) throws IOException {

        return AssemblyEnhancerFactory.create(
                args.getTool(),
                args.getInputAssembly(),
                args.getBubbleFile() != null && args.getBubbleFile().exists() ? args.getBubbleFile() : null,
                args.getOutputDir(),
                "amp-" + args.getIndex(),
                libs, args.getThreads(),
                args.getMemory(),
                args.getCheckedArgs(),
                args.getUncheckedArgs(),
                this.conanExecutorService);
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
            log.warn("Could not create AMP stage " + args.getIndex() + " for tool: " + args.getTool() + "; check tool is installed and configured correctly.");
            return false;
        }

        if (proc == null) {
            log.warn("Could not create AMP stage " + args.getIndex() + " for tool: " + args.getTool() + "; Tool not recognised.  Check tool is supported and you have the correct spelling.");
            return false;
        }

        if (args.getCheckedArgs() != null && !args.getCheckedArgs().trim().isEmpty()) {
            try {
                proc.getAssemblyEnhancerArgs().parse(args.getCheckedArgs());
            } catch (IOException e) {
                throw new IllegalArgumentException("Invalid or unrecognised checked arguments provided to " + args.tool + " in stage " + args.index, e);
            }
        }

        return proc.isOperational(executionContext);
    }

    protected List<Library> validateInputs(int ampIndex, List<ReadsInput> inputs, List<Library> allLibraries, List<Mecq.EcqArgs> allMecqs) throws IOException {

        List<Library> selectedLibs = new ArrayList<>();

        for(ReadsInput mi : inputs) {
            Library lib = mi.findLibrary(allLibraries);
            Mecq.EcqArgs ecqArgs = mi.findMecq(allMecqs);

            if (lib == null) {
                throw new IOException("Unrecognised library: " + mi.getLib() + "; not processing AMP stage: " + ampIndex);
            }

            if (ecqArgs == null) {
                if (mi.getEcq().equalsIgnoreCase(Mecq.EcqArgs.RAW)) {
                    selectedLibs.add(lib);
                }
                else {
                    throw new IOException("Unrecognised MECQ dataset requested: " + mi.getEcq() + "; not processing AMP stage: " + ampIndex);
                }
            }
            else {
                selectedLibs.add(ecqArgs.getOutputLibrary(lib));
            }

            log.info("Found library.  Lib name: " + mi.getLib() + "; ECQ name: " + mi.getEcq());
        }

        return selectedLibs;
    }

    public static class Args extends AbstractProcessArgs {

        private static final String KEY_ATTR_TOOL = "tool";
        private static final String KEY_ATTR_THREADS = "threads";
        private static final String KEY_ATTR_MEMORY = "memory";
        private static final String KEY_ATTR_CHECKED_ARGS = "checked_args";
        private static final String KEY_ATTR_UNCHECKED_ARGS = "unchecked_args";
        private static final String KEY_ELEM_INPUTS = "inputs";
        private static final String KEY_ELEM_SINGLE_INPUT = "input";


        // Defaults
        public static final int DEFAULT_THREADS = 1;
        public static final int DEFAULT_MEMORY = 0;


        // Common stuff
        private File outputDir;
        private File assembliesDir;
        private String jobPrefix;
        private List<Library> allLibraries;
        private List<Mecq.EcqArgs> allMecqs;
        private Organism organism;

        // Specifics
        private String tool;
        private File inputAssembly;
        private File bubbleFile;
        private List<ReadsInput> inputs;
        private int index;
        private int threads;
        private int memory;
        private String checkedArgs;
        private String uncheckedArgs;


        public Args() {

            super(new Params());

            this.tool = "";
            this.inputAssembly = null;
            this.bubbleFile = null;
            this.index = 0;
            this.threads = DEFAULT_THREADS;
            this.memory = DEFAULT_MEMORY;
            this.checkedArgs = null;
            this.uncheckedArgs = null;

            this.outputDir = new File("");
            this.jobPrefix = "AMP-" + this.index;
            this.inputs = new ArrayList<>();
            this.allLibraries = null;
            this.allMecqs = null;
            this.organism = null;
        }

        public Args(Element ele, File outputDir, File assembliesDir, String jobPrefix, List<Library> allLibraries,
                            List<Mecq.EcqArgs> allMecqs, Organism organism, File inputAssembly, File bubbleFile, int index) throws IOException {

            // Set defaults
            this();

            // Check there's nothing unexpected in this element
            if (!XmlHelper.validate(ele,
                    new String[] {
                            KEY_ATTR_TOOL
                    },
                    new String[]{
                            KEY_ATTR_THREADS,
                            KEY_ATTR_MEMORY,
                            KEY_ATTR_CHECKED_ARGS,
                            KEY_ATTR_UNCHECKED_ARGS
                    },
                    new String[]{
                            KEY_ELEM_INPUTS
                    },
                    new String[0])) {
                throw new IllegalArgumentException("Found unrecognised element or attribute in AMP stage: " + index);
            }

            // Required
            if (!ele.hasAttribute(KEY_ATTR_TOOL))
                throw new IOException("Could not find " + KEY_ATTR_TOOL + " attribute in AMP stage element");

            this.tool = XmlHelper.getTextValue(ele, KEY_ATTR_TOOL);
            this.inputAssembly = inputAssembly;
            this.bubbleFile = bubbleFile;

            // Required Elements
            Element inputElements = XmlHelper.getDistinctElementByName(ele, KEY_ELEM_INPUTS);
            NodeList actualInputs = inputElements.getElementsByTagName(KEY_ELEM_SINGLE_INPUT);
            for(int i = 0; i < actualInputs.getLength(); i++) {
                ReadsInput ri = new ReadsInput((Element) actualInputs.item(i));

                if (!foundInLibs(ri.getLib(), allLibraries)) {
                    throw new IOException("Could not find library \"" + ri.getLib() + "\" in defined libraries");
                }

                if (!foundInMecq(ri.getEcq(), allMecqs)) {
                    throw new IOException("Could not find ECQ \"" + ri.getEcq() + "\" in defined set of ECQs");
                }

                this.inputs.add(ri);
            }

            // Optional
            this.threads = ele.hasAttribute(KEY_ATTR_THREADS) ? XmlHelper.getIntValue(ele, KEY_ATTR_THREADS) : DEFAULT_THREADS;
            this.memory = ele.hasAttribute(KEY_ATTR_MEMORY) ? XmlHelper.getIntValue(ele, KEY_ATTR_MEMORY) : DEFAULT_MEMORY;
            this.checkedArgs = ele.hasAttribute(KEY_ATTR_CHECKED_ARGS) ? XmlHelper.getTextValue(ele, KEY_ATTR_CHECKED_ARGS) : null;
            this.uncheckedArgs = ele.hasAttribute(KEY_ATTR_UNCHECKED_ARGS) ? XmlHelper.getTextValue(ele, KEY_ATTR_UNCHECKED_ARGS) : null;

            // Other args
            this.outputDir = outputDir;
            this.assembliesDir = assembliesDir;
            this.jobPrefix = jobPrefix;
            this.allLibraries = allLibraries;
            this.allMecqs = allMecqs;
            this.organism = organism;
            this.index = index;
        }

        private boolean foundInLibs(String libName, List<Library> ll) {

            for(Library s : ll) {
                if (libName.equalsIgnoreCase(s.getName())) {
                    return true;
                }
            }

            return false;
        }

        private boolean foundInMecq(String ecqName, List<Mecq.EcqArgs> ll) {

            if (ecqName.equalsIgnoreCase("raw")) {
                return true;
            }

            for(Mecq.EcqArgs s : ll) {
                if (ecqName.equalsIgnoreCase(s.getName())) {
                    return true;
                }
            }

            return false;
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

        public List<Mecq.EcqArgs> getAllMecqs() {
            return allMecqs;
        }

        public void setAllMecqs(List<Mecq.EcqArgs> allMecqs) {
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

        public File getBubbleFile() {
            return bubbleFile;
        }

        public void setBubbleFile(File bubbleFile) {
            this.bubbleFile = bubbleFile;
        }

        public File getOutputFile() {
            return new File(this.assembliesDir, "amp-stage-" + this.index + ".fa");
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

        public List<ReadsInput> getInputs() {
            return inputs;
        }

        @Override
        public void parseCommandLine(CommandLine cmdLine) {

            Params params = this.getParams();
        }

        @Override
        public ParamMap getArgMap() {

            Params params = this.getParams();

            ParamMap pvp = new DefaultParamMap();

            if (this.inputAssembly != null)
                pvp.put(params.getInput(), this.inputAssembly.getAbsolutePath());

            if (this.bubbleFile != null)
                pvp.put(params.getBubbleFile(), this.bubbleFile.getAbsolutePath());

            if (this.outputDir != null)
                pvp.put(params.getOutputDir(), this.outputDir.getAbsolutePath());

            if (this.jobPrefix != null)
                pvp.put(params.getJobPrefix(), this.jobPrefix);

            pvp.put(params.getThreads(), Integer.toString(this.threads));
            pvp.put(params.getMemory(), Integer.toString(this.memory));

            if (this.checkedArgs != null)
                pvp.put(params.getCheckedArgs(), this.checkedArgs);

            if (this.uncheckedArgs != null)
                pvp.put(params.getUncheckedArgs(), this.uncheckedArgs);

            return pvp;
        }

        @Override
        protected void setOptionFromMapEntry(ConanParameter param, String value) {

            Params params = this.getParams();

            if (param.equals(params.getInput())) {
                this.inputAssembly = new File(value);
            } else if (param.equals(params.getBubbleFile())) {
                this.bubbleFile = new File(value);
            } else if (param.equals(params.getOutputDir())) {
                this.outputDir = new File(value);
            } else if (param.equals(params.getJobPrefix())) {
                this.jobPrefix = value;
            } else if (param.equals(params.getThreads())) {
                this.threads = Integer.parseInt(value);
            } else if (param.equals(params.getMemory())) {
                this.memory = Integer.parseInt(value);
            } else if (param.equals(params.getCheckedArgs())) {
                this.checkedArgs = value;
            } else if (param.equals(params.getUncheckedArgs())) {
                this.uncheckedArgs = value;
            }
        }

        @Override
        protected void setArgFromMapEntry(ConanParameter param, String value) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public static class Params extends AbstractProcessParams {

        private ConanParameter input;
        private ConanParameter bubbleFile;
        private ConanParameter outputDir;
        private ConanParameter jobPrefix;
        private ConanParameter threads;
        private ConanParameter memory;
        private ConanParameter checkedArgs;
        private ConanParameter uncheckedArgs;

        public Params() {

            this.input = new PathParameter(
                    "input",
                    "The input assembly containing the assembly to enhance",
                    true
            );

            this.bubbleFile = new PathParameter(
                    "bubble",
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

            this.checkedArgs = new ParameterBuilder()
                    .longName("checked_args")
                    .description("Any additional arguments to provide to this specific process.  MUST be in posix format.  Will be checked by wrappers")
                    .argValidator(ArgValidator.OFF)
                    .create();

            this.uncheckedArgs = new ParameterBuilder()
                    .longName("checked_args")
                    .description("Any additional arguments to provide to this specific process.  Will NOT be checked by wrappers.  Will be passed as is to the underlying process.")
                    .argValidator(ArgValidator.OFF)
                    .create();
        }

        public ConanParameter getInput() {
            return input;
        }

        public ConanParameter getBubbleFile() {
            return bubbleFile;
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

        public ConanParameter getCheckedArgs() {
            return checkedArgs;
        }

        public ConanParameter getUncheckedArgs() {
            return uncheckedArgs;
        }

        @Override
        public ConanParameter[] getConanParametersAsArray() {
            return new ConanParameter[]{
                    this.input,
                    this.bubbleFile,
                    this.outputDir,
                    this.jobPrefix,
                    this.threads,
                    this.memory,
                    this.checkedArgs,
                    this.uncheckedArgs
            };
        }

    }

}
