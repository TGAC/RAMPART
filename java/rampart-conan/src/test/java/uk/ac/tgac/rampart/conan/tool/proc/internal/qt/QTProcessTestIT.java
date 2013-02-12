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
package uk.ac.tgac.rampart.conan.tool.proc.internal.qt;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.exec.context.ExecutionContext;
import uk.ac.tgac.rampart.conan.ui.IntegrationTest;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 12/02/13
 * Time: 12:31
 */
@Category(IntegrationTest.class)
public class QTProcessTestIT {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void testQT() throws InterruptedException, ProcessExecutionException {

        File outputDir = temp.newFolder("qtTest");

        File cfgFile = FileUtils.toFile(this.getClass().getResource("/tools/test_rampart_1.cfg"));

        QTArgs qtArgs = new QTArgs();
        qtArgs.setOutputDir(outputDir);
        qtArgs.setConfig(cfgFile);

        QTProcess qtProcess = new QTProcess(qtArgs);

        boolean success = qtProcess.execute(new ExecutionContext());

        assertTrue(success);
    }


}
