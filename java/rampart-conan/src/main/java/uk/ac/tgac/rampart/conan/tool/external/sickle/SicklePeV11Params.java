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
package uk.ac.tgac.rampart.conan.tool.external.sickle;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.parameter.FlagParameter;
import uk.ac.tgac.rampart.conan.conanx.parameter.NumericParameter;
import uk.ac.tgac.rampart.conan.conanx.parameter.PathParameter;
import uk.ac.tgac.rampart.conan.tool.ToolParams;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SicklePeV11Params implements ToolParams {
	
	private ConanParameter peFile1;
    private ConanParameter peFile2;
    private ConanParameter outputPe1;
    private ConanParameter outputPe2;
    private ConanParameter outputSingles;
    private ConanParameter qualityThreshold;
    private ConanParameter lengthThreshold;
    private ConanParameter discardN;
    private ConanParameter qualityType;

    public SicklePeV11Params() {

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

        this.qualityThreshold = new NumericParameter(
                "qual-threshold",
                "Threshold for trimming based on average quality in a window. Default 20.",
                true);

        this.lengthThreshold = new NumericParameter(
                "length-threshold",
                "Threshold to keep a read based on length after trimming. Default 20.",
                true);

        this.discardN = new FlagParameter(
                "discard-n",
                "Discard sequences with any Ns in them.");

        this.qualityType = new SicklePeV11QualityTypeParameter();
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

    public ConanParameter getQualityThreshold() {
        return qualityThreshold;
    }

    public ConanParameter getLengthThreshold() {
        return lengthThreshold;
    }

    public ConanParameter getDiscardN() {
        return discardN;
    }

    public ConanParameter getQualityType() {
        return qualityType;
    }


    @Override
    public Set<ConanParameter> getConanParameters() {
        return new HashSet<ConanParameter>(Arrays.asList(
                new ConanParameter[] {
                        this.peFile1,
                        this.peFile2,
                        this.outputPe1,
                        this.outputPe2,
                        this.outputSingles,
                        this.qualityThreshold,
                        this.lengthThreshold,
                        this.discardN,
                        this.qualityType
                }
        ));
    }
}
