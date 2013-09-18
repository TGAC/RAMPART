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
package uk.ac.tgac.rampart.tool.process.mass.selector;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionResult;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Organism;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * User: maplesod
 * Date: 25/04/13
 * Time: 19:42
 */
@RunWith(MockitoJUnitRunner.class)
public class MassSelectorProcessTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Mock
    private ExecutionContext ec;

    @Mock
    private ConanProcessService conanProcessService;

    private File statsFile1 = FileUtils.toFile(this.getClass().getResource("/tools/stats/stats1.txt"));
    private File statsFile2 = FileUtils.toFile(this.getClass().getResource("/tools/stats/stats2.txt"));
    private File statsMerged = FileUtils.toFile(this.getClass().getResource("/tools/stats/stats-merged.tab"));
    private File weightingsFile = FileUtils.toFile(this.getClass().getResource("/data/weightings.tab"));


    @Test
    public void testMultiFileExecute() throws IOException, InterruptedException, ProcessExecutionException {

        File outputDir = temp.newFolder("massSelector-MultiFile");

        List<File> statsFiles = new ArrayList<File>();
        statsFiles.add(statsFile1);
        statsFiles.add(statsFile2);

        MassSelectorArgs args = new MassSelectorArgs();
        args.setOutputDir(outputDir);
        args.setStatsFiles(statsFiles);
        args.setWeightings(weightingsFile);
        args.setOrganism(new Organism("test", 1, 400000000, 52.0));

        MassSelectorProcess process = new MassSelectorProcess(args);
        AbstractConanProcess parentProcess = process;

        when(conanProcessService.execute(process, ec)).thenReturn(new DefaultExecutionResult(0, null, -1));
        when(ec.usingScheduler()).thenReturn(false);
        when(ec.copy()).thenReturn(ec);

        ReflectionTestUtils.setField(parentProcess, "conanProcessService", conanProcessService);

        process.execute(ec);
    }

    @Test
    public void testSingleFileExecute() throws IOException, InterruptedException, ProcessExecutionException {

        File outputDir = temp.newFolder("massSelector-SingleFile");

        List<File> statsFiles = new ArrayList<File>();
        statsFiles.add(statsFile1);
        statsFiles.add(statsFile2);

        MassSelectorArgs args = new MassSelectorArgs();
        args.setOutputDir(outputDir);
        args.setMergedFile(statsMerged);
        args.setWeightings(weightingsFile);

        MassSelectorProcess process = new MassSelectorProcess(args);
        AbstractConanProcess parentProcess = process;

        when(conanProcessService.execute(process, ec)).thenReturn(new DefaultExecutionResult(0, null, -1));
        when(ec.usingScheduler()).thenReturn(false);
        when(ec.copy()).thenReturn(ec);

        ReflectionTestUtils.setField(parentProcess, "conanProcessService", conanProcessService);

        process.execute(ec);
    }
}
