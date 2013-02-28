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
package uk.ac.tgac.rampart.conan.process.scaffold.sspace;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.tgac.rampart.conan.process.scaffold.ScaffolderArgs;
import uk.ac.tgac.rampart.core.data.Library;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SSpaceBasicV2Args extends ScaffolderArgs {

    private SSpaceBasicV2Params params = new SSpaceBasicV2Params();

    private File libraryConfigFile;
    private Integer extend;
    private String baseName;


    public SSpaceBasicV2Args() {
        this.libraryConfigFile = null;
        this.extend = null;
        this.baseName = null;
    }

    public File getLibraryConfigFile() {
        return libraryConfigFile;
    }

    public void setLibraryConfigFile(File libraryConfigFile) {
        this.libraryConfigFile = libraryConfigFile;
    }

    public int getExtend() {
        return extend;
    }

    public void setExtend(int extend) {
        this.extend = extend;
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public void createLibraryConfigFile(List<Library> libs, File libraryConfigFile) throws IOException {

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

        FileUtils.writeLines(libraryConfigFile, lines);
    }


    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();

        if (this.libraryConfigFile != null)
            pvp.put(params.getLibraryFile(), this.libraryConfigFile.getPath());
        else {
            throw new IllegalArgumentException("Must have a library file specified.  If working from a Set<Library> you can call setLibraryFile(Set<Library> libs, File outputLibFile) to automatically generate a SSPACE library file and set the class variable.");
        }

        if (this.extend != null)
            pvp.put(params.getExtend(), this.extend.toString());

        if (this.baseName != null)
            pvp.put(params.getBaseName(), this.baseName);

        return pvp;
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {
        for (Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            }

            String param = entry.getKey().getName();

            if (param.equals(this.params.getExtend().getName())) {
                this.extend = Integer.parseInt(entry.getValue());
            } else if (param.equals(this.params.getBaseName().getName())) {
                this.baseName = entry.getValue();
            } else if (param.equals(this.params.getLibraryFile().getName())) {
                this.libraryConfigFile = new File(entry.getValue());
            } else {
                throw new IllegalArgumentException("Unknown param found: " + param);
            }
        }
    }
}
