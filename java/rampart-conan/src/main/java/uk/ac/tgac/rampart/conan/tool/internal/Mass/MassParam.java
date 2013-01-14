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
package uk.ac.tgac.rampart.conan.tool.internal.Mass;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.parameter.DefaultConanParameter;
import uk.ac.tgac.rampart.conan.conanx.parameter.NumericParameter;
import uk.ac.tgac.rampart.conan.conanx.parameter.PathParameter;
import uk.ac.tgac.rampart.conan.tool.ToolParameter;

/**
 * User: maplesod
 * Date: 11/01/13
 * Time: 13:23
 */
public enum MassParam implements ToolParameter {

     ASSEMBLER {
         @Override
         public ConanParameter getConanParameter() {
             return new DefaultConanParameter(
                     "assembler",
                     "De Brujin Assembler to use",
                     false,
                     true,
                     false);
         }
     },
    ASSEMBLER_ARGS {
        @Override
        public ConanParameter getConanParameter() {
            return new DefaultConanParameter(
                    "asm_args",
                    "Any special arguments to pass to the De Brujin Assembler",
                    false,
                    true,
                    false);
        }
    },
    K_MIN {
        @Override
        public ConanParameter getConanParameter() {
            return new NumericParameter(
                    "kmin",
                    "The minimum k-mer value to assemble",
                    true
            );
        }
    },
    K_MAX {
        @Override
        public ConanParameter getConanParameter() {
            return new NumericParameter(
                    "kmax",
                    "The maximum k-mer value to assemble",
                    true
            );
        }
    },
    CONFIG {
        @Override
        public ConanParameter getConanParameter() {
            return new PathParameter(
                    "config",
                    "A rampart config file describing the libraries to assemble",
                    true
            );
        }
    },
    OUTPUT_DIR {
        @Override
        public ConanParameter getConanParameter() {
            return new PathParameter(
                    "output",
                    "The output directory",
                    true
            );
        }
    },
    JOB_PREFIX {
        @Override
        public ConanParameter getConanParameter() {
            return new DefaultConanParameter(
                    "job_prefix",
                    "The job_prefix to be assigned to all sub processes in MASS.  Useful if executing with a scheduler.",
                    false,
                    true,
                    false);
        }
    };

}
