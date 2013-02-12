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
package uk.ac.tgac.rampart.conan.conanx.exec.context.locality;

import org.junit.Test;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 31/01/13
 * Time: 16:32
 */
public class LocalTest {

    @Test
    public void testLocal() throws InterruptedException, ProcessExecutionException, CommandExecutionException, IOException {

        String CMD = "ls ~";

        Local local = new Local();

        int exitCode = local.execute(CMD);

        assertTrue(exitCode == 0);
    }


}
