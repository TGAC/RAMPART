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
package uk.ac.tgac.rampart.conan.tool.proc.mass;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.rampart.conan.conanx.exec.context.ExecutionContext;
import uk.ac.tgac.rampart.conan.conanx.exec.context.WaitCondition;
import uk.ac.tgac.rampart.conan.conanx.exec.process.ProcessExecutionService;
import uk.ac.tgac.rampart.conan.conanx.exec.task.TaskExecutionService;
import uk.ac.tgac.rampart.conan.tool.proc.RampartProcess;
import uk.ac.tgac.rampart.conan.tool.proc.mass.multi.MultiMassArgs;
import uk.ac.tgac.rampart.conan.tool.proc.mass.multi.MultiMassProcess;
import uk.ac.tgac.rampart.conan.tool.proc.mass.single.SingleMassArgs;
import uk.ac.tgac.rampart.conan.tool.proc.mass.single.SingleMassProcess;
import uk.ac.tgac.rampart.conan.tool.task.external.asm.abyss.AbyssV134Task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * User: maplesod
 * Date: 14/01/13
 * Time: 16:13
 */
@RunWith(MockitoJUnitRunner.class)
public class MassProcessTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Mock
    ExecutionContext ec;

    @Mock
    TaskExecutionService taskExecutionService;

    @Mock
    ProcessExecutionService processExecutionService;

    private File cfg1 = FileUtils.toFile(this.getClass().getResource("/tools/test_rampart_1.cfg"));
    private File cfg2 = FileUtils.toFile(this.getClass().getResource("/tools/test_rampart_2.cfg"));


    @Before
    public void setup() throws InterruptedException, CommandExecutionException, ProcessExecutionException, IOException {
        when(ec.getScheduler()).thenReturn(null);
        when(ec.usingScheduler()).thenReturn(false);

        when(processExecutionService.execute((RampartProcess)anyObject(), (ExecutionContext)anyObject())).thenReturn(0);
        when(taskExecutionService.waitFor((WaitCondition)anyObject(), (ExecutionContext)anyObject())).thenReturn(0);
    }

    private void assertDirExists(File rootDir, String dir) {
        assertTrue(new File(rootDir, dir).exists());
    }

    @Test
    public void testSingleMass() throws InterruptedException, ProcessExecutionException, IOException, CommandExecutionException {

        File outputDir = temp.newFolder("testSingleMass");

        SingleMassArgs args = new SingleMassArgs();
        args.setAssembler(new AbyssV134Task());
        args.setKmin(51);
        args.setKmax(65);
        args.setJobPrefix("massTest");
        args.setOutputDir(outputDir);
        args.setConfig(cfg1);

        SingleMassProcess simpleMass = new SingleMassProcess(args);
        ReflectionTestUtils.setField(simpleMass, "taskExecutionService", taskExecutionService);

        simpleMass.execute(ec);

        assertDirExists(outputDir, "51");
        assertDirExists(outputDir, "65");
        assertDirExists(outputDir, "contigs");
        assertDirExists(outputDir, "logs");
    }


    @Test
    public void testMultiMass() throws InterruptedException, CommandExecutionException, ProcessExecutionException, IOException {
        File outputDir = temp.newFolder("testMultiMass");

        List<File> configs = new ArrayList<File>();
        configs.add(cfg1);
        configs.add(cfg2);

        MultiMassArgs args = new MultiMassArgs();
        args.setConfigs(configs);
        args.setKmin(31);
        args.setJobPrefix("testMultiMass");
        args.setOutputDir(outputDir);

        assertTrue(args.getKmin() == 31);
        assertTrue(args.getKmax() == MultiMassArgs.DEFAULT_KMER_MAX);

        MultiMassProcess multiMass = new MultiMassProcess(args);
        ReflectionTestUtils.setField(multiMass, "taskExecutionService", taskExecutionService);
        ReflectionTestUtils.setField(multiMass, "processExecutionService", processExecutionService);
        multiMass.execute(ec);

        assertDirExists(outputDir, "stats");
        assertDirExists(outputDir, "RAW");
        assertDirExists(outputDir, "QT");
    }

}
