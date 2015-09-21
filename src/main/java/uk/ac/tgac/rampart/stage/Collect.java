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
import uk.ac.tgac.conan.process.asm.stats.QuastV23;
import uk.ac.tgac.conan.process.kmer.kat.KatCompV2;
import uk.ac.tgac.rampart.stage.analyse.asm.AnalyseMassAssemblies;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is derived from Richard's FastA-to-AGP script in TGAC tools, which is in turn derived form Shaun Jackman's
 * FastA-to-AGP script in Abyss.
 */
public class Collect extends RampartProcess {

    private static Logger log = LoggerFactory.getLogger(Collect.class);

    public static final String NAME = "Collect";

    public Collect() {
        this(null);
    }

    public Collect(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public Collect(ConanExecutorService ces, Args args) {
        super(ces, args);

    }

    public Args getArgs() {
        return (Args)this.getProcessArgs();
    }

    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        log.info("Collect stage is operational.");
        return true;
    }

    @Override
    public TaskResult executeSample(Mecq.Sample sample, ExecutionContext executionContext)
            throws ProcessExecutionException, InterruptedException, IOException {

        Args args = this.getArgs();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<ExecutionResult> res = new ArrayList<>();

        File assembly = args.getFinaliseArgs().getScaffoldsFile(sample);

        if (!args.getAssembliesDir().exists()) {
            args.getAssembliesDir().mkdirs();
        }

         // Create link for the assembly
        this.getConanProcessService().createLocalSymbolicLink(
                assembly,
                new File(args.getAssembliesDir(), assembly.getName()));

        if (sample.libraries.size() != 1) {
            throw new IOException("Can only run KAT for 1 library per sample");
        }

        res.add(this.executeKat(sample, assembly));

        if (!args.getKatDir().exists()) {
            args.getKatDir().mkdirs();
        }


        stopWatch.stop();

        return new DefaultTaskResult(sample.name + "-" + args.stage.getOutputDirName(), true, res, stopWatch.getTime() / 1000L);
    }

    public boolean validateOutput(Mecq.Sample sample) throws IOException, InterruptedException, ProcessExecutionException {

        Args args = this.getArgs();

        File inputFile = new File(args.getStageDir(sample), args.getFinaliseArgs().getOutputPrefix(sample) + "-main.mx.spectra-cn.png");
        File outputFile = new File(args.getKatDir(), args.getFinaliseArgs().getOutputPrefix(sample) + ".png");

        if (!inputFile.exists()) {
            return false;
        }

        // Copy the KAT output.  Note I copy here intentionally as I like to view the KAT plots on a remote system, which
        // doesn't like symbolic links.  File sizes are small so this shouldn't cause a big issue.
        if (outputFile.exists()) {
            outputFile.delete();
        }
        FileUtils.copyFile(inputFile, outputFile);

        return true;
    }

    public void finalise() throws IOException, InterruptedException, ProcessExecutionException {

        ExecutionResult res = this.executeQuast();

        if (res.getExitCode() != 0) {
            throw new ProcessExecutionException(-2, "Failed to run Quast on final collected output from all samples");
        }

        Args args = this.getArgs();

        for(Mecq.Sample s : args.getSamples()) {
            if (s.failedAtStage != -1) {
                log.warn("Failed to fully process sample: " + s.name + " Failed at stage: " + RampartStage.values()[s.failedAtStage].name());
            }
        }
    }

    protected ExecutionResult executeKat(Mecq.Sample sample, File assembly) throws ProcessExecutionException, InterruptedException {
        Args args = this.getArgs();

        Library lib = sample.libraries.get(0);

        String input = lib.isPairedEnd() ? "'" + lib.getFile1().getAbsolutePath() + " " + lib.getFile2().getAbsolutePath() + "'" : lib.getFile1().getAbsolutePath();

        // Setup kat comp
        KatCompV2.Args katCompArgs = new KatCompV2.Args();
        katCompArgs.setInput1(input);
        katCompArgs.setInput2(assembly.getAbsolutePath());
        katCompArgs.setCanonical1(true);
        katCompArgs.setCanonical2(true);
        katCompArgs.setHashSize1(assembly.length() * 10);
        katCompArgs.setHashSize2(assembly.length() * 2);
        katCompArgs.setKmer(31);
        katCompArgs.setOutputPrefix(new File(args.getStageDir(sample), args.getFinaliseArgs().getOutputPrefix(sample)).getAbsolutePath());
        katCompArgs.setThreads(args.getThreads());

        KatCompV2 katCompProcess = new KatCompV2(this.conanExecutorService, katCompArgs);

        return this.conanExecutorService.executeProcess(
                katCompProcess,
                args.getStageDir(sample),
                args.getJobPrefix() + "-" + sample.name,
                args.getThreads(),
                args.getMemoryMb(),
                args.isRunParallel());
    }

    protected ExecutionResult executeQuast() throws IOException, ProcessExecutionException, InterruptedException {

        Args args = this.getArgs();

        List<File> assemblies = AnalyseMassAssemblies.assembliesFromDir(args.getAssembliesDir(), true);

        // Execute Quast on assemblies
        QuastV23.Args quastArgs = new QuastV23.Args();
        quastArgs.setInputFiles(assemblies);
        quastArgs.setOutputDir(args.getQuastDir());   // No need to create this directory first... quast will take care of that
        quastArgs.setEstimatedGenomeSize(args.getOrganism().getGenomeSize());
        quastArgs.setReference(args.getOrganism().getReference().getPath());
        quastArgs.setFindGenes(true);
        quastArgs.setEukaryote(args.getOrganism().getPloidy() > 1);
        quastArgs.setThreads(args.getThreads());
        quastArgs.setScaffolds(false);

        QuastV23 quastProcess = new QuastV23(this.conanExecutorService, quastArgs);

        return this.conanExecutorService.executeProcess(
                quastProcess,
                args.getQuastDir(),
                args.getJobPrefix(),
                args.getThreads(),
                5000,
                false);
    }

    public static class Args extends RampartProcessArgs {

        private Finalise.Args finaliseArgs;

        public Args() {

            super(RampartStage.COLLECT);
            this.finaliseArgs = null;
        }

        public Args(Element element, File outputDir, String jobPrefix, List<Mecq.Sample> samples, Organism organism, Finalise.Args finaliseArgs, boolean runParallel) throws IOException {

            super(RampartStage.COLLECT, outputDir, jobPrefix, samples, organism, runParallel);

            // Check there's nothing unexpected in this element
            if (!XmlHelper.validate(element,
                    new String[0],
                    new String[]{
                            KEY_ATTR_PARALLEL,
                            KEY_ATTR_THREADS,
                            KEY_ATTR_MEMORY
                    },
                    new String[0],
                    new String[0])) {
                throw new IllegalArgumentException("Found unrecognised element or attribute in Finaliser");
            }

            // Set from Xml
            this.runParallel = element.hasAttribute(KEY_ATTR_PARALLEL) ? XmlHelper.getBooleanValue(element, KEY_ATTR_PARALLEL) : runParallel;
            this.threads = element.hasAttribute(KEY_ATTR_THREADS) ? XmlHelper.getIntValue(element, KEY_ATTR_THREADS) : DEFAULT_THREADS;
            this.memoryMb = element.hasAttribute(KEY_ATTR_MEMORY) ? XmlHelper.getIntValue(element, KEY_ATTR_MEMORY) : DEFAULT_MEMORY;

            this.finaliseArgs = finaliseArgs;
        }

        public Finalise.Args getFinaliseArgs() {
            return finaliseArgs;
        }

        public File getAssembliesDir() {
            return new File(this.outputDir, "assemblies");
        }

        public File getQuastDir() {
            return new File(this.getAssembliesDir(), "quast");
        }

        public File getKatDir() {
            return new File(this.getAssembliesDir(), "kat");
        }


        @Override
        public void parseCommandLine(CommandLine cmdLine) {

        }

        @Override
        public ParamMap getArgMap() {
            return null;
        }

        @Override
        protected void setOptionFromMapEntry(ConanParameter param, String value) {

        }

        @Override
        protected void setArgFromMapEntry(ConanParameter param, String value) {

        }


        @Override
        public List<ConanProcess> getExternalProcesses() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

}
