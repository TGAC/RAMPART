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
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Organism;
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
public class AnalyseMassAssemblies extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(AnalyseMassAssemblies.class);

    SpiFactory<AssemblyAnalyser> assemblyAnalyserFactory;

    public AnalyseMassAssemblies() {
        this(null);
    }

    public AnalyseMassAssemblies(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public AnalyseMassAssemblies(ConanExecutorService ces, Args args) {
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

            // Keep a list of all job ids
            List<Integer> jobIds = new ArrayList<>();

            for(AssemblyAnalyser analyser : requestedServices) {

                // Loop through MASS groups
                for(MassJob.Args singleMassArgs : args.getMassGroups()) {

                    String massGroup = singleMassArgs.getName();

                    File inputDir = new File(args.getMassDir(), massGroup);

                    if (!inputDir.exists()) {
                        throw new ProcessExecutionException(-1, "Could not find output from mass group: " + massGroup + "; at: " + inputDir.getAbsolutePath());
                    }

                    File unitigsDir = new File(inputDir, "unitigs");
                    File contigsDir = new File(inputDir, "contigs");
                    File scaffoldsDir = new File(inputDir, "scaffolds");

                    File outputDir = new File(args.getOutputDir(), massGroup + "/" + analyser.getName().toLowerCase());
                    String jobPrefix = this.getArgs().getJobPrefix() + "-" + massGroup + "-" + analyser.getName().toLowerCase();

                    if (analyser.isFast()) {

                        if (unitigsDir.exists()) {
                            jobIds.addAll(analyser.execute(
                                    AnalyseMassAssemblies.assembliesFromDir(unitigsDir),
                                    new File(outputDir, "unitigs"),
                                    jobPrefix + "-unitigs",
                                    this.getArgs(),
                                    this.conanExecutorService
                            ));
                        }

                        if (contigsDir.exists()) {
                            jobIds.addAll(analyser.execute(
                                    AnalyseMassAssemblies.assembliesFromDir(contigsDir),
                                    new File(outputDir, "contigs"),
                                    jobPrefix + "-contigs",
                                    this.getArgs(),
                                    this.conanExecutorService
                            ));
                        }

                        if (scaffoldsDir.exists()) {
                            jobIds.addAll(analyser.execute(
                                    AnalyseMassAssemblies.assembliesFromDir(scaffoldsDir),
                                    new File(outputDir, "scaffolds"),
                                    jobPrefix + "-scaffolds",
                                    this.getArgs(),
                                    this.conanExecutorService
                            ));
                        }
                    }
                    else {
                        File seqDir = null;

                        // We only do the longest sequence types for slow processes
                        if (scaffoldsDir.exists()) {
                            seqDir = scaffoldsDir;
                        }
                        else if (contigsDir.exists()) {
                            seqDir = contigsDir;
                        }
                        else if (unitigsDir.exists()) {
                            seqDir = unitigsDir;
                        }
                        else {
                            throw new ProcessExecutionException(-2, "Could not find any output sequences for this mass group: " + massGroup);
                        }

                        jobIds.addAll(analyser.execute(
                                AnalyseMassAssemblies.assembliesFromDir(seqDir),
                                outputDir,
                                jobPrefix,
                                this.getArgs(),
                                this.conanExecutorService
                        ));
                    }
                }

            }

            // If we're using a scheduler and we have been asked to run
            // in parallel, then we should wait for all those to complete before continueing.
            if (this.conanExecutorService.usingScheduler() && args.isRunParallel() && !jobIds.isEmpty()) {
                log.debug("Analysing assemblies in parallel, waiting for completion");
                this.conanExecutorService.executeScheduledWait(
                        jobIds,
                        args.getJobPrefix() + "-*",
                        ExitStatus.Type.COMPLETED_ANY,
                        args.getJobPrefix() + "-cont-wait",
                        args.getOutputDir());
            }


        } catch (ConanParameterException | IOException e) {
            throw new ProcessExecutionException(4, e);
        }


        try {
            AssemblyStatsTable table = new AssemblyStatsTable();

            // Merge all the results
            for(AssemblyAnalyser analyser : requestedServices) {

                // Loop through MASS groups
                for(MassJob.Args singleMassArgs : args.getMassGroups()) {

                    String massGroup = singleMassArgs.getName();

                    File asmDirs = new File(args.getMassDir(), massGroup);
                    File aUnitigsDir = new File(asmDirs, "unitigs");
                    File aContigsDir = new File(asmDirs, "contigs");
                    File aScaffoldsDir = new File(asmDirs, "scaffolds");

                    File asmDir = null;
                    if (aScaffoldsDir.exists()) {
                        asmDir = aScaffoldsDir;
                    }
                    else if (aContigsDir.exists()) {
                        asmDir = aContigsDir;
                    }
                    else if (aUnitigsDir.exists()) {
                        asmDir = aUnitigsDir;
                    }
                    else {
                        throw new IllegalStateException("Could not find any output sequences for this mass group: " + massGroup);
                    }

                    File reportDir = new File(args.getOutputDir(), massGroup + "/" + analyser.getName().toLowerCase());


                    analyser.updateTable(table, assembliesFromDir(asmDir), reportDir, massGroup);
                }
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


    public static class Args extends AnalyseAssembliesArgs {

        private File massDir;
        private List<MassJob.Args> massGroups;

        public Args() {
            super(new Params());

            this.massDir = null;
            this.massGroups = null;
        }

        public Args(Element element, File massDir, File analyseReadsDir, File outputDir, List<MassJob.Args> massGroups,
                               Organism organism, String jobPrefix) {

            super(  new Params(),
                    element,
                    analyseReadsDir,
                    outputDir,
                    organism,
                    jobPrefix
                    );

            this.massDir = massDir;
            this.massGroups = massGroups;
        }

        public Params getParams() {
            return (Params)this.params;
        }

        public File getMassDir() {
            return massDir;
        }

        public void setMassDir(File massDir) {
            this.massDir = massDir;
        }

        public List<MassJob.Args> getMassGroups() {
            return massGroups;
        }

        public void setMassGroups(List<MassJob.Args> massGroups) {
            this.massGroups = massGroups;
        }

    }



    public static class Params extends AnalyseAssembliesParams {

        private ConanParameter massDir;
        private ConanParameter massGroups;

        public Params() {

            super();

            this.massDir = new ParameterBuilder()
                    .longName("massDir")
                    .isOptional(false)
                    .description("The location of the MASS output containing the assemblies to analyse")
                    .argValidator(ArgValidator.PATH)
                    .create();

            this.massGroups = new ParameterBuilder()
                    .longName("massGroups")
                    .description("A comma separated list of the mass groups that should be analysed")
                    .argValidator(ArgValidator.OFF)
                    .create();

        }

        public ConanParameter getMassDir() {
            return massDir;
        }

        public ConanParameter getMassGroups() {
            return massGroups;
        }

        @Override
        public ConanParameter[] getConanParametersAsArray() {
            return ArrayUtils.addAll(
                    super.getConanParametersAsArray(),
                    new ConanParameter[] {
                        this.massDir,
                        this.massGroups,
                    });
        }
    }

}
