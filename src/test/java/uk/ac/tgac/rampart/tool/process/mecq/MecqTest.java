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
package uk.ac.tgac.rampart.tool.process.mecq;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionResult;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.conan.process.ec.sickle.SickleV11Process;
import uk.ac.tgac.rampart.tool.process.Mecq;
import uk.ac.tgac.rampart.tool.process.MockedConanProcess;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * User: maplesod
 * Date: 24/01/13
 * Time: 11:53
 */
public class MecqTest extends MockedConanProcess {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Mock
    private SickleV11Process sickle;

    @Test
    public void testQT() throws InterruptedException, ProcessExecutionException, IOException, CommandExecutionException, ConanParameterException {

        File outputDir = temp.newFolder("qtTest");

        File cfgFile = FileUtils.toFile(this.getClass().getResource("/tools/test_rampart_1.cfg"));

        Mecq.Args mecqArgs = new Mecq.Args();
        mecqArgs.setMecqDir(outputDir);

        Mecq mecq = new Mecq(this.conanExecutorService, mecqArgs);

        when(conanExecutorService.getConanProcessService().execute(sickle, ec)).thenReturn(new DefaultExecutionResult(0, null, null, -1));

        ReflectionTestUtils.setField(mecq, "conanExecutorService", conanExecutorService);

        boolean success = mecq.execute(ec);

        assertTrue(success);
    }

}