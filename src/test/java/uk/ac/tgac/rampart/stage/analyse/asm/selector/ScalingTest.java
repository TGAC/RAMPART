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

import org.junit.Test;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.Scaling;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 26/04/13
 * Time: 10:57
 */
public class ScalingTest {

    @Test
    public void testStandardNormalise() throws IOException {

        double[] a = new double[] {
                851620,
                746981,
                637136,
                581571,
                521126,
                485352
        };

        Scaling.standardScale(a, true);

        assertTrue(a[0] == 0.0);
    }

    @Test
    public void testDeviationNormalise0() throws IOException {

        double[] a = new double[] {
                399155917,
                400283582,
                402925744,
                404793555,
                408730927,
                411925857
        };

        Scaling.deviationScale(a, 352155917.0);

        assertTrue(a[0] == 0.7863484554275946);
    }

}
