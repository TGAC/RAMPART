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

import uk.ac.tgac.rampart.conan.process.AbstractAmpArgs;

import java.io.File;

/**
 * User: maplesod
 * Date: 01/02/13
 * Time: 09:56
 */
public abstract class AbstractClipperArgs extends AbstractAmpArgs {

    private File outputFile;
    private int minLen;

    protected AbstractClipperArgs() {
        super();
        this.outputFile = null;
        this.minLen = 1000;
    }

    public int getMinLen() {
        return minLen;
    }

    public void setMinLen(int minLen) {
        this.minLen = minLen;
    }

    @Override
    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }
}
