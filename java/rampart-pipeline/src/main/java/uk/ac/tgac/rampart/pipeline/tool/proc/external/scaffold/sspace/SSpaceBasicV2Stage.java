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
package uk.ac.tgac.rampart.pipeline.tool.proc.external.scaffold.sspace;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.pipeline.conanx.exec.process.AbstractConanXProcess;
import uk.ac.tgac.rampart.pipeline.tool.proc.external.scaffold.Scaffolder;
import uk.ac.tgac.rampart.pipeline.tool.proc.external.scaffold.ScaffolderArgs;

import java.io.File;
import java.util.Collection;

/**
 * User: maplesod
 * Date: 23/01/13
 * Time: 16:00
 */
public class SSpaceBasicV2Stage extends AbstractConanXProcess implements Scaffolder {

    public static final String EXE = "SSPACE_Basic_v2.0.pl";

    public SSpaceBasicV2Stage() {
        this(new SSpaceBasicV2Args());
    }

    public SSpaceBasicV2Stage(SSpaceBasicV2Args args) {
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
    public void setInputAssembly(File inputFile) {
        ((ScaffolderArgs) this.getProcessArgs()).setInputContigFile(inputFile);
    }

    @Override
    public File getOutputAssembly() {
        return ((ScaffolderArgs) this.getProcessArgs()).getOutputScaffoldFile();
    }
}
