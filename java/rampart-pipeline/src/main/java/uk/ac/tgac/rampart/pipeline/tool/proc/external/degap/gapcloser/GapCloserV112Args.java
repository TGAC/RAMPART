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
package uk.ac.tgac.rampart.pipeline.tool.proc.external.degap.gapcloser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.tgac.rampart.core.data.Library;
import uk.ac.tgac.rampart.pipeline.tool.proc.external.degap.DegapperArgs;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GapCloserV112Args implements DegapperArgs {

    private GapCloserV112Params params = new GapCloserV112Params();

    // GapCloser vars
    private File inputScaffoldFile;
    private File libraryFile;
    private File outputScaffoldFile;
    private Integer maxReadLength;
    private Integer overlap;
    private int threads;

    // DegapperArgs vars
    private Set<Library> libs;


    public GapCloserV112Args() {
        this.inputScaffoldFile = null;
        this.libraryFile = null;
        this.outputScaffoldFile = null;
        this.maxReadLength = null;
        this.overlap = null;
        this.threads = 1;
    }

    @Override
    public File getInputScaffoldFile() {
        return inputScaffoldFile;
    }

    @Override
    public void setInputScaffoldFile(File inputScaffoldFile) {
        this.inputScaffoldFile = inputScaffoldFile;
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

    @Override
    public int getThreads() {
        return threads;
    }

    @Override
    public void setThreads(int threads) {
        this.threads = threads;
    }

    @Override
    public Set<Library> getLibraries() {
        return libs;
    }

    @Override
    public void setLibraries(Set<Library> libraries) {
        this.libs = libraries;
    }

    @Override
    public File getOutputScaffoldFile() {
        return this.outputScaffoldFile;
    }

    @Override
    public void setOutputScaffoldFile(File outputScaffoldFile) {
        this.outputScaffoldFile = outputScaffoldFile;
    }


    public void setLibraryFile(Set<Library> libs, File outputLibFile) throws IOException {

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

        this.libs = libs;
        this.libraryFile = outputLibFile;
    }


    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();

        if (this.inputScaffoldFile != null)
            pvp.put(params.getInputScaffoldFile(), this.inputScaffoldFile.getPath());

        if (this.libraryFile != null)
            pvp.put(params.getLibraryFile(), this.inputScaffoldFile.getPath());

        if (this.outputScaffoldFile != null)
            pvp.put(params.getOutputFile(), this.outputScaffoldFile.getPath());

        if (this.maxReadLength != null)
            pvp.put(params.getMaxReadLength(), this.maxReadLength.toString());

        if (this.overlap != null)
            pvp.put(params.getOverlap(), this.overlap.toString());

        if (this.threads > 1)
            pvp.put(params.getThreads(), String.valueOf(this.threads));

        return pvp;
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {
        for (Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            }

            String param = entry.getKey().getName();

            if (param.equals(this.params.getInputScaffoldFile().getName())) {
                this.inputScaffoldFile = new File(entry.getValue());
            } else if (param.equals(this.params.getLibraryFile().getName())) {
                this.libraryFile = new File(entry.getValue());
            } else if (param.equals(this.params.getOutputFile().getName())) {
                this.outputScaffoldFile = new File(entry.getValue());
            } else if (param.equals(this.params.getMaxReadLength().getName())) {
                this.maxReadLength = Integer.parseInt(entry.getValue());
            } else if (param.equals(this.params.getOverlap().getName())) {
                this.overlap = Integer.parseInt(entry.getValue());
            } else if (param.equals(this.params.getThreads().getName())) {
                this.threads = Integer.parseInt(entry.getValue());
            } else {
                throw new IllegalArgumentException("Unknown param found: " + param);
            }
        }
    }

}
