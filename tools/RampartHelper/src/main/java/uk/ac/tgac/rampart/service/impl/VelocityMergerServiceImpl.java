package uk.ac.tgac.rampart.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.stereotype.Service;

import uk.ac.tgac.rampart.service.VelocityMergerService;

@Service
public class VelocityMergerServiceImpl implements VelocityMergerService {

	private static Logger log = Logger.getLogger(VelocityMergerServiceImpl.class.getName());
	
	
	@Override
	public void merge(File template, File context, File output) throws IOException {
		VelocityContext vc = loadContext(context);
		this.merge(template, vc, output);
	}

	@Override
	public void merge(File template, VelocityContext context, File output) throws IOException {
		
		log.debug("Loading template data");
		
		// Load the template
	    String template_data = FileUtils.readFileToString(template);
	    
	    // Create the output writer
	    StringWriter writer = new StringWriter();
	    
	    log.debug("Merging template and context");
	    
	    // Create Velocity properties
	    Properties vp = new Properties();
	    vp.setProperty("resource.loader", "class");
	    vp.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
	    vp.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
	    vp.setProperty("runtime.log.logsystem.log4j.logger", VelocityMergerServiceImpl.class.getName());
	    
	    // Merge the template and the context
	    VelocityEngine ve = new VelocityEngine(vp);
	    ve.init();
	    ve.evaluate(context, writer, "LaTeX Report Builder", template_data);
	    writer.flush();
	    writer.close();
	    
	    log.debug("Writing merged file to disk");
	    
	    // Output to file 
	    FileUtils.writeStringToFile(output, writer.toString() );

	    
	    log.debug("Velocity template and context merged and saved successfully");
	}
	
	
	
	
	protected VelocityContext loadContext(File context_path) throws IOException {
		
		log.debug("Loading velocity context from ini-style file");
		
		List<String> context_lines = FileUtils.readLines(context_path);
	    VelocityContext vc = new VelocityContext();
	    for(String context_line : context_lines) {
	    	
	    	if (context_line == null || context_line.trim().isEmpty()) {
	    		continue;
	    	}
	    	
	    	String[] parts = context_line.split("=");
	    	vc.put(parts[0], parts[1]);
	    }
	    
	    log.debug("Velocity context loaded successfully from ini-style file");
			    
	    return vc;
	}
	
}
