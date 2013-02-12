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

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.param.DefaultConanParameter;
import uk.ac.tgac.rampart.conan.conanx.param.NumericParameter;
import uk.ac.tgac.rampart.conan.conanx.param.ProcessParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbyssV134Params implements ProcessParams {

    private ConanParameter libs;
    private ConanParameter nbContigPairs;
    private ConanParameter kmer;
    private ConanParameter threads;
    private ConanParameter name;

    public AbyssV134Params() {

        this.libs = new AbyssV134InputLibsParameter();

        this.nbContigPairs = new NumericParameter(
                "n",
                "minimum  number  of  pairs  (default: 10). The optimal value for this param depends on  coverage, but 10 is a reasonable default.",
                true);

        this.kmer = new NumericParameter(
                "k",
                "k-mer size",
                false);

        this.threads = new NumericParameter(
                "np",
                "the number of processes of an MPI assembly",
                false);

        this.name = new DefaultConanParameter(
                "name",
                "The name of this assembly. The resulting contigs will be stored in ${name}-contigs.fa",
                false, false, false);
    }

    public ConanParameter getLibs() {
        return libs;
    }

    public ConanParameter getNbContigPairs() {
        return nbContigPairs;
    }

    public ConanParameter getKmer() {
        return kmer;
    }

    public ConanParameter getThreads() {
        return threads;
    }

    public ConanParameter getName() {
        return name;
    }


    @Override
    public List<ConanParameter> getConanParameters() {
        return new ArrayList<ConanParameter>(Arrays.asList(
                new ConanParameter[]{
                        this.libs,
                        this.nbContigPairs,
                        this.kmer,
                        this.threads,
                        this.name
                }
        ));
    }
}
