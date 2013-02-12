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
package uk.ac.tgac.rampart.conan.tool.proc.external.asm.abyss;

import org.junit.Test;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.rampart.conan.util.PETestLibrary;
import uk.ac.tgac.rampart.core.data.Library;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 05/02/13
 * Time: 17:32
 */
public class AbyssV134ProcessTest {

    @Test
    public void testAbyssV134() throws InterruptedException, ProcessExecutionException, IOException, CommandExecutionException {

        List<Library> libs = new PETestLibrary().createLocalPETestLibrary();

        AbyssV134Args args = new AbyssV134Args();
        args.setLibraries(libs);
        args.setKmer(61);
        args.setName("OUTPUT_FILE");
        args.setThreads(16);

        AbyssV134Process abyss = new AbyssV134Process(args);
        abyss.addPreCommand("source abyss_cb-1.3.4");

        String command = abyss.getCommand();
        String fullCommand = abyss.getFullCommand();

        assertTrue(command.equals("abyss-pe  lib='peLib1' peLib1='tools/mass/LIB1896_R1.r95.fastq tools/mass/LIB1896_R2.r95.fastq'  np=16  name=OUTPUT_FILE  n=10  k=61"));
        assertTrue(fullCommand.equals("source abyss_cb-1.3.4; abyss-pe  lib='peLib1' peLib1='tools/mass/LIB1896_R1.r95.fastq tools/mass/LIB1896_R2.r95.fastq'  np=16  name=OUTPUT_FILE  n=10  k=61"));
    }
}
