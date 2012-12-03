package uk.ac.tgac.rampart.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.velocity.VelocityContext;

import uk.ac.tgac.rampart.data.ImproverStats;
import uk.ac.tgac.rampart.data.Library;
import uk.ac.tgac.rampart.data.MassStats;
import uk.ac.tgac.rampart.data.RampartConfiguration;
import uk.ac.tgac.rampart.data.RampartJobFileStructure;
import uk.ac.tgac.rampart.data.RampartSettings;

import com.itextpdf.text.DocumentException;

public interface RampartJobService {

	void seperatePlots(File in, File outDir, String filenamePrefix) throws IOException, DocumentException;
	
	RampartConfiguration loadConfiguration(File in) throws IOException;
	
	List<MassStats> getMassStats(File in) throws IOException;
	
	List<ImproverStats> getImproverStats(File in) throws IOException;
	
	void calcReadStats(List<Library> libs) throws IOException;
	
	VelocityContext buildContext(File jobDir) throws IOException;
	
	RampartSettings determineSettings(RampartJobFileStructure job) throws IOException;
	
	void persistContext(final VelocityContext context);
}
