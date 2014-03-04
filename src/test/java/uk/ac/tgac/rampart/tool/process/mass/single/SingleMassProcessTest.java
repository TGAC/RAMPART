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
package uk.ac.tgac.rampart.tool.process.mass.single;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionResult;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.conan.process.asm.Assembler;
import uk.ac.tgac.rampart.tool.process.MockedConanProcess;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * User: maplesod
 * Date: 12/02/13
 * Time: 10:20
 */
public class SingleMassProcessTest extends MockedConanProcess {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Mock
    private SingleMassExecutor singleMassExecutor;

    @Test
    public void testExecute() throws ProcessExecutionException, InterruptedException, IOException, CommandExecutionException, ConanParameterException {

        File outputDir = temp.newFolder("singleMASSTest");

        File cfgFile = FileUtils.toFile(this.getClass().getResource("/tools/test_rampart_1.cfg"));

        SingleMassArgs args = new SingleMassArgs();
        args.setTool("ABYSS_V1.3.4");
        args.setKmerRange(new KmerRange(51, 65, KmerRange.StepSize.MEDIUM));
        args.setJobPrefix("massTest");
        args.setOutputDir(outputDir);

        SingleMassProcess singleMassProcess = new SingleMassProcess(args, conanExecutorService.getConanProcessService());
        AbstractConanProcess smParent = singleMassProcess;

        when(conanProcessService.execute(singleMassProcess, ec)).thenReturn(new DefaultExecutionResult(0, null, null, -1));
        when(singleMassExecutor.executeAssembler((Assembler) any(), anyString(), anyBoolean(), (List<Integer>)any()))
                .thenReturn(new DefaultExecutionResult(0, null, null, -1));


        ReflectionTestUtils.setField(smParent, "conanExecutorService", conanExecutorService);
        ReflectionTestUtils.setField(smParent, "singleMassExecutor", singleMassExecutor);

        singleMassProcess.execute(ec);

        assertTrue(new File(outputDir, "cvg-all_k-51").exists());
        assertTrue(new File(outputDir, "cvg-all_k-63").exists());
        assertTrue(new File(outputDir, "unitigs").exists());
    }
}
