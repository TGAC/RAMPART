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
package uk.ac.tgac.rampart.conan.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

public class Configure {

	public static void configureProperties() throws IOException {
		
		final String rampartSettingsDir = System.getProperty("user.home") + "/.rampart/";
		
		final File loggingPropsFile = new File(rampartSettingsDir + "logging.properties");

		if (loggingPropsFile.exists()) {
			Properties loggingProps = new Properties();
			loggingProps.load(new FileInputStream(loggingPropsFile));
			PropertyConfigurator.configure(loggingProps);
		}
		else {
			BasicConfigurator.configure();
		}
		
		final File conanPropsFile = new File(rampartSettingsDir + "conan.properties");
		if (conanPropsFile.exists()) {
			ConanProperties.getConanProperties().setPropertiesFile(conanPropsFile);
		}
		
		final File toolPropsFile = new File(rampartSettingsDir + "load_tool_commands.properties");
		if (toolPropsFile.exists()) {
			ToolCommandLoader.getInstance().loadPropertiesFile(toolPropsFile.getPath());
		}
	}
}
