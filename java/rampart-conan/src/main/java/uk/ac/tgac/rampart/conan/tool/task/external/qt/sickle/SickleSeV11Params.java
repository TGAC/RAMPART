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
package uk.ac.tgac.rampart.conan.tool.task.external.qt.sickle;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.param.PathParameter;

import java.util.Arrays;
import java.util.Set;

/**
 * User: maplesod
 * Date: 23/01/13
 * Time: 15:04
 */
public class SickleSeV11Params extends SickleV11Params {

    private ConanParameter inputFile;
    private ConanParameter outputFile;

    public SickleSeV11Params() {

        super();

        this.inputFile = new PathParameter(
                "fastq-file",
                "Input fastq file (required)",
                false);

        this.outputFile = new PathParameter(
                "output-file",
                "Output trimmed fastq file (required)",
                false);
    }


    public ConanParameter getSeFile() {
        return inputFile;
    }

    public ConanParameter getOutputFile() {
        return outputFile;
    }

    @Override
    public Set<ConanParameter> getConanParameters() {

        Set<ConanParameter> params = super.getConanParameters();

        params.addAll(Arrays.asList(this.inputFile,
                this.outputFile));

        return params;
    }
}
