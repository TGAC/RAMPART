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
package uk.ac.tgac.rampart.conan.tool.process.scaffold.sspace;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.parameter.DefaultConanParameter;
import uk.ac.tgac.rampart.conan.conanx.parameter.NumericParameter;
import uk.ac.tgac.rampart.conan.conanx.parameter.PathParameter;
import uk.ac.tgac.rampart.conan.conanx.parameter.ToolParams;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SSpaceBasicV2Params implements ToolParams {

    private ConanParameter libraryFile;
    private ConanParameter contigFile;
    private ConanParameter extend;
    private ConanParameter bowtieThreads;
    private ConanParameter baseName;

    public SSpaceBasicV2Params() {

        this.libraryFile = new PathParameter(
                "l",
                "Library file containing two mate pair files with insert size, error and either mate pair or paired end indication.",
                false);

        this.contigFile = new PathParameter(
                "s",
                "Fasta file containing contig sequences used for extension. Inserted pairs are mapped to extended and non-extended contigs (REQUIRED)",
                false);

        this.extend = new NumericParameter(
                "x",
                "Indicate whether to extend the contigs of -s using paired reads in -l. (-x 1=extension, -x 0=no extension, default -x 0)",
                true);

        this.bowtieThreads = new NumericParameter(
                "T",
                "Specify the number of threads in Bowtie. Corresponds to the -p/--threads option in Bowtie (default -T 1, optional)",
                true);

        this.baseName = new DefaultConanParameter(
                "b",
                "Base name for your output files (optional)",
                false,
                true,
                false);
    }

    public ConanParameter getLibraryFile() {
        return libraryFile;
    }

    public ConanParameter getContigFile() {
        return contigFile;
    }

    public ConanParameter getExtend() {
        return extend;
    }

    public ConanParameter getBowtieThreads() {
        return bowtieThreads;
    }

    public ConanParameter getBaseName() {
        return baseName;
    }

    @Override
    public Set<ConanParameter> getConanParameters() {
        return new HashSet<ConanParameter>(Arrays.asList(
                new ConanParameter[]{
                        this.libraryFile,
                        this.contigFile,
                        this.extend,
                        this.bowtieThreads,
                        this.baseName
                }
        ));
    }
}
