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
package uk.ac.tgac.rampart.tool.process.mass;

import org.w3c.dom.Element;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.tool.process.mecq.MecqSingleArgs;

import java.io.File;
import java.util.List;

/**
 * User: maplesod
 * Date: 15/08/13
 * Time: 11:47
 */
public class MassInput {

    public static final String KEY_ATTR_MECQ = "mecq";
    public static final String KEY_ATTR_LIB = "lib";

    private String mecq;
    private String lib;

    public MassInput(String mecq, String lib) {
        this.mecq = mecq;
        this.lib = lib;
    }

    public MassInput(Element ele) {
        this.mecq = XmlHelper.getTextValue(ele, KEY_ATTR_MECQ);
        this.lib = XmlHelper.getTextValue(ele, KEY_ATTR_LIB);
    }

    public String getMecq() {
        return mecq;
    }

    public void setMecq(String mecq) {
        this.mecq = mecq;
    }

    public String getLib() {
        return lib;
    }

    public void setLib(String lib) {
        this.lib = lib;
    }

    public MecqSingleArgs findMecq(List<MecqSingleArgs> allMecqs) {
        for(MecqSingleArgs currentMecq : allMecqs) {
            if (currentMecq.getName().equalsIgnoreCase(this.mecq.trim())) {
                return currentMecq;
            }
        }

        return null;
    }

    public Library findLibrary(List<Library> allLibraries) {
        for(Library currentLib : allLibraries) {
            if (currentLib.getName().equalsIgnoreCase(this.lib.trim())) {
                return currentLib;
            }
        }

        return null;
    }

    public boolean isPairedEndLib(List<Library> allLibraries) {
        Library actualLib = findLibrary(allLibraries);
        return actualLib.isPairedEnd();
    }

    public List<File> getFiles(List<MecqSingleArgs> allMecqs) {

        MecqSingleArgs actualMecq = findMecq(allMecqs);
        return actualMecq.getOutputFiles(this.lib.trim());
    }

    public File getFile1(List<MecqSingleArgs> allMecqs) {
        return getFiles(allMecqs).get(0);
    }

    public File getFile2(List<MecqSingleArgs> allMecqs) {
        return getFiles(allMecqs).get(0);
    }
}
