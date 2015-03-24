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

package uk.ac.tgac.jellyswarm;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by maplesod on 08/07/14.
 */
public class FileFinderTest {

    private File jsDirGood = null;
    private File jsDirBad = null;

    @Before
    public void setup() {
        jsDirGood = FileUtils.toFile(this.getClass().getResource("/tools/jellyswarm/good"));
        jsDirBad = FileUtils.toFile(this.getClass().getResource("/tools/jellyswarm/bad"));
    }


    @Test
    public void findPairedTest() throws IOException {

        List<FilePair> files = FileFinder.find(jsDirGood, false, true);

        assertTrue(files.size() == 2);
    }

    @Test
    public void findPairedRecursiveTest() throws IOException {

        List<FilePair> files = FileFinder.find(jsDirGood, true, true);

        assertTrue(files.size() == 3);
    }

    @Test
    public void findSingleTest() throws IOException {

        List<FilePair> files = FileFinder.find(jsDirGood, false, false);

        assertTrue(files.size() == 4);
    }

    @Test
    public void findSingleRecursiveTest() throws IOException {

        List<FilePair> files = FileFinder.find(jsDirGood, true, false);

        assertTrue(files.size() == 6);
    }

    @Test(expected = IOException.class)
    public void findPairedBadTest() throws IOException {

        List<FilePair> files = FileFinder.find(jsDirBad, true, true);
    }
}
