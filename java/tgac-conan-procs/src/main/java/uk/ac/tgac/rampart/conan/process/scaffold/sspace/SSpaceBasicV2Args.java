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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SSpaceBasicV2Args extends ScaffolderArgs {

    private SSpaceBasicV2Params params = new SSpaceBasicV2Params();

    // **** Main args ****
    private File libraryConfigFile;
    private int extend;

    // **** Extension args ****
    private int minOverlap;
    private int nbReads;
    private int trim;
    private int minBaseRatio;

    // **** Scaffolding args ****
    private int minLinks;
    private int maxLinks;
    private int minContigOverlap;
    private int minContigLength;

    // **** Bowtie args ****
    private int maxGaps;

    // **** Additional args ****
    private boolean plot;
    private String baseName;
    private boolean verbose;


    public SSpaceBasicV2Args() {

        super();

        // **** Main args ****
        this.libraryConfigFile = null;
        this.extend = 0;

        // **** Extension args ****

        // **** Scaffolding args ****

        // **** Bowtie args ****
        this.maxGaps = 0;

        // **** Additional args ****
        this.plot = false;
        this.baseName = null;
        this.verbose = false;
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

    public int getMinOverlap() {
        return minOverlap;
    }

    public void setMinOverlap(int minOverlap) {
        this.minOverlap = minOverlap;
    }

    public int getNbReads() {
        return nbReads;
    }

    public void setNbReads(int nbReads) {
        this.nbReads = nbReads;
    }

    public int getTrim() {
        return trim;
    }

    public void setTrim(int trim) {
        this.trim = trim;
    }

    public int getMinBaseRatio() {
        return minBaseRatio;
    }

    public void setMinBaseRatio(int minBaseRatio) {
        this.minBaseRatio = minBaseRatio;
    }

    public int getMinLinks() {
        return minLinks;
    }

    public void setMinLinks(int minLinks) {
        this.minLinks = minLinks;
    }

    public int getMaxLinks() {
        return maxLinks;
    }

    public void setMaxLinks(int maxLinks) {
        this.maxLinks = maxLinks;
    }

    public int getMinContigOverlap() {
        return minContigOverlap;
    }

    public void setMinContigOverlap(int minContigOverlap) {
        this.minContigOverlap = minContigOverlap;
    }

    public int getMinContigLength() {
        return minContigLength;
    }

    public void setMinContigLength(int minContigLength) {
        this.minContigLength = minContigLength;
    }

    public int getMaxGaps() {
        return maxGaps;
    }

    public void setMaxGaps(int maxGaps) {
        this.maxGaps = maxGaps;
    }

    public boolean isPlot() {
        return plot;
    }

    public void setPlot(boolean plot) {
        this.plot = plot;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
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

        Map<ConanParameter, String> pvp = new LinkedHashMap<ConanParameter, String>();

        // **** Main args ****

        if (this.libraryConfigFile != null)
            pvp.put(params.getLibraryFile(), this.libraryConfigFile.getPath());

        if (this.getInput() != null)
            pvp.put(params.getContigsFile(), this.getInput().getPath());

        pvp.put(params.getExtend(), Integer.toString(this.extend));


        // **** Extension args ****



        // **** Scaffolding args ****



        // **** Bowtie args ****

        if (this.maxGaps > 0)
            pvp.put(params.getBowtieGaps(), Integer.toString(this.maxGaps));

        if (this.getThreads() > 1)
            pvp.put(params.getBowtieThreads(), Integer.toString(this.getThreads()));


        // **** Additional args ****

        if (this.plot)
            pvp.put(params.getPlot(), Boolean.toString(this.plot));

        if (this.baseName != null)
            pvp.put(params.getBaseName(), this.baseName);

        if (this.verbose)
            pvp.put(params.getVerbose(), Boolean.toString(this.verbose));

        return pvp;
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {
        for (Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            }

            String param = entry.getKey().getName();

            // **** Main args ****

            if (param.equals(this.params.getLibraryFile().getName())) {
                this.libraryConfigFile = new File(entry.getValue());
            }
            else if (param.equals(this.params.getContigsFile().getName())) {
                this.setInput(new File(entry.getValue()));
            }
            else if (param.equals(this.params.getExtend().getName())) {
                this.extend = Integer.parseInt(entry.getValue());
            }

            // **** Extension args ****



            // **** Scaffolding args ****



            // **** Bowtie args ****

            else if (param.equals(this.params.getBowtieGaps().getName())) {
                this.maxGaps = Integer.parseInt(entry.getValue());
            }
            else if (param.equals(this.params.getBowtieThreads().getName())) {
                this.setThreads(Integer.parseInt(entry.getValue()));
            }


            // **** Additional args ****

            else if (param.equals(this.params.getPlot().getName())) {
                this.plot = Boolean.parseBoolean(entry.getValue());
            }
            else if (param.equals(this.params.getBaseName().getName())) {
                this.baseName = entry.getValue();
            }
            else if (param.equals(this.params.getVerbose().getName())) {
                this.verbose = Boolean.parseBoolean(entry.getValue());
            }



            else {
                throw new IllegalArgumentException("Unknown param found: " + param);
            }
        }
    }
}
