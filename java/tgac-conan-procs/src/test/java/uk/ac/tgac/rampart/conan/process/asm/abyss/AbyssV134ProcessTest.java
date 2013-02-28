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
package uk.ac.tgac.rampart.conan.process.asm.abyss;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.rampart.core.data.Library;
import uk.ac.tgac.rampart.core.data.SeqFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 28/02/13
 * Time: 11:17
 */
public class AbyssV134ProcessTest {

    private String pwd;

    @Before
    public void setup() {

        String pwdFull = new File(".").getAbsolutePath();
        this.pwd = pwdFull.substring(0, pwdFull.length() - 1);
    }

    private List<Library> createLocalPETestLibrary() {
        SeqFile peFile1 = new SeqFile();
        peFile1.setFileType(SeqFile.FileType.FASTQ);
        peFile1.setFilePath(pwd + "tools/mass/LIB1896_R1.r95.fastq");

        SeqFile peFile2 = new SeqFile();
        peFile2.setFileType(SeqFile.FileType.FASTQ);
        peFile2.setFilePath(pwd + "tools/mass/LIB1896_R2.r95.fastq");

        Library peLib = new Library();
        peLib.setDataset(Library.Dataset.RAW);
        peLib.setName("peLib1");
        peLib.setIndex(1);
        peLib.setUsage("ASM");
        peLib.setType(Library.Type.PE);
        peLib.setFilePaired1(peFile1);
        peLib.setFilePaired2(peFile2);

        List<Library> libs = new ArrayList<Library>();
        libs.add(peLib);

        return libs;
    }

    @Test
    public void testAbyssV134() throws InterruptedException, ProcessExecutionException, IOException, CommandExecutionException {

        List<Library> libs = this.createLocalPETestLibrary();

        AbyssV134Args args = new AbyssV134Args();
        args.setLibraries(libs);
        args.setKmer(61);
        args.setName("OUTPUT_FILE");
        args.setThreads(16);

        AbyssV134Process abyss = new AbyssV134Process(args);

        String command = abyss.getCommand();

        String correct = "abyss-pe  lib='peLib1' peLib1='" + pwd + "tools/mass/LIB1896_R1.r95.fastq " + pwd + "tools/mass/LIB1896_R2.r95.fastq'  np=16  name=OUTPUT_FILE  k=61";

        //assertTrue(command.equals(correct));
    }
}
