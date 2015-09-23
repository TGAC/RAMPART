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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionResult;
import uk.ac.ebi.fgpt.conan.core.context.DefaultTaskResult;
import uk.ac.ebi.fgpt.conan.core.param.*;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.*;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.conan.process.asm.Assembler;
import uk.ac.tgac.conan.process.asm.KmerRange;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * User: maplesod
 * Date: 01/02/13
 * Time: 11:10
 */
public class Mass extends RampartProcess {

    private static Logger log = LoggerFactory.getLogger(Mass.class);

    private TaskResult taskResult;

    public Mass() {
        this(null);
    }

    public Mass(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public Mass(ConanExecutorService ces, Args args) {
        super(ces, args);
    }

    public TaskResult getTaskResult() {
        return taskResult;
    }

    @Override
    public String getName() {
        return "MASS";
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        Args args = (Args) this.getProcessArgs();

        for(MassJob.Args massJobArgs : args.getMassJobArgMap().values().iterator().next()) {
            if (!new MassJob(this.conanExecutorService, massJobArgs).isOperational(executionContext)) {
                log.warn("A MASS job is NOT operational.");
                return false;
            }
        }

        log.info("MASS stage is operational.");

        return true;
    }

    @Override
    public TaskResult executeSample(Mecq.Sample sample, ExecutionContext executionContext)
            throws ProcessExecutionException, InterruptedException, IOException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Get shortcut to the args
        Args args = (Args) this.getRampartArgs();

        List<ExecutionResult> results = new ArrayList<>();
        List<ExecutionResult> allResults = new ArrayList<>();
        List<TaskResult> massJobResults = new ArrayList<>();
        Map<String,Integer> optimalKmerMap = null;

        // Work out kmer genie configs and how they relate to mass jobs
        if (args.kmerCalcArgs != null) {
            setKmerValues(args.kmerCalcArgs.getResultFile(sample), args.getMassJobArgMap().get(sample));
            log.info("Loaded optimal kmer values");
        }

        log.info("Starting MASS jobs");

        for (MassJob.Args massJobArgs : args.getMassJobArgMap().get(sample)) {

            // Ensure output directory for this MASS run exists
            if (!massJobArgs.getOutputDir().exists() && !massJobArgs.getOutputDir().mkdirs()) {
                throw new IOException("Couldn't create directory for MASS");
            }

            // Execute the mass job and record any job ids
            MassJobResult mjr = this.executeMassJob(massJobArgs, executionContext);
            massJobResults.add(mjr.getAllResults());
        }

        for(TaskResult mjr : massJobResults) {
            results.addAll(mjr.getProcessResults());
            allResults.addAll(mjr.getProcessResults());
        }

        stopWatch.stop();

        return new DefaultTaskResult(sample.name + "-" + args.stage.getOutputDirName(), true, allResults, stopWatch.getTime() / 1000L);
    }

    @Override
    public boolean validateOutput(Mecq.Sample sample) throws IOException {

        Args args = (Args) this.getRampartArgs();

        // For each MASS job to check an output file exist
        for(MassJob.Args singleMassArgs : args.getMassJobArgMap().get(sample))  {
            for(Assembler asm : singleMassArgs.getAssemblers()) {

                if (    (asm.makesScaffolds() && !asm.getScaffoldsFile().exists()) ||
                        (asm.makesContigs() && !asm.getContigsFile().exists()) ||
                        (asm.makesUnitigs() && !asm.getUnitigsFile().exists())) {
                    log.error("MASS job \"" + singleMassArgs.getName() + "\" for sample \"" + sample.name + "\" did not produce any output files");
                    return false;
                }
            }
        }

        return true;
    }

    protected static class MassJobResult {
        private ExecutionResult result;
        private List<Integer> jobIds;
        private TaskResult allResults;

        public MassJobResult() {
            this(new DefaultExecutionResult("test", 0), new ArrayList<Integer>(), new DefaultTaskResult("test", false, new ArrayList<ExecutionResult>(), 0L));
        }

        public MassJobResult(ExecutionResult result, List<Integer> jobIds, TaskResult allResults) {
            this.result = result;
            this.jobIds = jobIds;
            this.allResults = allResults;
        }

        public ExecutionResult getResult() {
            return result;
        }

        public List<Integer> getJobIds() {
            return jobIds;
        }

        public TaskResult getAllResults() {
            return allResults;
        }
    }

    public Args getArgs() {
        return (Args)this.getProcessArgs();
    }

    public static void setKmerValues(File kmerMapFile, List<MassJob.Args> massJobArgList) throws IOException {

        Map<String, Integer> optimalKmerMap = CalcOptimalKmer.loadOptimalKmerMap(kmerMapFile);
        log.info("Loaded optimal kmer values");

        // Get auto kmer setting if required
        for(MassJob.Args massJobArgs : massJobArgList) {
            if (optimalKmerMap.containsKey(massJobArgs.getName())) {
                int kmerVal = optimalKmerMap.get(massJobArgs.getName());

                log.info("Using automatically determined optimal kmer value (" + kmerVal + ") for " + massJobArgs.getName());
                massJobArgs.setKmerRange(new KmerRange(Integer.toString(kmerVal)));
            }
        }
    }

    protected MassJobResult executeMassJob(MassJob.Args massJobArgs, ExecutionContext executionContext)
            throws InterruptedException, ProcessExecutionException {

        massJobArgs.initialise(false);
        MassJob massJob = new MassJob(this.conanExecutorService, massJobArgs);
        ExecutionResult result = massJob.execute(executionContext);

        return new MassJobResult(result, massJob.getJobIds(), massJob.getTaskResult());
    }

    public static enum OutputLevel {
        UNITIGS,
        CONTIGS,
        SCAFFOLDS;

        public static String getListAsString() {

            List<String> levels = new ArrayList<>();

            for(OutputLevel level : OutputLevel.values()) {
                levels.add(level.toString());
            }

            return StringUtils.join(levels, ",");
        }
    }



    public static class Args extends RampartProcess.RampartProcessArgs {

        // Keys for config file
        private static final String KEY_ELEM_MASS_JOB = "job";

        // Constants
        public static final int DEFAULT_CVG_CUTOFF = -1;
        public static final OutputLevel DEFAULT_OUTPUT_LEVEL = OutputLevel.CONTIGS;

        private CalcOptimalKmer.Args kmerCalcArgs;
        private Map<Mecq.Sample, List<MassJob.Args>> massJobArgMap;    // List of MASS groups to run separately
        private Organism organism;
        private OutputLevel outputLevel;

        @Override
        public List<ConanProcess> getExternalProcesses() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Args() {
            super(RampartStage.MASS);

            this.outputLevel = DEFAULT_OUTPUT_LEVEL;
            this.organism = null;
            this.kmerCalcArgs = null;
            this.massJobArgMap = new HashMap<>();
        }

        public Args(Element ele, File outputDir, String jobPrefix, List<Mecq.Sample> samples, Organism organism, CalcOptimalKmer.Args kmerCalcArgs, boolean runParallel)
                throws IOException {

            // Set defaults first
            super(RampartStage.MASS, outputDir, jobPrefix, samples, organism, runParallel);

            // Check there's nothing
            if (!XmlHelper.validate(ele,
                    new String[0],
                    new String[]{
                            KEY_ATTR_PARALLEL
                    },
                    new String[]{
                            KEY_ELEM_MASS_JOB
                    },
                    new String[0])) {
                throw new IOException("Found unrecognised element or attribute in MASS");
            }

            this.massJobArgMap = new HashMap<>();
            this.kmerCalcArgs = kmerCalcArgs;
            this.organism = organism;

            // From Xml (optional)
            this.runParallel = ele.hasAttribute(KEY_ATTR_PARALLEL) ?
                    XmlHelper.getBooleanValue(ele, KEY_ATTR_PARALLEL) :
                    runParallel;

            // All single mass args
            NodeList nodes = ele.getElementsByTagName(KEY_ELEM_MASS_JOB);
            for(Mecq.Sample sample : samples) {
                List<MassJob.Args> massJobArgs = new ArrayList<>();
                for (int i = 0; i < nodes.getLength(); i++) {
                    massJobArgs.add(
                            new MassJob.Args(
                                    (Element) nodes.item(i), this.getStageDir(sample), jobPrefix + "-group",
                                    sample, this.organism, this.runParallel, i + 1, this.kmerCalcArgs != null)
                    );
                }

                this.massJobArgMap.put(sample, massJobArgs);
            }
        }

        public void initialise() {
            for(List<MassJob.Args> jobArgList : this.massJobArgMap.values()) {
                for (MassJob.Args jobArgs : jobArgList) {
                    jobArgs.initialise(false);
                }
            }
        }

        public OutputLevel getOutputLevel() {
            return outputLevel;
        }

        public void setOutputLevel(OutputLevel outputLevel) {
            this.outputLevel = outputLevel;
        }

        public Organism getOrganism() {
            return organism;
        }

        public void setOrganism(Organism organism) {
            this.organism = organism;
        }

        public Map<Mecq.Sample, List<MassJob.Args>> getMassJobArgMap() {
            return massJobArgMap;
        }

        public void setMassJobArgMap(Map<Mecq.Sample, List<MassJob.Args>> massJobArgMap) {
            this.massJobArgMap = massJobArgMap;
        }

        public CalcOptimalKmer.Args getKmerCalcArgs() {
            return kmerCalcArgs;
        }

        public void setKmerCalcArgs(CalcOptimalKmer.Args kmerCalcArgs) {
            this.kmerCalcArgs = kmerCalcArgs;
        }


        @Override
        public ParamMap getArgMap() {
            ParamMap pvp = new DefaultParamMap();
            return pvp;
        }

        @Override
        protected void setOptionFromMapEntry(ConanParameter param, String value) {
        }

        @Override
        protected void setArgFromMapEntry(ConanParameter param, String value) {
        }

    }

}
