package uk.ac.tgac.rampart.tool.process.analyse.asm;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.tool.process.analyse.asm.analysers.AssemblyAnalyser;
import uk.ac.tgac.rampart.tool.process.analyse.asm.selector.AssemblySelector;
import uk.ac.tgac.rampart.tool.process.analyse.asm.selector.DefaultAssemblySelector;
import uk.ac.tgac.rampart.tool.process.analyse.asm.stats.AssemblyStatsTable;
import uk.ac.tgac.rampart.tool.process.analyse.reads.kmer.KmerAnalysisReadsArgs;
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
public class AnalyseAsmsProcess extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(AnalyseAsmsProcess.class);

    private AnalyseAsmsExecutor analyseAsmsExecutor;
    SpiFactory<AssemblyAnalyser> assemblyAnalyserFactory;

    public AnalyseAsmsProcess() {
        this(new KmerAnalysisReadsArgs());
    }

    public AnalyseAsmsProcess(ProcessArgs args) {
        super("", args, new AnalyseAsmsParams());

        this.analyseAsmsExecutor = new AnalyseAsmsExecutorImpl();
        this.assemblyAnalyserFactory = new SpiFactory<AssemblyAnalyser>(AssemblyAnalyser.class);
    }

    public AnalyseAsmsArgs getArgs() {
        return (AnalyseAsmsArgs)this.getProcessArgs();
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
                requestedServices.add(this.assemblyAnalyserFactory.create(requestedService, this.conanProcessService));
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

        AnalyseAsmsArgs args = this.getArgs();

        // Initialise executor
        this.analyseAsmsExecutor.initialise(this.conanProcessService, executionContext);

        if (!args.getOutputDir().exists()) {
            args.getOutputDir().mkdirs();
        }

        // Create requested services
        Set<AssemblyAnalyser> requestedServices = new HashSet<>();
        for(String requestedService : this.getArgs().getAsmAnalyses()) {
            requestedServices.add(this.assemblyAnalyserFactory.create(requestedService, this.conanProcessService));
        }

        // Just loop through all requested stats levels and execute each.
        // Each stage is processed linearly
        try {
            for(AssemblyAnalyser analyser : requestedServices) {
                analyser.execute(args, executionContext, this.analyseAsmsExecutor);
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
            this.conanProcessService.createLocalSymbolicLink(selectedAssembly, outputAssembly);

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



}
