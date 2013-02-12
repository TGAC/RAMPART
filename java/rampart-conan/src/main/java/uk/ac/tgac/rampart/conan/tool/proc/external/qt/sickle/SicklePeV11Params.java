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
package uk.ac.tgac.rampart.conan.tool.proc.external.qt.sickle;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.param.PathParameter;

import java.util.Arrays;
import java.util.List;

public class SicklePeV11Params extends SickleV11Params {

    private ConanParameter peFile1;
    private ConanParameter peFile2;
    private ConanParameter outputPe1;
    private ConanParameter outputPe2;
    private ConanParameter outputSingles;


    public SicklePeV11Params() {

        super();

        this.peFile1 = new PathParameter(
                "pe-file1",
                "Input paired-end fastq file 1 (required, must have same number of records as pe2)",
                false);

        this.peFile2 = new PathParameter(
                "pe-file2",
                "Input paired-end fastq file 2 (required, must have same number of records as pe1)",
                false);

        this.outputPe1 = new PathParameter(
                "output-pe1",
                "Output trimmed fastq file 1 (required)",
                false);

        this.outputPe2 = new PathParameter(
                "output-pe2",
                "Output trimmed fastq file 2 (required)",
                false);

        this.outputSingles = new PathParameter(
                "output-single",
                "Output trimmed singles fastq file (required)",
                false);
    }

    public ConanParameter getPeFile1() {
        return peFile1;
    }

    public ConanParameter getPeFile2() {
        return peFile2;
    }

    public ConanParameter getOutputPe1() {
        return outputPe1;
    }

    public ConanParameter getOutputPe2() {
        return outputPe2;
    }

    public ConanParameter getOutputSingles() {
        return outputSingles;
    }


    @Override
    public List<ConanParameter> getConanParameters() {

        List<ConanParameter> params = super.getConanParameters();

        params.addAll(Arrays.asList(this.peFile1,
                this.peFile2,
                this.outputPe1,
                this.outputPe2,
                this.outputSingles));

        return params;
    }
}
