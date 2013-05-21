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
package uk.ac.tgac.rampart.util;

import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.Locality;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.io.File;
import java.util.List;

/**
 * User: maplesod
 * Date: 08/05/13
 * Time: 13:00
 */
public class FileHelper {

    public static File currentWorkingDir() {

        return new File(".").getAbsoluteFile().getParentFile();
    }

    public static void createSymbolicLinks(ConanProcessService conanProcessService, List<File> inputFiles, File outputDir, Locality locality)
            throws InterruptedException, ProcessExecutionException {

        for(File inputFile : inputFiles) {
            createSymbolicLink(conanProcessService, inputFile, new File(outputDir, inputFile.getName()), locality);
        }

    }

    public static void createSymbolicLink(ConanProcessService conanProcessService, File inputFile, File outputFile, Locality locality)
            throws ProcessExecutionException, InterruptedException {

        ExecutionContext linkingExecutionContext = new DefaultExecutionContext(locality, null, null, true);

        conanProcessService.execute("ln -s -f " + inputFile.getAbsolutePath() + " " + outputFile.getAbsolutePath(), linkingExecutionContext);
    }
}
