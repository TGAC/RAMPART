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
package uk.ac.tgac.rampart.stage.analyse.asm.selector;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStatsMatrix;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStatsMatrixRow;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStatsTable;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 26/04/13
 * Time: 10:57
 */
public class AssemblyStatsMatrixTest {

    private File statsFile2 = FileUtils.toFile(this.getClass().getResource("/tools/stats/stats2.txt"));

    private AssemblyStatsMatrix matrix;

    @Before
    public void setup() throws IOException {
        matrix = new AssemblyStatsMatrix(new AssemblyStatsTable(statsFile2));
    }

    @Test
    public void testStandardNormalise() throws IOException {

        assertTrue(matrix.get(0).getAt(AssemblyStatsMatrixRow.IDX_NB_SEQS) == 851620.0);

        matrix.standardScale(AssemblyStatsMatrixRow.IDX_NB_SEQS, true);

        assertTrue(matrix.get(0).getAt(AssemblyStatsMatrixRow.IDX_NB_SEQS) == 0.0);
    }

    @Test
    public void testDeviationNormalise0() throws IOException {

        assertTrue(matrix.get(0).getAt(AssemblyStatsMatrixRow.IDX_NB_BASES) == 399155917.0);

        matrix.deviationScale(AssemblyStatsMatrixRow.IDX_NB_BASES, 352155917.0);

        assertTrue(matrix.get(0).getAt(AssemblyStatsMatrixRow.IDX_NB_BASES) == 0.7139892568315214);
    }

    @Test
    public void testNormalise0() {

        assertTrue(matrix.get(0).getAt(AssemblyStatsMatrixRow.IDX_NB_BASES) == 399155917.0);
        assertTrue(matrix.get(0).getAt(AssemblyStatsMatrixRow.IDX_NB_SEQS) == 851620.0);

        matrix.normalise(0, 0.0);

        assertTrue(true);
    }

    @Test
    public void testWeight() {

        assertTrue(matrix.get(0).getAt(AssemblyStatsMatrixRow.IDX_NB_BASES) == 399155917.0);
        assertTrue(matrix.get(0).getAt(AssemblyStatsMatrixRow.IDX_NB_SEQS) == 851620.0);

        AssemblyStatsMatrixRow weights = new AssemblyStatsMatrixRow(new double[]{0.2,0.1,0,0,0,0,0,0,0.2,0.2,0,0.2,0,0.1});

        matrix.normalise(0, 0.0);
        matrix.weight(weights);

        assertTrue(true);
    }
}
