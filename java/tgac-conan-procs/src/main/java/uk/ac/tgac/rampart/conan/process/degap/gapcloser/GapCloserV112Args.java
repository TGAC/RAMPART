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
import uk.ac.tgac.rampart.conan.process.degap.AbstractDegapperArgs;
import uk.ac.tgac.rampart.core.data.Library;
import uk.ac.tgac.rampart.core.data.SeqFile;
import uk.ac.tgac.rampart.core.utils.StringJoiner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GapCloserV112Args extends AbstractDegapperArgs {

    private GapCloserV112Params params = new GapCloserV112Params();

    public static int DEFAULT_OVERLAP = 25;
    public static int DEFAULT_READ_LENGTH = 100;

    // GapCloser vars
    private File libraryFile;
    private File outputFile;
    private int overlap = DEFAULT_OVERLAP;
    private int maxReadLength = DEFAULT_READ_LENGTH;

    public GapCloserV112Args() {
        this.libraryFile = null;
        this.outputFile = null;
        this.overlap = DEFAULT_OVERLAP;
        this.maxReadLength = DEFAULT_READ_LENGTH;
    }

    @Override
    public File getOutputFile() {
        return this.outputFile;
    }

    @Override
    public void setOutputPrefix(String outputPrefix) {

        if (this.getOutputDir() != null) {
            this.setOutputFile(new File(this.getOutputDir(), outputPrefix + ".fa"));
        }
        else {
            this.setOutputFile(new File(outputPrefix + ".fa"));
        }

        super.setOutputPrefix(outputPrefix);
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public File getLibraryFile() {
        return libraryFile;
    }

    public void setLibraryFile(File libraryFile) {
        this.libraryFile = libraryFile;
    }

    public Integer getOverlap() {
        return overlap;
    }

    public void setOverlap(Integer overlap) {
        this.overlap = overlap;
    }

    public int getMaxReadLength() {
        return maxReadLength;
    }

    public void setMaxReadLength(int maxReadLength) {
        this.maxReadLength = maxReadLength;
    }

    public static void createLibraryFile(List<Library> libs, File outputLibFile) throws IOException {

        List<String> lines = new ArrayList<String>();

        for (Library lib : libs) {

            if (lib.testUsage(Library.Usage.GAP_CLOSING)) {

                StringJoiner sj = new StringJoiner("\n");

                sj.add("[LIB]");
                sj.add(lib.getReadLength() != null, "max_rd_len=", Integer.toString(lib.getReadLength()));
                sj.add(lib.getAverageInsertSize() != null, "avg_ins=", Integer.toString(lib.getAverageInsertSize()));
                sj.add(lib.getSeqOrientation() != null, "reverse_seq=", lib.getSeqOrientation() == Library.SeqOrientation.FORWARD_REVERSE ? "0" : "1");
                sj.add("asm_flags=3");
                sj.add(lib.getIndex() != null, "rank=", Integer.toString(lib.getIndex()));
                sj.add(lib.getFilePaired1() != null && lib.getFilePaired1().getFileType() == SeqFile.FileType.FASTQ, "q1=", lib.getFilePaired1().getFilePath());
                sj.add(lib.getFilePaired2() != null && lib.getFilePaired2().getFileType() == SeqFile.FileType.FASTQ, "q2=", lib.getFilePaired2().getFilePath());
                //sj.add(lib.getSeFile() != null && lib.getSeFile().getFileType() == SeqFile.FileType.FASTQ, "q=", lib.getSeFile().getFilePath());
                sj.add(lib.getFilePaired1() != null && lib.getFilePaired1().getFileType() == SeqFile.FileType.FASTA, "f1=", lib.getFilePaired1().getFilePath());
                sj.add(lib.getFilePaired2() != null && lib.getFilePaired2().getFileType() == SeqFile.FileType.FASTA, "f2=", lib.getFilePaired2().getFilePath());
                //sj.add(lib.getSeFile() != null && lib.getSeFile().getFileType() == SeqFile.FileType.FASTA, "f=", lib.getSeFile().getFilePath());

                lines.add(sj.toString() + "\n");
            }

        }

        FileUtils.writeLines(outputLibFile, lines);
    }


    @Override
    public void parse(String args) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = new LinkedHashMap<ConanParameter, String>();

        if (this.getInputFile() != null)
            pvp.put(params.getInputScaffoldFile(), this.getInputFile().getAbsolutePath());

        if (this.libraryFile != null)
            pvp.put(params.getLibraryFile(), this.libraryFile.getAbsolutePath());

        if (this.getOutputFile() != null)
            pvp.put(params.getOutputFile(), this.getOutputFile().getAbsolutePath());

        if (this.overlap != DEFAULT_OVERLAP)
            pvp.put(params.getOverlap(), Integer.toString(this.overlap));

        if (this.maxReadLength != DEFAULT_READ_LENGTH)
            pvp.put(params.getMaxReadLength(), Integer.toString(this.maxReadLength));

        if (this.getThreads() > 1)
            pvp.put(params.getThreads(), Integer.toString(this.getThreads()));

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
            }
            else if (param.equals(this.params.getOverlap().getName())) {
                this.overlap = Integer.parseInt(entry.getValue());
            }
            else if (param.equals(this.params.getMaxReadLength().getName())) {
                this.maxReadLength = Integer.parseInt(entry.getValue());
            }
            else if (param.equals(this.params.getThreads().getName())) {
                this.setThreads(Integer.parseInt(entry.getValue()));
            }
            else if (param.equals(this.params.getInputScaffoldFile().getName())) {
                this.setInputFile(new File(entry.getValue()));
            }
            else if (param.equals(this.params.getOutputFile().getName())) {
                this.setOutputFile(new File(entry.getValue()));
            }
            else {
                throw new IllegalArgumentException("Unknown param found: " + param);
            }
        }
    }

}
