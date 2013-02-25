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
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExternalProcessConfiguration;
import uk.ac.ebi.fgpt.conan.core.context.locality.Local;
import uk.ac.ebi.fgpt.conan.core.context.locality.LocalityFactory;
import uk.ac.ebi.fgpt.conan.core.context.scheduler.SchedulerFactory;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExternalProcessConfiguration;
import uk.ac.ebi.fgpt.conan.model.context.Locality;
import uk.ac.ebi.fgpt.conan.model.context.Scheduler;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.pipeline.spring.RampartAppContext;
import uk.ac.tgac.rampart.pipeline.tool.Rampart;

import java.io.File;
import java.io.IOException;


public class RampartCLI {

    private static Logger log = LoggerFactory.getLogger(RampartCLI.class);


    private static RampartOptions processCmdLine(String[] args) throws ParseException {

        // Create the available options
        Options options = RampartOptions.createOptions();

        // Parse the actual arguments
        CommandLineParser parser = new PosixParser();

        // parse the command line arguments
        CommandLine line = parser.parse(options, args);
        RampartOptions rampartOptions = new RampartOptions(line);


        return rampartOptions;
    }

    private static Rampart configureSystem(RampartOptions rampartOptions) throws IOException {

        // Environment specific configuration options are set in the user's home directory
        final String rampartSettingsDir = System.getProperty("user.home") + "/.rampart/";

        // Setup logging first
        File loggingProperties = new File(rampartSettingsDir, "log4j.properties");

        // If logging file exists use settings from that, otherwise use basic settings.
        if (loggingProperties.exists()) {
            PropertyConfigurator.configure(loggingProperties.getAbsolutePath());
        }
        else {
            BasicConfigurator.configure();
        }

        // Load spring
        RampartAppContext.INSTANCE.load("applicationContext.xml");

        // Load Conan properties
        final File conanPropsFile = new File(rampartSettingsDir + "conan.properties");
        if (conanPropsFile.exists()) {
            ConanProperties.getConanProperties().setPropertiesFile(conanPropsFile);
        }

        // Get external process pre-commands
        ExternalProcessConfiguration externalProcessConfiguration = new DefaultExternalProcessConfiguration();
        if (ConanProperties.containsKey("externalProcessConfigFile")) {
            externalProcessConfiguration.setProcessConfigFilePath(ConanProperties.getProperty("externalProcessConfigFile"));
            externalProcessConfiguration.load();
        }

        // Get execution context
        Locality locality = ConanProperties.containsKey("executionContext.locality") ?
                LocalityFactory.createLocality(ConanProperties.getProperty("executionContext.locality")) :
                new Local();

        Scheduler scheduler = ConanProperties.containsKey("executionContext.scheduler") ?
                SchedulerFactory.createScheduler(ConanProperties.getProperty("executionContext.scheduler")) :
                null;

        ExecutionContext executionContext = new DefaultExecutionContext(locality, scheduler, externalProcessConfiguration, true);


        // Get RAMPART bean from Spring and configure with user defined properties
        Rampart rampart = (Rampart)RampartAppContext.INSTANCE.getApplicationContext().getBean("rampart");
        rampart.setOptions(rampartOptions);
        rampart.setExecutionContext(executionContext);

        return rampart;
    }


    /**
     * @param args
     */
    public static void main(String[] args) {

        try {

            // Process the command line
            RampartOptions rampartOptions = processCmdLine(args);

            // If help was requested output that and finish before starting Spring
            if (rampartOptions.doHelp()) {
                rampartOptions.printUsage();
                System.exit(0);
            }

            // Create the fully configured RAMPART object
            Rampart rampart = configureSystem(rampartOptions);

            log.info("RAMPART: Started");

            // Run RAMPART
            rampart.process();

            log.info("RAMPART: Finished");
        }
        catch (ParseException exp) {
            System.err.println(exp.getMessage());
            System.err.println(StringUtils.join(exp.getStackTrace(), "\n"));
            System.exit(1);
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
    }

}
