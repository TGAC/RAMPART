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
package uk.ac.tgac.rampart.stage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.test.util.ReflectionTestUtils;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.process.asm.KmerRange;
import uk.ac.tgac.rampart.MockedConanProcess;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * User: maplesod
 * Date: 12/02/13
 * Time: 10:20
 */
public class MassTest extends MockedConanProcess {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void testExecute() throws InterruptedException, ProcessExecutionException, IOException, ConanParameterException {

        File outputDir = temp.newFolder("testMass");

        MassJob.Args singleMassArgs = new MassJob.Args();
        singleMassArgs.setTool("Abyss_V1.5");
        singleMassArgs.setKmerRange(new KmerRange(31, 61, KmerRange.StepSize.COARSE));
        singleMassArgs.setOutputDir(new File(outputDir, "raw"));

        List<MassJob.Args> singleMassArgsList = new ArrayList<>();
        singleMassArgsList.add(singleMassArgs);

        Mass.Args args = new Mass.Args();
        args.setJobPrefix("testMass");
        args.setOutputDir(outputDir);
        args.setMassJobArgList(singleMassArgsList);

        assertTrue(args.getMassJobArgList().get(0).getKmerRange().getFirstKmer() == 31);

        Mass multiMass = new Mass(this.conanExecutorService, args);
        Mass spy = spy(multiMass);

        doReturn(new ArrayList<Integer>()).when(spy).executeMassJob((MassJob.Args) anyObject(), (ExecutionContext) anyObject());

        ReflectionTestUtils.setField(multiMass, "conanExecutorService", conanExecutorService);

        spy.execute(ec);

        assertTrue(new File(outputDir, "raw").exists());
    }
}
