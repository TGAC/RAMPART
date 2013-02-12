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
package uk.ac.tgac.rampart.conan.tool.proc.external.asm.abyss;

import uk.ac.tgac.rampart.conan.conanx.exec.process.AbstractConanXProcess;
import uk.ac.tgac.rampart.conan.tool.proc.external.asm.Assembler;
import uk.ac.tgac.rampart.conan.tool.proc.external.asm.AssemblerArgs;

/**
 * User: maplesod
 * Date: 07/01/13
 * Time: 12:12
 */
public class AbyssV134Process extends AbstractConanXProcess implements Assembler {

    public static final String EXE = "abyss-pe";

    public AbyssV134Process() {
        this(new AbyssV134Args());
    }

    public AbyssV134Process(AbyssV134Args args) {
        super(EXE, args, new AbyssV134Params());
    }

    @Override
    public String getCommand() {
        return this.getCommand(this.getProcessArgs(), false);
    }

    @Override
    public AssemblerArgs getArgs() {
        return (AssemblerArgs) this.getProcessArgs();
    }

    @Override
    public boolean makesUnitigs() {
        return true;
    }

    @Override
    public boolean makesContigs() {
        return true;
    }

    @Override
    public boolean makesScaffolds() {
        return true;
    }

    @Override
    public String getName() {
        return "AbyssV134";
    }
}
