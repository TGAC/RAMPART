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
package uk.ac.tgac.rampart.pipeline.tool.process.analyser.length;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.process.r.RV2122Args;
import uk.ac.tgac.rampart.conan.process.r.RV2122Process;
import uk.ac.tgac.rampart.core.data.AssemblyStats;
import uk.ac.tgac.rampart.core.service.SequenceStatisticsService;
import uk.ac.tgac.rampart.core.service.impl.SequenceStatisticsServiceImpl;
import uk.ac.tgac.rampart.pipeline.tool.pipeline.RampartStage;
import uk.ac.tgac.rampart.pipeline.util.RHelper;

import java.io.*;
import java.util.*;

/**
 * User: maplesod
 * Date: 30/01/13
 * Time: 14:49
 */
@Component
public class LengthAnalysisProcess extends AbstractConanProcess {

    @Autowired
    private SequenceStatisticsService sequenceStatisticsService = new SequenceStatisticsServiceImpl();

    private static Logger log = LoggerFactory.getLogger(LengthAnalysisProcess.class);


    public LengthAnalysisProcess() {
        this(new LengthAnalysisArgs());
    }

    public LengthAnalysisProcess(LengthAnalysisArgs args) {
        super("", args, new LengthAnalysisParams());
    }

    @Override
    public String getName() {
        return "Assembly Statistics";
    }

    @Override
    public Collection<ConanParameter> getParameters() {
        return new LengthAnalysisParams().getConanParameters();
    }

    @Override
    public String getCommand() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean execute(ExecutionContext env) throws ProcessExecutionException, InterruptedException {

        LengthAnalysisArgs args = (LengthAnalysisArgs)this.getProcessArgs();

        // Assume we want to process all the FastA files in the directory specified by the user.
        File[] assemblyFiles = args.getInputDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".fa") || name.endsWith(".fasta");
            }
        });

        // Sort the files
        Arrays.sort(assemblyFiles);

        log.debug("Found these files in the input directory:\n" + StringUtils.join(assemblyFiles, "\n"));

        try {
            // Process the assemblies to get the statistics
            Map<String, AssemblyStats> statsMap = new LinkedHashMap<String, AssemblyStats>();
            for (File assemblyFile : assemblyFiles) {

                String key = args.getRampartStage().translateFilenameToKey(assemblyFile.getName());
                AssemblyStats stats = this.sequenceStatisticsService.analyseAssembly(assemblyFile);
                statsMap.put(key, stats);
            }

            File statsFile = new File(args.getOutputDir(), "analyser.txt");

            // Store the analyser in a file, so we can load them in R
            writeStatistics(statsFile, statsMap, args.getRampartStage());

            // Plot the analyser using our R script
            plot(args.getOutputDir(), statsFile, env);

        } catch (IOException ioe) {
            // Just log the exception and carry on
            throw new ProcessExecutionException(-1, ioe);
        }

        return true;
    }

    protected void writeStatistics(File outputFile, Map<String, AssemblyStats> statsMap, RampartStage rampartStage) throws IOException {

        PrintWriter writer = new PrintWriter(new FileWriter(outputFile));

        writer.println(rampartStage.getStatsID() + "|" + AssemblyStats.getStatsFileHeader());
        for (Map.Entry<String, AssemblyStats> entry : statsMap.entrySet()) {
            writer.println(entry.getKey() + "|" + (entry.getValue() != null ? entry.getValue().toStatsFileString() : ""));
        }

        writer.close();
    }

    protected void plot(File outputDir, File statsFile, ExecutionContext env) throws InterruptedException, ProcessExecutionException {

        // Build the mass plotter R scripts arg list
        List<String> rScriptArgs = new ArrayList<String>();
        rScriptArgs.add(statsFile.getAbsolutePath());
        rScriptArgs.add(new File(outputDir, "analyser.pdf").getAbsolutePath());

        // Set the args for R
        RV2122Args rArgs = new RV2122Args();
        rArgs.setScript(RHelper.STATS_PLOTTER.getExternalScript());
        rArgs.setOutput(new File(outputDir, "stats_plotter.log"));
        rArgs.setArgs(rScriptArgs);

        // Create the Mass Plotter R process
        RV2122Process statsPlotterProcess = new RV2122Process(rArgs);


        ExecutionContext envCopy = env.copy();

        if (envCopy.usingScheduler()) {
            envCopy.getScheduler().getArgs().setMonitorFile(new File(outputDir, "analyser.log"));
        }

        // Execute the Mass Plotter R script.
        this.conanProcessService.execute(statsPlotterProcess, envCopy);
    }
}
