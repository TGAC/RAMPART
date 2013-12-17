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
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 12/02/13
 * Time: 12:31
 */
public class MecqProcessITCase {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void testQTDirect() throws InterruptedException, ProcessExecutionException, IOException {

        File outputDir = temp.newFolder("qtTest");

        File cfgFile = FileUtils.toFile(this.getClass().getResource("/config/test_rampart_config.xml"));

        MecqArgs mecqArgs = new MecqArgs();
        mecqArgs.setOutputDir(outputDir);

        MecqProcess mecqProcess = new MecqProcess(mecqArgs);

        boolean success = mecqProcess.execute(new DefaultExecutionContext());

        assertTrue(success);
    }


    /*@Test
    public void testQTViaCLI() throws InterruptedException, ProcessExecutionException, URISyntaxException, IOException {

        File outputDir = temp.newFolder("qtTestCLI");

        File cfgFile = FileUtils.toFile(this.getClass().getResource("/tools/test_rampart_1.cfg"));


        RampartCLI.main(new String[]{
                "--config",
                cfgFile.getAbsolutePath(),
                "--output",
                outputDir.getAbsolutePath(),
                "--stages",
                "QT"
        });
    }*/


}
