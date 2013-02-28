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
package uk.ac.tgac.rampart.conan.process.clip;

import uk.ac.tgac.rampart.conan.process.clip.simple.SimpleClipperProcess;

/**
 * User: maplesod
 * Date: 01/02/13
 * Time: 09:54
 */
public enum ClipperFactory {

    RAMPART_CLIPPER {
        @Override
        public Clipper create() {
            return new SimpleClipperProcess();
        }

        /*@Override
        public Clipper create(ClipperArgs args) {
            return new SimpleClipperProcess(args);
        }   */
    };


    public abstract Clipper create();
    //public abstract Clipper create(ClipperArgs args);

    public static Clipper createClipper() {
        return RAMPART_CLIPPER.create();
    }

    public static Clipper createClipper(String name) {
        return ClipperFactory.valueOf(name).create();
    }
}
