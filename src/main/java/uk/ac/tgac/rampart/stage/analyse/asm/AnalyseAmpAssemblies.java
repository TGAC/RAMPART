package uk.ac.tgac.rampart.stage.analyse.asm;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionResult;
import uk.ac.ebi.fgpt.conan.core.context.DefaultTaskResult;
import uk.ac.ebi.fgpt.conan.core.param.ArgValidator;
import uk.ac.ebi.fgpt.conan.core.param.ParameterBuilder;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.ResourceUsage;
import uk.ac.ebi.fgpt.conan.model.context.TaskResult;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.stage.AmpStage;
import uk.ac.tgac.rampart.stage.analyse.asm.analysers.AssemblyAnalyser;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStats;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStatsTable;
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
public class AnalyseAmpAssemblies extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(AnalyseAmpAssemblies.class);

    SpiFactory<AssemblyAnalyser> assemblyAnalyserFactory;

    public AnalyseAmpAssemblies() {
        this(null);
    }

    public AnalyseAmpAssemblies(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public AnalyseAmpAssemblies(ConanExecutorService ces, Args args) {
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
        for(AnalyseAssembliesArgs.ToolArgs requestedService : this.getArgs().getTools()) {

            if (!this.assemblyAnalyserFactory.serviceAvailable(requestedService.getName())) {

                log.error("Could not find the specified assembly analysis service: " + requestedService);
                return false;
            }
            else {
                requestedServices.add(this.assemblyAnalyserFactory.create(requestedService.getName(), this.conanExecutorService));
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
    public ExecutionResult execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        log.info("Starting Analysis of AMP assemblies");

        Args args = this.getArgs();

        if (!args.getOutputDir().exists()) {
            args.getOutputDir().mkdirs();
        }

        // Create requested services
        Set<AssemblyAnalyser> requestedServices = new HashSet<>();
        for(AnalyseAssembliesArgs.ToolArgs requestedService : this.getArgs().getTools()) {
            AssemblyAnalyser aa = this.assemblyAnalyserFactory.create(requestedService.getName(), this.conanExecutorService);
            aa.setArgs(requestedService);
            requestedServices.add(aa);
        }

        List<ExecutionResult> jobResults = new ArrayList<>();

        // Just loop through all requested stats levels and execute each.
        // Each stage is processed linearly
        try {
            for(AssemblyAnalyser analyser : requestedServices) {

                List<File> assemblies = this.findAssemblies(analyser);
                File outputDir = new File(args.getOutputDir(), analyser.getName().toLowerCase());
                String jobPrefix = this.getArgs().getJobPrefix() + "-" + analyser.getName().toLowerCase();

                jobResults.addAll(analyser.execute(assemblies, outputDir, jobPrefix, this.conanExecutorService));
            }

            // Create the stats table with information derived from the configuration file.
            AssemblyStatsTable table = this.createTable();

            // Merge all the results
            for(AssemblyAnalyser analyser : requestedServices) {

                List<File> assemblies = this.findAssemblies(analyser);
                File outputDir = new File(args.getOutputDir(), analyser.getName().toLowerCase());

                Map<String, String> asm2GroupMap = new HashMap<>();
                for(File b : assemblies) {
                    asm2GroupMap.put(b.getName(), "amp");
                }

                analyser.updateTable(table, outputDir);
            }

            // Save table to disk
            File finalFile = new File(args.getOutputDir(), "scores.tab");
            table.save(finalFile);
            log.debug("Saved final results to disk at: " + finalFile.getAbsolutePath());

            stopWatch.stop();

            TaskResult taskResult = new DefaultTaskResult("rampart-amp_analysis", true, jobResults, stopWatch.getTime() / 1000L);

            // Output the resource usage to file
            FileUtils.writeLines(new File(args.getOutputDir(), args.getJobPrefix() + ".summary"), taskResult.getOutput());

            return new DefaultExecutionResult(
                    taskResult.getTaskName(),
                    0,
                    new String[] {},
                    null,
                    -1,
                    new ResourceUsage(taskResult.getMaxMemUsage(), taskResult.getActualTotalRuntime(), taskResult.getTotalExternalCputime()));
        }
        catch(ConanParameterException | IOException ioe) {
            throw new ProcessExecutionException(5, ioe);
        }

    }

    protected AssemblyStatsTable createTable() {

        Args args = this.getArgs();

        AssemblyStatsTable table = new AssemblyStatsTable();

        AssemblyStats stats = new AssemblyStats();
        stats.setIndex(0);
        stats.setDataset("amp");
        stats.setDesc("stage-0");
        stats.setFilePath(args.getAmpStages().get(0).getInputAssembly().getAbsolutePath());
        stats.setBubblePath("");//assembler.getBubbleFile() != null ? assembler.getBubbleFile().getAbsolutePath() : "");
        table.add(stats);

        int index = 1;
        for(AmpStage.Args asArgs : args.getAmpStages()) {

            stats = new AssemblyStats();
            stats.setIndex(index);
            stats.setDataset("amp");
            stats.setDesc("stage-" + index);
            stats.setFilePath(asArgs.getOutputFile().getAbsolutePath());
            stats.setBubblePath("");//assembler.getBubbleFile() != null ? assembler.getBubbleFile().getAbsolutePath() : "");
            table.add(stats);
            index++;
        }

        return table;
    }

    protected List<File> findAssemblies(AssemblyAnalyser analyser) throws ProcessExecutionException {

        Args args = this.getArgs();


        if (args.getAmpStages() == null || args.getAmpStages().isEmpty()) {
            return null;
        }

        File asmDir = args.getAmpStages().get(0).getAssembliesDir();

        if (analyser.isFast() || args.isAnalyseAll()) {
            return assembliesFromDir(asmDir);
        }
        else {
            AmpStage.Args finalStage = args.getAmpStages().get(args.getAmpStages().size() - 1);

            File assembly = finalStage.getOutputFile();

            if (!assembly.exists()) {
                throw new ProcessExecutionException(-1, "Could not find final output from AMP at: " + assembly.getAbsolutePath());
            }
            List<File> assemblies = new ArrayList<>();
            assemblies.add(assembly);
            return assemblies;
        }
    }

    /**
     * Gets all the FastA files in the directory specified by the user.
     * @param inputDir The input directory containing assemblies
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


    public static class Args extends AnalyseAssembliesArgs {

        private static final String KEY_ATTR_ALL = "analyse_all";

        public static final boolean DEFAULT_ANALYSE_ALL = false;

        private List<AmpStage.Args> ampStages;
        private boolean analyseAll;

        public Args() {
            super(new Params());

            this.ampStages = null;
            this.analyseAll = false;

            this.setJobPrefix("amp-analyses");
        }

        public Args(Element element, File analyseReadsDir, File outputDir, List<AmpStage.Args> ampStages,
                               Organism organism, String jobPrefix) throws IOException {

            super(  new Params(),
                    element,
                    analyseReadsDir,
                    outputDir,
                    organism,
                    jobPrefix
                    );

            this.analyseAll = element.hasAttribute(KEY_ATTR_ALL) ?
                    XmlHelper.getBooleanValue(element, KEY_ATTR_ALL) :
                    DEFAULT_ANALYSE_ALL;

            this.ampStages = ampStages;

        }

        public Params getParams() {
            return (Params)this.params;
        }

        public List<AmpStage.Args> getAmpStages() {
            return ampStages;
        }

        public void setAmpStages(List<AmpStage.Args> ampStages) {
            this.ampStages = ampStages;
        }

        public boolean isAnalyseAll() {
            return analyseAll;
        }

        public void setAnalyseAll(boolean analyseAll) {
            this.analyseAll = analyseAll;
        }
    }



    public static class Params extends AnalyseAssembliesParams {

        private ConanParameter ampStages;
        private ConanParameter analyseAll;

        public Params() {

            this.ampStages = new ParameterBuilder()
                    .longName("ampStages")
                    .description("A comma separated list of the amp stages that should be analysed")
                    .argValidator(ArgValidator.OFF)
                    .create();

            this.analyseAll = new ParameterBuilder()
                    .longName("analyseAll")
                    .description("Whether or not to run a complete analysis on all assemblies.  If set to false, then we only conduct long analyses on the final assembly.")
                    .argValidator(ArgValidator.OFF)
                    .isFlag(true)
                    .create();
        }

        public ConanParameter getAmpsStages() {
            return ampStages;
        }

        public ConanParameter getAnalyseAll() {
            return analyseAll;
        }

        @Override
        public ConanParameter[] getConanParametersAsArray() {
            return ArrayUtils.addAll(
                    super.getConanParametersAsArray(),
                    new ConanParameter[] {
                        this.ampStages,
                        this.analyseAll
                    });
        }
    }

}
