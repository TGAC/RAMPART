package uk.ac.tgac.rampart.stage.analyse.asm.analysers;

import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.stage.analyse.asm.AnalyseAssembliesArgs;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStatsTable;
import uk.ac.tgac.rampart.util.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 22/01/14
 * Time: 11:19
 * To change this template use File | Settings | File Templates.
 */
public interface AssemblyAnalyser extends Service {

    /**
     * Checks whether or not this assembly analyser looks properly configured for the system
     * @param executionContext The environment in which to check if the process is operational
     * @return True if analyser is operational, false otherwise
     */
    boolean isOperational(ExecutionContext executionContext);

    /**
     * Execute this assembly analysis
     * @param assemblies The list of assemblies to analyse
     * @param outputDir The output directory in which to produce files
     * @param jobPrefix The prefix to apply to any scheduled jobs executed as part of this analysis
     * @param args The args describing how to analyse the provided assemblies
     * @param ces The conan execution service
     * @return A list of job ids from the executed jobs
     * @throws InterruptedException Thrown if user has interrupted the process during execution
     * @throws ProcessExecutionException Thrown if there is an issue during execution of an external process
     * @throws ConanParameterException Thrown if there was an issue configuring the parameters for the external process
     * @throws IOException Thrown if there was an issue handling files before or after the external process is run
     */
    List<Integer> execute(List<File> assemblies, File outputDir, String jobPrefix, AnalyseAssembliesArgs args, ConanExecutorService ces)
            throws InterruptedException, ProcessExecutionException, ConanParameterException, IOException;

    /**
     * Updates the provided table with information from this analysis
     * @param table The table to update with new information
     * @param assemblies The analysed assemblies
     * @param reportDir The location in which reports from this analysis might be located
     * @param subGroup Information about the subgroup of assemblies analysed... only used for error messages if problems occur
     * @throws IOException Thrown if there we issues accessing all the necessary files
     */
    void updateTable(AssemblyStatsTable table, List<File> assemblies, File reportDir, String subGroup)
            throws IOException;

    /**
     * If this assembly analysis runs quickly, then we might to this information to consider doing more work with it.
     * @return True if this process runs quickly, false if not.
     */
    boolean isFast();
}
