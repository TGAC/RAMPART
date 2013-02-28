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
package uk.ac.tgac.rampart.conan.process.qt.sickle;

import org.junit.Test;
import uk.ac.ebi.fgpt.conan.core.param.FilePair;
import uk.ac.tgac.rampart.conan.process.scaffold.sspace.SSpaceBasicV2Args;
import uk.ac.tgac.rampart.conan.process.scaffold.sspace.SSpaceBasicV2Process;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 28/02/13
 * Time: 14:05
 */
public class SickleV11ProcessTest {

    @Test
    public void testSickleV11Pe() {

        SicklePeV11Args args = new SicklePeV11Args();
        args.setPairedEndInputFiles(new FilePair(new File("1.fq"), new File("2.fq")));
        args.setPairedEndOutputFiles(new FilePair(new File("1.out.fq"), new File("2.out.fq")));
        args.setSingleEndOutputFile(new File("se.out.fq"));
        args.setDiscardN(true);
        args.setMinLength(50);
        args.setQualType(SickleV11QualityTypeParameter.SickleQualityTypeOptions.SANGER);
        args.setQualityThreshold(50);

        SickleV11Process sickleV11Process = new SickleV11Process(SickleV11Process.JobType.PAIRED_END, args);

        String command = sickleV11Process.getCommand();

        assertTrue(command.equals("sickle pe  --qual-threshold=50  --length-threshold=50   --qual-type sanger  --pe-file1 1.fq  --pe-file2 2.fq  --output-pe1 1.out.fq  --output-pe2 2.out.fq  --output-single se.out.fq"));
    }

    @Test
    public void testSickleV11Se() {

        SickleSeV11Args args = new SickleSeV11Args();
        args.setSingleEndInputFile(new File("se.fq"));
        args.setSingleEndOutputFile(new File("se.out.fq"));
        args.setDiscardN(true);
        args.setMinLength(50);
        args.setQualType(SickleV11QualityTypeParameter.SickleQualityTypeOptions.SANGER);
        args.setQualityThreshold(50);

        SickleV11Process sickleV11Process = new SickleV11Process(SickleV11Process.JobType.PAIRED_END, args);

        String command = sickleV11Process.getCommand();

        assertTrue(command.equals("sickle pe  --qual-threshold=50  --length-threshold=50   --qual-type sanger  --fastq-file=se.fq  --output-file=se.out.fq"));
    }
}
