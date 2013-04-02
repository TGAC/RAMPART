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
package uk.ac.tgac.rampart.conan.process.degap;

import uk.ac.tgac.rampart.conan.process.degap.gapcloser.GapCloserV112Args;
import uk.ac.tgac.rampart.conan.process.degap.gapcloser.GapCloserV112Process;

/**
 * User: maplesod
 * Date: 31/01/13
 * Time: 14:03
 */
public enum DegapperFactory {

    SOAP_GAPCLOSER_V1_12 {

        @Override
        public AbstractDegapperProcess create() {
            return new GapCloserV112Process();
        }

        @Override
        public AbstractDegapperProcess create(AbstractDegapperArgs abstractDegapperArgs) {
            return new GapCloserV112Process((GapCloserV112Args)abstractDegapperArgs);
        }

        @Override
        public AbstractDegapperProcess create(String degapperArgs) {
            GapCloserV112Args gcArgs = new GapCloserV112Args();
            gcArgs.parse(degapperArgs);
            return new GapCloserV112Process(gcArgs);
        }
    };


    public abstract AbstractDegapperProcess create();
    public abstract AbstractDegapperProcess create(AbstractDegapperArgs abstractDegapperArgs);
    public abstract AbstractDegapperProcess create(String degapperArgs);

    public static DegapperFactory getDefaultDegapper() {
        return SOAP_GAPCLOSER_V1_12;
    }

}
