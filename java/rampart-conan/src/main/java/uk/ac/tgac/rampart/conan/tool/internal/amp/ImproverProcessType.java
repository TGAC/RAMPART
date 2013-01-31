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
package uk.ac.tgac.rampart.conan.tool.internal.amp;

import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.tgac.rampart.conan.tool.external.degap.Degapper;

/**
 * User: maplesod
 * Date: 31/01/13
 * Time: 13:48
 */
public enum ImproverProcessType {

    CLIP {
        @Override
        public ConanProcess create() {
            return new Clipper();
        }
    },
    DEDUPLICATE {
        @Override
        public ConanProcess create() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    },
    DEGAP {
        @Override
        public ConanProcess create() {
            return null;
        }
    },
    SCAFFOLD {
        @Override
        public ConanProcess create() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    };

    public abstract ConanProcess create();
}
