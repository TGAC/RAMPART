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

package uk.ac.tgac.jellyswarm;

import org.apache.commons.lang3.tuple.MutablePair;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 11/11/13
 * Time: 14:10
 * To change this template use File | Settings | File Templates.
 */
public class FilePair extends MutablePair<File, File> {

    public FilePair(File left, File right) throws IOException {
        super(left, right);

        if (right != null) {

            if (!left.getParentFile().getAbsolutePath().equals(right.getParentFile().getAbsolutePath()))
                throw new IOException("Paired files are not found in the same directory");

            int fn1m = left.getName().toUpperCase().indexOf("R1");
            int fn2m = right.getName().toUpperCase().indexOf("R2");

            String fn1s = left.getName().substring(0,fn1m);
            String fn2s = right.getName().substring(0,fn2m);

            if (!fn1s.equals(fn2s))
                throw new IOException("Paired files do not share the same prefix");
        }
    }

    public String getNamePrefix() {
        return left.getName().substring(0, left.getName().indexOf("R1"));
    }

    public String getNameSuffix() {
        return left.getName().substring(left.getName().indexOf("R1")+2);
    }

    public String getGlobedName() {
        return this.getNamePrefix() + "R?" + this.getNameSuffix();
    }

    public File getParentFile() {
        return left.getParentFile();
    }

    public File getGlobedFile() {
        return new File(this.getParentFile(), this.getGlobedName());
    }
}
