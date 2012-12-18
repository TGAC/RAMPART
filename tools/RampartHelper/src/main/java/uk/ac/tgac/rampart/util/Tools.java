package uk.ac.tgac.rampart.util;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

public class Tools {

	 public static String getStackTrace(Throwable t)
	  {
	    StringBuilder sb = new StringBuilder();
	    
	    for(StackTraceElement e : t.getStackTrace())
	    {
	      sb.append( e.toString() ).append( String.valueOf(LINE_SEPARATOR) );
	    }
	    
	    return sb.toString();
	  }
	 
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
