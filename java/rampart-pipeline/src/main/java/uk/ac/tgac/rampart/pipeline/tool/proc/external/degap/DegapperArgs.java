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
package uk.ac.tgac.rampart.pipeline.tool.proc.external.degap;

import uk.ac.tgac.rampart.pipeline.conanx.param.ProcessArgs;
import uk.ac.tgac.rampart.core.data.Library;

import java.io.File;
import java.util.Set;

public interface DegapperArgs extends ProcessArgs {

    Set<Library> getLibraries();

    void setLibraries(Set<Library> libraries);

    File getInputScaffoldFile();

    void setInputScaffoldFile(File inputScaffoldFile);

    File getOutputScaffoldFile();

    void setOutputScaffoldFile(File outputScaffoldFile);

    int getThreads();

    void setThreads(int threads);
}
