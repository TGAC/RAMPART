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
package uk.ac.tgac.rampart.conan.tool.external.degap.gapcloser;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.process.DefaultExtendedConanProcess;
import uk.ac.tgac.rampart.conan.tool.external.degap.Degapper;

import java.util.Collection;
import java.util.Map;

/**
 * User: maplesod
 * Date: 23/01/13
 * Time: 13:44
 */
public class GapCloserV112Process extends DefaultExtendedConanProcess implements Degapper {

    public static final String EXE = "GapCloser";

    private GapCloserV112Args args;

    public GapCloserV112Process() {
        this(new GapCloserV112Args());
    }

    public GapCloserV112Process(GapCloserV112Args args) {
        super(EXE);
        this.args = args;
    }

    @Override
    public String getCommand() {
        return this.getCommandLineBuilder().getFullCommand(this.args, true, "-", " ");
    }

    @Override
    public boolean execute(Map<ConanParameter, String> parameters) throws ProcessExecutionException, IllegalArgumentException, InterruptedException {
        //TODO
        return false;
    }

    @Override
    public String getName() {
        return "SOAP_GapCloser_v1.12-LSF";
    }

    @Override
    public Collection<ConanParameter> getParameters() {
        return new GapCloserV112Params().getConanParameters();
    }
}
