/*
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2015  Daniel Mapleson - TGAC
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
 */

package uk.ac.tgac.rampart.util;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 21/01/14
 * Time: 15:15
 * To change this template use File | Settings | File Templates.
 */
public class JobOutputMap extends HashMap<String, Set<File>> {

    public void updateTracker(String group, File newFile) {

        Set<File> fileSet = null;

        if (this.containsKey(group)) {
            fileSet = this.get(group);
        }
        else {
            this.put(group, new HashSet<File>());
            fileSet = this.get(group);
        }

        fileSet.add(newFile);
    }

    public void combine(JobOutputMap other) {

        for(Map.Entry<String, Set<File>> entry : other.entrySet()) {

            if(this.containsKey(entry.getKey())) {
                Set<File> fileSet = this.get(entry.getKey());

                for(File f : entry.getValue()) {
                    if (!fileSet.contains(f)) {
                        fileSet.add(f);
                    }
                }
            }
            else {
                this.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
