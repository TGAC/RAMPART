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
package uk.ac.tgac.rampart.conan.tool.proc.external.qt;

import uk.ac.tgac.rampart.conan.conanx.param.FilePair;
import uk.ac.tgac.rampart.conan.tool.proc.external.qt.sickle.SicklePeV11Args;
import uk.ac.tgac.rampart.conan.tool.proc.external.qt.sickle.SickleSeV11Args;
import uk.ac.tgac.rampart.conan.tool.proc.external.qt.sickle.SickleV11Process;
import uk.ac.tgac.rampart.core.data.Library;

import java.io.File;

/**
 * User: maplesod
 * Date: 30/01/13
 * Time: 18:42
 */
public enum QualityTrimmerFactory {

    SICKLE_SE_V1_1 {
        @Override
        public String getToolName() {
            return "SICKLE";
        }

        @Override
        public boolean isPairedEnd() {
            return false;
        }

        @Override
        public QualityTrimmer createQT() {
            return createQT(new SickleSeV11Args());
        }

        @Override
        public QualityTrimmer createQT(QualityTrimmerArgs args) {
            return new SickleV11Process(SickleV11Process.JobType.SINGLE_END, args);
        }

        @Override
        public QualityTrimmerArgs createArgs(Library library, File outputDir) {

            QualityTrimmerArgs args = new SickleSeV11Args();

            args.setSingleEndInputFile(library.getSeFile().getFile());
            args.setSingleEndOutputFile(new File(outputDir, library.getName() + ".qt.fastq"));

            return args;
        }

    },
    SICKLE_PE_V1_1 {
        @Override
        public String getToolName() {
            return "SICKLE";
        }

        @Override
        public boolean isPairedEnd() {
            return true;
        }

        @Override
        public QualityTrimmer createQT() {
            return createQT(new SicklePeV11Args());
        }

        @Override
        public QualityTrimmer createQT(QualityTrimmerArgs args) {
            return new SickleV11Process(SickleV11Process.JobType.PAIRED_END, args);
        }

        @Override
        public QualityTrimmerArgs createArgs(Library library, File outputDir) {

            QualityTrimmerArgs args = new SicklePeV11Args();

            args.setPairedEndInputFiles(new FilePair(
                    library.getFilePaired1().getFile(),
                    library.getFilePaired2().getFile()
            ));

            args.setPairedEndOutputFiles(new FilePair(
                    new File(outputDir, library.getName() + "_1.qt.fastq"),
                    new File(outputDir, library.getName() + "_2.qt.fastq")
            ));

            args.setSingleEndOutputFile(new File(outputDir, library.getName() + "_se.qt.fastq"));

            return args;
        }
    };

    public abstract String getToolName();

    public abstract boolean isPairedEnd();

    public abstract QualityTrimmer createQT();

    public abstract QualityTrimmer createQT(QualityTrimmerArgs args);

    public abstract QualityTrimmerArgs createArgs(Library library, File outputDir);


    public static QualityTrimmer create(Library lib, File outputDir) {

        return create("SICKLE", lib, outputDir);
    }


    public static QualityTrimmer create(String qtType, Library lib, File outputDir) {

        for (QualityTrimmerFactory inst : QualityTrimmerFactory.values()) {

            if (inst.getToolName().equalsIgnoreCase(qtType) &&
                    inst.isPairedEnd() == lib.isPairedEnd() &&
                    lib.testUsage(Library.Usage.QUALITY_TRIMMING)) {

                return inst.createQT(inst.createArgs(lib, outputDir));
            }

        }

        return null;
    }
}
