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
package uk.ac.tgac.rampart.conan.tool.module.mass;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.rampart.conan.conanx.env.DefaultEnvironment;
import uk.ac.tgac.rampart.conan.tool.module.mass.single.SingleMassArgs;
import uk.ac.tgac.rampart.conan.tool.module.mass.single.SingleMassProcess;
import uk.ac.tgac.rampart.conan.tool.process.asm.abyss.AbyssV134Process;
import uk.ac.tgac.rampart.conan.util.PETestLibrary;

import java.io.File;
import java.io.IOException;

/**
 * User: maplesod
 * Date: 14/01/13
 * Time: 16:13
 */
public class MassProcessTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();


    @Test
    public void massTest() throws InterruptedException, ProcessExecutionException, IOException, CommandExecutionException {

        File outputDir = temp.newFolder("massTest");

        SingleMassArgs args = new SingleMassArgs();
        args.setAssembler(new AbyssV134Process());
        args.setKmin(51);
        args.setKmax(65);
        args.setJobPrefix("massTest");
        args.setOutputDir(outputDir);
        args.setConfig(new File("tools/test.cfg"));

        SingleMassProcess simpleMass = new SingleMassProcess(args);

        simpleMass.execute(new DefaultEnvironment());
    }

}
