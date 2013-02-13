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

import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.pipeline.tool.Rampart;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;


public class RampartCLI {

    private static Logger log = LoggerFactory.getLogger(RampartCLI.class);

    private ApplicationContext context;

    public RampartCLI() throws IOException {


    }


    /**
     * @param args
     * @throws URISyntaxException
     */
    public static void main(String[] args) throws URISyntaxException {

        // Create the available options
        Options options = RampartOptions.createOptions();

        // Parse the actual arguments
        CommandLineParser parser = new PosixParser();
        RampartOptions rampartOptions = null;
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            rampartOptions = new RampartOptions(line);

            // If help was requested output that and finish before starting Spring
            if (rampartOptions.doHelp()) {
                rampartOptions.printUsage();
                System.exit(0);
            }
        }
        catch (ParseException exp) {
            System.err.println(exp.getMessage());
            System.err.println(StringUtils.join(exp.getStackTrace(), "\n"));
            System.exit(1);
        }


        // Before we run RAMPART we should initialise SPRING and any other essential systems
        ApplicationContext context = null;
        try {
            final String rampartSettingsDir = System.getProperty("user.home") + "/.rampart/";

            final File loggingPropsFile = new File(rampartSettingsDir + "log4j.properties");

            if (loggingPropsFile.exists()) {
                Properties loggingProps = new Properties();
                loggingProps.load(new FileInputStream(loggingPropsFile));
                PropertyConfigurator.configure(loggingProps);
            } else {
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

            context = new ClassPathXmlApplicationContext("applicationContext.xml");
        }
        catch(IOException e) {
            System.err.println(e.getMessage());
            System.err.println(StringUtils.join(e.getStackTrace(), "\n"));
            System.exit(2);
        }

        Rampart rampart = (Rampart)context.getBean("rampart");
        rampart.setOptions(rampartOptions);


        // Run RAMPART
        try {
            rampart.process();
        }
        catch (ProcessExecutionException pee) {
            System.exit(3);
        }
        catch (InterruptedException ie) {
            log.error(ie.getMessage(), ie);
            System.exit(4);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            System.exit(6);
        }

        log.info("Finished RAMPART");
    }

}
