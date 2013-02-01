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
package uk.ac.tgac.rampart.conan.tool.module.mass;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.parameter.DefaultConanParameter;
import uk.ac.tgac.rampart.conan.conanx.parameter.NumericParameter;
import uk.ac.tgac.rampart.conan.conanx.parameter.PathParameter;
import uk.ac.tgac.rampart.conan.conanx.parameter.ToolParams;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * User: maplesod
 * Date: 11/01/13
 * Time: 13:23
 */
public abstract class MassParams implements ToolParams {

    private ConanParameter assembler;
    private ConanParameter kmin;
    private ConanParameter kmax;
    private ConanParameter stepSize;

    public MassParams() {

        this.assembler = new DefaultConanParameter(
                "asm",
                "De Brujin Assembler to use",
                false,
                true,
                false);

        this.kmin = new NumericParameter(
                "kmin",
                "The minimum k-mer value to assemble",
                true);

        this.kmax = new NumericParameter(
                "kmax",
                "The maximum k-mer value to assemble",
                true);

        this.stepSize = new DefaultConanParameter(
                "step",
                "The kmer step size between each assembly: FINE, MEDIUM, COARSE",
                false,
                true,
                false);

    }

    public ConanParameter getAssembler() {
        return assembler;
    }

    public ConanParameter getKmin() {
        return kmin;
    }

    public ConanParameter getKmax() {
        return kmax;
    }

    public ConanParameter getStepSize() {
        return stepSize;
    }

    @Override
    public Set<ConanParameter> getConanParameters() {

        return new HashSet<ConanParameter>(Arrays.asList(
                new ConanParameter[]{
                        this.assembler,
                        this.kmin,
                        this.kmax,
                        this.stepSize}));
    }

}
