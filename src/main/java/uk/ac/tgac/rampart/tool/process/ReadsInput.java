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
package uk.ac.tgac.rampart.tool.process;

import org.w3c.dom.Element;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.util.XmlHelper;

import java.io.IOException;
import java.util.List;

/**
 * User: maplesod
 * Date: 15/08/13
 * Time: 11:47
 */
public class ReadsInput {

    public static final String KEY_ATTR_ECQ = "ecq";
    public static final String KEY_ATTR_LIB = "lib";

    private String ecq;
    private String lib;

    public ReadsInput(String ecq, String lib) {
        this.ecq = ecq;
        this.lib = lib;
    }

    public ReadsInput(Element ele) throws IOException {

        if (!ele.hasAttribute(KEY_ATTR_ECQ))
            throw new IOException("Could not find " + KEY_ATTR_ECQ + " attribute in Input element");

        if (!ele.hasAttribute(KEY_ATTR_LIB))
            throw new IOException("Could not find " + KEY_ATTR_LIB + " attribute in input element");

        this.ecq = XmlHelper.getTextValue(ele, KEY_ATTR_ECQ);
        this.lib = XmlHelper.getTextValue(ele, KEY_ATTR_LIB);
    }

    public String getEcq() {
        return ecq;
    }

    public void setEcq(String ecq) {
        this.ecq = ecq;
    }

    public String getLib() {
        return lib;
    }

    public void setLib(String lib) {
        this.lib = lib;
    }

    public Mecq.EcqArgs findMecq(List<Mecq.EcqArgs> allMecqs) {

        if (allMecqs == null)
            return null;

        for(Mecq.EcqArgs currentMecq : allMecqs) {
            if (currentMecq.getName().equalsIgnoreCase(this.ecq.trim())) {
                return currentMecq;
            }
        }

        return null;
    }

    public Library findLibrary(List<Library> allLibraries) {

        if (allLibraries == null)
            return null;

        for(Library currentLib : allLibraries) {
            if (currentLib.getName().equalsIgnoreCase(this.lib.trim())) {
                return currentLib;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return this.ecq + "-" + this.lib;
    }

    /*public boolean isPairedEndLib(List<Library> allLibraries) {
        Library actualLib = findLibrary(allLibraries);
        return actualLib.isPairedEnd();
    }

    public List<File> getFiles(List<EcqArgs> allEcqs, List<Library> allLibraries) {

        EcqArgs actualEcq = findMecq(allEcqs);
        Library lib = findLibrary(allLibraries);
        ErrorCorrector ec = new MecqExecutorImpl().makeErrorCorrector(actualEcq, lib, );
        return ec.getArgs().getCorrectedFiles();
    }

    public File getFile1(List<EcqArgs> allEcqs, List<Library> allLibraries) {
        return getFiles(allEcqs, allLibraries).get(0);
    }

    public File getFile2(List<EcqArgs> allEcqs, List<Library> allLibraries) {
        return getFiles(allEcqs, allLibraries).get(0);
    }*/
}
