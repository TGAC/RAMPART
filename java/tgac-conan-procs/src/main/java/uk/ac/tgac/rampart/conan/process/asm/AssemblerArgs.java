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
package uk.ac.tgac.rampart.conan.process.asm;

import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.tgac.rampart.core.data.Library;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class AssemblerArgs implements ProcessArgs {

    private int kmer;
    private int threads;
    private File outputDir;
    private List<Library> libraries;

    protected AssemblerArgs() {
        this.kmer = 65;
        this.threads = 0;
        this.outputDir = new File(".");
        this.libraries = new ArrayList<Library>();
    }

    public abstract AssemblerArgs copy();



    public int getKmer() {
        return kmer;
    }

    public void setKmer(int kmer) {
        this.kmer = kmer;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public List<Library> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<Library> libraries) {
        this.libraries = libraries;
    }
}
