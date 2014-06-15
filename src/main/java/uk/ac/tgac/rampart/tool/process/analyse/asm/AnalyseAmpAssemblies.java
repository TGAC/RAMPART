package uk.ac.tgac.rampart.tool.process.analyse.asm;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import uk.ac.ebi.fgpt.conan.core.param.ArgValidator;
import uk.ac.ebi.fgpt.conan.core.param.ParameterBuilder;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.tool.process.amp.AmpStage;
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

                List<File> assemblies = this.findAssemblies(analyser);
                File outputDir = new File(args.getOutputDir(), analyser.getName().toLowerCase());
                String jobPrefix = this.getArgs().getJobPrefix() + "-" + analyser.getName().toLowerCase();

                analyser.execute(assemblies, outputDir, jobPrefix, args, this.conanExecutorService);
            }
        } catch (ConanParameterException | IOException e) {
            throw new ProcessExecutionException(4, e);
        }


        try {
            AssemblyStatsTable table = new AssemblyStatsTable();

            // Merge all the results
            for(AssemblyAnalyser analyser : requestedServices) {

                List<File> assemblies = this.findAssemblies(analyser);
                File outputDir = new File(args.getOutputDir(), analyser.getName().toLowerCase());

                analyser.updateTable(table, assemblies, outputDir, "AMP");
            }

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

    protected List<File> findAssemblies(AssemblyAnalyser analyser) throws ProcessExecutionException {

        Args args = this.getArgs();

        List<File> assemblies = new ArrayList<>();

        if (analyser.isFast() || args.isAnalyseAll()) {

            // Loop through AMP stages
            for (AmpStage.Args ampStageArgs : args.getAmpStages()) {

                int index = ampStageArgs.getIndex();

                File assembly = ampStageArgs.getOutputFile();

                if (!assembly.exists()) {
                    throw new ProcessExecutionException(-1, "Could not find output from AMP stage: " + index + "; at: " + assembly.getAbsolutePath());
                }

                assemblies.add(assembly);
            }
        }
        else {
            AmpStage.Args finalStage = args.getAmpStages().get(args.getAmpStages().size() - 1);

            File assembly = finalStage.getOutputFile();

            if (!assembly.exists()) {
                throw new ProcessExecutionException(-1, "Could not find final output from AMP at: " + assembly.getAbsolutePath());
            }

            assemblies.add(assembly);
        }

        return assemblies;
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
                               Organism organism, String jobPrefix) {

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
