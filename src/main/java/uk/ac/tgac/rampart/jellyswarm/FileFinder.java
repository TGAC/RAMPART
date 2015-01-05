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

package uk.ac.tgac.rampart.jellyswarm;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by maplesod on 08/07/14.
 */
public class FileFinder {

    public static List<FilePair> find(File inputDir, boolean recursive, boolean paired) throws IOException {

        Collection<File> files = FileUtils.listFiles(inputDir, new String[]{"fastq", "fq"}, recursive);

        java.util.List<File> fileList = new ArrayList<>(files);

        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
            }
        });

        List<uk.ac.tgac.rampart.jellyswarm.FilePair> pairedFiles = new ArrayList<>();

        if (paired) {

            if (fileList.size() % 2 != 0)
                throw new IOException("Found an odd number of files");

            for(int i = 0; i < fileList.size(); i+=2) {

                File f1 = fileList.get(i);
                File f2 = fileList.get(i+1);

                pairedFiles.add(new uk.ac.tgac.rampart.jellyswarm.FilePair(f1, f2));
            }
        }
        else {

            for(File f1 : fileList) {
                pairedFiles.add(new uk.ac.tgac.rampart.jellyswarm.FilePair(f1, null));
            }
        }

        return pairedFiles;
    }

}
