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
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import uk.ac.ebi.fgpt.conan.core.context.DefaultTaskResult;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.TaskResult;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.conan.process.asm.Assembler;
import uk.ac.tgac.conan.process.asm.stats.KmerGenieV16;
import uk.ac.tgac.rampart.stage.util.ReadsInput;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by maplesod on 06/10/14.
 */
public class CalcOptimalKmer extends RampartProcess {

    private static Logger log = LoggerFactory.getLogger(Mass.class);

    private Map<String,Integer> mass2OptimalKmerMap;
    private Map<String, File> kg2FileMap;


    public CalcOptimalKmer() {
        this(null);
    }

    public CalcOptimalKmer(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public CalcOptimalKmer(ConanExecutorService ces, Args args) {
        super(ces, args);
        this.mass2OptimalKmerMap = new HashMap<>();
        this.kg2FileMap = new HashMap<>();
    }

    public Args getArgs() {
        return (Args) this.getProcessArgs();
    }

    @Override
    public String getName() {
        return "kmer_calc";
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        if (!new KmerGenieV16(this.conanExecutorService).isOperational(executionContext)) {
            log.warn("MASS kmer optimisation is NOT operational.");
            return false;
        }

        log.info("MASS kmer optimisation stage is operational.");

        return true;
    }

    @Override
    public TaskResult executeSample(Mecq.Sample sample, ExecutionContext executionContext)
            throws ProcessExecutionException, InterruptedException, IOException {


        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Args args = this.getArgs();

        List<ExecutionResult> results = new ArrayList<>();

        this.kg2FileMap.clear();
        this.mass2OptimalKmerMap.clear();

        // Execute each config
        for (Map.Entry<String, List<Library>> entry : args.getKg2inputsMap().entrySet()) {

            File kgOutputDir = new File(args.getStageDir(sample), entry.getKey());
            File kgOutputFile = new File(kgOutputDir, "kmergenie_results.log");

            kg2FileMap.put(entry.getKey(), kgOutputFile);

            // Ensure output directory for this MASS run exists
            if (!kgOutputDir.exists() && !kgOutputDir.mkdirs()) {
                throw new IOException("Couldn't create kmer genie output directory at: " + kgOutputDir.getAbsolutePath());
            }

            ExecutionResult result = this.executeKmerGenie(kgOutputDir, kgOutputFile, entry.getValue());
            result.setName("kmergenie-" + entry.getKey());
            results.add(result);
        }

        stopWatch.stop();

        return new DefaultTaskResult(sample.name + "-" + args.stage.getOutputDirName(), true, results, stopWatch.getTime() / 1000L);
    }

    /**
     * Calculates the best kmer to use for each dataset
     * @throws IOException Thrown if output files do not exist
     */
    @Override
    public boolean validateOutput(Mecq.Sample sample) throws IOException {

        Args args = this.getArgs();

        // Retrieve kmer genie results
        for (MassJob.Args massJobArgs : args.massJobArgMap.get(sample)) {
            String massJobName = massJobArgs.getName();
            String kgName = args.mass2kgMap.get(massJobName);

            if (kgName != null) {
                int bestKmer = KmerGenieV16.Args.getBestKmer(kg2FileMap.get(kgName));

                if (bestKmer <= 0) {
                    log.error("Best kmer could not be determined by Kmer Genie.  Recommend you restart the MASS stage with manually specified kmer values for this MASS job.  Mass job : " + massJobName + "; kmer config: " + kgName);
                    return false;
                } else {
                    log.info("Best kmer for " + massJobName + " (" + kgName + ") is " + bestKmer);
                }

                mass2OptimalKmerMap.put(massJobName, bestKmer);
            }
        }

        List<String> kmerMapLines = new ArrayList<>();

        for (Map.Entry<String, Integer> bestKmerEntry : this.mass2OptimalKmerMap.entrySet()) {
            kmerMapLines.add(bestKmerEntry.getKey() + "\t" + bestKmerEntry.getValue());
        }

        FileUtils.writeLines(args.getResultFile(sample), kmerMapLines);
        log.info("Written kmer calculations to: " + args.getResultFile(sample).getAbsolutePath());

        return true;
    }


    public static Map<String, Integer> loadOptimalKmerMap(File resultsFile) throws IOException {

        List<String> lines = FileUtils.readLines(resultsFile);

        Map<String, Integer> optMap = new HashMap<>();

        for(String line : lines) {
            String[] parts = line.split("\t");

            optMap.put(parts[0], Integer.parseInt(parts[1]));
        }

        return optMap;
    }

    public Map<String, Integer> getOptimalKmerMap() {
        return mass2OptimalKmerMap;
    }

    private ExecutionResult executeKmerGenie(File outputDir, File outputFile, List< Library > inputs)
            throws InterruptedException, ProcessExecutionException {

        // Get shortcut to the args
        Args args = this.getArgs();

        KmerGenieV16.Args kgArgs = new KmerGenieV16.Args();
        kgArgs.setThreads(args.threads);
        kgArgs.setDiploid(args.getOrganism().getPloidy() == 2 ? true : false);

        List<File> inputFiles = new ArrayList<>();
        int maxK = 125;
        for(Library lib : inputs) {
            inputFiles.addAll(lib.getFiles());

            maxK = Math.min(lib.getReadLength() -1, maxK);
        }

        kgArgs.setInputFiles(inputFiles);
        kgArgs.setLargestK(maxK);
        kgArgs.setOutputDir(outputDir);
        kgArgs.setOutputPrefix("kmergenie_" + outputDir.getName());
        kgArgs.setOutputFile(outputFile);
        kgArgs.setLogFile(new File(outputFile.getAbsolutePath() + ".log"));

        KmerGenieV16 kg = new KmerGenieV16(this.conanExecutorService, kgArgs);

        return this.conanExecutorService.executeProcess(kg, outputDir,
                args.getJobPrefix() + "-" + outputDir.getName(),
                args.threads, args.memoryMb,
                args.runParallel);
    }

    public static class Args extends RampartProcessArgs {

        // From user
        private Map<Mecq.Sample, List<MassJob.Args>> massJobArgMap;

        // Internal
        private Map<String, String> mass2kgMap;
        private Map<String, String> kg2massMap;
        private Map<String, List<Library>> kg2inputsMap;

        public Args() {

            super(RampartStage.KMER_CALC);
            this.massJobArgMap = new HashMap<>();

            this.mass2kgMap = new HashMap<>();
            this.kg2massMap = new HashMap<>();
            this.kg2inputsMap = new HashMap<>();
        }

        public Args(Element ele, File outputDir, String jobPrefix, List<Mecq.Sample> samples, Organism organism, boolean runParallel) throws IOException {

            // Set defaults first
            super(RampartStage.KMER_CALC, outputDir, jobPrefix, samples, organism, runParallel);

            // Check there's nothing
            if (!XmlHelper.validate(ele,
                    new String[0],
                    new String[]{
                        KEY_ATTR_THREADS,
                        KEY_ATTR_MEMORY,
                        KEY_ATTR_PARALLEL
                    },
                    new String[0],
                    new String[0]
                    )) {
                throw new IOException("Found unrecognised element or attribute in MASS");
            }

            this.massJobArgMap = new HashMap<>();
            this.mass2kgMap = new HashMap<>();
            this.kg2massMap = new HashMap<>();
            this.kg2inputsMap = new HashMap<>();

            // Check ploidy level
            if (this.organism.getPloidy() > 2) {
                throw new IOException("Can't run kmer optimisation because kmer genie cannot handle genomes with ploidy > 2");
            }

            // From Xml (optional)
            this.threads = ele.hasAttribute(KEY_ATTR_THREADS) ?
                    XmlHelper.getIntValue(ele, KEY_ATTR_THREADS) :
                    DEFAULT_THREADS;

            // From Xml (optional)
            this.memoryMb = ele.hasAttribute(KEY_ATTR_MEMORY) ?
                    XmlHelper.getIntValue(ele, KEY_ATTR_MEMORY) :
                    DEFAULT_MEMORY;

            // From Xml (optional)
            this.runParallel = ele.hasAttribute(KEY_ATTR_PARALLEL) ?
                    XmlHelper.getBooleanValue(ele, KEY_ATTR_PARALLEL) :
                    runParallel;
        }

        public void initialise() {

            if (this.massJobArgMap != null) {
                // Setup mass job to kmergenie config maps
                for (List<MassJob.Args> allMassArgs : this.massJobArgMap.values()) {
                    for (MassJob.Args singleMassArgs : allMassArgs) {

                        if ((singleMassArgs.getGenericAssembler().getType() == Assembler.Type.DE_BRUIJN ||
                                singleMassArgs.getGenericAssembler().getType() == Assembler.Type.DE_BRUIJN_OPTIMISER) &&
                                singleMassArgs.getKmerRange() == null) {

                            StringBuilder sb = new StringBuilder();
                            int i = 0;
                            for (ReadsInput input : singleMassArgs.getInputs()) {

                                if (i > 0) {
                                    sb.append("_");
                                }
                                sb.append(input.getEcq()).append("-").append(input.getLib());
                                i++;
                            }

                            String kgSetName = sb.toString();

                            mass2kgMap.put(singleMassArgs.getName(), kgSetName);
                            kg2massMap.put(kgSetName, singleMassArgs.getName());

                            if (!kg2inputsMap.containsKey(kgSetName)) {
                                kg2inputsMap.put(kgSetName, singleMassArgs.getSelectedLibs());
                            }
                        }
                    }
                }
            }
            else {
                throw new IllegalStateException("Require MASS job information to be set in order to determine which libraries to combine for optimal kmer calculation");
            }
        }

        public File getResultFile(Mecq.Sample sample) {
            return new File(this.getStageDir(sample), "kmermap.tsv");
        }

        public Map<Mecq.Sample, List<MassJob.Args>> getMassJobArgMap() {
            return massJobArgMap;
        }

        public void setMassJobArgMap(Map<Mecq.Sample, List<MassJob.Args>> massJobArgMap) {
            this.massJobArgMap = massJobArgMap;
        }

        public String getKmerGenieConfig(String massJobName) {
            return this.mass2kgMap.get(massJobName);
        }

        public List<Library> getLibraries(String kmerGenieConfig) {
            return this.kg2inputsMap.get(kmerGenieConfig);
        }

        public int getNbKmerGenieConfigs() {
            return this.kg2inputsMap.size();
        }

        public Map<String, String> getMass2kgMap() {
            return mass2kgMap;
        }

        public Map<String, String> getKg2massMap() {
            return kg2massMap;
        }

        public Map<String, List<Library>> getKg2inputsMap() {
            return kg2inputsMap;
        }

        @Override
        protected void setOptionFromMapEntry(ConanParameter param, String value) {

        }

        @Override
        protected void setArgFromMapEntry(ConanParameter param, String value) {

        }

        @Override
        public ParamMap getArgMap() {
            return null;
        }

        @Override
        public List<ConanProcess> getExternalProcesses() {
            return null;
        }
    }

    public static class Params extends AbstractProcessParams {

        @Override
        public ConanParameter[] getConanParametersAsArray() {
            return new ConanParameter[0];
        }
    }
}
