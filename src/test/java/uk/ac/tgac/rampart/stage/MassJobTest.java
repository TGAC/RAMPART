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
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionResult;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.conan.process.asm.Assembler;
import uk.ac.tgac.conan.process.asm.KmerRange;
import uk.ac.tgac.rampart.MockedConanProcess;
import uk.ac.tgac.rampart.stage.util.CoverageRange;
import uk.ac.tgac.rampart.stage.util.VariableRange;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
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

        File outputDir = temp.newFolder("testExecuteAbyss");

        File cfgFile = FileUtils.toFile(this.getClass().getResource("/tools/test_rampart_1.cfg"));

        MassJob.Args args = new MassJob.Args();
        args.setTool("ABYSS_V1.5");
        args.setKmerRange(new KmerRange(51, 65, KmerRange.StepSize.MEDIUM));
        args.setJobPrefix("testExecuteAbyss");
        args.setOutputDir(outputDir);
        args.initialise();

        MassJob massJob = new MassJob(conanExecutorService, args);
        MassJob spy = Mockito.spy(massJob);

        AbstractConanProcess smParent = massJob;

        when(conanProcessService.execute(massJob, ec)).thenReturn(new DefaultExecutionResult("test", 0, null, null, -1));
        doReturn(new DefaultExecutionResult("test", 0, null, null, -1))
                .when(spy)
                .executeAssembler((Assembler) any(), anyString(), (List<Integer>) any(), anyBoolean());


        ReflectionTestUtils.setField(smParent, "conanExecutorService", conanExecutorService);

        spy.execute(ec);

        assertTrue(new File(outputDir, "k-51").exists());
        assertTrue(new File(outputDir, "k-61").exists());
        assertTrue(new File(outputDir, "unitigs").exists());
    }

    @Test
    public void testExecuteSoap() throws ProcessExecutionException, InterruptedException, IOException, CommandExecutionException, ConanParameterException {

        File outputDir = temp.newFolder("testExecuteSoap");

        MassJob.Args args = new MassJob.Args();
        args.setTool("SOAP_Assemble_V2.4");
        args.setKmerRange(new KmerRange(51, 65, 14));
        args.setCoverageRange(new CoverageRange(50, 100, CoverageRange.StepSize.COARSE, true));
        args.setJobPrefix("testExecuteSoap");
        args.setOutputDir(outputDir);
        args.initialise();

        MassJob massJob = new MassJob(conanExecutorService, args);
        MassJob spy = Mockito.spy(massJob);

        AbstractConanProcess smParent = massJob;

        when(conanProcessService.execute(massJob, ec)).thenReturn(new DefaultExecutionResult("test", 0, null, null, -1));
        doReturn(new DefaultExecutionResult("test", 0, null, null, -1))
                .when(spy)
                .executeAssembler((Assembler) any(), anyString(), (List<Integer>) any(), anyBoolean());


        ReflectionTestUtils.setField(smParent, "conanExecutorService", conanExecutorService);

        spy.execute(ec);

        assertTrue(new File(outputDir, "cvg-all_k-51").exists());
        assertTrue(new File(outputDir, "cvg-all_k-65").exists());
        assertTrue(new File(outputDir, "contigs").exists());
    }

    @Test
    public void testExecuteVelvet() throws ProcessExecutionException, InterruptedException, IOException, CommandExecutionException, ConanParameterException {

        File outputDir = temp.newFolder("testExecuteVelvet");

        MassJob.Args args = new MassJob.Args();
        args.setTool("Velvet_V1.2");
        args.setKmerRange(new KmerRange(51, 51, 0));
        args.setVariableRange(new VariableRange("cov_cutoff", "2,5,10"));
        args.setJobPrefix("testExecuteVelvet");
        args.setOutputDir(outputDir);
        args.initialise();

        MassJob massJob = new MassJob(conanExecutorService, args);
        MassJob spy = Mockito.spy(massJob);

        AbstractConanProcess smParent = massJob;

        when(conanProcessService.execute(massJob, ec)).thenReturn(new DefaultExecutionResult("test", 0, null, null, -1));
        doReturn(new DefaultExecutionResult("test", 0, null, null, -1))
                .when(spy)
                .executeAssembler((Assembler) any(), anyString(), (List<Integer>) any(), anyBoolean());


        ReflectionTestUtils.setField(smParent, "conanExecutorService", conanExecutorService);

        spy.execute(ec);

        assertTrue(new File(outputDir, "k-51_cov_cutoff-2").exists());
        assertTrue(new File(outputDir, "k-51_cov_cutoff-10").exists());
        assertTrue(new File(outputDir, "contigs").exists());
    }
}
