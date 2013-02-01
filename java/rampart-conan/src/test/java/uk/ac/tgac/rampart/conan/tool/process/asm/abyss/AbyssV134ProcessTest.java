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
package uk.ac.tgac.rampart.conan.tool.process.asm.abyss;

import org.junit.Test;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.rampart.conan.conanx.env.DefaultEnvironment;
import uk.ac.tgac.rampart.conan.conanx.env.Environment;
import uk.ac.tgac.rampart.conan.conanx.env.scheduler.lsf.LSF;
import uk.ac.tgac.rampart.conan.conanx.env.scheduler.lsf.LSFArgs;
import uk.ac.tgac.rampart.conan.service.ProcessExecutionService;
import uk.ac.tgac.rampart.conan.service.impl.DefaultProcessExecutionService;
import uk.ac.tgac.rampart.conan.util.PETestLibrary;
import uk.ac.tgac.rampart.core.data.Library;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * User: maplesod
 * Date: 31/01/13
 * Time: 11:47
 */
public class AbyssV134ProcessTest {

    @Test
    public void testAbyssV134() throws InterruptedException, ProcessExecutionException, IOException, CommandExecutionException {

        Set<Library> libs = new PETestLibrary().createLocalPETestLibrary();

        AbyssV134Args args = new AbyssV134Args();
        args.setLibraries(libs);
        args.setKmer(61);
        args.setName("OUTPUT_FILE");
        args.setThreads(16);

        AbyssV134Process abyss = new AbyssV134Process(args);
        abyss.addPreCommand("source abyss_cb-1.3.4");

        LSFArgs envArgs = new LSFArgs();
        envArgs.setMonitorFile(new File("~/test/rampart-conan/output.log"));
        envArgs.setMemoryMB(60000);
        envArgs.setThreads(16);
        envArgs.setJobName("testAbyssV134");
        envArgs.setOpenmpi(true);
        envArgs.setProjectName("test");
        envArgs.setExtraLsfOptions("-Rselect[hname!='n57142.tgaccluster']"); // Abyss doesn't like this node

        Environment env = new DefaultEnvironment(new LSF(envArgs));

        ProcessExecutionService exec = new DefaultProcessExecutionService();

        exec.execute(abyss, env);
    }
}
