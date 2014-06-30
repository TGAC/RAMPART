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
package uk.ac.tgac.rampart.tool.process.mass;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionResult;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.conan.process.asm.Assembler;
import uk.ac.tgac.conan.process.asm.KmerRange;
import uk.ac.tgac.rampart.tool.process.MockedConanProcess;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * User: maplesod
 * Date: 12/02/13
 * Time: 10:20
 */
public class MassJobTest extends MockedConanProcess {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void testExecuteAbyss() throws ProcessExecutionException, InterruptedException, IOException, CommandExecutionException, ConanParameterException {

        File outputDir = temp.newFolder("massJobTest");

        File cfgFile = FileUtils.toFile(this.getClass().getResource("/tools/test_rampart_1.cfg"));

        MassJob.Args args = new MassJob.Args();
        args.setTool("ABYSS_V1.3");
        args.setKmerRange(new KmerRange(51, 65, KmerRange.StepSize.MEDIUM));
        args.setJobPrefix("massTest");
        args.setOutputDir(outputDir);

        args.validateInputs();

        MassJob massJob = new MassJob(conanExecutorService, args);
        MassJob spy = Mockito.spy(massJob);

        AbstractConanProcess smParent = massJob;

        when(conanProcessService.execute(massJob, ec)).thenReturn(new DefaultExecutionResult(0, null, null, -1));
        doReturn(new DefaultExecutionResult(0, null, null, -1))
                .when(spy)
                .executeAssembler((Assembler) any(), anyString(), anyBoolean(), (List<Integer>) any());


        ReflectionTestUtils.setField(smParent, "conanExecutorService", conanExecutorService);

        spy.execute(ec);

        assertTrue(new File(outputDir, "cvg-all_k-51").exists());
        assertTrue(new File(outputDir, "cvg-all_k-61").exists());
        assertTrue(new File(outputDir, "unitigs").exists());
    }

    @Test
    public void testExecuteSoap() throws ProcessExecutionException, InterruptedException, IOException, CommandExecutionException, ConanParameterException {

        File outputDir = temp.newFolder("massJobTest");

        MassJob.Args args = new MassJob.Args();
        args.setTool("SOAP_Assemble_V2.4");
        args.setKmerRange(new KmerRange(51, 65, 14));
        args.setJobPrefix("massTest");
        args.setOutputDir(outputDir);
        args.initialise();
        args.validate();

        MassJob massJob = new MassJob(conanExecutorService, args);
        MassJob spy = Mockito.spy(massJob);

        AbstractConanProcess smParent = massJob;

        when(conanProcessService.execute(massJob, ec)).thenReturn(new DefaultExecutionResult(0, null, null, -1));
        doReturn(new DefaultExecutionResult(0, null, null, -1))
                .when(spy)
                .executeAssembler((Assembler) any(), anyString(), anyBoolean(), (List<Integer>) any());


        ReflectionTestUtils.setField(smParent, "conanExecutorService", conanExecutorService);

        spy.execute(ec);

        assertTrue(new File(outputDir, "cvg-all_k-51").exists());
        assertTrue(new File(outputDir, "cvg-all_k-65").exists());
        assertTrue(new File(outputDir, "contigs").exists());
    }
}
