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
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import uk.ac.ebi.fgpt.conan.core.context.DefaultTaskResult;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.TaskResult;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.conan.process.kmer.kat.KatGcpV2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 20/01/14
 * Time: 11:22
 * To change this template use File | Settings | File Templates.
 */
public class MecqAnalysis extends RampartProcess {

    private static Logger log = LoggerFactory.getLogger(MecqAnalysis.class);

    public MecqAnalysis() {
        this(null);
    }

    public MecqAnalysis(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public MecqAnalysis(ConanExecutorService ces, Args args) {
        super(ces, args);
    }

    public Args getArgs() {
        return (Args)this.getProcessArgs();
    }

    @Override
    public String getName() {
        return "MecqAnalysis";
    }

    @Override
    public TaskResult executeSample(Mecq.Sample sample, ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException, IOException {

        Args args = this.getArgs();

        TaskResult executionResult = null;

        if (args.organism == null) {
            throw new IOException("Organism not defined");
        }

        // Check what analyses are requested and check if those are operational
        if (args.isKmerAnalysis()) {

            File kmerOutputDir = new File(args.getStageDir(sample), "kmer");
            executionResult = this.kmerAnalysis(sample, kmerOutputDir);
        }

        if (executionResult == null) {
            return new DefaultTaskResult(
                    "rampart-,mecq_analysis",
                    false,
                    new ArrayList<ExecutionResult>(),
                    0
            );
        }
        else {
            return executionResult;
        }
    }


    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        // Create shortcut to args for convenience
        Args args = this.getArgs();

        // Check what analyses are requested and check if those are operational
        if (args.isKmerAnalysis()) {

            KatGcpV2 katGcpProc = new KatGcpV2(this.conanExecutorService);
            if (!katGcpProc.isOperational(executionContext)) {
                log.warn("KAT GCP is not operational.");
                return false;
            }

            log.info("KAT GCP is operational.");
        }

        log.info("MECQ Analysis stage is operational.");

        return true;
    }

    protected TaskResult kmerAnalysis(Mecq.Sample sample, File kmerOutputDir)
            throws InterruptedException, ProcessExecutionException, IOException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Args args = this.getArgs();

        List<ExecutionResult> jobResults = new ArrayList<>();

        // Create the output directory for the RAW datasets
        File rawOutputDir = new File(kmerOutputDir, "raw");

        if (!rawOutputDir.exists()) {
            rawOutputDir.mkdirs();
        }

        // Start kat on all RAW datasets
        for (Library lib : sample.libraries) {

            // Execute jellyfish and add id to list of job ids
            File katOutDir = new File(rawOutputDir, lib.getName());
            ExecutionResult res = this.executeKatGcp(args, "raw", katOutDir, lib);
            jobResults.add(res);
        }

        // Also start kat on all the pre-processed libraries from MECQ
        for (Mecq.EcqArgs ecqArgs : sample.ecqArgList) {

            // Create the output directory for the non-RAW datasets
            File ecqOutputDir = new File(kmerOutputDir, ecqArgs.getName());

            if (!ecqOutputDir.exists()) {
                ecqOutputDir.mkdirs();
            }

            for (Library lib : ecqArgs.getOutputLibraries(sample)) {

                // Add jellyfish id to list of job ids
                File katOutDir = new File(ecqOutputDir, lib.getName());
                ExecutionResult res = this.executeKatGcp(args, ecqArgs.getName(), katOutDir, lib);
                jobResults.add(res);
            }
        }

        stopWatch.stop();

        String taskName = sample.name + "-" + args.getStage().getOutputDirName() + "-kmer";

        return new DefaultTaskResult(taskName, true, jobResults, stopWatch.getTime() / 1000L);
    }

    protected ExecutionResult executeKatGcp(Args args, String ecqName, File outputDir, Library lib)
            throws ProcessExecutionException, InterruptedException, IOException {

        String suffix = "kat_gcp-" + ecqName + "_" + lib.getName();

        File katGcpEcqOutDir = new File(outputDir, ecqName);

        // Create the process
        KatGcpV2 kat = this.makeKatGcp(
                lib.getFiles().toArray(new File[lib.getFiles().size()]),
                new File(katGcpEcqOutDir, suffix).getAbsolutePath());

        // Create a job name
        String jobName = args.getJobPrefix() + "-" + suffix;

        // Start kat
        return this.conanExecutorService.executeProcess(
                kat,
                katGcpEcqOutDir,
                jobName,
                args.threads,
                args.memoryMb,
                args.runParallel);
    }


    /**
     * Hopefully this is a conservative estimate for most projects.  We ignore very low count kmers so hopefully this
     * size just needs to accomodate genuine kmers, and should therefore be roughly equivalent to genomesize * ploidy.
     * We multiply by 10 to be on the safe side and make sure we can handle some sequencing errors.
     * @return An overestimate of the expected jellyfish hash size
     * @throws IOException Thrown if there is an issue calculating genome size from a reference fasta file
     */
    public static long guessJellyfishHashSize(Organism organism) throws IOException {

        long hashSize = organism.getGenomeSize() * organism.getPloidy() * 10;

        // Check to make sure we don't have anything weird... if we do use a default of 100 million (this should be enough
        // for most organisms running through RAMPART, although it might still fail on low mem systems.  Need to think
        // this through...
        return hashSize <= 0 ? 100000000L : hashSize;
    }

    protected KatGcpV2 makeKatGcp(File[] inputs, String outputPrefix) throws ProcessExecutionException, IOException {

        Args args = this.getArgs();

        KatGcpV2.Args katArgs = new KatGcpV2.Args();
        katArgs.setOutputPrefix(outputPrefix);
        katArgs.setHashSize(guessJellyfishHashSize(args.getOrganism()));
        katArgs.setKmer(27);     // 27 should be sufficient for most organisms and datasets
        katArgs.setCanonical(true);
        katArgs.setThreads(args.getThreads());
        katArgs.setInputFiles(inputs);

        return new KatGcpV2(this.conanExecutorService, katArgs);
    }



    public static class Args extends RampartProcess.RampartProcessArgs {

        private static final String KEY_ATTR_KMER = "kmer";

        public static final boolean DEFAULT_KMER = false;

        private boolean kmerAnalysis;

        public Args() {
            super(RampartStage.MECQ_ANALYSIS);
        }


        public Args(Element ele, File outputDir, String jobPrefix, List<Mecq.Sample> samples, Organism organism, boolean runParallel) throws IOException {

            super(RampartStage.MECQ_ANALYSIS, outputDir, jobPrefix, samples, organism, runParallel);

            // From Xml (optional)
            this.kmerAnalysis = ele.hasAttribute(KEY_ATTR_KMER) ?
                    XmlHelper.getBooleanValue(ele, KEY_ATTR_KMER) :
                    DEFAULT_KMER;

            this.runParallel = ele.hasAttribute(KEY_ATTR_PARALLEL) ?
                    XmlHelper.getBooleanValue(ele, KEY_ATTR_PARALLEL) :
                    runParallel;

            this.threads = ele.hasAttribute(KEY_ATTR_THREADS) ?
                    XmlHelper.getIntValue(ele, KEY_ATTR_THREADS) :
                    DEFAULT_THREADS;

            this.memoryMb = ele.hasAttribute(KEY_ATTR_MEMORY) ?
                    XmlHelper.getIntValue(ele, KEY_ATTR_MEMORY) :
                    DEFAULT_MEMORY;
        }

        public boolean isKmerAnalysis() {
            return kmerAnalysis;
        }

        public void setKmerAnalysis(boolean kmerAnalysis) {
            this.kmerAnalysis = kmerAnalysis;
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
        public void parseCommandLine(CommandLine cmdLine) {

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

}
