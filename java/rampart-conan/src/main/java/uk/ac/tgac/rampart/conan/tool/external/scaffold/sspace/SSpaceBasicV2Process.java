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
package uk.ac.tgac.rampart.conan.tool.external.scaffold.sspace;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.process.DefaultExtendedConanProcess;
import uk.ac.tgac.rampart.conan.tool.external.scaffold.Scaffolder;

import java.util.Collection;
import java.util.Map;

/**
 * User: maplesod
 * Date: 23/01/13
 * Time: 16:00
 */
public class SSpaceBasicV2Process extends DefaultExtendedConanProcess implements Scaffolder {

    public static final String EXE = "SSPACE_Basic_v2.0.pl";

    private SSpaceBasicV2Args args;

    public SSpaceBasicV2Process() {
        this(new SSpaceBasicV2Args());
    }

    public SSpaceBasicV2Process(SSpaceBasicV2Args args) {
        super(EXE);
        this.args = args;
    }

    @Override
    public String getCommand() {
        return this.getCommandLineBuilder().getFullCommand(this.args, true, "-", " ");
    }

    @Override
    public boolean execute(Map<ConanParameter, String> parameters) throws ProcessExecutionException, IllegalArgumentException, InterruptedException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getName() {
        return "SSPACE_Basic_v2.0";
    }

    @Override
    public Collection<ConanParameter> getParameters() {
        return new SSpaceBasicV2Params().getConanParameters();
    }
}
