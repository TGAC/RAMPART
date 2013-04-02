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
package uk.ac.tgac.rampart.conan.process.scaffold.sspace;

import uk.ac.ebi.fgpt.conan.core.param.DefaultConanParameter;
import uk.ac.ebi.fgpt.conan.core.param.FlagParameter;
import uk.ac.ebi.fgpt.conan.core.param.NumericParameter;
import uk.ac.ebi.fgpt.conan.core.param.PathParameter;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SSpaceBasicV2Params implements ProcessParams {

    // Main parameters
    private ConanParameter contigsFile;
    private ConanParameter libraryFile;
    private ConanParameter extend;

    // Extension parameters
    private ConanParameter minOverlap;
    private ConanParameter nbReads;
    private ConanParameter trim;
    private ConanParameter minBaseRatio;

    // Scaffolding parameters
    private ConanParameter minLinks;
    private ConanParameter maxLinks;
    private ConanParameter minContigOverlap;
    private ConanParameter minContigLength;


    // Bowtie parameters
    private ConanParameter bowtieGaps;
    private ConanParameter bowtieThreads;

    // Additional parameters
    private ConanParameter plot;
    private ConanParameter baseName;
    private ConanParameter verbose;


    public SSpaceBasicV2Params() {

        // **** Main parameters ****

        this.contigsFile = new PathParameter(
                "s",
                "The ‘–s’ contigs file should be in a .fasta format. The headers are used " +
                        "to trace back the original contigs on the final scaffold fasta file. " +
                        "Therefore, names of the headers should not be too complex. A naming " +
                        "of “>contig11” or “>11”, should be fine. Otherwise, headers of the final " +
                        "scaffold fasta file will be too large and hard to read. " +
                        "Contigs having a non-ACGT character like “.” or “N” are not discarded. " +
                        "They are used for extension, mapping and building scaffolds. However, " +
                        "contigs having such character at either end of the sequence, could fail " +
                        "for proper contig extension and read mapping.",
                false);

        this.libraryFile = new PathParameter(
                "l",
                "Library file containing two mate pair files with insert size, error and either mate pair or paired end indication.",
                false);

        this.extend = new NumericParameter(
                "x",
                "Indicate whether to extend the contigs of -s using paired reads in -l. (-x 1=extension, -x 0=no extension, default -x 0)",
                true);



        // **** Extension parameters ****

        this.minOverlap = new NumericParameter(
                "m",
                "Minimum number of overlapping bases of the reads with the contig " +
                        "during overhang consensus build up. Higher ‘-m’ values lead to more " +
                        "accurate contigs at the cost of decreased contiguity. We suggest to take" +
                        "a value close to the largest read length.",
                true);

        this.nbReads = new NumericParameter(
                "o",
                "Minimum number of reads needed to call a base during an extension, " +
                        "also known as base coverage. The higher the ‘-o’, the more reads are " +
                        "considered for an extension, increasing the reliability of the extension.",
                true);

        this.trim = new NumericParameter(
                "t",
                "Trims up to ‘-t’ base(s) on the contig end when all possibilities have been " +
                        "exhausted for an extension.",
                true
        );

        this.minBaseRatio = new NumericParameter(
                "r",
                "Minimum base ratio used to accept a overhang consensus base. Higher " +
                        "'-r' value lead to more accurate contig extension.",
                true
        );


        // **** Scaffolding parameters ****

        this.minLinks = new NumericParameter(
                "k",
                "The minimum number of links (read pairs) a valid contig pair must have to be considered.",
                true
        );

        this.maxLinks = new NumericParameter(
                "a",
                "The maximum ratio between the best two contig pairs for a given contig being extended.",
                true
        );

        this.minContigOverlap = new NumericParameter(
                "n",
                "Minimum overlap required between contigs to merge adjacent contigs in " +
                        "a scaffold. Overlaps in the final output are shown in lower-case " +
                        "characters.",
                true
        );

        this.minContigLength = new NumericParameter(
                "z",
                        "Minimal contig size to use for scaffolding. Contigs below this value are " +
                        "not used for scaffolding and are filtered out. Larger contigs produce " +
                        "more reliable scaffolds and also the amount of scaffolds is vastly " +
                        "reduced. Smaller contigs (< 100bp) are likely to be repeated elements " +
                        "and can stop the extension of the scaffold due to exceeding the -a " +
                        "parameter.",
                true
        );


        // **** Bowtie parameters ****

        this.bowtieGaps = new NumericParameter(
                "g",
                "Maximum allowed gaps for Bowtie, this parameter is used both at " +
                        "mapping during extension and mapping during scaffolding. This option " +
                        "corresponds to the -v option in Bowtie. We strongly recommend using no " +
                        "gaps, since this will slow down the process and can decrease the " +
                        "reliability of the scaffolds. We only suggest to increase this parameter " +
                        "when large reads are used, e.g. Roche 454 data or Illumina 100bp.",
                true
        );

        this.bowtieThreads = new NumericParameter(
                "T",
                "Number of search threads for mapping reads to the contigs with Bowtie.",
                true);


        // **** Additional parameters ****

        this.plot = new FlagParameter(
                "p",
                "Indicate whether to generate a .dot file for visualisation of the produced scaffolds."
        );

        this.baseName = new DefaultConanParameter(
                "b",
                "Base name for your output files (optional)",
                false,
                true,
                false);

        this.verbose = new FlagParameter(
                "v",
                "Indicate whether to run in verbose mode or not. If set, detailed " +
                        "information about the contig extension and contig pairing process is " +
                        "printed on the screen."
        );
    }

    public ConanParameter getContigsFile() {
        return contigsFile;
    }

    public ConanParameter getLibraryFile() {
        return libraryFile;
    }

    public ConanParameter getExtend() {
        return extend;
    }

    public ConanParameter getMinOverlap() {
        return minOverlap;
    }

    public ConanParameter getNbReads() {
        return nbReads;
    }

    public ConanParameter getTrim() {
        return trim;
    }

    public ConanParameter getMinBaseRatio() {
        return minBaseRatio;
    }

    public ConanParameter getMinLinks() {
        return minLinks;
    }

    public ConanParameter getMaxLinks() {
        return maxLinks;
    }

    public ConanParameter getMinContigOverlap() {
        return minContigOverlap;
    }

    public ConanParameter getMinContigLength() {
        return minContigLength;
    }

    public ConanParameter getBowtieGaps() {
        return bowtieGaps;
    }

    public ConanParameter getBowtieThreads() {
        return bowtieThreads;
    }

    public ConanParameter getPlot() {
        return plot;
    }

    public ConanParameter getBaseName() {
        return baseName;
    }

    public ConanParameter getVerbose() {
        return verbose;
    }



    @Override
    public List<ConanParameter> getConanParameters() {
        return new ArrayList<ConanParameter>(Arrays.asList(
                new ConanParameter[]{
                        this.contigsFile,
                        this.libraryFile,
                        this.extend,

                        this.minOverlap,
                        this.nbReads,
                        this.trim,
                        this.minBaseRatio,

                        this.minLinks,
                        this.maxLinks,
                        this.minContigOverlap,
                        this.minContigLength,

                        this.bowtieGaps,
                        this.bowtieThreads,

                        this.plot,
                        this.baseName,
                        this.verbose
                }
        ));
    }
}
