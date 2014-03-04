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
package uk.ac.tgac.rampart.tool.process.report;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionResult;
import uk.ac.ebi.fgpt.conan.core.context.locality.Local;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.service.VelocityMergerService;
import uk.ac.tgac.rampart.tool.process.MockedConanProcess;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.when;

/**
 * User: maplesod
 * Date: 08/03/13
 * Time: 11:41
 */
public class ReportProcessTest extends MockedConanProcess {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Mock
    private VelocityMergerService velocityMergerService;


    @Test
    public void testReportExecute() throws InterruptedException, ProcessExecutionException, IOException, ConanParameterException {

        File testDir = temp.newFolder("testReport");
        File jobDir = new File(testDir, "job");

        jobDir.mkdir();

        createRampartDirStructure(jobDir);

        ReportArgs args = new ReportArgs();
        args.setJobDir(jobDir);

        ReportProcess process = new ReportProcess(args);

        when(conanProcessService.execute(process, ec)).thenReturn(new DefaultExecutionResult(0, null, null, -1));

        ReflectionTestUtils.setField(process, "conanExecutorService", conanExecutorService);
        ReflectionTestUtils.setField(process, "velocityMergerService", velocityMergerService);

        process.execute(ec);
       /*
        assertTrue(new File(jobDir, "report/template.tex").exists());
        assertTrue(new File(jobDir, "report/images").exists());
        assertTrue(new File(jobDir, "report/images").isDirectory()); */
    }


    private void createRampartDirStructure(File jobDir) {
        File readsDir = new File(jobDir, "mecq");
        File massDir = new File(jobDir, "mass");
        File massStatsDir = new File(massDir, "stats");
        File ampDir = new File(jobDir, "amp");

        readsDir.mkdir();
        massDir.mkdir();
        massStatsDir.mkdir();
        ampDir.mkdir();
    }

}
