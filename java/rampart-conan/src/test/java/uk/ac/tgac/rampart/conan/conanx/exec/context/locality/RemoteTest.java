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

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 31/01/13
 * Time: 15:29
 */
public class RemoteTest {

    @Test
    public void testRemoteSession() throws InterruptedException, ProcessExecutionException {

        ConnectionDetails connectionDetails = new ConnectionDetails("norwich", 22, "maplesod", "#5803sb152DlM");

        Remote remote = new Remote(connectionDetails);

        if (!remote.establishConnection())
            throw new ProcessExecutionException(-1, "Couldn't connect");

        int exitCode = remote.execute("ls ~");

        if (!remote.disconnect()) {
            throw new ProcessExecutionException(-1, "Couldn't disconnect");
        }

        assertTrue(exitCode == 0);
    }
}
