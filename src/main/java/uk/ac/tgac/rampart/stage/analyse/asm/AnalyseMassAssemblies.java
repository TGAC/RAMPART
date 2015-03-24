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
import uk.ac.tgac.conan.process.asm.Assembler;
import uk.ac.tgac.rampart.stage.Mass;
import uk.ac.tgac.rampart.stage.MassJob;
import uk.ac.tgac.rampart.stage.analyse.asm.analysers.AssemblyAnalyser;

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


    public AnalyseMassAssemblies() {
        this(null);
    }

    public AnalyseMassAssemblies(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public AnalyseMassAssemblies(ConanExecutorService ces, Args args) {
        super("", args, new Params(), ces);
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

        // Create requested services
        Set<AssemblyAnalyser> requestedServices = this.getArgs().getAssemblyAnalysers();
        for(AssemblyAnalyser requestedService : requestedServices) {
            requestedService.setConanExecutorService(this.conanExecutorService);
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

        try {

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            Args args = this.getArgs();

            // Force run parallel to false if not using a scheduler
            if (!executionContext.usingScheduler() && args.isRunParallel()) {
                log.warn("Forcing linear execution due to lack of job scheduler");
                args.setRunParallel(false);
            }

            log.info("Starting Analysis of MASS assemblies");

            if (!args.getOutputDir().exists()) {
                args.getOutputDir().mkdirs();
            }

            // Create requested services
            Set<AssemblyAnalyser> requestedServices = args.getAssemblyAnalysers();
            for(AssemblyAnalyser requestedService : requestedServices) {
                requestedService.setConanExecutorService(this.conanExecutorService);
            }

            // Keep a list of all job ids
            List<ExecutionResult> jobResults = new ArrayList<>();

            List<File> unitigAssemblies = new ArrayList<>();
            List<File> contigAssemblies = new ArrayList<>();
            List<File> scaffoldAssemblies = new ArrayList<>();
            List<File> bestAssemblies = new ArrayList<>();
            List<File> bubbles = new ArrayList<>();
            List<String> mappings = new ArrayList<>();

            int index = 1;

            // Update mass job values with kmer genie info if required
            if (args.kmerCalcResults != null && args.kmerCalcResults.exists()) {
                Mass.setKmerValues(args.getKmerCalcResults(), args.getMassJobs());
            }

            // Loop through MASS groups to get assemblies
            for (MassJob.Args jobArgs : args.getMassJobs()) {

                jobArgs.initialise();

                String massGroup = jobArgs.getName();

                File inputDir = new File(args.getMassDir(), massGroup);

                if (!inputDir.exists()) {
                    throw new ProcessExecutionException(-1, "Could not find output from mass group: " + massGroup + "; at: " + inputDir.getAbsolutePath());
                }

                final File unitigsDir = jobArgs.getUnitigsDir();
                final File contigsDir = jobArgs.getContigsDir();
                final File scaffoldsDir = jobArgs.getScaffoldsDir();
                final File longestDir = jobArgs.getLongestDir();

                if (unitigsDir.exists()) {
                    unitigAssemblies.addAll(AnalyseMassAssemblies.assembliesFromDir(unitigsDir));
                }

                if (contigsDir.exists()) {
                    contigAssemblies.addAll(AnalyseMassAssemblies.assembliesFromDir(contigsDir));
                }

                if (scaffoldsDir.exists()) {
                    scaffoldAssemblies.addAll(AnalyseMassAssemblies.assembliesFromDir(scaffoldsDir));
                }

                bestAssemblies.addAll(AnalyseMassAssemblies.assembliesFromDir(longestDir));


                for (Assembler assembler : jobArgs.getAssemblers()) {

                    File assembly = assembler.getBestAssembly();

                    if (assembler.getBubbleFile() != null) {
                        bubbles.add(assembler.getBubbleFile());
                    }

                    String run = assembler.getAssemblerArgs().getOutputDir().getName();

                    String line = Integer.toString(index) + "\t" + massGroup + "\t" + run + "\t" + assembly.getAbsolutePath() + "\t" +
                            (assembler.getBubbleFile() != null ? assembler.getBubbleFile().getAbsolutePath() : "NA");

                    mappings.add(line);

                    index++;
                }
            }

            // Make symbolic links for easy access
            File unitigsDir = new File(args.getAssembliesDir(), "unitigs");
            File contigsDir = new File(args.getAssembliesDir(), "contigs");
            File scaffoldsDir = new File(args.getAssembliesDir(), "scaffolds");
            File bestDir = new File(args.getAssembliesDir(), "longest");
            File bubblesDir = new File(args.getAssembliesDir(), "bubbles");

            this.makeLinks(unitigAssemblies, unitigsDir);
            this.makeLinks(contigAssemblies, contigsDir);
            this.makeLinks(scaffoldAssemblies, scaffoldsDir);
            this.makeLinks(bestAssemblies, bestDir);
            this.makeLinks(bubbles, bubblesDir);

            // Write out linkage file
            FileUtils.writeLines(args.getAssemblyLinkageFile(), mappings);

            for(AssemblyAnalyser analyser : requestedServices) {

                File outputDir = new File(args.getOutputDir(), analyser.getName().toLowerCase());
                String jobPrefix = this.getArgs().getJobPrefix() + "-" + analyser.getName().toLowerCase();

                // Run analysis for each assembly grouping if fast.  Otherwise just do the highest order assemblies.
                if (analyser.isFast()) {

                    if (!unitigAssemblies.isEmpty()) {
                        jobResults.addAll(analyser.execute(
                                unitigAssemblies,
                                new File(outputDir, "unitigs"),
                                jobPrefix + "-unitigs",
                                this.conanExecutorService
                        ));
                    }

                    if (!contigAssemblies.isEmpty()) {
                        jobResults.addAll(analyser.execute(
                                contigAssemblies,
                                new File(outputDir, "contigs"),
                                jobPrefix + "-contigs",
                                this.conanExecutorService
                        ));
                    }

                    if (!scaffoldAssemblies.isEmpty()) {
                        jobResults.addAll(analyser.execute(
                                scaffoldAssemblies,
                                new File(outputDir, "scaffolds"),
                                jobPrefix + "-scaffolds",
                                this.conanExecutorService
                        ));
                    }
                }

                File bestOutDir = analyser.isFast() ? new File(outputDir, "longest") : outputDir;

                jobResults.addAll(analyser.execute(
                        bestAssemblies,
                        bestOutDir,
                        jobPrefix,
                        this.conanExecutorService
                ));
            }

            stopWatch.stop();

            TaskResult taskResult = new DefaultTaskResult("rampart-mass_analysis", true, jobResults, stopWatch.getTime() / 1000L);

            // Output the resource usage to file
            FileUtils.writeLines(new File(args.getOutputDir(), args.getJobPrefix() + ".summary"), taskResult.getOutput());

            return new DefaultExecutionResult(
                    taskResult.getTaskName(),
                    0,
                    new String[] {},
                    null,
                    -1,
                    new ResourceUsage(taskResult.getMaxMemUsage(), taskResult.getActualTotalRuntime(), taskResult.getTotalExternalCputime()));

        } catch (ConanParameterException | IOException e) {
            throw new ProcessExecutionException(4, e);
        }
    }

    protected void makeLinks(List<File> assemblies, File dir) throws InterruptedException, ProcessExecutionException {

        if (dir.exists()) {
            dir.delete();
        }
        dir.mkdirs();

        for(File asm : assemblies) {
            this.conanExecutorService.getConanProcessService().createLocalSymbolicLink(asm.getAbsoluteFile(), new File(dir, asm.getName()));
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

        private File massDir;
        private List<MassJob.Args> massJobs;
        private File kmerCalcResults;


        public Args() {
            super(new Params());

            this.massDir = null;
            this.massJobs = null;
            this.kmerCalcResults = null;

            this.setJobPrefix("analyse_mass");
        }

        public Args(Element element, File massDir, File analyseReadsDir, File outputDir, List<MassJob.Args> massJobs,
                               Organism organism, String jobPrefix, File kmerCalcResults, boolean doingReadKmerAnalysis) throws IOException {

            super(  new Params(),
                    element,
                    analyseReadsDir,
                    outputDir,
                    organism,
                    jobPrefix,
                    doingReadKmerAnalysis
                    );

            this.massDir = massDir;
            this.massJobs = massJobs;
            this.kmerCalcResults = kmerCalcResults;
        }

        public Params getParams() {
            return (Params)this.params;
        }

        public File getAssemblyLinkageFile() {
            return new File(this.getOutputDir(), "assembly_linkage.txt");
        }

        public File getMassDir() {
            return massDir;
        }

        public void setMassDir(File massDir) {
            this.massDir = massDir;
        }

        public List<MassJob.Args> getMassJobs() {
            return massJobs;
        }

        public void setMassJobs(List<MassJob.Args> massJobs) {
            this.massJobs = massJobs;
        }

        public File getKmerCalcResults() {
            return kmerCalcResults;
        }

        public void setKmerCalcResults(File kmerCalcResults) {
            this.kmerCalcResults = kmerCalcResults;
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
                    .longName("massJobs")
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
