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
package uk.ac.tgac.rampart.conan.process.degap.gapcloser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.tgac.rampart.conan.process.degap.DegapperArgs;
import uk.ac.tgac.rampart.core.data.Library;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GapCloserV112Args extends DegapperArgs {

    private GapCloserV112Params params = new GapCloserV112Params();

    // GapCloser vars
    private File libraryFile;
    private Integer maxReadLength;
    private Integer overlap;

    public GapCloserV112Args() {
        this.libraryFile = null;
        this.maxReadLength = null;
        this.overlap = null;
    }

    public File getLibraryFile() {
        return libraryFile;
    }

    public void setLibraryFile(File libraryFile) {
        this.libraryFile = libraryFile;
    }

    public Integer getMaxReadLength() {
        return maxReadLength;
    }

    public void setMaxReadLength(Integer maxReadLength) {
        this.maxReadLength = maxReadLength;
    }

    public Integer getOverlap() {
        return overlap;
    }

    public void setOverlap(Integer overlap) {
        this.overlap = overlap;
    }


    public static void createLibraryFile(List<Library> libs, File outputLibFile) throws IOException {

        List<String> lines = new ArrayList<String>();

        for (Library lib : libs) {

            if (lib.testUsage(Library.Usage.SCAFFOLDING)) {

                String[] parts = new String[]{
                        lib.getName(),
                        lib.getFilePaired1().getFilePath(),
                        lib.getFilePaired2().getFilePath(),
                        lib.getAverageInsertSize().toString(),
                        lib.getInsertErrorTolerance().toString(),
                        lib.getSeqOrientation().toString()
                };

                lines.add(StringUtils.join(parts, " "));
            }

        }

        FileUtils.writeLines(outputLibFile, lines);
    }


    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();

        if (this.maxReadLength != null)
            pvp.put(params.getMaxReadLength(), this.maxReadLength.toString());

        if (this.overlap != null)
            pvp.put(params.getOverlap(), this.overlap.toString());

        return pvp;
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {
        for (Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            }

            String param = entry.getKey().getName();

            if (param.equals(this.params.getLibraryFile().getName())) {
                this.libraryFile = new File(entry.getValue());
            } else if (param.equals(this.params.getMaxReadLength().getName())) {
                this.maxReadLength = Integer.parseInt(entry.getValue());
            } else if (param.equals(this.params.getOverlap().getName())) {
                this.overlap = Integer.parseInt(entry.getValue());
            } else {
                throw new IllegalArgumentException("Unknown param found: " + param);
            }
        }
    }

}
