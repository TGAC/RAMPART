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
package uk.ac.tgac.rampart.pipeline.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;


public class ToolCommandLoader {


    // Keys used for each tool used by rampart
    public static final String PERL_5_16_1 = "perl-5.16.1";
    public static final String R_2_12_2 = "r-2.12.2";
    public static final String JRE_6 = "jre-6";
    public static final String TEXLIVE_2012 = "texlive-2012";
    public static final String FASTX_0_0_13 = "fastx-0.0.13";
    public static final String SICKLE_1_1 = "sickle-1.1";
    public static final String EXONERATE_2_2_0 = "exonerate-2.2.0";
    public static final String ABYSS_1_3_4 = "abyss-1.3.4";
    public static final String SSPACE_BASIC_2_0 = "sspace-basic-2.0";
    public static final String GAPCLOSER_1_12 = "gapcloser-1.12";


    private Properties loadToolCommands = new Properties();

    private static ToolCommandLoader instance = new ToolCommandLoader();

    public static ToolCommandLoader getInstance() {
        return instance;
    }

    public void loadPropertiesFile(String propertiesFile) throws IOException {

        if (new File(propertiesFile).exists()) {
            loadToolCommands.load(new InputStreamReader(new FileInputStream(propertiesFile)));
        } else {
            loadToolCommands.load(ClassLoader.getSystemResourceAsStream(propertiesFile));
        }
    }

    public String getLoadToolCommand(String toolKey) {
        return loadToolCommands.getProperty(toolKey);
    }
}
