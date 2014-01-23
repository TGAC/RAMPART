package uk.ac.tgac.rampart.tool.process.analyse.asm.selector;

import uk.ac.tgac.rampart.tool.process.analyse.asm.stats.AssemblyStatsMatrixRow;
import uk.ac.tgac.rampart.tool.process.analyse.asm.stats.AssemblyStatsTable;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 22/01/14
 * Time: 15:51
 * To change this template use File | Settings | File Templates.
 */
public interface AssemblySelector {

    File selectAssembly(AssemblyStatsTable table,
                        int estimatedGenomeSize,
                        double estimatedGcPercentage);
}
