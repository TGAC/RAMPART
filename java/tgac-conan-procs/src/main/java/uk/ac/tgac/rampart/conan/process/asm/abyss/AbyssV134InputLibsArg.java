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
package uk.ac.tgac.rampart.conan.process.asm.abyss;

import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.fgpt.conan.core.param.FilePair;
import uk.ac.tgac.rampart.core.data.Library;
import uk.ac.tgac.rampart.core.data.SeqFile;

import java.io.File;
import java.util.*;


public class AbyssV134InputLibsArg {

    private List<Library> libs;

    public AbyssV134InputLibsArg() {
        this(new ArrayList<Library>());
    }

    public AbyssV134InputLibsArg(List<Library> libs) {
        this.libs = libs;
    }


    public List<Library> getLibs() {
        return libs;
    }

    public void setLibs(List<Library> libs) {
        this.libs = libs;
    }

    protected String joinPairedLibs(Map<String, FilePair> libs) {
        List<String> list = new ArrayList<String>();

        for (Map.Entry<String, FilePair> pp : libs.entrySet()) {
            list.add(pp.getKey() + "='" + pp.getValue().toString() + "'");
        }

        return StringUtils.join(list, " ");
    }

    public Map<String, FilePair> getPairedLibs(Library.Type type) {

        Map<String, FilePair> peLibs = new HashMap<String, FilePair>();

        for (Library lib : this.libs) {
            if (lib.getType() == type) {
                peLibs.put(lib.getName(), new FilePair(lib.getFilePaired1().getFile(), lib.getFilePaired2().getFile()));
            }
        }

        return peLibs;
    }

    public Set<File> getSingleEndLibs() {
        Set<File> seLibs = new HashSet<File>();

        for (Library lib : this.libs) {
            if (lib.getSeFile() != null) {
                seLibs.add(lib.getSeFile().getFile());
            }
        }

        return seLibs;
    }


    @Override
    public String toString() {

        Map<String, FilePair> peLibs = getPairedLibs(Library.Type.PAIRED_END);
        Map<String, FilePair> mpLibs = getPairedLibs(Library.Type.MATE_PAIR);
        Set<File> seLibs = getSingleEndLibs();

        StringBuilder sb = new StringBuilder();

        if (peLibs.size() > 0) {

            sb.append("lib='");
            sb.append(StringUtils.join(peLibs.keySet(), " "));
            sb.append("'");

            sb.append(" ");

            sb.append(joinPairedLibs(peLibs));

            sb.append(" ");
        }

        if (mpLibs != null && mpLibs.size() > 0) {

            sb.append("mp='");
            sb.append(StringUtils.join(mpLibs.keySet(), " "));
            sb.append("'");

            sb.append(" ");

            sb.append(joinPairedLibs(mpLibs));

            sb.append(" ");
        }

        if (seLibs != null && seLibs.size() > 0) {

            sb.append("se='");
            sb.append(StringUtils.join(seLibs, " "));
            sb.append("'");
        }

        return sb.toString().trim();
    }

    public static AbyssV134InputLibsArg parse(String libs) {

        AbyssV134InputLibsArg libsArg = new AbyssV134InputLibsArg();

        // Get PE libs
        List<String> peLibs = getAbyssArgs(libs, "lib");

        // Get MP libs
        List<String> mpLibs = getAbyssArgs(libs, "mp");

        // Get SE libs
        List<String> seLibs = getAbyssArgs(libs, "se");

        // Convert all string to actual library objects and add to libsArg.
        List<Library> allLibs = new ArrayList<Library>();

        for(String peLib : peLibs) {
            List<String> peLibPaths = getAbyssArgs(libs, peLib);

            if (peLibPaths.size() == 2) {
                allLibs.add(createNewPELibrary(peLibPaths.get(0), peLibPaths.get(1), Library.Type.PE));
            }
            else {
                throw new IllegalArgumentException("Paired end library does not contain two file paths");
            }
        }

        for(String mpLib : mpLibs) {
            List<String> mpLibPaths = getAbyssArgs(libs, mpLib);

            if (mpLibPaths.size() == 2) {
                allLibs.add(createNewPELibrary(mpLibPaths.get(0), mpLibPaths.get(1), Library.Type.MP));
            }
            else {
                throw new IllegalArgumentException("Paired end library does not contain two file paths");
            }
        }

        for(String seLib : seLibs) {

            allLibs.add(createNewSELibrary(seLib));
        }

        libsArg.setLibs(allLibs);

        return libsArg;
    }

    protected static Library createNewPELibrary(String libPath1, String libPath2, Library.Type type) {

        Library lib = new Library();

        lib.setType(type);
        lib.setFilePaired1(new SeqFile(libPath1));
        lib.setFilePaired2(new SeqFile(libPath2));
        lib.setSeqOrientation(type == Library.Type.PE ? Library.SeqOrientation.FR : Library.SeqOrientation.RF);

        return lib;
    }

    protected static Library createNewSELibrary(String libPath) {

        Library lib = new Library();

        lib.setType(Library.Type.SE);
        lib.setSeFile(new SeqFile(libPath));

        return lib;
    }

    protected static List<String> getAbyssArgs(String string, String header) {

        int indexStart = string.indexOf("header");

        if (indexStart < 0) {
            return null;
        }

        String subStr = string.substring(indexStart);

        int indexStartArgs = subStr.indexOf("\"");

        if (indexStartArgs < 0) {
            return null;
        }

        String subStrArgs = string.substring(indexStartArgs);

        int indexEndArgs = subStrArgs.indexOf("\"");

        if (indexEndArgs < 0) {
            return null;
        }

        String args = subStrArgs.substring(0, indexEndArgs).trim();

        String[] argArray = args.split(" ");

        List<String> argList = new ArrayList<String>();

        for(String arg : argArray) {

            String argTrimmed = arg.trim();

            if (arg != null && !arg.isEmpty()) {
                argList.add(argTrimmed);
            }
        }

        return argList;
    }

}
