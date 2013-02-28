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
package uk.ac.tgac.rampart.conan.process.scaffold.sspace;

import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.tgac.rampart.conan.process.scaffold.Scaffolder;
import uk.ac.tgac.rampart.conan.process.scaffold.ScaffolderArgs;

import java.io.File;
import java.util.Collection;

/**
 * User: maplesod
 * Date: 23/01/13
 * Time: 16:00
 */
public class SSpaceBasicV2Process extends AbstractConanProcess implements Scaffolder {

    public static final String EXE = "SSPACE_Basic_v2.0.pl";

    public SSpaceBasicV2Process() {
        this(new SSpaceBasicV2Args());
    }

    public SSpaceBasicV2Process(SSpaceBasicV2Args args) {
        super(EXE, args, new SSpaceBasicV2Params());
    }

    @Override
    public String getCommand() {
        return this.getCommand(this.getProcessArgs(), true, "-", " ");
    }

    @Override
    public String getName() {
        return "SSPACE_Basic_v2.0";
    }

    @Override
    public Collection<ConanParameter> getParameters() {
        return new SSpaceBasicV2Params().getConanParameters();
    }

    @Override
    public ScaffolderArgs getArgs() {
        return (ScaffolderArgs)this.getProcessArgs();
    }

    @Override
    public void setArgs(ScaffolderArgs scaffolderArgs) {
        this.setProcessArgs(scaffolderArgs);
    }

    @Override
    public void setInputFile(File inputFile) {
        this.getArgs().setInput(inputFile);
    }

    @Override
    public File getOutputFile() {
        return this.getArgs().getOutput();
    }
}
