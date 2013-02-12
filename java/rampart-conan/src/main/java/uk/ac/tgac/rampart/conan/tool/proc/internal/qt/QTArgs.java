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
package uk.ac.tgac.rampart.conan.tool.proc.internal.qt;

import org.ini4j.Profile;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.param.ProcessArgs;
import uk.ac.tgac.rampart.conan.tool.proc.external.qt.QualityTrimmer;
import uk.ac.tgac.rampart.conan.tool.proc.external.qt.QualityTrimmerFactory;
import uk.ac.tgac.rampart.core.data.Library;
import uk.ac.tgac.rampart.core.data.RampartConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private File outputDir;
    private String tool;
    private int minLen;
    private int minQual;
    private List<Library> libs;

    private File config;

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
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

    public List<Library> getLibs() {
        return libs;
    }

    public void setLibs(List<Library> libs) {
        this.libs = libs;
    }

    public File getConfig() {
        return config;
    }

    public void setConfig(File config) {
        this.config = config;
    }

    public void parseConfig() throws IOException {
        parseConfig(this.config);
    }

    public static QTArgs parseConfig(File config) throws IOException {

        RampartConfiguration rampartConfig = new RampartConfiguration();

        QTArgs args = new QTArgs();

        rampartConfig.load(config);
        args.setLibs(rampartConfig.getLibs());
        Profile.Section section = rampartConfig.getQtSettings();

        if (section != null) {
            for (Map.Entry<String, String> entry : section.entrySet()) {

                if (entry.getKey().equalsIgnoreCase(QT_TOOL)) {
                    args.setTool(entry.getValue());
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

        if (this.config != null)
            pvp.put(params.getRampartConfig(), this.config.getAbsolutePath());

        if (this.outputDir != null)
            pvp.put(params.getOutputDir(), this.outputDir.getAbsolutePath());

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
                this.config = new File(entry.getValue());
            } else if (param.equals(this.params.getOutputDir().getName())) {
                this.outputDir = new File(entry.getValue());
            } else {
                throw new IllegalArgumentException("Unknown param found: " + param);
            }
        }

        try {
            this.parseConfig();
        } catch (IOException e) {
            throw new IllegalArgumentException("Config file does not exist of could not be parsed");
        }
    }

    public RampartConfiguration createRampartConfiguration() {

        RampartConfiguration config = new RampartConfiguration();
        config.getQtSettings();

        // TODO set all the relevant vars here.

        return config;
    }

    // ***** Construction methods *****

    public List<QualityTrimmer> createQualityTrimmers() {

        List<QualityTrimmer> qtList = new ArrayList<QualityTrimmer>();

        for (Library lib : this.getLibs()) {

            if (lib.testUsage(Library.Usage.QUALITY_TRIMMING)) {

                QualityTrimmer qt = QualityTrimmerFactory.create(this.getTool(), lib, this.getOutputDir());

                qt.getArgs().setMinLength(this.getMinLen());
                qt.getArgs().setQualityThreshold(this.getMinQual());

                qtList.add(qt);
            }
        }

        return qtList;
    }
}
