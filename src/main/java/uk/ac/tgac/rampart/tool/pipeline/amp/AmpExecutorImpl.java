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
package uk.ac.tgac.rampart.tool.pipeline.amp;

import org.springframework.stereotype.Service;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.rampart.tool.RampartExecutorImpl;
import uk.ac.tgac.rampart.tool.process.stats.StatsExecutor;
import uk.ac.tgac.rampart.tool.process.stats.StatsExecutorImpl;

import java.io.File;
import java.io.IOException;

/**
 * User: maplesod
 * Date: 25/03/13
 * Time: 11:10
 */
@Service
public class AmpExecutorImpl extends RampartExecutorImpl implements AmpExecutor {

    private StatsExecutor statsExecutor = new StatsExecutorImpl();

    @Override
    public void initialise(ConanProcessService conanProcessService, ExecutionContext executionContext) {
        super.initialise(conanProcessService, executionContext);

        statsExecutor.initialise(conanProcessService, executionContext);
    }

    @Override
    public void executeAnalysisJob(AmpArgs args)
            throws InterruptedException, ProcessExecutionException, IOException, CommandExecutionException {

        if (args.getStatsLevels() != null) {
            this.statsExecutor.dispatchAnalyserJobs(args.getStatsLevels(), args.getAssembliesDir(), 1,
                    args.getOrganism() != null ? args.getOrganism().getEstGenomeSize() : 0,
                    true, args.isRunParallel(), null, args.getJobPrefix() + "-stats");
        }
    }

    @Override
    public void createInitialLink(File sourceFile, File outputDir)
            throws InterruptedException, ProcessExecutionException {

        String linkCommand = this.makeLinkCommand(sourceFile, new File(outputDir, "amp-stage-0-scaffolds.fa"));

        ExecutionContext linkingExecutionContext = new DefaultExecutionContext(executionContext.getLocality(), null, null, true);

        this.conanProcessService.execute(linkCommand, linkingExecutionContext);
    }

}
