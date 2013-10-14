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
package uk.ac.tgac.rampart.tool.process.mecq;

import org.w3c.dom.Element;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.conan.process.ec.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * User: maplesod
 * Date: 14/08/13
 * Time: 17:21
 */
public class EcqArgs {

    // **** Xml Config file property keys ****

    public static final String KEY_ELEM_TOOL = "tool";
    public static final String KEY_ELEM_MIN_LEN = "min_len";
    public static final String KEY_ELEM_MIN_QUAL = "min_qual";
    public static final String KEY_ELEM_KMER = "kmer";
    public static final String KEY_ELEM_LIBS = "libs";

    public static final String KEY_ATTR_NAME = "name";
    public static final String KEY_ATTR_THREADS = "threads";
    public static final String KEY_ATTR_MEMORY = "memory";
    public static final String KEY_ATTR_PARALLEL = "parallel";


    // **** Default values ****

    public static final int DEFAULT_MIN_LEN = 60;
    public static final int DEFAULT_MIN_QUAL = 30;
    public static final int DEFAULT_KMER = 17;
    public static final int DEFAULT_THREADS = 1;
    public static final int DEFAULT_MEMORY = 0;
    public static final boolean DEFAULT_RUN_PARALLEL = false;

    public static final String RAW = "raw";


    // **** Class vars ****

    private String name;
    private String tool;
    private int minLen;
    private int minQual;
    private int kmer;
    private int threads;
    private int memory;
    private boolean runParallel;
    private List<Library> libraries;
    private File outputDir;
    private String jobPrefix;

    public EcqArgs() {
        this.name = "";
        this.minLen = DEFAULT_MIN_LEN;
        this.minQual = DEFAULT_MIN_QUAL;
        this.kmer = DEFAULT_KMER;
        this.threads = DEFAULT_THREADS;
        this.memory = DEFAULT_MEMORY;
        this.runParallel = DEFAULT_RUN_PARALLEL;
        this.libraries = new ArrayList<Library>();
    }


    public EcqArgs(Element ele, List<Library> allLibraries, File parentOutputDir, String parentJobPrefix, boolean forceParallel) {

        // Set defaults
        this();

        // Required
        this.name = XmlHelper.getTextValue(ele, KEY_ATTR_NAME);
        this.tool = XmlHelper.getTextValue(ele, KEY_ELEM_TOOL);

        // Optional
        this.minLen = ele.hasAttribute(KEY_ELEM_MIN_LEN) ? XmlHelper.getIntValue(ele, KEY_ELEM_MIN_LEN) : DEFAULT_MIN_LEN;
        this.minQual = ele.hasAttribute(KEY_ELEM_MIN_QUAL) ? XmlHelper.getIntValue(ele, KEY_ELEM_MIN_QUAL): DEFAULT_MIN_QUAL;
        this.kmer = ele.hasAttribute(KEY_ELEM_KMER) ? XmlHelper.getIntValue(ele, KEY_ELEM_KMER) : DEFAULT_KMER;
        this.threads = ele.hasAttribute(KEY_ATTR_THREADS) ? XmlHelper.getIntValue(ele, KEY_ATTR_THREADS) : DEFAULT_THREADS;
        this.memory = ele.hasAttribute(KEY_ATTR_MEMORY) ? XmlHelper.getIntValue(ele, KEY_ATTR_MEMORY) : DEFAULT_MEMORY;
        this.runParallel = forceParallel ? true :
                ele.hasAttribute(KEY_ATTR_PARALLEL) ? XmlHelper.getBooleanValue(ele, KEY_ATTR_PARALLEL) : DEFAULT_RUN_PARALLEL;

        // Filter the provided libs
        String libList = XmlHelper.getTextValue(ele, KEY_ELEM_LIBS);
        String[] libIds = libList.split(",");

        for(String libId : libIds) {
            for(Library lib : allLibraries) {
                if (lib.getName().equalsIgnoreCase(libId.trim())) {
                    this.libraries.add(lib);
                    break;
                }
            }
        }

        // Other args
        this.outputDir = new File(parentOutputDir, name);
        this.jobPrefix = parentJobPrefix + "-name";
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public int getMinLen() {
        return minLen;
    }

    public void setMinLen(int minLen) {
        this.minLen = minLen;
    }

    public int getMinQual() {
        return minQual;
    }

    public void setMinQual(int minQual) {
        this.minQual = minQual;
    }

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

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public boolean isRunParallel() {
        return runParallel;
    }

    public void setRunParallel(boolean runParallel) {
        this.runParallel = runParallel;
    }

    public List<Library> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<Library> libraries) {
        this.libraries = libraries;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public String getJobPrefix() {
        return jobPrefix;
    }

    public void setJobPrefix(String jobPrefix) {
        this.jobPrefix = jobPrefix;
    }

    public Library findLibrary(String libName) {

        for (Library lib : this.libraries) {
            if (lib.getName().equalsIgnoreCase(libName)) {
                return lib;
            }
        }

        return null;
    }

    public List<File> getOutputFiles(Library lib) {

        ErrorCorrector ec = ErrorCorrectorFactory.createQualityTrimmer(this.getTool());

        List<File> altInputFiles = new ArrayList<>();
        if (lib.isPairedEnd()) {
            altInputFiles.add(new File(outputDir, lib.getFile1().getName()));
            altInputFiles.add(new File(outputDir, lib.getFile2().getName()));
        }
        else {
            altInputFiles.add(new File(outputDir, lib.getFile1().getName()));
        }
        ec.getArgs().setFromLibrary(lib, altInputFiles);

        return ec.getArgs().getCorrectedFiles();
    }
}
