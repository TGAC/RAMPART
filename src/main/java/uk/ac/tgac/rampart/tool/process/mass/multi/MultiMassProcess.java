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
package uk.ac.tgac.rampart.tool.process.mass.multi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.asc.AssemblyStats;
import uk.ac.tgac.asc.AssemblyStatsMatrixRow;
import uk.ac.tgac.rampart.data.RampartConfiguration;
import uk.ac.tgac.rampart.tool.process.mass.MassArgs;
import uk.ac.tgac.rampart.tool.process.mass.selector.AssemblyStatsTable;
import uk.ac.tgac.rampart.tool.process.mass.selector.MassSelectorArgs;
import uk.ac.tgac.rampart.tool.process.mass.selector.MassSelectorExecutor;
import uk.ac.tgac.rampart.tool.process.mass.selector.MassSelectorExecutorImpl;
import uk.ac.tgac.rampart.tool.process.mass.single.SingleMassArgs;
import uk.ac.tgac.rampart.tool.process.mass.single.SingleMassExecutor;
import uk.ac.tgac.rampart.tool.process.mass.single.SingleMassExecutorImpl;

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
@Component
public class MultiMassProcess extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(MultiMassProcess.class);

    @Autowired
    private MassSelectorExecutor massSelectorExecutor = new MassSelectorExecutorImpl();

    @Autowired
    private SingleMassExecutor singleMassExecutor = new SingleMassExecutorImpl();

    public MultiMassProcess() {
        this(new MultiMassArgs());
    }

    public MultiMassProcess(MultiMassArgs args) {
        super("", args, new MultiMassParams());
    }

    @Override
    public String getName() {
        return "MultiMASS";
    }

    @Override
    public Collection<ConanParameter> getParameters() {
        return new MultiMassParams().getConanParameters();
    }

    @Override
    public String getCommand() {
        return this.getFullCommand();
    }

    @Override
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        try {
            log.info("Starting MultiMass run");

            MultiMassArgs args = (MultiMassArgs) this.getProcessArgs();

            List<File> statsFiles = new ArrayList<File>();

            List<SingleMassArgs> singleMassArgsList = this.createSingleMassArgsList(args);

            List<Thread> singleMassThreads = new ArrayList<Thread>();

            for (SingleMassArgs singleMassArgs : singleMassArgsList) {

                // Add the predicted analyser file to the list for processing later.
                statsFiles.add(singleMassArgs.getStatsFile());

                // Ensure output directory for this MASS run exists
                if (!singleMassArgs.getOutputDir().exists() && !singleMassArgs.getOutputDir().mkdirs()) {
                    throw new IOException("Couldn't create directory for MASS");
                }

                this.singleMassExecutor.executeSingleMass(singleMassArgs, this.conanProcessService, executionContext);
            }

            // Wait for all assembly jobs to finish if they are running as background tasks.
            if (args.getParallelismLevel().doParallelMass()) {
                log.debug("Single MASS jobs executed in parallel, waiting for completion");
                this.executeScheduledWait(args.getJobPrefix(), args.getOutputDir(), executionContext);
            }

            log.info("Assemblies complete");

            // Execute the Mass Selector job
            log.info("Analysing and comparing assemblies");
            executeMassSelector(args, statsFiles, executionContext);

            log.info("Multi MASS run complete");

        } catch (IOException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        }

        return true;
    }

    protected void executeScheduledWait(String jobPrefix, File outputDir, ExecutionContext executionContext)
            throws ProcessExecutionException, InterruptedException {

        // Duplicate the execution context so we don't modify the original accidentally.
        ExecutionContext executionContextCopy = executionContext.copy();

        if (executionContext.usingScheduler()) {

            String jobName = jobPrefix + "_wait";
            executionContextCopy.getScheduler().getArgs().setJobName(jobName);
            executionContextCopy.getScheduler().getArgs().setMonitorFile(new File(outputDir, jobName + ".log"));
            executionContextCopy.setForegroundJob(true);
        }

        this.conanProcessService.waitFor(
                executionContextCopy.getScheduler().createWaitCondition(ExitStatus.Type.COMPLETED_SUCCESS, jobPrefix + "*"),
                executionContextCopy);
    }

    protected void executeMassSelector(MultiMassArgs args, List<File> statsFiles, ExecutionContext executionContext)
            throws IOException, ProcessExecutionException, InterruptedException {

        File statsDir = new File(args.getOutputDir(), "stats");
        statsDir.mkdirs();

        if (!statsDir.exists()) {
            throw new IOException("Couldn't create directory for MASS analyser");
        }

        MassSelectorArgs massSelectorArgs = new MassSelectorArgs();
        massSelectorArgs.setStatsFiles(statsFiles);
        massSelectorArgs.setConfigs(args.getConfigs());
        massSelectorArgs.setOutputDir(statsDir);
        massSelectorArgs.setApproxGenomeSize(0L);
        massSelectorArgs.setWeightings(args.getWeightingsFile());

        this.massSelectorExecutor.executeMassSelector(massSelectorArgs, this.getConanProcessService(), executionContext);
    }

    private List<SingleMassArgs> createSingleMassArgsList(MultiMassArgs args) throws IOException {

        List<SingleMassArgs> singleMassArgsList = new ArrayList<SingleMassArgs>();

        List<RampartConfiguration> configs = RampartConfiguration.createList(
                args.getConfigs() != null ?
                    args.getConfigs() :
                    filterConfigs(args.getConfigDir(), args.getInputSource())
                , true);

        for (RampartConfiguration config : configs) {

            // Assume we're using args gathered from the config file for now
            SingleMassArgs singleMassArgs = new SingleMassArgs();
            singleMassArgs.parseConfig(config.getFile());

            // Override args loaded from file if explicitly specified by the MultiMassArgs
            if (args.getAssembler() != null) {
                singleMassArgs.setAssembler(args.getAssembler());
            }

            if (args.getKmin() != MassArgs.DEFAULT_KMER_MIN) {
                singleMassArgs.setKmin(args.getKmin());
            }

            if (args.getKmax() != MassArgs.DEFAULT_KMER_MAX) {
                singleMassArgs.setKmax(args.getKmax());
            }

            if (args.getStepSize() != MassArgs.DEFAULT_STEP_SIZE) {
                singleMassArgs.setStepSize(args.getStepSize());
            }

            if (args.getParallelismLevel() != MassArgs.DEFAULT_PARALLELISM_LEVEL) {
                singleMassArgs.setParallelismLevel(args.getParallelismLevel());
            }

            if (args.getCoverageCutoff() != MassArgs.DEFAULT_CVG_CUTOFF) {
                singleMassArgs.setCoverageCutoff(args.getCoverageCutoff());
            }

             // These args are automatically set.
            singleMassArgs.setOutputDir(new File(args.getOutputDir(), singleMassArgs.getJobName()));
            singleMassArgs.setJobPrefix(args.getJobPrefix() + "-" + singleMassArgs.getJobName());
            singleMassArgs.setConfig(config.getFile());
            singleMassArgs.setStatsOnly(args.isStatsOnly());

            // Add to list
            singleMassArgsList.add(singleMassArgs);
        }

        return singleMassArgsList;
    }

    protected List<File> filterConfigs(File configDir, String inputSource) {

        if (configDir == null || inputSource == null || inputSource.isEmpty())
            return null;

        MassArgs.InputSource source = MassArgs.InputSource.valueOf(inputSource);

        File[] files = source != null ? source.filter(configDir) : new File[]{new File(inputSource)};

        List<File> configFiles = new ArrayList<File>();

        for(File f : files) {
            configFiles.add(f);
            log.debug("Running mass for: " + f.getAbsolutePath());
        }



        return configFiles;
    }
}
