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
import uk.ac.tgac.rampart.stage.*;
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
public class AnalyseMassAssemblies extends RampartProcess {

    private static Logger log = LoggerFactory.getLogger(AnalyseMassAssemblies.class);


    public AnalyseMassAssemblies() {
        this(null);
    }

    public AnalyseMassAssemblies(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public AnalyseMassAssemblies(ConanExecutorService ces, Args args) {
        super(ces, args);
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
    public TaskResult executeSample(Mecq.Sample sample, ExecutionContext executionContext)
            throws ProcessExecutionException, InterruptedException, IOException {

        try {

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            Args args = this.getArgs();

            // Keep a list of all job ids
            List<ExecutionResult> jobResults = new ArrayList<>();

            // Create requested services
            Set<AssemblyAnalyser> requestedServices = args.getAssemblyAnalysers();
            for (AssemblyAnalyser requestedService : requestedServices) {
                requestedService.setConanExecutorService(this.conanExecutorService);
            }

            List<File> unitigAssemblies = new ArrayList<>();
            List<File> contigAssemblies = new ArrayList<>();
            List<File> scaffoldAssemblies = new ArrayList<>();
            List<File> bestAssemblies = new ArrayList<>();
            List<File> bubbles = new ArrayList<>();
            List<String> mappings = new ArrayList<>();

            int index = 1;

            // Update mass job values with kmer genie info if required
            if (args.kmerCalcResults != null) {
                File kmerCalcFile = args.kmerCalcResults.getResultFile(sample);
                if (kmerCalcFile != null && kmerCalcFile.exists()) {
                    Mass.setKmerValues(kmerCalcFile, args.getMassJobs().get(sample));
                }
            }

            // Loop through MASS groups to get assemblies
            for (MassJob.Args jobArgs : args.getMassJobs().get(sample)) {

                jobArgs.initialise();

                String massGroup = jobArgs.getName();

                if (!jobArgs.getOutputDir().exists()) {
                    throw new ProcessExecutionException(-1, "Could not find output from mass group: " + massGroup + "; at: " + jobArgs.getOutputDir().getAbsolutePath());
                }

                final File unitigsInDir = jobArgs.getUnitigsDir();
                final File contigsInDir = jobArgs.getContigsDir();
                final File scaffoldsInDir = jobArgs.getScaffoldsDir();
                final File longestInDir = jobArgs.getLongestDir();

                if (unitigsInDir.exists()) {
                    unitigAssemblies.addAll(AnalyseMassAssemblies.assembliesFromDir(unitigsInDir));
                }

                if (contigsInDir.exists()) {
                    contigAssemblies.addAll(AnalyseMassAssemblies.assembliesFromDir(contigsInDir));
                }

                if (scaffoldsInDir.exists()) {
                    scaffoldAssemblies.addAll(AnalyseMassAssemblies.assembliesFromDir(scaffoldsInDir));
                }

                bestAssemblies.addAll(AnalyseMassAssemblies.assembliesFromDir(longestInDir));


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

                // Make symbolic links for easy access
                /*File bestDir = new File(args.getAssembliesDir(), "longest");
                File bubblesDir = new File(args.getAssembliesDir(), "bubbles");

                this.makeLinks(unitigAssemblies, unitigsDir);
                this.makeLinks(contigAssemblies, contigsDir);
                this.makeLinks(scaffoldAssemblies, scaffoldsDir);
                this.makeLinks(bestAssemblies, bestDir);
                this.makeLinks(bubbles, bubblesDir);*/

                // Write out linkage file
                FileUtils.writeLines(args.getAssemblyLinkageFile(sample), mappings);

                for (AssemblyAnalyser analyser : requestedServices) {

                    File analyserOutputDir = new File(args.getStageDir(sample), analyser.getName().toLowerCase());
                    String jobPrefix = this.getArgs().getJobPrefix() + "-" + analyser.getName().toLowerCase();

                    // Run analysis for each assembly grouping if fast.  Otherwise just do the highest order assemblies.
                    if (analyser.isFast()) {

                        if (!unitigAssemblies.isEmpty()) {
                            jobResults.addAll(analyser.execute(
                                    unitigAssemblies,
                                    new File(analyserOutputDir, "unitigs"),
                                    jobPrefix + "-unitigs",
                                    this.conanExecutorService
                            ));
                        }

                        if (!contigAssemblies.isEmpty()) {
                            jobResults.addAll(analyser.execute(
                                    contigAssemblies,
                                    new File(analyserOutputDir, "contigs"),
                                    jobPrefix + "-contigs",
                                    this.conanExecutorService
                            ));
                        }

                        if (!scaffoldAssemblies.isEmpty()) {
                            jobResults.addAll(analyser.execute(
                                    scaffoldAssemblies,
                                    new File(analyserOutputDir, "scaffolds"),
                                    jobPrefix + "-scaffolds",
                                    this.conanExecutorService
                            ));
                        }
                    }

                    File bestOutDir = analyser.isFast() ? new File(analyserOutputDir, "longest") : analyserOutputDir;

                    jobResults.addAll(analyser.execute(
                            bestAssemblies,
                            bestOutDir,
                            jobPrefix,
                            this.conanExecutorService
                    ));
                }
            }

            stopWatch.stop();

            return new DefaultTaskResult("rampart-mass_analysis", true, jobResults, stopWatch.getTime() / 1000L);
        }
        catch (ConanParameterException cpe) {
            throw new ProcessExecutionException(2, "Error processing analyser parameters", cpe);
        }
    }

    /**
     * Gets all the FastA files in the directory specified by the user.
     * @param inputDir The input directory containing assemblies
     * @return A list of fasta files in the user specified directory
     */
    public static List<File> assembliesFromDir(File inputDir) {
        return assembliesFromDir(inputDir, false);
    }

    /**
     * Gets all the FastA files in the directory specified by the user.
     * @param inputDir The input directory containing assemblies
     * @return A list of fasta files in the user specified directory
     */
    public static List<File> assembliesFromDir(File inputDir, boolean checkFileExists) {

        if (inputDir == null || !inputDir.exists())
            return null;

        List<File> fileList = new ArrayList<>();

        Collection<File> fileCollection = FileUtils.listFiles(inputDir, new String[]{"fa", "fasta"}, true);

        for(File file : fileCollection) {

            if (file.exists()) {
                fileList.add(file);
            }
        }

        Collections.sort(fileList);

        return fileList;
    }


    public static class Args extends AnalyseAssembliesArgs {

        private Map<Mecq.Sample, List<MassJob.Args>> massJobs;
        private CalcOptimalKmer.Args kmerCalcResults;


        public Args() {
            super(RampartStage.MASS_ANALYSIS);

            this.massJobs = null;
            this.kmerCalcResults = null;

            this.setJobPrefix("analyse_mass");
        }

        public Args(Element element, File outputDir, Map<Mecq.Sample, List<MassJob.Args>> massJobs,
                    Organism organism, String jobPrefix, CalcOptimalKmer.Args kmerCalcResults, boolean runParallel) throws IOException {

            super(  RampartStage.MASS_ANALYSIS,
                    element,
                    outputDir,
                    jobPrefix,
                    new ArrayList<>(massJobs.keySet()),
                    organism,
                    runParallel
                    );

            this.massJobs = massJobs;
            this.kmerCalcResults = kmerCalcResults;
        }

        public Params getParams() {
            return (Params)this.params;
        }

        public File getAssemblyLinkageFile(Mecq.Sample sample) {
            return new File(this.getStageDir(sample), "assembly_linkage.txt");
        }

        public Map<Mecq.Sample, List<MassJob.Args>> getMassJobs() {
            return massJobs;
        }

        public void setMassJobs(Map<Mecq.Sample, List<MassJob.Args>> massJobs) {
            this.massJobs = massJobs;
        }

        public CalcOptimalKmer.Args getKmerCalcResults() {
            return kmerCalcResults;
        }

        public void setKmerCalcResults(CalcOptimalKmer.Args kmerCalcResults) {
            this.kmerCalcResults = kmerCalcResults;
        }
    }

}
