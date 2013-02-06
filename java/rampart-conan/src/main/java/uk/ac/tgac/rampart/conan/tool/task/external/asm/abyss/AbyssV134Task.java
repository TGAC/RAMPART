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
package uk.ac.tgac.rampart.conan.tool.task.external.asm.abyss;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.exec.task.AbstractConanExternalTask;
import uk.ac.tgac.rampart.conan.tool.task.external.asm.Assembler;
import uk.ac.tgac.rampart.conan.tool.task.external.asm.AssemblerArgs;

import java.util.Collection;
import java.util.Map;

/**
 * User: maplesod
 * Date: 07/01/13
 * Time: 12:12
 */
public class AbyssV134Task extends AbstractConanExternalTask implements Assembler {

    public static final String EXE = "abyss-pe";


    private AbyssV134Args args;

    public AbyssV134Task() {
        this(new AbyssV134Args());
    }

    public AbyssV134Task(AbyssV134Args args) {
        super(EXE);
        this.args = args;
    }

    @Override
    public String getCommand() {
        return this.getCommand(this.args, false);
    }

    public void setProcessArgs(AbyssV134Args args) {
        this.args = args;
    }

    public AbyssV134Args getProcessArgs() {
        return this.args;
    }


    @Override
    public AssemblerArgs getArgs() {
        return getProcessArgs();
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

    @Override
    public Collection<ConanParameter> getParameters() {
        return new AbyssV134Params().getConanParameters();
    }
}
