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
package uk.ac.tgac.rampart.pipeline.tool.proc.external.degap.gapcloser;

import uk.ac.tgac.rampart.pipeline.conanx.exec.process.AbstractConanXProcess;
import uk.ac.tgac.rampart.pipeline.tool.proc.external.degap.Degapper;

import java.io.File;

/**
 * User: maplesod
 * Date: 23/01/13
 * Time: 13:44
 */
public class GapCloserV112Stage extends AbstractConanXProcess implements Degapper {

    public static final String EXE = "GapCloser";

    public GapCloserV112Stage() {
        this(new GapCloserV112Args());
    }

    public GapCloserV112Stage(GapCloserV112Args args) {
        super(EXE, args, new GapCloserV112Params());
    }

    @Override
    public String getCommand() {
        return this.getCommand(this.getProcessArgs(), true, "-", " ");
    }

    @Override
    public String getName() {
        return "SOAP_GapCloser_v1.12";
    }

    @Override
    public void setInputAssembly(File inputFile) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public File getOutputAssembly() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
