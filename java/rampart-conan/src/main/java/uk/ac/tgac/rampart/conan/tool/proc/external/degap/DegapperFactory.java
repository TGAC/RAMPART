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
package uk.ac.tgac.rampart.conan.tool.proc.external.degap;

import uk.ac.tgac.rampart.conan.tool.proc.external.degap.gapcloser.GapCloserV112Stage;

/**
 * User: maplesod
 * Date: 31/01/13
 * Time: 14:03
 */
public enum DegapperFactory {

    SOAP_GAPCLOSER_V1_12 {
        @Override
        public Degapper create() {
            return new GapCloserV112Stage();
        }
    };


    public abstract Degapper create();

    public static Degapper createDegapper() {
        return SOAP_GAPCLOSER_V1_12.create();
    }

    public static Degapper createDegapper(String name) {
        return DegapperFactory.valueOf(name).create();
    }
}
