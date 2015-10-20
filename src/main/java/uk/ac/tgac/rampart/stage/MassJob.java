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
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionResult;
import uk.ac.ebi.fgpt.conan.core.context.DefaultTaskResult;
import uk.ac.ebi.fgpt.conan.core.param.ArgValidator;
import uk.ac.ebi.fgpt.conan.core.param.ParameterBuilder;
import uk.ac.ebi.fgpt.conan.core.param.PathParameter;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.model.context.*;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.util.StringJoiner;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.conan.process.asm.*;
import uk.ac.tgac.conan.process.asm.tools.AbyssV15;
import uk.ac.tgac.conan.process.subsampler.TgacSubsamplerV1;
import uk.ac.tgac.rampart.stage.util.CoverageRange;
import uk.ac.tgac.rampart.stage.util.ReadsInput;
import uk.ac.tgac.rampart.stage.util.VariableRange;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MassJob extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(MassJob.class);

    private List<ExecutionResult> jobResults;
    private TaskResult taskResult;

    public MassJob() {
        this(null);
    }

    public MassJob(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public MassJob(ConanExecutorService ces, Args args) {
        super("", args, new Params(), ces);
        this.jobResults = new ArrayList<>();
    }

    public Args getArgs() {
        return (Args)this.getProcessArgs();
    }

    public TaskResult getTaskResult() {
        return taskResult;
    }

    /**
     * Dispatches assembly jobs to the specified environments
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

            // Force run parallel to false if not using a scheduler
            if (!executionContext.usingScheduler()) {
                args.setRunParallel(false);
                if (args.isRunParallel()) {
                    log.warn("Forcing linear execution due to lack of job scheduler");
                }
            }

            log.info("Starting Single MASS run for \"" + args.getName() + "\"");

            Assembler genericAssembler = args.getGenericAssembler();

            // Create any required directories for this job
            this.createSupportDirectories(genericAssembler);
            log.debug("Created directories in: \"" + args.getOutputDir() + "\"");

            jobResults.clear();

            Map<Integer, List<Integer>> ssResults = new HashMap<>();

            // Iterate over coverage range, and do subsampling if required
            for (Integer cvg : args.getCoverageRange()) {

                // Do subsampling for this coverage level if required
                ssResults.put(cvg, this.doSubsampling(genericAssembler.doesSubsampling(), cvg, args.getSelectedLibs(),
                        args.isRunParallel()));
            }

            for(Assembler assembler : args.getAssemblers()) {

                AbstractAssemblerArgs asmArgs = assembler.getAssemblerArgs();
                File outputDir = asmArgs.getOutputDir();

                String title = args.getName() + "-" + outputDir.getName();

                log.debug("Starting '" + args.getTool() + "' in \"" + outputDir.getAbsolutePath() + "\"");

                // Make the output directory for this child job (delete the directory if it already exists)
                if (outputDir.exists()) {
                    FileUtils.deleteDirectory(outputDir);
                }
                outputDir.mkdirs();

                // Execute the assembler
                ExecutionResult result = this.executeAssembler(
                        assembler,
                        args.getJobPrefix() + "-assembly-" + outputDir.getName(),
                        ssResults.get(asmArgs.getDesiredCoverage()),
                        executionContext.usingScheduler() && (args.isMassParallel() || args.isRunParallel()));

                // Add assembler id to list
                result.setName(title);
                jobResults.add(result);

                // Create links for outputs from this assembler to known locations
                this.createAssemblyLinks(assembler, args, title);
            }

            // Check to see if we should run each MASS group in parallel, if not wait here until each MASS group has completed
            if (executionContext.usingScheduler() && !args.isMassParallel() && args.isRunParallel()) {
                log.info("Waiting for completion of: " + args.getName());
                this.conanExecutorService.executeScheduledWait(
                        jobResults,
                        args.getJobPrefix() + "-mass-*",
                        ExitStatus.Type.COMPLETED_ANY,
                        args.getJobPrefix() + "-wait",
                        args.getOutputDir());
            }

            // Finish
            log.info("Finished MASS group: \"" + args.getName() + "\"");

            stopWatch.stop();

            this.taskResult = new DefaultTaskResult("rampart-mass-" + args.name, true, this.jobResults, stopWatch.getTime() / 1000L);

            return new DefaultExecutionResult(
                    this.taskResult.getTaskName(),
                    0,
                    new String[] {},
                    null,
                    -1,
                    new ResourceUsage(this.taskResult.getMaxMemUsage(), this.taskResult.getActualTotalRuntime(), this.taskResult.getTotalExternalCputime()));

        } catch (IOException | ConanParameterException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        }
    }

    public List<Integer> getJobIds() {

        List<Integer> ids = new ArrayList<>();

        for(ExecutionResult res : this.jobResults) {
            ids.add(res.getJobId());
        }

        return ids;
    }


    private List<Integer> doSubsampling(boolean assemblerDoesSubsampling, int desiredCoverage,
                                        List<Library> libraries, boolean runParallel)
            throws IOException, InterruptedException, ProcessExecutionException, ConanParameterException {

        Args args = this.getArgs();

        // Check to see if we even need to do subsampling.  If not just return the current libraries.
        if (assemblerDoesSubsampling || desiredCoverage == CoverageRange.ALL) {
            return new ArrayList<Integer>();
        }

        // Try to create directory to contain subsampled libraries
        File subsamplingDir = new File(args.getOutputDir(), "subsampled_libs");

        if (!subsamplingDir.exists()) {
            if (!subsamplingDir.mkdirs()) {
                throw new IOException("Couldn't create subsampling directory for " + args.getName() + " in " + args.getOutputDir().getAbsolutePath());
            }
        }

        // Subsample each library
        List<Integer> jobIds = new ArrayList<>();

        for(Library lib : libraries) {

            Library subsampledLib = lib.copy();

            // Subsample to this coverage level if required
            long timestamp = System.currentTimeMillis();
            String fileSuffix = "_cvg-" + desiredCoverage + ".fastq";
            String jobPrefix = args.getJobPrefix() + "-subsample-" + lib.getName() + "-" + desiredCoverage + "x";

            subsampledLib.setName(lib.getName() + "-" + desiredCoverage + "x");

            final long genomeSize = args.getOrganism().getGenomeSize();

            if (subsampledLib.isPairedEnd()) {

                // This calculation is much quicker if library is uniform, in this case we just calculate from the number
                // of entries, otherwise we have to scan the whole file.
                long sequencedBases = lib.isUniform() ?
                        2 * lib.getReadLength() * this.getNbEntries(lib.getFile1(), subsamplingDir, jobPrefix + "-file1-line_count") :
                        this.getNbBases(lib.getFile1(), subsamplingDir, jobPrefix + "-file1-base_count") +
                                this.getNbBases(lib.getFile2(), subsamplingDir, jobPrefix + "-file2-base_count");

                // Calculate the probability of keeping an entry (assumes equal coverage in each file)
                double actualCoveragePerFile = (double)sequencedBases / (double)genomeSize / 2.0;

                double probability = desiredCoverage / 2.0 / actualCoveragePerFile;
                double probPerc = probability * 100.0;


                log.info("Estimated that library: " + lib.getName() + "; has approximately " + sequencedBases + " bases in both files.  " +
                        "Estimated genome size is: " + genomeSize + "; so actual coverage (per file) is approximately: " + actualCoveragePerFile +
                        "; we will only keep " + probPerc + "% of the reads in each file to achieve approximately " + desiredCoverage + "X coverage in both files");


                subsampledLib.setFiles(
                        new File(subsamplingDir, lib.getFile1().getName() + fileSuffix),
                        new File(subsamplingDir, lib.getFile2().getName() + fileSuffix)
                );

                ExecutionResult f1Result = this.executeSubsampler(probability, timestamp,
                        lib.getFile1(), subsampledLib.getFile1(), jobPrefix + "-file1", runParallel);

                ExecutionResult f2Result = this.executeSubsampler(probability, timestamp,
                        lib.getFile2(), subsampledLib.getFile2(), jobPrefix + "-file2", runParallel);

                jobIds.add(f1Result.getJobId());
                jobIds.add(f2Result.getJobId());
            }
            else {

                // This calculation is much quicker if library is uniform, in this case we just calculate from the number
                // of entries, otherwise we have to scan the whole file.
                long sequencedBases = lib.isUniform() ?
                        lib.getReadLength() * this.getNbEntries(lib.getFile1(), subsamplingDir, jobPrefix + "-file1-line_count") :
                        this.getNbBases(lib.getFile1(), subsamplingDir, jobPrefix + "-file1-base_count");

                // Calculate the probability of keeping an entry
                double actualCoverage = (double)sequencedBases / (double)genomeSize;
                double probability = desiredCoverage / actualCoverage;
                double probPerc = probability * 100.0;

                log.info("Estimated that library: " + lib.getName() + "; has approximately " + sequencedBases + " bases.  " +
                        "Estimated genome size is: " + genomeSize + "; so actual coverage is approximately: " + actualCoverage +
                        "; we will only keep " + probPerc + "% of the reads to achieve approximately " + desiredCoverage + "X coverage");

                subsampledLib.setFiles(
                        new File(subsamplingDir, lib.getFile1().getName() + fileSuffix),
                        null
                );

                ExecutionResult result = this.executeSubsampler(probability, timestamp,
                        lib.getFile1(), subsampledLib.getFile1(), jobPrefix, runParallel);

                jobIds.add(result.getJobId());
            }
        }

        return jobIds;
    }

    /**
     * Create output directories that contain symbolic links to to all the assemblies generated during this run
     * @param assembler The assembler for which to create support directories
     */
    protected void createSupportDirectories(Assembler assembler) {

        Args args = this.getArgs();

        // Create directory for links to assembled contigs
        if (assembler.makesUnitigs()) {
            args.getUnitigsDir().mkdir();
        }

        // Create directory for links to assembled contigs
        if (assembler.makesContigs()) {
            args.getContigsDir().mkdir();
        }

        // Create dir for scaffold links if this asm creates them
        if (assembler.makesScaffolds()) {
            args.getScaffoldsDir().mkdir();
        }

        args.getLongestDir().mkdir();
    }


    protected File getHighestStatsLevelDir() {

        Args args = this.getArgs();

        if (args.getScaffoldsDir().exists()) {
            return args.getScaffoldsDir();
        }
        else if (args.getContigsDir().exists()) {
            return args.getContigsDir();
        }
        else if (args.getUnitigsDir().exists()) {
            return args.getUnitigsDir();
        }

        return null;
    }


    @Override
    public String getName() {
        return "MASS";
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        Args args = this.getArgs();

        final String toolErrMsg = "\"Unidentified assembler \"" + args.getTool() + "\" requested for MASS job: \"" + args.getName() + "\"";

        Assembler asm = AssemblerFactory.createGenericAssembler(args.getTool(), this.conanExecutorService);

        if (asm == null) {
            throw new NullPointerException(toolErrMsg);
        }

        if (!validConfig(asm)) {
            log.warn("Assembler \"" + args.getTool() + "\" does not have a compatible MASS job configuration: \"" + args.getName() + "\"");
            return false;
        }

        if (args.getCheckedArgs() != null && !args.getCheckedArgs().trim().isEmpty()) {
            try {
                asm.getAssemblerArgs().parse(args.getCheckedArgs());
            } catch (IOException e) {
                throw new IllegalArgumentException("Invalid or unrecognised checked arguments provided to " + args.tool + " in " + args.name, e);
            }
        }

        if (!asm.isOperational(executionContext)) {

            log.warn("Assembler \"" + args.getTool() + "\" used in MASS job \"" + args.getName() + "\" is NOT operational.");
            return false;
        }

        log.info("Single MASS process \"" + args.getName() + "\" is operational.");

        return true;
    }

    private boolean validConfig(Assembler asm) {

        Args args = this.getArgs();

        if (asm.getType() == Assembler.Type.DE_BRUIJN || asm.getType() == Assembler.Type.DE_BRUIJN_OPTIMISER) {
            /*if (args.getKmerRange() == null) {
                throw new IllegalArgumentException("MASS job \"" + args.getName() + "\" is a De Bruijn graph assembler (or optimiser) but you have not specified a Kmer range to process in this MASS job.");
            }*/
        }
        else if (asm.getType() == Assembler.Type.DE_BRUIJN_AUTO || asm.getType() == Assembler.Type.OVERLAP_LAYOUT_CONSENSUS) {
            if (args.getKmerRange() != null) {
                log.warn("MASS job \"" + args.getName() + "\" does not support Kmer ranges, but you have specified one.");
                return false;
            }
        }

        return true;
    }

    @Override
    public String getCommand() {
        return null;
    }

    public long getNbEntries(File seqFile, File outputDir, String jobName) throws ProcessExecutionException, InterruptedException, IOException {

        return getCount(seqFile, outputDir, jobName, true);
    }

    public long getNbBases(File seqFile, File outputDir, String jobName) throws IOException, ProcessExecutionException, InterruptedException {

        return getCount(seqFile, outputDir, jobName, false);
    }

    protected long getCount(File seqFile, File outputDir, String jobName, boolean linesOnly) throws ProcessExecutionException, InterruptedException, IOException {

        File outputFile = new File(outputDir, seqFile.getName() + ".nb_entries.out");

        String wcOption = linesOnly ? "-l" : "-m";
        String command = "awk '/^@/{getline; print}' " + seqFile.getAbsolutePath() + " | wc " + wcOption + " > " + outputFile.getAbsolutePath();

        this.conanExecutorService.executeProcess(command, outputDir, jobName, 1, 0, 0, false);

        List<String> lines = FileUtils.readLines(outputFile);

        if (lines == null || lines.isEmpty()) {
            throw new IOException("Failed to retrieve number of lines in file: " + seqFile.getAbsolutePath());
        }

        return Long.parseLong(lines.get(0).trim());
    }

    public ExecutionResult executeAssembler(Assembler assembler, String jobName, List<Integer> jobIds, boolean runParallel)
            throws ProcessExecutionException, InterruptedException, IOException, ConanParameterException {

        // Important that this happens after directory cleaning.
        assembler.setup();

        Args args = this.getArgs();

        return this.conanExecutorService.executeProcess(
                assembler,
                assembler.getAssemblerArgs().getOutputDir(),
                jobName,
                args.getThreads(),
                args.getMemory(),
                args.getExpWallTimeMins(),
                runParallel,
                jobIds,
                assembler.usesOpenMpi());
    }

    public void createAssemblyLinks(Assembler assembler, Args jobArgs, String jobName)
            throws ProcessExecutionException, InterruptedException {

        ExecutionContext linkingExecutionContext = new DefaultExecutionContext(
                this.conanExecutorService.getExecutionContext().getLocality(), null, null);

        ConanProcessService cps = this.conanExecutorService.getConanProcessService();

        StringJoiner compoundLinkCmdLine = new StringJoiner(";");

        if (assembler.makesUnitigs()) {
            compoundLinkCmdLine.add(
                    cps.makeLinkCommand(assembler.getUnitigsFile(),
                            new File(jobArgs.getUnitigsDir(), jobName + "-unitigs.fa")));
        }

        if (assembler.makesContigs()) {
            compoundLinkCmdLine.add(
                    cps.makeLinkCommand(assembler.getContigsFile(),
                            new File(jobArgs.getContigsDir(), jobName + "-contigs.fa")));
        }

        if (assembler.makesScaffolds()) {
            compoundLinkCmdLine.add(
                    cps.makeLinkCommand(assembler.getScaffoldsFile(),
                            new File(jobArgs.getScaffoldsDir(), jobName + "-scaffolds.fa")));
        }

        compoundLinkCmdLine.add(
                cps.makeLinkCommand(assembler.getBestAssembly(),
                        new File(jobArgs.getLongestDir(), jobName + ".fa")));

        cps.execute(compoundLinkCmdLine.toString(), linkingExecutionContext);
    }


    public ExecutionResult executeSubsampler(double probability, long timestamp, File input, File output, String jobName,
                                             boolean runParallel)
            throws ProcessExecutionException, InterruptedException, IOException, ConanParameterException {

        TgacSubsamplerV1.Args ssArgs = new TgacSubsamplerV1.Args();
        ssArgs.setInputFile(input);
        ssArgs.setOutputFile(output);
        ssArgs.setLogFile(new File(output.getParentFile(), output.getName() + ".log"));
        ssArgs.setSeed(timestamp);
        ssArgs.setProbability(probability);

        TgacSubsamplerV1 ssProc = new TgacSubsamplerV1(ssArgs);

        // Create process
        return this.conanExecutorService.executeProcess(ssProc, output.getParentFile(), jobName, 1, 2000,
                this.conanExecutorService.usingScheduler() ? runParallel : false);
    }


    public static class Args extends AbstractProcessArgs {

        private static final String KEY_ELEM_INPUTS = "inputs";
        private static final String KEY_ELEM_SINGLE_INPUT = "input";
        private static final String KEY_ELEM_KMER_RANGE = "kmer";
        private static final String KEY_ELEM_CVG_RANGE = "coverage";
        private static final String KEY_ELEM_VAR_RANGE = "var";

        private static final String KEY_ATTR_NAME = "name";
        private static final String KEY_ATTR_TOOL = "tool";
        private static final String KEY_ATTR_THREADS = "threads";
        private static final String KEY_ATTR_MEMORY = "memory";
        private static final String KEY_ATTR_EXPWALLTIME = "exp_walltime";
        private static final String KEY_ATTR_PARALLEL = "parallel";
        private static final String KEY_ATTR_CHECKED_ARGS = "checked_args";
        private static final String KEY_ATTR_UNCHECKED_ARGS = "unchecked_args";

        public static final boolean DEFAULT_STATS_ONLY = false;
        public static final boolean DEFAULT_RUN_PARALLEL = false;
        public static final boolean DEFAULT_MASS_PARALLEL = false;
        public static final int DEFAULT_THREADS = 1;
        public static final int DEFAULT_MEMORY = 0;


        // Class vars
        private File outputDir;
        private String jobPrefix;

        private String name;
        private String tool;
        private KmerRange kmerRange;
        private CoverageRange coverageRange;
        private VariableRange variableRange;
        private Organism organism;
        private String checkedArgs;
        private String uncheckedArgs;
        private boolean kmerCalc;

        // Inputs
        private List<ReadsInput> inputs;
        private Mecq.Sample sample;

        // System settings
        private int threads;
        private int memory;
        private int expWallTimeMins;
        private boolean runParallel;
        private boolean massParallel;
        private List<Library> selectedLibs;

        // Temp vars
        private Assembler genericAssembler;
        private List<Assembler> assemblers;
        private boolean multiCoverageJob;
        private boolean multiKmerJob;


        public Args() {

            super(new Params());

            this.jobPrefix = "";

            this.tool = AbyssV15.NAME;
            this.kmerRange = null;
            this.coverageRange = new CoverageRange();
            this.variableRange = null;
            this.organism = null;
            this.checkedArgs = null;
            this.uncheckedArgs = null;
            this.kmerCalc = false;

            this.inputs = new ArrayList<>();
            this.sample = null;

            this.threads = 1;
            this.memory = 0;
            this.expWallTimeMins = 0;
            this.runParallel = DEFAULT_RUN_PARALLEL;
            this.massParallel = DEFAULT_MASS_PARALLEL;

            this.assemblers = new ArrayList<>();
            this.multiCoverageJob = false;
            this.multiKmerJob = false;
        }



        public Args(Element ele, File massDir, String parentJobPrefix,
                                Mecq.Sample sample, Organism organism,
                                boolean massParallel, int index, boolean kmerCalc) {

            // Set defaults
            this();

            // Check there's nothing unexpected in this element
            if (!XmlHelper.validate(ele,
                    new String[]{
                            KEY_ATTR_NAME,
                            KEY_ATTR_TOOL
                    },
                    new String[]{
                            KEY_ATTR_THREADS,
                            KEY_ATTR_MEMORY,
                            KEY_ATTR_PARALLEL,
                            KEY_ATTR_CHECKED_ARGS,
                            KEY_ATTR_UNCHECKED_ARGS
                    },
                    new String[]{
                            KEY_ELEM_INPUTS
                    },
                    new String[] {
                            KEY_ELEM_KMER_RANGE,
                            KEY_ELEM_CVG_RANGE,
                            KEY_ELEM_VAR_RANGE
                    })) {
                throw new IllegalArgumentException("Found unrecognised element or attribute in mass job: " + index);
            }

            // Required Attributes
            if (!ele.hasAttribute(KEY_ATTR_NAME))
                throw new IllegalArgumentException("Could not find " + KEY_ATTR_NAME + " attribute in MASS job element");

            if (!ele.hasAttribute(KEY_ATTR_TOOL))
                throw new IllegalArgumentException("Could not find " + KEY_ATTR_TOOL + " attribute in MASS job element");

            this.name = XmlHelper.getTextValue(ele, KEY_ATTR_NAME);
            this.tool = XmlHelper.getTextValue(ele, KEY_ATTR_TOOL);

            this.assemblers = new ArrayList<>();
            this.multiCoverageJob = false;
            this.multiKmerJob = false;
            this.kmerCalc = kmerCalc;

            // Required Elements
            Element inputElements = XmlHelper.getDistinctElementByName(ele, KEY_ELEM_INPUTS);
            NodeList actualInputs = inputElements.getElementsByTagName(KEY_ELEM_SINGLE_INPUT);
            for(int i = 0; i < actualInputs.getLength(); i++) {
                this.inputs.add(new ReadsInput((Element) actualInputs.item(i)));
            }

            // Optional
            this.threads = ele.hasAttribute(KEY_ATTR_THREADS) ?
                    XmlHelper.getIntValue(ele, KEY_ATTR_THREADS) :
                    DEFAULT_THREADS;

            this.memory = ele.hasAttribute(KEY_ATTR_MEMORY) ?
                    XmlHelper.getIntValue(ele, KEY_ATTR_MEMORY) :
                    DEFAULT_MEMORY;

            this.expWallTimeMins = ele.hasAttribute(KEY_ATTR_EXPWALLTIME) ?
                    XmlHelper.getIntValue(ele, KEY_ATTR_EXPWALLTIME) :
                    0;

            this.runParallel = ele.hasAttribute(KEY_ATTR_PARALLEL) ?
                    XmlHelper.getBooleanValue(ele, KEY_ATTR_PARALLEL) :
                    DEFAULT_RUN_PARALLEL;

            this.checkedArgs = ele.hasAttribute(KEY_ATTR_CHECKED_ARGS) ?
                    XmlHelper.getTextValue(ele, KEY_ATTR_CHECKED_ARGS) :
                    null;

            this.uncheckedArgs = ele.hasAttribute(KEY_ATTR_UNCHECKED_ARGS) ?
                    XmlHelper.getTextValue(ele, KEY_ATTR_UNCHECKED_ARGS) :
                    null;


            // Other args
            this.sample = sample;
            this.outputDir = new File(massDir, name);
            this.jobPrefix = parentJobPrefix + "-" + name;
            this.organism = organism;
            this.massParallel = massParallel;

            // Setup input and generic assembler
            this.genericAssembler = this.createGenericAssembler();
            this.selectedLibs = this.selectInputs(this.sample);
            this.genericAssembler.setLibraries(this.selectedLibs);

            Element kmerElement = XmlHelper.getDistinctElementByName(ele, KEY_ELEM_KMER_RANGE);
            this.kmerRange = kmerElement != null ?
                    new KmerRange(kmerElement, 35, KmerRange.getLastKmerFromLibs(selectedLibs), KmerRange.StepSize.COARSE.getStep()) :
                    null;

            Element cvgElement = XmlHelper.getDistinctElementByName(ele, KEY_ELEM_CVG_RANGE);
            this.coverageRange = cvgElement != null ?
                    new CoverageRange(cvgElement) :
                    new CoverageRange();

            Element varElement = XmlHelper.getDistinctElementByName(ele, KEY_ELEM_VAR_RANGE);
            this.variableRange = varElement != null ?
                    new VariableRange(varElement) :
                    null;
        }

        public final void initialise() {
            this.initialise(true);
        }

        protected final void initialise(boolean createGenericAssembler) {

            if (createGenericAssembler) {
                // Setup input and generic assembler
                this.genericAssembler = this.createGenericAssembler();
                this.selectedLibs = this.selectInputs(this.sample);
                this.genericAssembler.setLibraries(this.selectedLibs);
            }

            this.validateKmerRange();
            this.validateCoverageRange();
            this.validateVarRange();

            this.assemblers = this.createAssemblers();
        }

        public final Assembler createGenericAssembler() {

            Assembler genericAssembler = AssemblerFactory.createGenericAssembler(this.tool);

            if (genericAssembler == null)
                throw new IllegalArgumentException("Could not find assembler: " + this.tool + "; in MASS job: " + this.name);

            return genericAssembler;
        }

        public final List<Library> selectInputs(Mecq.Sample sample) {

            List<Library> selectedLibs = new ArrayList<>();

            for(ReadsInput mi : inputs) {
                Library lib = mi.findLibrary(sample.libraries);
                Mecq.EcqArgs ecqArgs = mi.findMecq(sample);

                if (lib == null) {
                    throw new IllegalArgumentException("Unrecognised library: " + mi.getLib() + "; not processing MASS run: " + name);
                }
                else if (mi.getEcq().equalsIgnoreCase(Mecq.EcqArgs.RAW)) {
                    selectedLibs.add(lib);
                }
                else if (ecqArgs == null) {
                    throw new IllegalArgumentException("Unrecognised MECQ dataset requested: " + mi.getEcq() + "; not processing MASS run: " + name);
                }
                else {
                    selectedLibs.add(ecqArgs.getOutputLibrary(sample, lib));
                }

                log.info("Found library.  Lib name: " + mi.getLib() + "; ECQ name: " + mi.getEcq() + "; Job name: " + name);
            }

            return selectedLibs;
        }

        protected final void validateKmerRange() {

            if (genericAssembler.getType() != Assembler.Type.DE_BRUIJN && genericAssembler.getType() != Assembler.Type.DE_BRUIJN_OPTIMISER) {
                this.kmerRange = null;  // Force to null
                log.warn("The selected assembler \"" + this.tool + "\" for job \"" + this.name + "\" does not support K parameter");
            }
            else if (kmerRange == null) {
                if (this.kmerCalc) {
                    log.info("Will calculate optimal kmer for: " + this.name);
                }
                else {
                    this.kmerRange = new KmerRange(35, KmerRange.getLastKmerFromLibs(selectedLibs), KmerRange.StepSize.COARSE);
                    log.info("No K-mer range specified for \"" + this.name + "\" and optimal kmer calculation was not requested, running assembler with default range: " + this.kmerRange.toString());
                }
            }
            else if (kmerRange.validate()) {
                log.info("K-mer range for \"" + this.name + "\" validated: " + kmerRange.toString());
            }
            else {
                throw new IllegalArgumentException("Invalid K-mer range: " + kmerRange.toString() + " Not processing MASS job: " + this.name);
            }
        }

        protected final void validateCoverageRange() {

            if (coverageRange == null) {
                CoverageRange defaultCoverageRange = new CoverageRange();
                log.info("No coverage range specified for \"" + this.name + "\" running assembler with default range: " + defaultCoverageRange.toString());
            }
            else if (organism == null || !organism.isGenomeSizeAvailable()) {
                if (organism == null) {
                    log.error("Organism not set");
                }
                else if (!organism.isGenomeSizeAvailable()) {
                   if (organism.getReference() == null) {
                       log.error("Reference not available");
                   }
                }

                CoverageRange defaultCoverageRange = new CoverageRange();
                log.warn("No genome size is available or specified.  Not possible to subsample to desired range without a genome " +
                        "size estimate. Running assembler with default coverage range: " + defaultCoverageRange.toString());
            }
            else if (coverageRange.validate()) {
                log.info("Coverage range for \"" + this.name + "\" validated: " + coverageRange.toString());
            }
            else {
                throw new IllegalArgumentException("Invalid coverage range: " + coverageRange.toString() + " Not processing MASS run: \"" + this.name + "\"");
            }
        }

        protected final void validateVarRange() {

            if (variableRange == null) {
                // Do nothing
            }
            else if (variableRange.getName() == null || variableRange.getName().isEmpty()) {
                log.warn("Variable Range requested but no name provided: " + variableRange.toString());
            }
            else if (variableRange.getValues() == null || variableRange.getValues().isEmpty()) {
                log.warn("Variable Range requested but not values provided: " + variableRange.toString());
            }
        }

        private List<Library> createSubsampledLibPaths(int coverage) {

            // Check to see if we even need to do subsampling.  If not just return the current libraries.
            if (this.genericAssembler.doesSubsampling() || coverage == CoverageRange.ALL) {
                return this.selectedLibs;
            }

            // Try to create directory to contain subsampled libraries
            File subsamplingDir = new File(this.outputDir, "subsampled_libs");

            // Subsample each library
            List<Library> subsampledLibs = new ArrayList<>();

            for(Library lib : this.selectedLibs) {

                Library subsampledLib = lib.copy();

                // Subsample to this coverage level if required
                String fileSuffix = "_cvg-" + coverage + ".fastq";

                subsampledLib.setName(lib.getName() + "-" + coverage + "x");

                if (subsampledLib.isPairedEnd()) {

                    subsampledLib.setFiles(
                            new File(subsamplingDir, lib.getFile1().getName() + fileSuffix),
                            new File(subsamplingDir, lib.getFile2().getName() + fileSuffix)
                    );
                }
                else {
                    subsampledLib.setFiles(
                            new File(subsamplingDir, lib.getFile1().getName() + fileSuffix),
                            null
                    );
                }

                subsampledLibs.add(subsampledLib);
            }

            return subsampledLibs;
        }

        private static class JobVars {

            private int cvg;
            private int k;
            private String varName;
            private String varValue;

            private JobVars(int cvg, int k, String varName, String varValue) {
                this.cvg = cvg;
                this.k = k;
                this.varName = varName;
                this.varValue = varValue;
            }

            public int getCvg() {
                return cvg;
            }

            public int getK() {
                return k;
            }

            public String getVarName() {
                return varName;
            }

            public String getVarValue() {
                return varValue;
            }
        }

        protected final List<JobVars> createJobVars(Assembler.Type asmType) {

            int[] cvg = null;

            if (this.coverageRange == null || this.coverageRange.isEmpty() || this.coverageRange.isAllOnly()) {
                cvg = new int[]{-1};
            }
            else {
                cvg = new int[this.coverageRange.size()];

                for(int i = 0; i < this.coverageRange.size(); i++) {
                    cvg[i] = this.coverageRange.get(i);
                }
            }

            int[] kmer = null;
            if (this.kmerRange == null || this.kmerRange.isEmpty() || asmType == Assembler.Type.DE_BRUIJN_OPTIMISER) {
                kmer = new int[]{0};
            }
            else {
                kmer = new int[this.kmerRange.size()];

                for(int i = 0; i < this.kmerRange.size(); i++) {
                    kmer[i] = this.kmerRange.get(i);
                }
            }

            String[] var = null;
            if (this.variableRange == null || this.variableRange.getValues() == null || this.variableRange.getValues().isEmpty()) {
                var = new String[]{""};
            }
            else {
                for (String val : this.variableRange.getValues()) {
                    var = new String[this.variableRange.getValues().size()];

                    for (int i = 0; i < this.variableRange.getValues().size(); i++) {
                        var[i] = this.variableRange.getValues().get(i);
                    }
                }
            }

            List<JobVars> list = new ArrayList<>();

            for(int i = 0; i < cvg.length; i++) {
                for (int j = 0; j < kmer.length; j++) {
                    for (int k = 0; k < var.length; k++) {

                        if (cvg[i] != -1) this.multiCoverageJob = true;
                        if (kmer[j] != 0) this.multiKmerJob = true;

                        list.add(new JobVars(cvg[i], kmer[j], this.variableRange == null ? "" : this.variableRange.getName(), var[k]));
                    }
                }
            }

            return list;
        }

        protected final List<Assembler> createAssemblers() {

            try {
                List<Assembler> assemblers = new ArrayList<>();

                Assembler.Type type = this.genericAssembler.getType();

                List<JobVars> jobs = this.createJobVars(type);

                for(JobVars jv : jobs) {

                    // Create the output directory name
                    String cvgName = this.multiCoverageJob ? "cvg-" + CoverageRange.toString(jv.cvg) : "";
                    String kmerName = this.multiKmerJob ? "k-" + jv.k : "";
                    String varName = (jv.varName != null && !jv.varName.isEmpty()) ?
                            (jv.varName + "-" + jv.varValue) : "";

                    List<String> dirNameParts = new ArrayList<>();
                    if (!cvgName.isEmpty())     dirNameParts.add(cvgName);
                    if (!kmerName.isEmpty())    dirNameParts.add(kmerName);
                    if (!varName.isEmpty())     dirNameParts.add(varName);
                    String dirName = dirNameParts.size() == 0 ?
                            this.name + "_output" :
                            StringUtils.join(dirNameParts, "_");


                    // Make sure we have a name to use if the user is doing any parameter optimisation
                    dirName = dirName.isEmpty() ? this.tool.toLowerCase() : dirName;

                    File jobOutputDir = new File(this.outputDir, dirName);

                    // Do subsampling for this coverage level if required
                    List<Library> libs = jv.cvg != -1 ?
                            this.createSubsampledLibPaths(jv.cvg) :
                            this.selectedLibs;

                    GenericAssemblerArgs asmArgs = new GenericAssemblerArgs();
                    asmArgs.setOutputDir(jobOutputDir);
                    asmArgs.setLibraries(libs);
                    asmArgs.setThreads(this.threads);
                    asmArgs.setMemory(this.memory);
                    asmArgs.setEstimatedWalltimeMins(this.expWallTimeMins);
                    asmArgs.setOrganism(this.organism);
                    asmArgs.setDesiredCoverage(jv.cvg);

                    Assembler asm = null;

                    if (type == Assembler.Type.DE_BRUIJN) {

                        GenericDeBruijnArgs dbgArgs = new GenericDeBruijnArgs(asmArgs);
                        dbgArgs.setK(jv.k);

                        // Create the actual assembler for these settings
                        asm = dbgArgs.createAssembler(this.tool);
                    }
                    else if (type == Assembler.Type.DE_BRUIJN_AUTO) {
                        asm = new GenericDeBruijnAutoArgs(asmArgs).createAssembler(this.tool);
                    }
                    else if (type == Assembler.Type.DE_BRUIJN_OPTIMISER) {

                        GenericDeBruijnOptimiserArgs dbgOptArgs = new GenericDeBruijnOptimiserArgs(asmArgs);
                        dbgOptArgs.setKmerRange(this.kmerRange);

                        // Create the actual assembler for these settings
                        asm = dbgOptArgs.createAssembler(this.tool);

                    } else {
                        throw new IllegalArgumentException("Unknown assembly type detected: " + type);
                    }

                    // Add the varname to checked args
                    String newCheckedArgs = (this.checkedArgs == null ? "" : this.checkedArgs) +
                            " -" + jv.varName + " " + jv.varValue;

                    // Record that we've done some command line parsing
                    /*if (!newCheckedArgs.isEmpty()) {
                        log.debug("Parsing: " + newCheckedArgs);
                    }*/

                    // Parse checked args
                    asm.getAssemblerArgs().parse(newCheckedArgs);

                    // Add unchecked args
                    asm.getAssemblerArgs().setUncheckedArgs(this.uncheckedArgs);

                    // Do a special check for ALLPATHs to ensure it has an overlapping paired end library provided
                    if (asm.getName().startsWith("AllpathsLg")) {
                        boolean foundFrag = false;
                        boolean foundJump = false;

                        for(Library lib : libs) {
                            if (lib.getType() == Library.Type.OVERLAPPING_PAIRED_END) {
                                foundFrag = true;
                            }
                            else if (lib.getType() == Library.Type.PAIRED_END || lib.getType() == Library.Type.MATE_PAIR) {
                                foundJump = true;
                            }
                        }

                        if (!foundFrag || !foundJump) {
                            throw new IOException("You requested an ALLPATHS run but it appears you do not have the correct kind of input data to drive it.  ALLPATHS requires at least one fragment (overlapping paired end) library and at least one jumping (paired end or mate pair) library.");
                        }
                    }

                    // Record assembler in list
                    assemblers.add(asm);
                }

                return assemblers;
            }
            catch(IOException ioe) {
                throw new IllegalArgumentException(ioe);
            }
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

        public KmerRange getKmerRange() {
            return kmerRange;
        }

        public void setKmerRange(KmerRange kmerRange) {
            this.kmerRange = kmerRange;
        }

        public boolean isKmerCalc() {
            return kmerCalc;
        }

        public void setKmerCalc(boolean kmerCalc) {
            this.kmerCalc = kmerCalc;
        }

        public CoverageRange getCoverageRange() {
            return coverageRange;
        }

        public void setCoverageRange(CoverageRange coverageRange) {
            this.coverageRange = coverageRange;
        }

        public VariableRange getVariableRange() {
            return variableRange;
        }

        public void setVariableRange(VariableRange variableRange) {
            this.variableRange = variableRange;
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

        public boolean isRunParallel() {
            return runParallel;
        }

        public void setRunParallel(boolean runParallel) {
            this.runParallel = runParallel;
        }

        public boolean isMassParallel() {
            return massParallel;
        }

        public void setMassParallel(boolean massParallel) {
            this.massParallel = massParallel;
        }

        public List<ReadsInput> getInputs() {
            return inputs;
        }

        public void setInputs(List<ReadsInput> inputs) {
            this.inputs = inputs;
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

        public int getExpWallTimeMins() {
            return expWallTimeMins;
        }

        public void setExpWallTimeMins(int expWallTimeMins) {
            this.expWallTimeMins = expWallTimeMins;
        }

        public Organism getOrganism() {
            return organism;
        }

        public void setOrganism(Organism organism) {
            this.organism = organism;
        }

        public Mecq.Sample getSample() {
            return sample;
        }

        public void setSample(Mecq.Sample sample) {
            this.sample = sample;
        }

        public File getUnitigsDir() {
            return new File(this.getOutputDir(), "unitigs");
        }

        public File getContigsDir() {
            return new File(this.getOutputDir(), "contigs");
        }

        public File getScaffoldsDir() {
            return new File(this.getOutputDir(), "scaffolds");
        }

        public File getLongestDir() {
            return new File(this.getOutputDir(), "longest");
        }

        public Assembler getGenericAssembler() {
            return this.genericAssembler;
        }

        public List<Assembler> getAssemblers() {
            return assemblers;
        }

        public void setAssemblers(List<Assembler> assemblers) {
            this.assemblers = assemblers;
        }

        public File getStatsFile(Mass.OutputLevel outputLevel) {

            File outputLevelStatsDir = null;

            if (outputLevel == Mass.OutputLevel.CONTIGS) {
                outputLevelStatsDir = this.getContigsDir();
            }
            else if (outputLevel == Mass.OutputLevel.SCAFFOLDS) {
                outputLevelStatsDir = this.getScaffoldsDir();
            }
            else {
                throw new IllegalArgumentException("Output Level not specified");
            }

            return new File(outputLevelStatsDir, "stats.txt");
        }

        @Override
        protected void setOptionFromMapEntry(ConanParameter param, String value) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        protected void setArgFromMapEntry(ConanParameter param, String value) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void parseCommandLine(CommandLine cmdLine) {

        }

        @Override
        public ParamMap getArgMap() {

            return null;
        }

        public List<Library> getSelectedLibs() {
            return selectedLibs;
        }
    }


    public static class Params extends Mass.Params {

        private ConanParameter config;
        private ConanParameter jobName;
        private ConanParameter outputDir;
        private ConanParameter jobPrefix;

        public Params() {

            this.config = new PathParameter(
                    "config",
                    "The rampart configuration file containing the libraries to assemble",
                    true);

            this.jobName = new ParameterBuilder()
                    .longName("job_name")
                    .description("The job name that distinguishes this MASS run from other mass runs that might be running in parallel.")
                    .argValidator(ArgValidator.DEFAULT)
                    .create();

            this.outputDir = new PathParameter(
                    "output",
                    "The output directory",
                    true);

            this.jobPrefix = new ParameterBuilder()
                    .longName("job_prefix")
                    .description("The job_prefix to be assigned to all sub processes in MASS.  Useful if executing with a scheduler.")
                    .argValidator(ArgValidator.DEFAULT)
                    .create();
        }

        public ConanParameter getConfig() {
            return config;
        }

        public ConanParameter getJobName() {
            return jobName;
        }

        public ConanParameter getOutputDir() {
            return outputDir;
        }

        public ConanParameter getJobPrefix() {
            return jobPrefix;
        }

        @Override
        public ConanParameter[] getConanParametersAsArray() {
            return (ConanParameter[])ArrayUtils.addAll(super.getConanParametersAsArray(),
                    new ConanParameter[]{
                            this.config,
                            this.jobName,
                            this.outputDir,
                            this.jobPrefix
            });
        }

    }

}
