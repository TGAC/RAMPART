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
package uk.ac.tgac.rampart.conan.process.scaffold;

import uk.ac.tgac.rampart.conan.process.scaffold.sspace.SSpaceBasicV2Process;

/**
 * User: maplesod
 * Date: 31/01/13
 * Time: 14:03
 */
public enum ScaffolderFactory {

    SSPACE_BASIC_V2 {
        @Override
        public Scaffolder create() {
            return new SSpaceBasicV2Process();
        }
    };


    public abstract Scaffolder create();

    public static Scaffolder createScaffolder() {
        return SSPACE_BASIC_V2.create();
    }

    public static Scaffolder createScaffolder(String name) {
        return ScaffolderFactory.valueOf(name.toUpperCase()).create();
    }
}
