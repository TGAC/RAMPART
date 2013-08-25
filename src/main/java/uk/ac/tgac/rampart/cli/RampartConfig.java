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
package uk.ac.tgac.rampart.cli;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
import uk.ac.tgac.conan.TgacConanConfigure;
import uk.ac.tgac.conan.core.util.JarUtils;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

/**
 * User: maplesod
 * Date: 25/04/13
 * Time: 19:09
 */
public class RampartConfig {

    private static Logger log = LoggerFactory.getLogger(RampartConfig.class);

    // Environment specific configuration options and resource files are set in the user's home directory
    public static final File SETTINGS_DIR = new File(System.getProperty("user.home") + "/.tgac/rampart/");

    public static final File DATA_DIR = new File(SETTINGS_DIR, "data");
    public static final File REPORT_DIR = new File(DATA_DIR, "report");
    public static final File SCRIPTS_DIR = new File(SETTINGS_DIR, "scripts");

    public static final File DEFAULT_ENV_CONFIG = new File(SETTINGS_DIR, "conan.properties");
    public static final File DEFAULT_LOG_CONFIG = new File(SETTINGS_DIR, "log4j.properties");

    public static void configureSystem(RampartOptions rampartOptions) throws IOException {

        if (!SETTINGS_DIR.exists()) {
            if (!SETTINGS_DIR.mkdirs()) {
                throw new IOException("Could not create RAMPART settings directory in: " + SETTINGS_DIR.getAbsolutePath());
            };
        }

        // Setup logging first
        File loggingProperties = rampartOptions.getLogConfig();

        // If logging file exists use settings from that, otherwise use basic settings.
        if (loggingProperties.exists()) {
            PropertyConfigurator.configure(loggingProperties.getAbsolutePath());
            log.debug("Using user specified logging properties: " + loggingProperties.getAbsolutePath());
        }
        else {
            BasicConfigurator.configure();
            log.debug("No log4j properties file found.  Using default logging properties.");
        }

        // Load spring
        //RampartAppContext.INSTANCE.load("/applicationContext.xml");

        // Load Conan properties
        final File conanPropsFile = rampartOptions.getEnvironmentConfig();
        if (conanPropsFile.exists()) {
            ConanProperties.getConanProperties().setPropertiesFile(conanPropsFile);
        }

        // Copy resources to external system

        File internalScripts = FileUtils.toFile(RampartConfig.class.getResource("/scripts"));
        File internalData = FileUtils.toFile(RampartConfig.class.getResource("/data"));

        File externalScriptsDir = new File(SETTINGS_DIR, "scripts");
        File externalDataDir = new File(SETTINGS_DIR, "data");

        JarFile thisJar = JarUtils.jarForClass(RampartConfig.class, null);

        if (thisJar == null) {

            log.debug("Copying resources to settings directory.");
            FileUtils.copyDirectory(internalScripts, externalScriptsDir);
            FileUtils.copyDirectory(internalData, externalDataDir);
        }
        else {

            log.debug("Executing from JAR.  Copying resources to settings directory.");
            JarUtils.copyResourcesToDirectory(thisJar, "scripts", externalScriptsDir.getAbsolutePath());
            JarUtils.copyResourcesToDirectory(thisJar, "data", externalDataDir.getAbsolutePath());
        }

        // Intialise TGAC Conan
        TgacConanConfigure.initialise();
    }

    public static File currentWorkingDir() {
        return new File(".").getAbsoluteFile().getParentFile();
    }
}
