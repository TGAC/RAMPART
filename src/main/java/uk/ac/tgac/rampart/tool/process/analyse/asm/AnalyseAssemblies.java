package uk.ac.tgac.rampart.tool.process.analyse.asm;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import uk.ac.ebi.fgpt.conan.core.param.ArgValidator;
import uk.ac.ebi.fgpt.conan.core.param.ParameterBuilder;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.RampartCLI;
import uk.ac.tgac.rampart.tool.pipeline.rampart.RampartStageArgs;
import uk.ac.tgac.rampart.tool.process.analyse.asm.analysers.AssemblyAnalyser;
import uk.ac.tgac.rampart.tool.process.analyse.asm.selector.AssemblySelector;
import uk.ac.tgac.rampart.tool.process.analyse.asm.selector.DefaultAssemblySelector;
import uk.ac.tgac.rampart.tool.process.analyse.asm.stats.AssemblyStatsTable;
import uk.ac.tgac.rampart.tool.process.mass.MassJob;
import uk.ac.tgac.rampart.util.SpiFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 21/01/14
 * Time: 10:45
 * To change this template use File | Settings | File Templates.
 */
public class AnalyseAssemblies extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(AnalyseAssemblies.class);

    SpiFactory<AssemblyAnalyser> assemblyAnalyserFactory;

    public AnalyseAssemblies() {
        this(null);
    }

    public AnalyseAssemblies(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public AnalyseAssemblies(ConanExecutorService ces, Args args) {
        super("", args, new Params(), ces);

        this.assemblyAnalyserFactory = new SpiFactory<AssemblyAnalyser>(AssemblyAnalyser.class);
    }

    public Args getArgs() {
        return (Args)this.getProcessArgs();
    }

    @Override
    public String getName() {
        return "Analyse_Assemblies";
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        List<String> serviceNames = this.assemblyAnalyserFactory.listServices();

        // By using a set then we essentially ignore any duplications in the users input string
        Set<AssemblyAnalyser> requestedServices = new HashSet<>();

        // Create the requested subset of services
        for(String requestedService : this.getArgs().getAsmAnalyses()) {

            if (!this.assemblyAnalyserFactory.serviceAvailable(requestedService)) {

                log.error("Could not find the specified assembly analysis service: " + requestedService);
                return false;
            }
            else {
                requestedServices.add(this.assemblyAnalyserFactory.create(requestedService, this.getConanProcessService()));
            }
        }

        for(AssemblyAnalyser analyser : requestedServices) {

            if (!analyser.isOperational(executionContext)) {
                log.warn("Assembly Analyser: " + analyser.getName() + " is NOT operational");
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        Args args = this.getArgs();

        if (!args.getOutputDir().exists()) {
            args.getOutputDir().mkdirs();
        }

        // Create requested services
        Set<AssemblyAnalyser> requestedServices = new HashSet<>();
        for(String requestedService : this.getArgs().getAsmAnalyses()) {
            requestedServices.add(this.assemblyAnalyserFactory.create(requestedService, this.getConanProcessService()));
        }

        // Just loop through all requested stats levels and execute each.
        // Each stage is processed linearly
        try {
            for(AssemblyAnalyser analyser : requestedServices) {
                analyser.execute(args, conanExecutorService);
            }
        } catch (ConanParameterException | IOException e) {
            throw new ProcessExecutionException(4, e);
        }


        try {
            AssemblyStatsTable table = new AssemblyStatsTable();

            // Merge all the results
            for(AssemblyAnalyser analyser : requestedServices) {
                analyser.getStats(table, args);
            }

            // Select the assembly
            AssemblySelector assemblySelector = new DefaultAssemblySelector(args.getWeightings());
            File selectedAssembly = assemblySelector.selectAssembly(
                    table,
                    args.getOrganism().getEstGenomeSize(),
                    args.getOrganism().getEstGcPercentage());

            File outputAssembly = new File(args.getOutputDir(), "best.fa");

            log.info("Best assembly path: " + selectedAssembly.getAbsolutePath());

            // Create link to "best" assembly in stats dir
            this.getConanProcessService().createLocalSymbolicLink(selectedAssembly, outputAssembly);

            // Save table to disk
            File finalFile = new File(args.getOutputDir(), "scores.tab");
            table.save(finalFile);
            log.debug("Saved final results to disk at: " + finalFile.getAbsolutePath());
        }
        catch(IOException ioe) {
            throw new ProcessExecutionException(5, ioe);
        }

        return true;
    }



    /**
     * Gets all the FastA files in the directory specified by the user.
     * @param inputDir
     * @return A list of fasta files in the user specified directory
     */
    public static List<File> assembliesFromDir(File inputDir) {

        if (inputDir == null || !inputDir.exists())
            return null;

        List<File> fileList = new ArrayList<>();

        Collection<File> fileCollection = FileUtils.listFiles(inputDir, new String[]{"fa", "fasta"}, true);

        for(File file : fileCollection) {
            fileList.add(file);
        }

        Collections.sort(fileList);

        return fileList;
    }


    public static class Args extends AbstractProcessArgs implements RampartStageArgs {

        private static final String KEY_ATTR_TYPES = "types";
        private static final String KEY_ATTR_PARALLEL = "parallel";
        private static final String KEY_ATTR_THREADS = "threads";
        private static final String KEY_ATTR_WEIGHTINGS = "weightings_file";

        public static final boolean DEFAULT_RUN_PARALLEL = false;
        public static final int DEFAULT_THREADS = 1;

        public static final File    DEFAULT_SYSTEM_WEIGHTINGS_FILE = new File(RampartCLI.ETC_DIR, "weightings.tab");
        public static final File    DEFAULT_USER_WEIGHTINGS_FILE = new File(RampartCLI.USER_DIR, "weightings.tab");
        public static final File    DEFAULT_WEIGHTINGS_FILE = DEFAULT_USER_WEIGHTINGS_FILE.exists() ?
                DEFAULT_USER_WEIGHTINGS_FILE : DEFAULT_SYSTEM_WEIGHTINGS_FILE;


        private String[] asmAnalyses;
        private File massDir;
        private File analyseReadsDir;
        private List<MassJob.Args> massGroups;
        private File outputDir;
        private Organism organism;
        private File weightingsFile;
        private int threadsPerProcess;
        private boolean runParallel;
        private String jobPrefix;

        public Args() {
            super(new Params());

            this.asmAnalyses = null;
            this.massDir = null;
            this.analyseReadsDir = null;
            this.massGroups = null;
            this.outputDir = null;
            this.organism = null;
            this.weightingsFile = DEFAULT_WEIGHTINGS_FILE;
            this.threadsPerProcess = 1;
            this.runParallel = false;
            this.jobPrefix = "assembly-analyses";
        }

        public Args(Element element, File massDir, File analyseReadsDir, File outputDir, List<MassJob.Args> massGroups,
                               Organism organism, String jobPrefix) {

            super(new Params());

            this.massDir = massDir;
            this.analyseReadsDir = analyseReadsDir;
            this.outputDir = outputDir;
            this.massGroups = massGroups;
            this.organism = organism;
            this.jobPrefix = jobPrefix;

            this.asmAnalyses = element.hasAttribute(KEY_ATTR_TYPES) ?
                    XmlHelper.getTextValue(element, KEY_ATTR_TYPES).split(",") :
                    null;

            this.threadsPerProcess = element.hasAttribute(KEY_ATTR_THREADS) ?
                    XmlHelper.getIntValue(element, KEY_ATTR_THREADS) :
                    DEFAULT_THREADS;

            this.runParallel = element.hasAttribute(KEY_ATTR_PARALLEL) ?
                    XmlHelper.getBooleanValue(element, KEY_ATTR_PARALLEL) :
                    DEFAULT_RUN_PARALLEL;

            this.weightingsFile = element.hasAttribute(KEY_ATTR_WEIGHTINGS) ?
                    new File(XmlHelper.getTextValue(element, KEY_ATTR_WEIGHTINGS)) :
                    DEFAULT_WEIGHTINGS_FILE;

        }

        public Params getParams() {
            return (Params)this.params;
        }

        public String[] getAsmAnalyses() {
            return asmAnalyses;
        }

        public void setAsmAnalyses(String[] asmAnalyses) {
            this.asmAnalyses = asmAnalyses;
        }

        public File getMassDir() {
            return massDir;
        }

        public void setMassDir(File massDir) {
            this.massDir = massDir;
        }

        public File getAnalyseReadsDir() {
            return analyseReadsDir;
        }

        public void setAnalyseReadsDir(File analyseReadsDir) {
            this.analyseReadsDir = analyseReadsDir;
        }

        public List<MassJob.Args> getMassGroups() {
            return massGroups;
        }

        public void setMassGroups(List<MassJob.Args> massGroups) {
            this.massGroups = massGroups;
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

        public File getWeightings() {
            return weightingsFile;
        }

        public void setWeightings(File weightingsFile) {
            this.weightingsFile = weightingsFile;
        }

        public int getThreadsPerProcess() {
            return threadsPerProcess;
        }

        public void setThreadsPerProcess(int threadsPerProcess) {
            this.threadsPerProcess = threadsPerProcess;
        }

        public boolean isRunParallel() {
            return runParallel;
        }

        public void setRunParallel(boolean runParallel) {
            this.runParallel = runParallel;
        }

        public String getJobPrefix() {
            return jobPrefix;
        }

        public void setJobPrefix(String jobPrefix) {
            this.jobPrefix = jobPrefix;
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
        public void parse(String args) throws IOException {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public ParamMap getArgMap() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public List<ConanProcess> getExternalProcesses() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }



    public static class Params extends AbstractProcessParams {

        private ConanParameter statsLevels;
        private ConanParameter massDir;
        private ConanParameter analyseReadsDir;
        private ConanParameter massGroups;
        private ConanParameter outputDir;
        private ConanParameter organism;
        private ConanParameter weightingsFile;
        private ConanParameter threadsPerProcess;
        private ConanParameter runParallel;
        private ConanParameter jobPrefix;

        public Params() {

            this.statsLevels = new ParameterBuilder()
                    .longName("statsLevels")
                    .description("The type of assembly analysis to conduct.  Available options: " +
                            new SpiFactory<AssemblyAnalyser>(AssemblyAnalyser.class).listServicesAsString())
                    .argValidator(ArgValidator.OFF)
                    .create();

            this.massDir = new ParameterBuilder()
                    .longName("massDir")
                    .isOptional(false)
                    .description("The location of the MASS output containing the assemblies to analyse")
                    .argValidator(ArgValidator.PATH)
                    .create();

            this.analyseReadsDir = new ParameterBuilder()
                    .longName("analyseReadsDir")
                    .description("The location of the reads analysis.  This is required if you are conducting a kmer analysis")
                    .argValidator(ArgValidator.PATH)
                    .create();

            this.massGroups = new ParameterBuilder()
                    .longName("massGroups")
                    .description("A comma separated list of the mass groups that should be analysed")
                    .argValidator(ArgValidator.OFF)
                    .create();

            this.outputDir = new ParameterBuilder()
                    .longName("outputDir")
                    .description("The location that output from the assembly analyser module should be placed")
                    .argValidator(ArgValidator.PATH)
                    .create();

            this.organism = new ParameterBuilder()
                    .longName("organism")
                    .description("A description of the organism's genome")
                    .argValidator(ArgValidator.OFF)
                    .create();

            this.weightingsFile = new ParameterBuilder()
                    .longName("weightingsFile")
                    .isOptional(false)
                    .description("A file containing the weightings")
                    .argValidator(ArgValidator.PATH)
                    .create();

            this.threadsPerProcess = new ParameterBuilder()
                    .longName("threads")
                    .description("The number of threads to use for every child job.  Default: 1")
                    .argValidator(ArgValidator.DIGITS)
                    .create();

            this.runParallel = new ParameterBuilder()
                    .longName("runParallel")
                    .isFlag(true)
                    .description("Whether to execute child jobs in parallel when possible")
                    .argValidator(ArgValidator.OFF)
                    .create();

            this.jobPrefix = new ParameterBuilder()
                    .longName("jobPrefix")
                    .description("The scheduler job name prefix to apply to each spawned child job")
                    .argValidator(ArgValidator.DEFAULT)
                    .create();
        }

        public ConanParameter getStatsLevels() {
            return statsLevels;
        }

        public ConanParameter getMassDir() {
            return massDir;
        }

        public ConanParameter getAnalyseReadsDir() {
            return analyseReadsDir;
        }

        public ConanParameter getMassGroups() {
            return massGroups;
        }

        public ConanParameter getOutputDir() {
            return outputDir;
        }

        public ConanParameter getOrganism() {
            return organism;
        }

        public ConanParameter getWeightingsFile() {
            return weightingsFile;
        }

        public ConanParameter getThreadsPerProcess() {
            return threadsPerProcess;
        }

        public ConanParameter getRunParallel() {
            return runParallel;
        }

        public ConanParameter getJobPrefix() {
            return jobPrefix;
        }

        @Override
        public ConanParameter[] getConanParametersAsArray() {
            return new ConanParameter[] {
                    this.statsLevels,
                    this.massDir,
                    this.analyseReadsDir,
                    this.massGroups,
                    this.outputDir,
                    this.organism,
                    this.weightingsFile,
                    this.threadsPerProcess,
                    this.runParallel,
                    this.jobPrefix
            };
        }
    }

}
