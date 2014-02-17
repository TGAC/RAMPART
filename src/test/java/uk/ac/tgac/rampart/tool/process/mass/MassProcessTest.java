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
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionResult;
import uk.ac.ebi.fgpt.conan.core.context.locality.Local;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.tool.process.MockedConanProcess;
import uk.ac.tgac.rampart.tool.process.mass.single.KmerRange;
import uk.ac.tgac.rampart.tool.process.mass.single.SingleMassArgs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * User: maplesod
 * Date: 12/02/13
 * Time: 10:20
 */
public class MassProcessTest extends MockedConanProcess {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Mock
    private MassExecutor massExecutor;

    @Test
    public void testExecute() throws InterruptedException, ProcessExecutionException, IOException, ConanParameterException {

        File outputDir = temp.newFolder("testMass");

        File weightingsFile = FileUtils.toFile(this.getClass().getResource("/data/weightings.tab"));

        SingleMassArgs singleMassArgs = new SingleMassArgs();
        singleMassArgs.setTool("ABYSS_V1_3_4");
        singleMassArgs.setKmerRange(new KmerRange(31, 61, KmerRange.StepSize.COARSE));
        singleMassArgs.setOutputDir(new File(outputDir, "raw"));

        List<SingleMassArgs> singleMassArgsList = new ArrayList<>();
        singleMassArgsList.add(singleMassArgs);

        MassArgs args = new MassArgs();
        args.setJobPrefix("testMass");
        args.setOutputDir(outputDir);
        args.setSingleMassArgsList(singleMassArgsList);

        assertTrue(args.getSingleMassArgsList().get(0).getKmerRange().getFirstKmer() == 31);

        MassProcess multiMass = new MassProcess(args);

        when(conanProcessService.execute(multiMass, ec)).thenReturn(new DefaultExecutionResult(0, null, null, -1));
        when(ec.getLocality()).thenReturn(new Local());
        when(ec.usingScheduler()).thenReturn(false);
        when(ec.copy()).thenReturn(ec);

        ReflectionTestUtils.setField(multiMass, "conanProcessService", conanProcessService);
        ReflectionTestUtils.setField(multiMass, "massExecutor", massExecutor);

        multiMass.execute(ec);

        assertTrue(new File(outputDir, "raw").exists());
    }
}
