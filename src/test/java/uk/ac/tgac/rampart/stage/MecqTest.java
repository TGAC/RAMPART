/*
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2015  Daniel Mapleson - TGAC
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
 */
package uk.ac.tgac.rampart.stage;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.process.re.GenericReadEnhancerArgs;
import uk.ac.tgac.conan.process.re.ReadEnhancer;
import uk.ac.tgac.conan.process.re.ReadEnhancerFactory;
import uk.ac.tgac.conan.process.re.tools.SickleV12;
import uk.ac.tgac.rampart.MockedConanProcess;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
    private SickleV12 sickle;

    @Test
    public void testQT() throws InterruptedException, ProcessExecutionException, IOException, CommandExecutionException, ConanParameterException {

        File outputDir = temp.newFolder("qtTest");

        File cfgFile = FileUtils.toFile(this.getClass().getResource("/tools/test_rampart_1.cfg"));

        Mecq.Args mecqArgs = new Mecq.Args();
        mecqArgs.setMecqDir(outputDir);

        Mecq mecq = new Mecq(this.conanExecutorService, mecqArgs);

        when(conanExecutorService.getConanProcessService().execute(sickle, ec)).thenReturn(new DefaultExecutionResult("test", 0, null, null, -1));

        ReflectionTestUtils.setField(mecq, "conanExecutorService", conanExecutorService);

        ExecutionResult result = mecq.execute(ec);

        assertTrue(result.getExitCode() == 0);
    }

    @Test
    public void testGetOutputFiles() {

        Mecq.EcqArgs args = new Mecq.EcqArgs();

        args.setTool("SICKLE_V1.2");
        args.setOutputDir(new File("test/"));

        Library lib = new Library();
        lib.setFiles(new File("file1.fl"), new File("file2.fl"));

        GenericReadEnhancerArgs genericArgs = new GenericReadEnhancerArgs();
        genericArgs.setInput(lib);
        genericArgs.setOutputDir(args.getOutputDir());
        genericArgs.setThreads(1);
        genericArgs.setMemoryGb(0);

        ReadEnhancer ec = ReadEnhancerFactory.create(args.getTool(), genericArgs, null);

        List<File> files = ec.getEnhancedFiles();

        assertTrue(files.size() == 3);
    }

}
