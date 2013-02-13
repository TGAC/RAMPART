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
package uk.ac.tgac.rampart.pipeline.tool.proc.internal.clip.internal;

import uk.ac.ebi.fgpt.conan.core.param.NumericParameter;
import uk.ac.ebi.fgpt.conan.core.param.PathParameter;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: maplesod
 * Date: 01/02/13
 * Time: 10:16
 */
public class RampartClipperParams implements ProcessParams {

    private ConanParameter inputFile;
    private ConanParameter outputFile;
    private ConanParameter minLen;

    public RampartClipperParams() {
        this.inputFile = new PathParameter(
                "in",
                "input scaffold file name",
                false);

        this.outputFile = new PathParameter(
                "o",
                "output scaffold file name",
                false);

        this.minLen = new NumericParameter(
                "minlen",
                "minimum scaffold length to leave in the output file, default=1000",
                true);
    }

    public ConanParameter getInputFile() {
        return inputFile;
    }

    public ConanParameter getOutputFile() {
        return outputFile;
    }

    public ConanParameter getMinLen() {
        return minLen;
    }

    @Override
    public List<ConanParameter> getConanParameters() {
        return new ArrayList<ConanParameter>(Arrays.asList(
                new ConanParameter[]{
                        this.inputFile,
                        this.outputFile,
                        this.minLen
                }
        ));
    }
}
