/**
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
 **/
package uk.ac.tgac.rampart.tool.process.mass;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uk.ac.ebi.fgpt.conan.core.param.*;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.tool.pipeline.rampart.RampartStageArgs;
import uk.ac.tgac.rampart.tool.process.Mecq;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: maplesod
 * Date: 01/02/13
 * Time: 11:10
 */
public class Mass extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(Mass.class);

    public Mass() {
        this(null);
    }

    public Mass(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public Mass(ConanExecutorService ces, Args args) {
        super("", args, new Params(), ces);
    }

    @Override
    public String getName() {
        return "MASS";
    }

    @Override
    public Collection<ConanParameter> getParameters() {
        return new Params().getConanParameters();
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        Args args = (Args) this.getProcessArgs();

        for(MassJob.Args singleMassArgs : args.getSingleMassArgsList()) {
            if (!new MassJob(this.conanExecutorService, singleMassArgs).isOperational(executionContext)) {
                log.warn("MASS stage is NOT operational.");
                return false;
            }
        }

        log.info("MASS stage is operational.");

        return true;
    }

    @Override
    public String getCommand() throws ConanParameterException {
        return this.getFullCommand();
    }

    @Override
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        try {
            log.info("Starting MASS");

            // Get shortcut to the args
            Args args = (Args) this.getProcessArgs();

            List<Integer> jobIds = new ArrayList<>();

            for (MassJob.Args singleMassArgs : args.getSingleMassArgsList()) {

                // Ensure output directory for this MASS run exists
                if (!singleMassArgs.getOutputDir().exists() && !singleMassArgs.getOutputDir().mkdirs()) {
                    throw new IOException("Couldn't create directory for MASS");
                }

                // Execute the mass job and record any job ids
                jobIds.addAll(this.executeMassJob(singleMassArgs, executionContext));
            }

            // Wait for all assembly jobs to finish if they are running in parallel.
            if (executionContext.usingScheduler() && args.isRunParallel()) {
                log.debug("Single MASS jobs were executed in parallel, waiting for all to complete");
                this.conanExecutorService.executeScheduledWait(
                        jobIds,
                        args.getJobPrefix() + "-mass-*",
                        ExitStatus.Type.COMPLETED_ANY,
                        args.getJobPrefix() + "-wait",
                        args.getOutputDir());
            }

            log.info("MASS complete");

        } catch (IOException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        }

        return true;
    }

    protected List<Integer> executeMassJob(MassJob.Args massJobArgs, ExecutionContext executionContext)
            throws InterruptedException, ProcessExecutionException {
        MassJob massJob = new MassJob(this.conanExecutorService, massJobArgs);
        massJob.execute(executionContext);
        return massJob.getJobIds();
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

    public static class Args extends AbstractProcessArgs implements RampartStageArgs {

        // Keys for config file
        private static final String KEY_ATTR_PARALLEL = "parallel";
        private static final String KEY_ELEM_SINGLE_MASS = "job";

        // Constants
        public static final int DEFAULT_CVG_CUTOFF = -1;
        public static final OutputLevel DEFAULT_OUTPUT_LEVEL = OutputLevel.CONTIGS;
        public static final boolean DEFAULT_RUN_PARALLEL = false;


        // Need access to these
        //private SingleMassParams params = new SingleMassParams();

        // Rampart vars
        private String jobPrefix;
        private File outputDir;
        private List<MassJob.Args> singleMassArgsList;    // List of Single MASS groups to run separately
        private List<Library> allLibraries;                    // All allLibraries available in this job
        private List<Mecq.EcqArgs> allMecqs;                 // All mecq configurations
        private File mecqDir;
        private boolean runParallel;                // Whether to run MASS groups in parallel
        private Organism organism;


        private OutputLevel outputLevel;

        @Override
        public List<ConanProcess> getExternalProcesses() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Args() {
            super(new Params());

            this.jobPrefix = "";
            this.outputDir = null;

            this.allLibraries = new ArrayList<>();
            this.allMecqs = new ArrayList<>();
            this.mecqDir = null;

            this.outputLevel = DEFAULT_OUTPUT_LEVEL;

            this.organism = null;
            this.runParallel = DEFAULT_RUN_PARALLEL;
            this.singleMassArgsList = new ArrayList<>();
        }

        public Args(Element ele, File outputDir, File mecqDir, String jobPrefix, List<Library> allLibraries, List<Mecq.EcqArgs> allMecqs, Organism organism)
                throws IOException {

            // Set defaults first
            this();

            // Set from parameters
            this.outputDir = outputDir;
            this.jobPrefix = jobPrefix;
            this.allLibraries = allLibraries;
            this.allMecqs = allMecqs;
            this.organism = organism;
            this.mecqDir = mecqDir;


            // From Xml (optional)
            this.runParallel = ele.hasAttribute(KEY_ATTR_PARALLEL) ?
                    XmlHelper.getBooleanValue(ele, KEY_ATTR_PARALLEL) :
                    DEFAULT_RUN_PARALLEL;

            // All single mass args
            NodeList nodes = ele.getElementsByTagName(KEY_ELEM_SINGLE_MASS);
            for(int i = 0; i < nodes.getLength(); i++) {
                this.singleMassArgsList.add(
                        new MassJob.Args(
                                (Element)nodes.item(i), outputDir, mecqDir, jobPrefix + "-group",
                                this.allLibraries, this.allMecqs, this.organism, this.runParallel));
            }
        }

        protected Params getParams() {
            return (Params)this.params;
        }


        public File getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(File outputDir) {
            this.outputDir = outputDir;
        }

        public String getJobPrefix() {
            return jobPrefix;
        }

        public void setJobPrefix(String jobPrefix) {
            this.jobPrefix = jobPrefix;
        }

        public OutputLevel getOutputLevel() {
            return outputLevel;
        }

        public void setOutputLevel(OutputLevel outputLevel) {
            this.outputLevel = outputLevel;
        }

        public boolean isRunParallel() {
            return runParallel;
        }

        public void setRunParallel(boolean runParallel) {
            this.runParallel = runParallel;
        }

        public Organism getOrganism() {
            return organism;
        }

        public void setOrganism(Organism organism) {
            this.organism = organism;
        }

        public List<Library> getAllLibraries() {
            return allLibraries;
        }

        public void setAllLibraries(List<Library> allLibraries) {
            this.allLibraries = allLibraries;
        }

        public List<MassJob.Args> getSingleMassArgsList() {
            return singleMassArgsList;
        }

        public void setSingleMassArgsList(List<MassJob.Args> singleMassArgsList) {
            this.singleMassArgsList = singleMassArgsList;
        }

        public List<Mecq.EcqArgs> getAllMecqs() {
            return allMecqs;
        }

        public void setAllMecqs(List<Mecq.EcqArgs> allMecqs) {
            this.allMecqs = allMecqs;
        }

        public File getMecqDir() {
            return mecqDir;
        }

        public void setMecqDir(File mecqDir) {
            this.mecqDir = mecqDir;
        }

        @Override
        public void parse(String args) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public ParamMap getArgMap() {

            Params params = this.getParams();
            ParamMap pvp = new DefaultParamMap();

            if (this.outputLevel != null) {
                pvp.put(params.getOutputLevel(), this.outputLevel.toString());
            }

            if (this.outputDir != null)
                pvp.put(params.getOutputDir(), this.outputDir.getAbsolutePath());

            if (this.jobPrefix != null)
                pvp.put(params.getJobPrefix(), this.jobPrefix);


            return pvp;
        }

        @Override
        protected void setOptionFromMapEntry(ConanParameter param, String value) {

            Params params = this.getParams();

            if (param.equals(params.getJobPrefix())) {
                this.jobPrefix = value;
            } else if (param.equals(params.getOutputDir())) {
                this.outputDir = new File(value);
            } else if (param.equals(params.getOutputLevel())) {
                this.outputLevel = OutputLevel.valueOf(value);
            }
        }

        @Override
        protected void setArgFromMapEntry(ConanParameter param, String value) {

        }

    }

    public static class Params extends AbstractProcessParams {

        private ConanParameter assembler;
        private ConanParameter kmin;
        private ConanParameter kmax;
        private ConanParameter stepSize;
        private ConanParameter libs;
        private ConanParameter outputDir;
        private ConanParameter jobPrefix;
        private ConanParameter threads;
        private ConanParameter memory;
        private ConanParameter parallelismLevel;
        private ConanParameter coverageCutoff;
        private ConanParameter outputLevel;
        private ConanParameter inputSource;


        public Params() {

            this.assembler = new ParameterBuilder()
                    .longName("asm")
                    .description("De Brujin Assembler to use")
                    .argValidator(ArgValidator.DEFAULT)
                    .isOptional(false)
                    .create();

            this.kmin = new NumericParameter(
                    "kmin",
                    "The minimum k-mer value to assemble. (Default: 51)",
                    true);

            this.kmax = new NumericParameter(
                    "kmax",
                    "The maximum k-mer value to assemble. (Default: 85)",
                    true);

            this.stepSize = new ParameterBuilder()
                    .longName("step")
                    .description("The kmer step size between each assembly: [FINE, MEDIUM, COARSE].  (Default: MEDIUM)")
                    .argValidator(ArgValidator.DEFAULT)
                    .create();

            this.libs = new ParameterBuilder()
                    .longName("libs")
                    .description("All libraries to use for this MASS run")
                    .argValidator(ArgValidator.OFF)
                    .isOptional(false)
                    .create();

            this.outputDir = new PathParameter(
                    "output",
                    "The output directory",
                    true);

            this.jobPrefix = new ParameterBuilder()
                    .longName("job_prefix")
                    .description("The job_prefix to be assigned to all sub processes in MASS.  Useful if executing with a scheduler.")
                    .argValidator(ArgValidator.DEFAULT)
                    .create();

            this.threads = new NumericParameter(
                    "threads",
                    "The number of threads to use for each assembly process (Default: 8)",
                    true);

            this.memory = new NumericParameter(
                    "memory",
                    "The amount of memory to request for each assembly process (Default: 50000)",
                    true);

            this.parallelismLevel = new ParameterBuilder()
                    .longName("parallelismLevel")
                    .description("The level of parallelism to use when running MASS: [LINEAR, PARALLEL_ASSEMBLIES_ONLY, PARALLEL_MASS_ONLY, PARALLEL].  (Default: LINEAR)")
                    .argValidator(ArgValidator.DEFAULT)
                    .create();

            this.coverageCutoff = new NumericParameter(
                    "coverageCutoff",
                    "The kmer coverage level below which kmers are discarded (Default: -1 i.e. OFF)",
                    true);

            this.outputLevel = new ParameterBuilder()
                    .longName("outputLevel")
                    .description("The output level for the assembler used by MASS: [UNITIGS, CONTIGS, SCAFFOLDS].  (Default: CONTIGS)")
                    .argValidator(ArgValidator.DEFAULT)
                    .create();

            this.inputSource = new ParameterBuilder()
                    .longName("inputSource")
                    .description("The input source to use for MASS: [RAW, BEST, ALL, <NUM>].  (Default: ALL)")
                    .argValidator(ArgValidator.DEFAULT)
                    .create();

        }



        public ConanParameter getAssembler() {
            return assembler;
        }

        public ConanParameter getKmin() {
            return kmin;
        }

        public ConanParameter getKmax() {
            return kmax;
        }

        public ConanParameter getStepSize() {
            return stepSize;
        }

        public ConanParameter getLibs() {
            return libs;
        }

        public ConanParameter getOutputDir() {
            return outputDir;
        }

        public ConanParameter getJobPrefix() {
            return jobPrefix;
        }

        public ConanParameter getThreads() {
            return threads;
        }

        public ConanParameter getMemory() {
            return memory;
        }

        public ConanParameter getParallelismLevel() {
            return parallelismLevel;
        }

        public ConanParameter getCoverageCutoff() {
            return coverageCutoff;
        }

        public ConanParameter getOutputLevel() {
            return outputLevel;
        }

        public ConanParameter getInputSource() {
            return inputSource;
        }

        @Override
        public ConanParameter[] getConanParametersAsArray() {
            return new ConanParameter[]{
                    this.assembler,
                    this.kmin,
                    this.kmax,
                    this.stepSize,
                    this.libs,
                    this.outputDir,
                    this.jobPrefix,
                    this.threads,
                    this.memory,
                    this.parallelismLevel,
                    this.coverageCutoff,
                    this.outputLevel,
                    this.inputSource
            };
        }

    }

}
