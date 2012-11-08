package uk.ac.tgac.rampart.service;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;


public class VelocityMerger {

	private File template;
	private File output;
	
	public VelocityMerger (File template, File output) throws IOException {
		
		if (!template.exists() || !template.canRead()) {
			throw new IOException("Can't access template file: " + template.toString());
		}
		
		this.template = template;
		this.output = output;
	}
	
	
	private VelocityContext loadContext(File context_path) throws IOException {
		List<String> context_lines = FileUtils.readLines(context_path);
	    VelocityContext vc = new VelocityContext();
	    for(String context_line : context_lines) {
	    	
	    	if (context_line == null || context_line.trim().isEmpty()) {
	    		continue;
	    	}
	    	
	    	String[] parts = context_line.split("=");
	    	vc.put(parts[0], parts[1]);
	    }
	    
	    return vc;
	}
	
	
	public void process(File context) throws IOException {
		VelocityContext vc = loadContext(context);
		process(vc);
	}
	
	public void process(VelocityContext context) throws IOException {
		
		// Load the template
	    String template_data = FileUtils.readFileToString(this.template);
	    
	    // Create the output writer
	    StringWriter writer = new StringWriter();
	    
	    // Merge the template and the context
	    Velocity.evaluate(context, writer, "LaTeX Report Builder", template_data);
	    
	    // Output to file 
	    FileUtils.writeStringToFile(this.output, writer.toString() );
	}
	
	public static void process(File template, File context, File output) throws IOException {
		VelocityMerger lrb = new VelocityMerger(template, output);
		lrb.process(context);
	}
	
	public static void process(File template, VelocityContext context, File output) throws IOException {
		VelocityMerger lrb = new VelocityMerger(template, output);
		lrb.process(context);
	}
}
