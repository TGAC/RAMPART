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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.core.data.RampartJobFileStructure;
import uk.ac.tgac.rampart.pipeline.cli.IntegrationTest;
import uk.ac.tgac.rampart.pipeline.cli.RampartCLI;
import uk.ac.tgac.rampart.pipeline.tool.proc.internal.qt.QTArgs;
import uk.ac.tgac.rampart.pipeline.tool.proc.internal.qt.QTProcess;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 11/02/13
 * Time: 12:12
 */
@Category(IntegrationTest.class)
public class MultiMassProcessIT {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private List<File> configs;

    @Before
    public void setup() {

        File cfgFile1 = FileUtils.toFile(this.getClass().getResource("/tools/test_rampart_1.cfg"));
        File cfgFile2 = FileUtils.toFile(this.getClass().getResource("/tools/test_rampart_2.cfg"));

        List<File> configs = new ArrayList<File>();
        configs.add(cfgFile1);
        configs.add(cfgFile2);

        this.configs = configs;
    }

    @Test
    public void testMassDirect() throws InterruptedException, ProcessExecutionException {

        File outputDir = temp.newFolder("massTest");

        MultiMassArgs massArgs = new MultiMassArgs();
        massArgs.setOutputDir(outputDir);
        massArgs.setConfigs(configs);

        MultiMassProcess massProcess = new MultiMassProcess(massArgs);

        boolean success = massProcess.execute(new DefaultExecutionContext());

        assertTrue(success);
    }


    @Test
    public void testMassViaCLI() throws InterruptedException, ProcessExecutionException, URISyntaxException, IOException {

        File outputDir = temp.newFolder("massTestCLI");

        // Simulate RAMPART dir structure after QT
        RampartJobFileStructure jobFS = new RampartJobFileStructure(outputDir);
        jobFS.getReadsDir().mkdir();

        File cfgFile1 = FileUtils.toFile(this.getClass().getResource("/tools/test_rampart_1.cfg"));
        File cfgFile2 = FileUtils.toFile(this.getClass().getResource("/tools/test_rampart_2.cfg"));

        FileUtils.copyFile(cfgFile1, jobFS.getConfigRawFile());
        FileUtils.copyFile(cfgFile2, jobFS.getConfigQtFile());

        RampartCLI.main(new String[]{
                "--config",
                cfgFile1.getAbsolutePath(),
                "--output",
                outputDir.getAbsolutePath(),
                "--stages",
                "MASS"
        });
    }
}
