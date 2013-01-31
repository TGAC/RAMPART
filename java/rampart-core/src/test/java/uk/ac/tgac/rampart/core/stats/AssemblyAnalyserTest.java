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
package uk.ac.tgac.rampart.core.stats;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import uk.ac.tgac.rampart.core.data.AssemblyStats;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 31/01/13
 * Time: 12:29
 */
public class AssemblyAnalyserTest {

    @Test
    public void testAssemblyAnalyser() throws IOException {

        File assemblyFile = FileUtils.toFile(this.getClass().getResource("/stats/MASS-k71-scaffolds.fa"));

        AssemblyAnalyser aa = new AssemblyAnalyser();

        AssemblyStats stats = aa.analyse(assemblyFile);

        assertTrue(stats != null);
    }
}
