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
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.stage.AmpStage;
import uk.ac.tgac.rampart.stage.Mecq;
import uk.ac.tgac.rampart.stage.RampartProcess;
import uk.ac.tgac.rampart.stage.RampartStage;
import uk.ac.tgac.rampart.stage.analyse.asm.analysers.AssemblyAnalyser;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStats;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStatsTable;
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
public class AnalyseAmpAssemblies extends RampartProcess {

    private static Logger log = LoggerFactory.getLogger(AnalyseAmpAssemblies.class);

    SpiFactory<AssemblyAnalyser> assemblyAnalyserFactory;

    public AnalyseAmpAssemblies() {
        this(null);
    }

    public AnalyseAmpAssemblies(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public AnalyseAmpAssemblies(ConanExecutorService ces, Args args) {
        super(ces, args);

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
        for(AnalyseAssembliesArgs.ToolArgs requestedService : this.getArgs().getTools()) {

            if (!this.assemblyAnalyserFactory.serviceAvailable(requestedService.getName())) {

                log.error("Could not find the specified assembly analysis service: " + requestedService);
                return false;
            }
            else {
                requestedServices.add(this.assemblyAnalyserFactory.create(requestedService.getName(), this.conanExecutorService));
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
    public TaskResult executeSample(Mecq.Sample sample, File stageOutputDir, ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException, IOException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Args args = this.getArgs();

        // Create requested services
        Set<AssemblyAnalyser> requestedServices = new HashSet<>();
        for(AnalyseAssembliesArgs.ToolArgs requestedService : this.getArgs().getTools()) {
            AssemblyAnalyser aa = this.assemblyAnalyserFactory.create(requestedService.getName(), this.conanExecutorService);
            aa.setArgs(requestedService);
            requestedServices.add(aa);
        }

        List<ExecutionResult> jobResults = new ArrayList<>();

        // Just loop through all requested stats levels and execute each.
        // Each stage is processed linearly
        for(AssemblyAnalyser analyser : requestedServices) {

            List<File> assemblies = this.findAssemblies(sample, analyser);
            File outputDir = new File(args.getStageDir(sample), analyser.getName().toLowerCase());
            String jobPrefix = this.getArgs().getJobPrefix() + "-" + analyser.getName().toLowerCase();

            try {
                jobResults.addAll(analyser.execute(assemblies, outputDir, jobPrefix, this.conanExecutorService));
            } catch (ConanParameterException e) {
                throw new ProcessExecutionException(2, "Error parsing params for amp analyser", e);
            }
        }

        // Create the stats table with information derived from the configuration file.
        AssemblyStatsTable table = this.createTable(sample);

        // Merge all the results
        for(AssemblyAnalyser analyser : requestedServices) {

            List<File> assemblies = this.findAssemblies(sample, analyser);
            File outputDir = new File(args.getStageDir(sample), analyser.getName().toLowerCase());

            Map<String, String> asm2GroupMap = new HashMap<>();
            for(File b : assemblies) {
                asm2GroupMap.put(b.getName(), "amp");
            }

            analyser.updateTable(table, outputDir);
        }

        // Save table to disk
        File finalTSVFile = new File(args.getStageDir(sample), "scores.tsv");
        table.saveTsv(finalTSVFile);
        log.debug("Saved final results in TSV format to: " + finalTSVFile.getAbsolutePath());

        File finalSummaryFile = new File(args.getStageDir(sample), "scores.tsv");
        table.saveSummary(finalSummaryFile);
        log.debug("Saved final results in summary format to: " + finalSummaryFile.getAbsolutePath());

        stopWatch.stop();

        return new DefaultTaskResult("rampart-amp_analysis", true, jobResults, stopWatch.getTime() / 1000L);
    }

    protected AssemblyStatsTable createTable(Mecq.Sample sample) {

        Args args = this.getArgs();

        AssemblyStatsTable table = new AssemblyStatsTable();

        AssemblyStats stats = new AssemblyStats();
        stats.setIndex(0);
        stats.setDataset("amp");
        stats.setDesc("stage-0");
        stats.setFilePath(args.getAmpStages().get(sample).get(0).getInputAssembly().getAbsolutePath());
        stats.setBubblePath("");//assembler.getBubbleFile() != null ? assembler.getBubbleFile().getAbsolutePath() : "");
        table.add(stats);

        int index = 1;
        for(AmpStage.Args asArgs : args.getAmpStages().get(sample)) {

            stats = new AssemblyStats();
            stats.setIndex(index);
            stats.setDataset("amp");
            stats.setDesc("stage-" + index);
            stats.setFilePath(asArgs.getOutputFile().getAbsolutePath());
            stats.setBubblePath("");//assembler.getBubbleFile() != null ? assembler.getBubbleFile().getAbsolutePath() : "");
            table.add(stats);
            index++;
        }

        return table;
    }

    protected List<File> findAssemblies(Mecq.Sample sample, AssemblyAnalyser analyser) throws ProcessExecutionException {

        Args args = this.getArgs();


        if (args.getAmpStages() == null || args.getAmpStages().isEmpty()) {
            return null;
        }

        File asmDir = args.getAmpStages().get(sample).get(0).getAssembliesDir();

        if (analyser.isFast() || args.isAnalyseAll()) {
            return assembliesFromDir(asmDir);
        }
        else {
            AmpStage.Args finalStage = args.getAmpStages().get(sample).get(args.getAmpStages().size() - 1);

            File assembly = finalStage.getOutputFile();

            if (!assembly.exists()) {
                throw new ProcessExecutionException(-1, "Could not find final output from AMP at: " + assembly.getAbsolutePath());
            }
            List<File> assemblies = new ArrayList<>();
            assemblies.add(assembly);
            return assemblies;
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

        private static final String KEY_ATTR_ALL = "analyse_all";

        public static final boolean DEFAULT_ANALYSE_ALL = false;

        private Map<Mecq.Sample, List<AmpStage.Args>> ampStages;
        private boolean analyseAll;

        public Args() {
            super(RampartStage.AMP_ANALYSIS);

            this.ampStages = null;
            this.analyseAll = false;

            this.setJobPrefix("amp-analyses");
        }

        public Args(Element element, File outputDir, String jobPrefix, List<Mecq.Sample> samples, Organism organism,
                    Map<Mecq.Sample, List<AmpStage.Args>> ampStages) throws IOException {

            super(  RampartStage.AMP_ANALYSIS,
                    element,
                    outputDir,
                    jobPrefix,
                    samples,
                    organism);

            this.analyseAll = element.hasAttribute(KEY_ATTR_ALL) ?
                    XmlHelper.getBooleanValue(element, KEY_ATTR_ALL) :
                    DEFAULT_ANALYSE_ALL;

            this.ampStages = ampStages;

        }

        public Map<Mecq.Sample, List<AmpStage.Args>> getAmpStages() {
            return ampStages;
        }

        public void setAmpStages(Map<Mecq.Sample, List<AmpStage.Args>> ampStages) {
            this.ampStages = ampStages;
        }

        public boolean isAnalyseAll() {
            return analyseAll;
        }

        public void setAnalyseAll(boolean analyseAll) {
            this.analyseAll = analyseAll;
        }
    }

}
