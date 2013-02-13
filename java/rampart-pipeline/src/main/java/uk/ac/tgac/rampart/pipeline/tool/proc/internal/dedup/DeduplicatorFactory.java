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
package uk.ac.tgac.rampart.pipeline.tool.proc.internal.dedup;

import uk.ac.tgac.rampart.pipeline.tool.proc.internal.clip.Clipper;
import uk.ac.tgac.rampart.pipeline.tool.proc.internal.clip.internal.RampartClipperProcess;
import uk.ac.tgac.rampart.pipeline.tool.proc.internal.dedup.nizar.NizarDedupProcess;

/**
 * User: maplesod
 * Date: 12/02/13
 * Time: 17:34
 */
public enum DeduplicatorFactory {

    NIZAR {
        @Override
        public Deduplicator create() {
            return new NizarDedupProcess();
        }

    };


    public abstract Deduplicator create();

    public static Deduplicator createDeduplicator() {
        return NIZAR.create();
    }

    public static Deduplicator createDeduplicator(String name) {
        return DeduplicatorFactory.valueOf(name).create();
    }
}
