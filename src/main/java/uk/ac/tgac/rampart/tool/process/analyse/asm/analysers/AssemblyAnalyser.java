package uk.ac.tgac.rampart.tool.process.analyse.asm.analysers;

import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.tool.process.analyse.asm.AnalyseAsmsArgs;
import uk.ac.tgac.rampart.tool.process.analyse.asm.AnalyseAsmsExecutor;
import uk.ac.tgac.rampart.tool.process.analyse.asm.stats.AssemblyStatsTable;
import uk.ac.tgac.rampart.util.Service;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 22/01/14
 * Time: 11:19
 * To change this template use File | Settings | File Templates.
 */
public interface AssemblyAnalyser extends Service {

    boolean isOperational(ExecutionContext executionContext);

    boolean execute(AnalyseAsmsArgs args, ExecutionContext executionContext, AnalyseAsmsExecutor analyseAsmsExecutor)
            throws InterruptedException, ProcessExecutionException, ConanParameterException, IOException;

    void getStats(AssemblyStatsTable table, AnalyseAsmsArgs args) throws IOException;
}
