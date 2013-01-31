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
package uk.ac.tgac.rampart.conan.util;

import uk.ac.tgac.rampart.conan.conanx.parameter.FilePair;
import uk.ac.tgac.rampart.core.data.Library;
import uk.ac.tgac.rampart.core.data.SeqFile;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * User: maplesod
 * Date: 15/01/13
 * Time: 16:00
 */
public class PETestLibrary {


    public Set<Library> createLocalPETestLibrary() {
        SeqFile peFile1 = new SeqFile();
        peFile1.setFileType(SeqFile.FileType.FASTQ);
        peFile1.setFilePath("tools/mass/LIB1896_R1.r95.fastq");

        SeqFile peFile2 = new SeqFile();
        peFile2.setFileType(SeqFile.FileType.FASTQ);
        peFile2.setFilePath("tools/mass/LIB1896_R2.r95.fastq");

        Library peLib = new Library();
        peLib.setDataset(Library.Dataset.RAW);
        peLib.setName("peLib1");
        peLib.setIndex(1);
        peLib.setUsage(Library.Usage.ASSEMBLY_ONLY);
        peLib.setType(Library.Type.PE);
        peLib.setFilePaired1(peFile1);
        peLib.setFilePaired2(peFile2);

        Set<Library> libs = new HashSet<Library>();
        libs.add(peLib);

        return libs;
    }

    public Set<Library> createNorwichLSFPETestLibrary() {
        SeqFile peFile1 = new SeqFile();
        peFile1.setFileType(SeqFile.FileType.FASTQ);
        peFile1.setFilePath("~maplesod/dev/rampart/test/tools/mass/LIB1896_R1.r95.fastq");

        SeqFile peFile2 = new SeqFile();
        peFile2.setFileType(SeqFile.FileType.FASTQ);
        peFile2.setFilePath("~maplesod/dev/rampart/test/tools/mass/LIB1896_R2.r95.fastq");

        Library peLib = new Library();
        peLib.setDataset(Library.Dataset.RAW);
        peLib.setName("peLib1");
        peLib.setIndex(1);
        peLib.setUsage(Library.Usage.ASSEMBLY_ONLY);
        peLib.setType(Library.Type.PE);
        peLib.setFilePaired1(peFile1);
        peLib.setFilePaired2(peFile2);

        Set<Library> libs = new HashSet<Library>();
        libs.add(peLib);

        return libs;
    }


    public FilePair getLocalPairedEndTestLib() {

        File peFile1 = new File("tools/mass/LIB1896_R1.r95.fastq");
        File peFile2 = new File("tools/mass/LIB1896_R2.r95.fastq");

        return new FilePair(peFile1, peFile2);
    }

    public FilePair getNorwichLSFPairedEndTestLib() {

        File peFile1 = new File("~maplesod/dev/rampart/test/tools/mass/LIB1896_R1.r95.fastq");
        File peFile2 = new File("~maplesod/dev/rampart/test/tools/mass/LIB1896_R2.r95.fastq");

        return new FilePair(peFile1, peFile2);
    }
}
