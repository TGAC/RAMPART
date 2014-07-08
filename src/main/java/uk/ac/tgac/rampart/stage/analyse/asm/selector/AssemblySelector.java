package uk.ac.tgac.rampart.stage.analyse.asm.selector;

import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStats;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStatsTable;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 22/01/14
 * Time: 15:51
 * To change this template use File | Settings | File Templates.
 */
public interface AssemblySelector {

    AssemblyStats selectAssembly(
                        AssemblyStatsTable table,
                        long estimatedGenomeSize,
                        double estimatedGcPercentage,
                        File massDir);
}
