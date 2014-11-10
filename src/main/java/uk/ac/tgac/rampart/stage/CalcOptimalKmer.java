/*
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
 */

package uk.ac.tgac.rampart.stage;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionResult;
import uk.ac.ebi.fgpt.conan.core.context.DefaultTaskResult;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.*;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Library;
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
public class CalcOptimalKmer extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(Mass.class);

    private Map<String,Integer> mass2OptimalKmerMap;

    public CalcOptimalKmer() {
        this(null);
    }

    public CalcOptimalKmer(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public CalcOptimalKmer(ConanExecutorService ces, Args args) {
        super("", args, new Params(), ces);
        this.mass2OptimalKmerMap = new HashMap<>();
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
    public ExecutionResult execute(ExecutionContext executionContext)
            throws InterruptedException, ProcessExecutionException {

        try {

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            log.info("Starting Optimal Kmer calculations");

            Args args = this.getArgs();

            List<ExecutionResult> results = new ArrayList<>();

            Map<String, File> kg2FileMap = new HashMap<>();
            this.mass2OptimalKmerMap.clear();

            // Execute each config
            for (Map.Entry<String, List<Library>> entry : args.getKg2inputsMap().entrySet()) {

                File kgOutputDir = new File(args.getOutputDir(), entry.getKey());
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

            // Wait for all assembly jobs to finish if they are running in parallel.
            if (executionContext.usingScheduler() && args.isRunParallel()) {
                log.info("Kmer Genie jobs were executed in parallel, waiting for all to complete");
                this.conanExecutorService.executeScheduledWait(
                        results,
                        args.getJobPrefix() + "-*",
                        ExitStatus.Type.COMPLETED_ANY,
                        args.getJobPrefix() + "-wait",
                        args.getOutputDir());
            }

            // Retrieve kmer genie results
            for (MassJob.Args massJobArgs : args.massJobArgList) {
                String massJobName = massJobArgs.getName();
                String kgName = args.mass2kgMap.get(massJobName);

                if (kgName != null) {
                    int bestKmer = KmerGenieV16.Args.getBestKmer(kg2FileMap.get(kgName));

                    if (bestKmer <= 0) {
                        throw new IOException("Best kmer could not be determined by Kmer Genie.  Recommend you restart the MASS stage with manually specified kmer values for this MASS job.  Mass job : " + massJobName + "; kmer config: " + kgName);
                    } else {
                        log.info("Best kmer for " + massJobName + " (" + kgName + ") is " + bestKmer);
                    }

                    mass2OptimalKmerMap.put(massJobName, bestKmer);
                }
            }

            List<String> kmerMapLines = new ArrayList<>();

            for(Map.Entry<String, Integer> bestKmerEntry : this.mass2OptimalKmerMap.entrySet()) {
                kmerMapLines.add(bestKmerEntry.getKey() + "\t" + bestKmerEntry.getValue());
            }

            FileUtils.writeLines(args.getResultFile(), kmerMapLines);
            log.info("Written kmer calculations to: " + args.getResultFile().getAbsolutePath());

            stopWatch.stop();

            TaskResult tr = new DefaultTaskResult("rampart-mass-kmercalc", true, results, stopWatch.getTime() / 1000L);

            // Output the resource usage to file
            FileUtils.writeLines(new File(args.getOutputDir(), args.getJobPrefix() + ".summary"), tr.getOutput());

            log.info("Optimal Kmer calculations complete");

            return new DefaultExecutionResult(
                    tr.getTaskName(),
                    0,
                    new String[] {},
                    null,
                    -1,
                    new ResourceUsage(tr.getMaxMemUsage(), tr.getActualTotalRuntime(), tr.getTotalExternalCputime()));
        }
        catch (IOException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        }
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
        kgArgs.setDiploid(args.ploidy == 2 ? true : false);

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

        KmerGenieV16 kg = new KmerGenieV16(this.conanExecutorService, kgArgs);

        return this.conanExecutorService.executeProcess(kg, outputDir,
                args.getJobPrefix() + "-" + outputDir.getName(),
                args.threads, args.memoryMb,
                args.runParallel);
    }

    public static class Args extends AbstractProcessArgs implements RampartStageArgs {

        // Keys for config file
        private static final String KEY_ATTR_THREADS = "threads";
        private static final String KEY_ATTR_MEMORY = "memory";
        private static final String KEY_ATTR_PARALLEL = "parallel";

        public static final int DEFAULT_THREADS = 1;
        public static final int DEFAULT_MEMORY = 0;
        public static final boolean DEFAULT_RUN_PARALLEL = false;
        public static final int DEFAULT_PLOIDY = 1;

        private File outputDir;
        private String jobPrefix;
        private int threads;
        private int memoryMb;
        private boolean runParallel;
        private int ploidy;
        private List<MassJob.Args> massJobArgList;


        private Map<String, String> mass2kgMap;
        private Map<String, String> kg2massMap;
        private Map<String, List<Library>> kg2inputsMap;

        public Args() {

            super(new Params());
            this.outputDir = null;
            this.jobPrefix = "mass-kmercalc";
            this.threads = DEFAULT_THREADS;
            this.memoryMb = DEFAULT_MEMORY;
            this.runParallel = DEFAULT_RUN_PARALLEL;
            this.ploidy = DEFAULT_PLOIDY;
            this.massJobArgList = null;

            this.mass2kgMap = new HashMap<>();
            this.kg2massMap = new HashMap<>();
            this.kg2inputsMap = new HashMap<>();
        }

        public Args(Element ele, File outputDir, String jobPrefix, int ploidy) throws IOException {

            // Set defaults first
            this();

            // Check there's nothing
            if (!XmlHelper.validate(ele,
                    new String[0],
                    new String[]{
                        KEY_ATTR_THREADS,
                        KEY_ATTR_MEMORY
                    },
                    new String[0],
                    new String[0]
                    )) {
                throw new IOException("Found unrecognised element or attribute in MASS");
            }

            this.outputDir = outputDir;
            this.jobPrefix = jobPrefix;

            if (ploidy > 2) {
                throw new IOException("Can't run kmer optimisation because kmer genie cannot handle genomes with ploidy > 2");
            }
            this.ploidy = ploidy;

            // From Xml (optional)
            this.threads = ele.hasAttribute(KEY_ATTR_THREADS) ?
                    XmlHelper.getIntValue(ele, KEY_ATTR_THREADS) :
                    DEFAULT_THREADS;

            // From Xml (optional)
            this.memoryMb = ele.hasAttribute(KEY_ATTR_MEMORY) ?
                    XmlHelper.getIntValue(ele, KEY_ATTR_MEMORY) :
                    DEFAULT_MEMORY;

            // From Xml (optional) default to value of MASS element if not specified
            this.runParallel = ele.hasAttribute(KEY_ATTR_PARALLEL) ?
                    XmlHelper.getBooleanValue(ele, KEY_ATTR_PARALLEL) :
                    runParallel;



        }

        public void initialise() {

            if (this.massJobArgList != null) {
                // Setup mass job to kmergenie config maps
                for (MassJob.Args singleMassArgs : this.massJobArgList) {

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
            else {
                throw new IllegalStateException("Require MASS job information to be set in order to determine which libraries to combine for optimal kmer calculation");
            }
        }

        public File getResultFile() {
            return new File(this.outputDir, "kmermap.tsv");
        }

        public String getJobPrefix() {
            return jobPrefix;
        }

        public void setJobPrefix(String jobPrefix) {
            this.jobPrefix = jobPrefix;
        }

        public int getThreads() {
            return threads;
        }

        public void setThreads(int threads) {
            this.threads = threads;
        }

        public int getMemoryMb() {
            return memoryMb;
        }

        public void setMemoryMb(int memoryMb) {
            this.memoryMb = memoryMb;
        }

        public File getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(File outputDir) {
            this.outputDir = outputDir;
        }

        public boolean isRunParallel() {
            return runParallel;
        }

        public void setRunParallel(boolean runParallel) {
            this.runParallel = runParallel;
        }

        public int getPloidy() {
            return ploidy;
        }

        public void setPloidy(int ploidy) {
            this.ploidy = ploidy;
        }

        public List<MassJob.Args> getMassJobArgList() {
            return massJobArgList;
        }

        public void setMassJobArgList(List<MassJob.Args> massJobArgList) {
            this.massJobArgList = massJobArgList;
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
        protected void parseCommandLine(CommandLine commandLine) {

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
