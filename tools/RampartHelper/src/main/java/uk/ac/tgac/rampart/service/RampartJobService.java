package uk.ac.tgac.rampart.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.velocity.VelocityContext;

import uk.ac.tgac.rampart.data.ImproverStats;
import uk.ac.tgac.rampart.data.Library;
import uk.ac.tgac.rampart.data.MassStats;
import uk.ac.tgac.rampart.data.RampartConfiguration;
import uk.ac.tgac.rampart.data.SeqFile;

import com.itextpdf.text.DocumentException;

public interface RampartJobService {

	void seperatePlots(File in, File outDir) throws IOException, DocumentException;
	
	RampartConfiguration getRampartConfiguration(File in) throws IOException;
	
	List<MassStats> getMassStats(File in) throws IOException;
	
	List<ImproverStats> getImproverStats(File in) throws IOException;
	
	List<SeqFile> calcReadStats(List<Library> libs) throws IOException;
	
	VelocityContext buildContext(File jobDir) throws IOException;
	
	void persistContext(VelocityContext context);
}
