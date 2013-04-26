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

import org.ini4j.Profile;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.process.ec.ErrorCorrector;
import uk.ac.tgac.conan.process.ec.ErrorCorrectorArgs;
import uk.ac.tgac.conan.process.ec.ErrorCorrectorFactory;
import uk.ac.tgac.rampart.data.RampartConfiguration;

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
public class MecqArgs implements ProcessArgs {

    // Config file property keys
    public static final String QT_TOOL = "tool";
    public static final String QT_MIN_LEN = "minlen";
    public static final String QT_MIN_QUAL = "minqual";
    public static final String QT_KMER = "kmer";
    public static final String QT_THREADS = "threads";
    public static final String QT_MEMORY_GB = "memory";
    public static final String QT_NO_QT = "no_qt";

    // Default values
    public static final int DEFAULT_MIN_LEN = 60;
    public static final int DEFAULT_MIN_QUAL = 30;
    public static final int DEFAULT_KMER = 17;
    public static final int DEFAULT_THREADS = 8;
    public static final int DEFUALT_MEMORY_GB = 20;


    private MecqParams params = new MecqParams();

    private File config;
    private File outputDir;
    private List<String> ecqs;

    private int minLen;
    private int minQual;
    private int kmer;
    private int threads;
    private int memoryGb;


    private List<Library> libs;
    private boolean createConfigs;
    private String jobPrefix;
    private boolean runParallel;
    private boolean noQT;

    public MecqArgs() {
        this.config = null;
        this.outputDir = new File("");
        this.ecqs = new ArrayList<String>();
        this.minLen = DEFAULT_MIN_LEN;
        this.minQual = DEFAULT_MIN_QUAL;
        this.kmer = DEFAULT_KMER;
        this.threads = DEFAULT_THREADS;
        this.memoryGb = DEFUALT_MEMORY_GB;
        this.libs = null;
        this.createConfigs = false;
        this.runParallel = false;
        this.noQT = false;

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

    public List<String> getEcqs() {
        return ecqs;
    }

    public void setEcqs(List<String> ecqs) {
        this.ecqs = ecqs;
    }

    public void addEcq(String ecq) {
        this.ecqs.add(ecq);
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

    public boolean isNoQT() {
        return noQT;
    }

    public void setNoQT(boolean noQT) {
        this.noQT = noQT;
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

    public int getMemoryGb() {
        return memoryGb;
    }

    public void setMemoryGb(int memoryGb) {
        this.memoryGb = memoryGb;
    }

    public void parse(File configFile) throws IOException {
        MecqArgs mecqArgs = parseConfig(configFile);
        this.libs = mecqArgs.getLibs();
        this.ecqs = mecqArgs.getEcqs();
        this.minLen = mecqArgs.getMinLen();
        this.minQual = mecqArgs.getMinQual();
        this.config = configFile;
        this.noQT = mecqArgs.isNoQT();
    }

    /**
     * Parses a RAMPART configuration file for QT specific information.  Note that only libs, qualityTrimmer, minLength and minQual
     * are set from the configuration file.  It does not make sense to populate: outputDir, jobPrefix and createConfigs
     * from a configuration file.  These settings will be set directly from the command line or by the host process.
     * @param config The RAMPART configuration file to parse
     * @return A MecqArgs object populated with information from the configuration file.
     * @throws IOException Thrown if there were any problems reading the file.
     */
    public static MecqArgs parseConfig(File config) throws IOException {

        RampartConfiguration rampartConfig = new RampartConfiguration();

        MecqArgs args = new MecqArgs();

        rampartConfig.load(config);
        args.setLibs(rampartConfig.getLibs());
        Profile.Section section = rampartConfig.getMecqSettings();

        if (section != null) {
            for (Map.Entry<String, String> entry : section.entrySet()) {

                if (entry.getKey().equalsIgnoreCase(QT_MIN_LEN)) {
                    args.setMinLen(Integer.parseInt(entry.getValue()));
                } else if (entry.getKey().equalsIgnoreCase(QT_MIN_QUAL)) {
                    args.setMinQual(Integer.parseInt(entry.getValue()));
                } else if (entry.getKey().equalsIgnoreCase(QT_NO_QT)) {
                    args.setNoQT(Boolean.parseBoolean(entry.getValue()));
                } else if (entry.getKey().equalsIgnoreCase(QT_KMER)) {
                    args.setKmer(Integer.parseInt(entry.getValue()));
                } else if (entry.getKey().equalsIgnoreCase(QT_THREADS)) {
                    args.setThreads(Integer.parseInt(entry.getValue()));
                } else if (entry.getKey().equalsIgnoreCase(QT_MEMORY_GB)) {
                    args.setMemoryGb(Integer.parseInt(entry.getValue()));
                } else {
                    int index = Integer.parseInt(entry.getKey());

                    args.addEcq(entry.getValue());
                }
            }
        }

        args.config = config;

        return args;
    }


    @Override
    public void parse(String args) {
        //To change body of implemented methods use File | Settings | File Templates.
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
            } else if (param.equals(this.params.getMinLength().getName())) {
                this.minLen = Integer.parseInt(entry.getValue());
            } else if (param.equals(this.params.getMinQuality().getName())) {
                this.minQual = Integer.parseInt(entry.getValue());
            } else if (param.equals(this.params.getKmer().getName())) {
                this.kmer = Integer.parseInt(entry.getValue());
            } else if (param.equals(this.params.getThreads().getName())) {
                this.threads = Integer.parseInt(entry.getValue());
            } else if (param.equals(this.params.getMemoryGb().getName())) {
                this.memoryGb = Integer.parseInt(entry.getValue());
            } else if (param.equals(this.params.getRunParallel().getName())) {
                this.runParallel = Boolean.parseBoolean(entry.getValue());
            } else {
                throw new IllegalArgumentException("Unknown param found: " + param);
            }
        }
    }


    // ***** Construction methods *****

    public List<ErrorCorrector> createMecqs() {

        List<ErrorCorrector> ecqList = new ArrayList<ErrorCorrector>();

        for(String mecq : this.ecqs) {

            ErrorCorrector ec = ErrorCorrectorFactory.valueOf(mecq).create();
            ErrorCorrectorArgs args = ec.getArgs();

            args.setMinLength(this.getMinLen());
            args.setQualityThreshold(this.getMinQual());
            args.setKmer(this.getKmer());
            args.setThreads(this.getThreads());
            args.setMemoryGb(this.getMemoryGb());

            ecqList.add(ec);
        }

        return ecqList;
    }

    public List<Library> getLibsToProcess() {

        List<Library> libsToProcess = new ArrayList<Library>();

        for(Library lib : this.libs) {
           if (lib.testUsage(Library.Usage.QUALITY_TRIMMING)) {
               libsToProcess.add(lib);
           }
        }

        return libsToProcess;
    }
}
