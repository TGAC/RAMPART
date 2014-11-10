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
import uk.ac.ebi.fgpt.conan.core.param.ArgValidator;
import uk.ac.ebi.fgpt.conan.core.param.DefaultParamMap;
import uk.ac.ebi.fgpt.conan.core.param.ParameterBuilder;
import uk.ac.ebi.fgpt.conan.core.param.PathParameter;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.ResourceUsage;
import uk.ac.ebi.fgpt.conan.model.context.TaskResult;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.RampartCLI;
import uk.ac.tgac.rampart.stage.analyse.asm.analysers.AssemblyAnalyser;
import uk.ac.tgac.rampart.stage.analyse.asm.selector.AssemblySelector;
import uk.ac.tgac.rampart.stage.analyse.asm.selector.DefaultAssemblySelector;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStats;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStatsTable;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by maplesod on 24/10/14.
 */
public class Select extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(Amp.class);

    public Select() {
        this(null);
    }

    public Select(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public Select(ConanExecutorService ces, Args args) {
        super("", args, new Params(), ces);
    }

    public Args getArgs() {
        return (Args) this.getProcessArgs();
    }

    @Override
    public String getName() {
        return "Select";
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        log.info("Select stage is operational.");
        return true;
    }

    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public ExecutionResult execute(ExecutionContext executionContext) throws InterruptedException, ProcessExecutionException {

        try {

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            log.info("Starting Selection of MASS assemblies");

            Args args = this.getArgs();

            if (!args.getOutputDir().exists()) {
                args.getOutputDir().mkdirs();
            }

            // Create the stats table with information derived from the configuration file.
            AssemblyStatsTable table = this.createTable();

            for(AssemblyAnalyser analyser : args.analysers) {

                File reportDir = new File(args.getMassAnalysisDir(), analyser.getName().toLowerCase());

                analyser.updateTable(
                        table,
                        analyser.isFast() ? new File(reportDir, "longest") : reportDir
                );
            }

            // Select the assembly
            AssemblySelector assemblySelector = new DefaultAssemblySelector(args.getWeightingsFile());
            AssemblyStats selectedAssembly = assemblySelector.selectAssembly(
                    table,
                    args.getOrganism().getEstGenomeSize(),
                    args.getOrganism().getEstGcPercentage());

            File bestAssembly = new File(selectedAssembly.getFilePath());

            String bubblePath = selectedAssembly.getBubblePath();
            File bubbles = bubblePath.equalsIgnoreCase("NA") || bubblePath.isEmpty() ?
                    null :
                    new File(selectedAssembly.getBubblePath());

            File bestAssemblyLink = new File(args.getOutputDir(), "best.fa");
            File bubblesLink = new File(args.getOutputDir(), "best_bubbles.fa");

            log.info("Best assembly path: " + bestAssembly.getAbsolutePath());

            // Create link to "best" assembly in stats dir
            this.getConanProcessService().createLocalSymbolicLink(bestAssembly, bestAssemblyLink);
            if (bubbles != null && bubbles.exists()) {
                this.getConanProcessService().createLocalSymbolicLink(bubbles, bubblesLink);
            }

            // Save table to disk
            File finalFile = new File(args.getOutputDir(), "scores.tab");
            table.save(finalFile);
            log.debug("Saved final results to disk at: " + finalFile.getAbsolutePath());

            stopWatch.stop();

            long runtime = stopWatch.getTime() / 1000L;

            TaskResult taskResult = new DefaultTaskResult("rampart-mass_select", true, null, runtime);

            return new DefaultExecutionResult(
                    taskResult.getTaskName(),
                    0,
                    new String[] {},
                    null,
                    -1,
                    new ResourceUsage(0, runtime, runtime));

        } catch (IOException e) {
            throw new ProcessExecutionException(4, e);
        }
    }


    protected AssemblyStatsTable createTable() throws IOException {

        Args args = this.getArgs();

        AssemblyStatsTable table = new AssemblyStatsTable();

        List<String> lines = FileUtils.readLines(args.assemblyLinkageFile);

        for(String line : lines) {

            String tl = line.trim();

            if (!tl.isEmpty()) {
                String[] parts = line.split("\t");

                AssemblyStats stats = new AssemblyStats();
                stats.setIndex(Integer.parseInt(parts[0]));
                stats.setDataset(parts[1]);
                stats.setDesc(parts[2]);
                stats.setFilePath(parts[3]);
                stats.setBubblePath(parts[4]);
                table.add(stats);
            }
        }

        return table;
    }

    public static class Args extends AbstractProcessArgs implements RampartStageArgs {

        private static final String KEY_ATTR_WEIGHTINGS = "weightings_file";

        public static final File DEFAULT_SYSTEM_WEIGHTINGS_FILE = new File(RampartCLI.ETC_DIR, "weightings.tab");
        public static final File    DEFAULT_USER_WEIGHTINGS_FILE = new File(RampartCLI.USER_DIR, "weightings.tab");
        public static final File    DEFAULT_WEIGHTINGS_FILE = DEFAULT_USER_WEIGHTINGS_FILE.exists() ?
                DEFAULT_USER_WEIGHTINGS_FILE : DEFAULT_SYSTEM_WEIGHTINGS_FILE;

        private File massAnalysisDir;
        private File assemblyLinkageFile;
        private File outputDir;
        private Organism organism;
        private File weightingsFile;
        private String jobPrefix;
        private List<MassJob.Args> massJobs;
        private List<AssemblyAnalyser> analysers;

        public Args() {
            super(new Params());

            this.massAnalysisDir = null;
            this.assemblyLinkageFile = null;
            this.organism = null;
            this.weightingsFile = DEFAULT_WEIGHTINGS_FILE;
            this.jobPrefix = "select-assembly";
            this.massJobs = null;
            this.analysers = null;
        }

        public Args(Element element, File massAnalysisDir, File assemblyLinkageFile, File outputDir,
                    List<MassJob.Args> massJobs, List<AssemblyAnalyser> analysers,
                    Organism organism, String jobPrefix) throws IOException {

            this();

            // Check there's nothing
            if (!XmlHelper.validate(element,
                    new String[0],
                    new String[]{
                            KEY_ATTR_WEIGHTINGS
                    },
                    new String[0],
                    new String[0]
            )) {
                throw new IOException("Found unrecognised element or attribute in \"analyse_mass\"");
            }

            this.massAnalysisDir = massAnalysisDir;
            this.assemblyLinkageFile = assemblyLinkageFile;
            this.outputDir = outputDir;
            this.organism = organism;
            this.jobPrefix = jobPrefix;
            this.massJobs = massJobs;
            this.analysers = analysers;

            this.weightingsFile = element.hasAttribute(KEY_ATTR_WEIGHTINGS) ?
                    new File(XmlHelper.getTextValue(element, KEY_ATTR_WEIGHTINGS)) :
                    DEFAULT_WEIGHTINGS_FILE;

        }

        protected Params getParams() {
            return (Params)this.params;
        }

        public File getMassAnalysisDir() {
            return massAnalysisDir;
        }

        public void setMassAnalysisDir(File massAnalysisDir) {
            this.massAnalysisDir = massAnalysisDir;
        }

        public File getAssemblyLinkageFile() {
            return assemblyLinkageFile;
        }

        public void setAssemblyLinkageFile(File assemblyLinkageFile) {
            this.assemblyLinkageFile = assemblyLinkageFile;
        }

        public File getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(File outputDir) {
            this.outputDir = outputDir;
        }

        public Organism getOrganism() {
            return organism;
        }

        public void setOrganism(Organism organism) {
            this.organism = organism;
        }

        public File getWeightingsFile() {
            return weightingsFile;
        }

        public void setWeightingsFile(File weightingsFile) {
            this.weightingsFile = weightingsFile;
        }

        public String getJobPrefix() {
            return jobPrefix;
        }

        public void setJobPrefix(String jobPrefix) {
            this.jobPrefix = jobPrefix;
        }

        public List<MassJob.Args> getMassJobs() {
            return massJobs;
        }

        public void setMassJobs(List<MassJob.Args> massJobs) {
            this.massJobs = massJobs;
        }

        public List<AssemblyAnalyser> getAnalysers() {
            return analysers;
        }

        public void setAnalysers(List<AssemblyAnalyser> analysers) {
            this.analysers = analysers;
        }

        @Override
        protected void setOptionFromMapEntry(ConanParameter param, String value) {
            Params params = this.getParams();

            if (param.equals(params.getOutputDir())) {
                this.outputDir = new File(value);
            } else if (param.equals(params.getJobPrefix())) {
                this.jobPrefix = value;
            } else {
                throw new IllegalArgumentException("Unknown param found: " + param);
            }
        }

        @Override
        protected void setArgFromMapEntry(ConanParameter param, String value) {

        }

        @Override
        protected void parseCommandLine(CommandLine commandLine) {

        }

        @Override
        public List<ConanProcess> getExternalProcesses() {
            return null;
        }

        @Override
        public ParamMap getArgMap() {
            Params params = this.getParams();
            ParamMap pvp = new DefaultParamMap();

            if (this.outputDir != null)
                pvp.put(params.getOutputDir(), this.outputDir.getAbsolutePath());

            if (this.jobPrefix != null) {
                pvp.put(params.getJobPrefix(), this.jobPrefix);
            }

            return pvp;
        }
    }

    public static class Params extends AbstractProcessParams {

        private ConanParameter outputDir;
        private ConanParameter jobPrefix;

        public Params() {

            this.outputDir = new PathParameter(
                    "output",
                    "The output directory",
                    true);

            this.jobPrefix = new ParameterBuilder()
                    .longName("job_prefix")
                    .description("The job_prefix to be assigned to any subprocesses.  Useful if executing with a scheduler.")
                    .argValidator(ArgValidator.DEFAULT)
                    .create();
        }

        public ConanParameter getOutputDir() {
            return outputDir;
        }

        public ConanParameter getJobPrefix() {
            return jobPrefix;
        }


        @Override
        public ConanParameter[] getConanParametersAsArray() {
            return new ConanParameter[] {
                    this.outputDir,
                    this.jobPrefix
            };
        }
    }
}