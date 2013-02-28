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
package uk.ac.tgac.rampart.conan.process.qt.sickle;

import uk.ac.ebi.fgpt.conan.core.param.FilePair;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;

import java.io.File;
import java.util.Map;

public class SickleSeV11Args extends SickleV11Args {


    private SickleSeV11Params params = new SickleSeV11Params();


    private File inputFile;
    private File outputFile;


    public SickleSeV11Args() {
        this.inputFile = null;
        this.outputFile = null;
    }

    @Override
    public boolean isSingleEndArgs() {
        return true;
    }

    @Override
    public FilePair getPairedEndInputFiles() {
        return null;
    }

    @Override
    public void setPairedEndInputFiles(FilePair pairedEndInputFiles) {
    }

    @Override
    public FilePair getPairedEndOutputFiles() {
        return null;
    }

    @Override
    public void setPairedEndOutputFiles(FilePair pairedEndOutputFiles) {
    }

    @Override
    public File getSingleEndInputFile() {
        return inputFile;
    }

    @Override
    public void setSingleEndInputFile(File singleEndInputFile) {
        this.inputFile = singleEndInputFile;
    }

    @Override
    public File getSingleEndOutputFile() {
        return outputFile;
    }

    @Override
    public void setSingleEndOutputFile(File singleEndOutputFile) {
        this.outputFile = singleEndOutputFile;
    }


    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = super.getArgMap();

        if (this.inputFile != null) {
            pvp.put(params.getSeFile(), this.inputFile.getPath());
        }

        if (this.outputFile != null) {
            pvp.put(params.getOutputFile(), this.outputFile.getPath());
        }

        return pvp;
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {

        for (Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            }

            String param = entry.getKey().getName();

            if (param.equals(this.params.getSeFile().getName())) {
                this.inputFile = new File(entry.getValue());
            } else if (param.equals(this.params.getOutputFile().getName())) {
                this.outputFile = new File(entry.getValue());
            } else {
                throw new IllegalArgumentException("Unknown param found: " + param);
            }

        }
    }

}
