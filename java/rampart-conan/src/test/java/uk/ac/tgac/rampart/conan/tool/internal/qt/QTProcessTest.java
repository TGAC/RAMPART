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
package uk.ac.tgac.rampart.conan.tool.internal.qt;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.rampart.conan.conanx.env.DefaultEnvironment;
import uk.ac.tgac.rampart.conan.conanx.env.Environment;
import uk.ac.tgac.rampart.conan.conanx.env.scheduler.SchedulerArgs;
import uk.ac.tgac.rampart.conan.conanx.env.scheduler.lsf.LSF;
import uk.ac.tgac.rampart.conan.conanx.env.scheduler.lsf.LSFArgs;
import uk.ac.tgac.rampart.conan.conanx.env.locality.ConnectionDetails;
import uk.ac.tgac.rampart.conan.conanx.env.locality.Remote;
import uk.ac.tgac.rampart.conan.conanx.parameter.FilePair;
import uk.ac.tgac.rampart.conan.tool.external.qt.QualityTrimmer;
import uk.ac.tgac.rampart.conan.tool.external.qt.sickle.SicklePeV11Args;
import uk.ac.tgac.rampart.conan.tool.external.qt.sickle.SickleV11Process;
import uk.ac.tgac.rampart.conan.tool.external.qt.sickle.SickleV11QualityTypeParameter;
import uk.ac.tgac.rampart.conan.util.PETestLibrary;

import java.io.File;
import java.io.IOException;

/**
 * User: maplesod
 * Date: 24/01/13
 * Time: 11:53
 */
public class QTProcessTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();


    @Test
    public void localQTTest() throws InterruptedException, ProcessExecutionException, IOException, CommandExecutionException {

        File outputDir = temp.newFolder("qtTest");

        FilePair pairedEndInputLibrary = new PETestLibrary().getLocalPairedEndTestLib();

        SicklePeV11Args args = new SicklePeV11Args();
        args.setPairedEndInputFiles(pairedEndInputLibrary);
        args.setPairedEndOutputFiles(new FilePair(
                new File(outputDir, "peOut1.fastq"),
                new File(outputDir, "peOut2.fastq")
        ));
        args.setSingleEndOutputFile(new File(outputDir, "seOut.fastq"));
        args.setQualityThreshold(30);
        args.setMinLength(50);

        QualityTrimmer qt = new SickleV11Process(SickleV11Process.JobType.PAIRED_END, args);

        QTArgs qtArgs = new QTArgs();
        qtArgs.setQualityTrimmer(qt);

        QTProcess simpleQTProcess = new QTProcess(qtArgs);

        simpleQTProcess.execute(new DefaultEnvironment());
    }

}
