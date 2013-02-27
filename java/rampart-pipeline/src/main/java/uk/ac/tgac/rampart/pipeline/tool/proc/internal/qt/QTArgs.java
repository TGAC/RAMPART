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
package uk.ac.tgac.rampart.pipeline.tool.proc.internal.qt;

import org.ini4j.Profile;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.tgac.rampart.core.data.Library;
import uk.ac.tgac.rampart.core.data.RampartConfiguration;
import uk.ac.tgac.rampart.core.data.SeqFile;
import uk.ac.tgac.rampart.pipeline.tool.proc.external.qt.QualityTrimmer;
import uk.ac.tgac.rampart.pipeline.tool.proc.external.qt.QualityTrimmerFactory;

import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: maplesod
 * Date: 16/01/13
 * Time: 13:37
 */
public class QTArgs implements ProcessArgs {

    public static final String QT_TOOL = "tool";
    public static final String QT_MIN_LEN = "minlen";
    public static final String QT_MIN_QUAL = "minqual";


    private QTParams params = new QTParams();

    private File config;
    private File outputDir;
    private String qualityTrimmer;
    private int minLen;
    private int minQual;
    private List<Library> libs;
    private boolean createConfigs;
    private String jobPrefix;
    private boolean runParallel;

    public QTArgs() {
        this.config = null;
        this.outputDir = new File(".");
        this.qualityTrimmer = "SICKLE_PE_V1.1";
        this.minLen = 60;
        this.minQual = 30;
        this.libs = null;
        this.createConfigs = false;
        this.runParallel = false;

        Format formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateTime = formatter.format(new Date());
        this.jobPrefix = "qt-" + dateTime;
    }


    public File getConfig() {
        return config;
    }

    public void setConfig(File config) {
        this.config = config;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public String getQualityTrimmer() {
        return qualityTrimmer;
    }

    public void setQualityTrimmer(String qualityTrimmer) {
        this.qualityTrimmer = qualityTrimmer;
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

    public List<Library> getLibs() {
        return libs;
    }

    public void setLibs(List<Library> libs) {
        this.libs = libs;
    }

    public boolean isCreateConfigs() {
        return createConfigs;
    }

    public void setCreateConfigs(boolean createConfigs) {
        this.createConfigs = createConfigs;
    }

    public String getJobPrefix() {
        return jobPrefix;
    }

    public void setJobPrefix(String jobPrefix) {
        this.jobPrefix = jobPrefix;
    }

    public boolean isRunParallel() {
        return runParallel;
    }

    public void setRunParallel(boolean runParallel) {
        this.runParallel = runParallel;
    }

    public void parse(File configFile) throws IOException {
        QTArgs qtArgs = parseConfig(configFile);
        this.libs = qtArgs.getLibs();
        this.qualityTrimmer = qtArgs.getQualityTrimmer();
        this.minLen = qtArgs.getMinLen();
        this.minQual = qtArgs.getMinQual();
    }

    /**
     * Parses a RAMPART configuration file for QT specific information.  Note that only libs, qualityTrimmer, minLength and minQual
     * are set from the configuration file.  It does not make sense to populate: outputDir, jobPrefix and createConfigs
     * from a configuration file.  These settings will be set directly from the command line or by the host process.
     * @param config The RAMPART configuration file to parse
     * @return A QTArgs object populated with information from the configuration file.
     * @throws IOException Thrown if there were any problems reading the file.
     */
    public static QTArgs parseConfig(File config) throws IOException {

        RampartConfiguration rampartConfig = new RampartConfiguration();

        QTArgs args = new QTArgs();

        rampartConfig.load(config);
        args.setLibs(rampartConfig.getLibs());
        Profile.Section section = rampartConfig.getQtSettings();

        if (section != null) {
            for (Map.Entry<String, String> entry : section.entrySet()) {

                if (entry.getKey().equalsIgnoreCase(QT_TOOL)) {
                    args.setQualityTrimmer(entry.getValue());
                } else if (entry.getKey().equalsIgnoreCase(QT_MIN_LEN)) {
                    args.setMinLen(Integer.parseInt(entry.getValue()));
                } else if (entry.getKey().equalsIgnoreCase(QT_MIN_QUAL)) {
                    args.setMinQual(Integer.parseInt(entry.getValue()));
                }
            }
        }

        return args;
    }




    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();

        if (this.outputDir != null)
            pvp.put(params.getOutputDir(), this.outputDir.getAbsolutePath());

        pvp.put(params.getCreateConfigs(), Boolean.toString(this.createConfigs));
        pvp.put(params.getRunParallel(), Boolean.toString(this.runParallel));
        pvp.put(params.getMinLength(), Integer.toString(this.minLen));
        pvp.put(params.getMinQuality(), Integer.toString(this.minQual));


        if (this.jobPrefix != null) {
            pvp.put(params.getJobPrefix(), this.jobPrefix);
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

            if (param.equals(this.params.getRampartConfig().getName())) {
                try {
                    File newConfig = new File(entry.getValue());

                    if (newConfig != null && newConfig.exists()) {
                        this.parse(newConfig);
                    }

                    this.config = newConfig;
                } catch (IOException e) {
                    throw new IllegalArgumentException("Config file does not exist of could not be parsed");
                }
            } else if (param.equals(this.params.getOutputDir().getName())) {
                this.outputDir = new File(entry.getValue());
            } else if (param.equals(this.params.getCreateConfigs().getName())) {
                this.createConfigs = Boolean.parseBoolean(entry.getValue());
            } else if (param.equals(this.params.getJobPrefix().getName())) {
                this.jobPrefix = entry.getValue();
            } else if (param.equals(this.params.getRunParallel().getName())) {
                this.runParallel = Boolean.parseBoolean(entry.getValue());
            } else {
                throw new IllegalArgumentException("Unknown param found: " + param);
            }
        }
    }


    // ***** Construction methods *****

    public List<QualityTrimmer> createQualityTrimmers() {

        List<QualityTrimmer> qtList = new ArrayList<QualityTrimmer>();

        for (Library lib : this.getLibs()) {

            if (lib.testUsage(Library.Usage.QUALITY_TRIMMING)) {

                QualityTrimmer qt = QualityTrimmerFactory.create(this.getQualityTrimmer(), lib, this.getOutputDir());

                qt.getArgs().setMinLength(this.getMinLen());
                qt.getArgs().setQualityThreshold(this.getMinQual());

                qtList.add(qt);
            }
        }

        return qtList;
    }

    public List<Library> createQtLibs() {

        List<Library> libList = new ArrayList<Library>();

        for (Library lib : this.getLibs()) {

            if (lib.testUsage(Library.Usage.QUALITY_TRIMMING)) {

                QualityTrimmer qt = QualityTrimmerFactory.create(this.getQualityTrimmer(), lib, this.getOutputDir());

                Library qtLib = lib.copy();
                qtLib.setFilePaired1(new SeqFile(qt.getArgs().getPairedEndOutputFiles().getFile1()));
                qtLib.setFilePaired2(new SeqFile(qt.getArgs().getPairedEndOutputFiles().getFile2()));
                qtLib.setSeFile(new SeqFile(qt.getArgs().getSingleEndOutputFile()));

                libList.add(qtLib);
            }
        }

        return libList;
    }
}
