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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.ac.ebi.fgpt.conan.core.context.locality.Local;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.service.VelocityMergerService;

import java.io.File;

import static org.mockito.Mockito.when;

/**
 * User: maplesod
 * Date: 08/03/13
 * Time: 11:41
 */
@RunWith(MockitoJUnitRunner.class)
public class ReportProcessTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Mock
    ExecutionContext ec;

    @Mock
    ConanProcessService conanProcessService;

    @Mock
    VelocityMergerService velocityMergerService;


    @Test
    public void testReportExecute() throws InterruptedException, ProcessExecutionException {

        File testDir = temp.newFolder("testReport");
        File jobDir = new File(testDir, "job");

        jobDir.mkdir();

        createRampartDirStructure(jobDir);

        ReportArgs args = new ReportArgs();
        args.setJobDir(jobDir);

        ReportProcess process = new ReportProcess(args);

        when(conanProcessService.execute(process, ec)).thenReturn(0);
        when(ec.getLocality()).thenReturn(new Local());
        when(ec.usingScheduler()).thenReturn(false);
        when(ec.copy()).thenReturn(ec);

        ReflectionTestUtils.setField(process, "conanProcessService", conanProcessService);
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
