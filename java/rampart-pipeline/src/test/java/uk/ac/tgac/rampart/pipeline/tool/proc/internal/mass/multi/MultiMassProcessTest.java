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
package uk.ac.tgac.rampart.pipeline.tool.proc.internal.mass.multi;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.context.locality.Local;
import uk.ac.tgac.rampart.pipeline.conanx.exec.process.ProcessExecutionService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * User: maplesod
 * Date: 12/02/13
 * Time: 10:20
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiMassProcessTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Mock
    ExecutionContext ec;

    @Mock
    ProcessExecutionService processExecutionService;

    @Test
    public void testExecute() throws InterruptedException, ProcessExecutionException {

        File outputDir = temp.newFolder("testMultiMass");

        File cfgFile1 = FileUtils.toFile(this.getClass().getResource("/tools/test_rampart_1.cfg"));
        File cfgFile2 = FileUtils.toFile(this.getClass().getResource("/tools/test_rampart_2.cfg"));

        List<File> configs = new ArrayList<File>();
        configs.add(cfgFile1);
        configs.add(cfgFile2);

        MultiMassArgs args = new MultiMassArgs();
        args.setConfigs(configs);
        args.setKmin(31);
        args.setJobPrefix("testMultiMass");
        args.setOutputDir(outputDir);

        assertTrue(args.getKmin() == 31);
        assertTrue(args.getKmax() == MultiMassArgs.DEFAULT_KMER_MAX);

        MultiMassProcess multiMass = new MultiMassProcess(args);

        when(processExecutionService.execute(multiMass, ec)).thenReturn(0);
        when(ec.getLocality()).thenReturn(new Local());

        ReflectionTestUtils.setField(multiMass, "processExecutionService", processExecutionService);

        multiMass.execute(ec);

        assertTrue(new File(outputDir, "stats").exists());
        assertTrue(new File(outputDir, "RAW").exists());
        assertTrue(new File(outputDir, "QT").exists());
    }
}
