/*
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
 */

package uk.ac.tgac.rampart.tool.pipeline.rampart;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.tgac.rampart.tool.pipeline.RampartStageList;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 29/10/13
 * Time: 16:46
 * To change this template use File | Settings | File Templates.
 */
public class RampartArgsTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();


    @Test
    public void testConfigLoad() throws IOException {

        File cfgFile = FileUtils.toFile(this.getClass().getResource("/config/test_rampart_config.xml"));
        File outDir = temp.newFolder("configTest");
        String jobPrefix = "configTestJob";

        RampartArgs args = new RampartArgs(cfgFile, outDir, jobPrefix, RampartStageList.parse("ALL"), null);

        assertTrue(true);
    }
}
