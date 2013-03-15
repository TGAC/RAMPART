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

import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.ebi.fgpt.conan.model.param.ProcessParams;
import uk.ac.tgac.rampart.conan.process.AbstractIOProcess;

import java.io.File;

/**
 * User: maplesod
 * Date: 01/02/13
 * Time: 09:53
 */
public abstract class Clipper extends AbstractIOProcess {

    public Clipper(String exe, ProcessArgs processArgs, ProcessParams processParams) {
        super(exe, processArgs, processParams);
    }

    public ClipperArgs getClipperArgs() {
        return (ClipperArgs)this.getProcessArgs();
    }

    public void setClipperArgs(ClipperArgs clipperArgs) {
        this.setProcessArgs(clipperArgs);
    }

    @Override
    public void setInputFile(File inputFile) {
        this.getClipperArgs().setInput(inputFile);
        super.setInputFile(inputFile);
    }

    @Override
    public File getOutputFile() {
        return this.getClipperArgs().getOutput();
    }
}
