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

import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.ebi.fgpt.conan.model.param.ProcessParams;
import uk.ac.tgac.rampart.conan.process.AbstractIOProcess;
import uk.ac.tgac.rampart.conan.process.scaffold.ScaffolderArgs;

import java.io.File;

/**
 * User: maplesod
 * Date: 23/01/13
 * Time: 13:47
 */
public abstract class Degapper extends AbstractIOProcess {

    public Degapper(String exe, ProcessArgs processArgs, ProcessParams processParams) {
        super(exe, processArgs, processParams);
    }

    public DegapperArgs getDegapperArgs() {
        return (DegapperArgs)this.getProcessArgs();
    }

    public void setDegapperArgs(ScaffolderArgs scaffolderArgs) {
        this.setProcessArgs(scaffolderArgs);
    }

    @Override
    public void setInputFile(File inputFile) {
        this.getDegapperArgs().setInput(inputFile);
        super.setInputFile(inputFile);
    }

    @Override
    public File getOutputFile() {
        return this.getDegapperArgs().getOutput();
    }
}
