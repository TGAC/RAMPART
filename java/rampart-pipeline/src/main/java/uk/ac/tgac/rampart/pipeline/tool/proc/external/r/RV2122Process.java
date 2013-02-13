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
package uk.ac.tgac.rampart.pipeline.tool.proc.external.r;

import uk.ac.tgac.rampart.pipeline.conanx.exec.process.AbstractConanXProcess;

/**
 * User: maplesod
 * Date: 16/01/13
 * Time: 16:13
 */
public class RV2122Process extends AbstractConanXProcess {

    public static final String EXE = "Rscript";

    public RV2122Process() {
        this(new RV2122Args());
    }

    public RV2122Process(RV2122Args args) {
        super(EXE, args, new RV2122Params());
    }


    @Override
    public String getCommand() {
        return this.getCommand(this.getProcessArgs(), false);
    }

    @Override
    public String getName() {
        return "R V2.12.2";
    }
}
