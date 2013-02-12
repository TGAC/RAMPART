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
package uk.ac.tgac.rampart.conan.tool.proc.internal.util.clean;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.exec.context.ExecutionContext;
import uk.ac.tgac.rampart.conan.conanx.exec.process.ProcessExecutionService;

import java.io.File;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * User: maplesod
 * Date: 12/02/13
 * Time: 11:38
 */
@RunWith(MockitoJUnitRunner.class)
public class CleanJobProcessTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Mock
    ExecutionContext ec;

    @Mock
    ProcessExecutionService processExecutionService;

    @Test
    public void testCleanJob() throws ProcessExecutionException, InterruptedException {

        File rampartDir = temp.newFolder("cleanJobTest");

        CleanJobArgs args = new CleanJobArgs();
        args.setJobDir(rampartDir);

        CleanJobProcess cleanJobProcess = new CleanJobProcess(args);

        when(processExecutionService.execute(cleanJobProcess, ec)).thenReturn(0);

        ReflectionTestUtils.setField(cleanJobProcess, "processExecutionService", processExecutionService);

        boolean success = cleanJobProcess.execute(ec);

        assertTrue(success);
    }
}
