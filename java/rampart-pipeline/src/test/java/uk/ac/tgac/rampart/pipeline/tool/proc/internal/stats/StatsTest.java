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
package uk.ac.tgac.rampart.pipeline.tool.proc.internal.stats;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.context.ExecutionContext;
import uk.ac.tgac.rampart.pipeline.conanx.exec.process.ProcessExecutionService;
import uk.ac.tgac.rampart.pipeline.tool.pipeline.RampartStage;
import uk.ac.tgac.rampart.core.service.SequenceStatisticsService;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 30/01/13
 * Time: 19:49
 */
public class StatsTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void localQTTest() throws InterruptedException, ProcessExecutionException, URISyntaxException {

        File outputDir = temp.newFolder("statsTest");
        File statsAssemblyDir = FileUtils.toFile(this.getClass().getResource("/tools/stats/MASS-k75-scaffolds.fa")).getParentFile();

        SequenceStatisticsService sequenceStatisticsService = Mockito.mock(SequenceStatisticsService.class);
        ProcessExecutionService processExecutionService = Mockito.mock(ProcessExecutionService.class);

        StatsArgs statsArgs = new StatsArgs();
        statsArgs.setInputDir(statsAssemblyDir);
        statsArgs.setOutputDir(outputDir);
        statsArgs.setRampartStage(RampartStage.MASS);

        ExecutionContext env = new ExecutionContext();

        StatsProcess statsProcess = new StatsProcess(statsArgs);

        // Inject mocks
        ReflectionTestUtils.setField(statsProcess, "sequenceStatisticsService", sequenceStatisticsService);
        ReflectionTestUtils.setField(statsProcess, "processExecutionService", processExecutionService);

        statsProcess.execute(env);

        File[] files = outputDir.listFiles();
        boolean foundStats = false;
        boolean foundPlots = false;
        for (File file : files) {
            if (file.getName().equals("stats.txt")) {
                foundStats = true;
            } else if (file.getName().equals("stats.pdf")) {
                foundPlots = true;
            }
        }

        assertTrue(foundStats);
    }
}
