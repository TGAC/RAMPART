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
package uk.ac.tgac.rampart.conan.tool.proc.external.degap.gapcloser;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.param.NumericParameter;
import uk.ac.tgac.rampart.conan.conanx.param.PathParameter;
import uk.ac.tgac.rampart.conan.conanx.param.ProcessParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GapCloserV112Params implements ProcessParams {

    private ConanParameter inputScaffoldFile;
    private ConanParameter libraryFile;
    private ConanParameter outputFile;
    private ConanParameter maxReadLength;
    private ConanParameter overlap;
    private ConanParameter threads;

    public GapCloserV112Params() {

        this.inputScaffoldFile = new PathParameter(
                "a",
                "input scaffold file name",
                false);

        this.libraryFile = new PathParameter(
                "b",
                "input library info file name",
                false);

        this.outputFile = new PathParameter(
                "o",
                "output file name",
                false);

        this.maxReadLength = new NumericParameter(
                "l",
                "maximal read length (<=155), default=100",
                true);

        this.overlap = new NumericParameter(
                "p",
                "overlap param - kmer (<=31), default=25",
                true);

        this.threads = new NumericParameter(
                "t",
                "thread number, default=1",
                true);
    }

    public ConanParameter getInputScaffoldFile() {
        return inputScaffoldFile;
    }

    public ConanParameter getLibraryFile() {
        return libraryFile;
    }

    public ConanParameter getOutputFile() {
        return outputFile;
    }

    public ConanParameter getMaxReadLength() {
        return maxReadLength;
    }

    public ConanParameter getOverlap() {
        return overlap;
    }

    public ConanParameter getThreads() {
        return threads;
    }


    @Override
    public List<ConanParameter> getConanParameters() {
        return new ArrayList<ConanParameter>(Arrays.asList(
                new ConanParameter[]{
                        this.inputScaffoldFile,
                        this.libraryFile,
                        this.outputFile,
                        this.maxReadLength,
                        this.overlap,
                        this.threads
                }
        ));
    }

}
