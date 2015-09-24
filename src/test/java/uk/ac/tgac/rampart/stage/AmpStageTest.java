/*
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2015  Daniel Mapleson - TGAC
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
 */

package uk.ac.tgac.rampart.stage;

import org.junit.Test;
import uk.ac.tgac.conan.process.asmIO.AssemblyEnhancer;
import uk.ac.tgac.rampart.MockedConanProcess;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 17/10/13
 * Time: 11:57
 * To change this template use File | Settings | File Templates.
 */
public class AmpStageTest extends MockedConanProcess {

    @Test
    public void testMakeStage() throws IOException {

        AmpStage.Args args = new AmpStage.Args();
        args.setTool("SSPACE_Basic_V2.0");

        AssemblyEnhancer process = new AmpStage(this.conanExecutorService).makeStage(args, null);

        assertTrue(process != null);
    }
}
