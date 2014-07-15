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

package uk.ac.tgac.rampart.stage;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 20/03/13
 * Time: 15:26
 */
public class RampartStageTest {

    @Test
    public void testParseAll() {

        String test = "ALL";

        List<RampartStage> stageList = RampartStageList.parse(test);

        assertTrue(stageList.size() == RampartStage.values().length);
    }

    @Test
    public void testParseOne() {

        String test = "MASS";

        List<RampartStage> stageList = RampartStageList.parse(test);

        assertTrue(stageList.size() == 1);
        assertTrue(stageList.get(0) == RampartStage.MASS);
    }
}
