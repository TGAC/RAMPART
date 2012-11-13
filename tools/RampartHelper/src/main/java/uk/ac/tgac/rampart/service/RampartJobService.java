package uk.ac.tgac.rampart.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import uk.ac.tgac.rampart.data.ImproverStats;
import uk.ac.tgac.rampart.data.MassStats;

import com.itextpdf.text.DocumentException;

public interface RampartJobService {

	void seperatePlots(File in, File outDir) throws IOException, DocumentException;
	
	List<MassStats> getMassStats(File in) throws IOException;
	
	List<ImproverStats> getImproverStats(File in) throws IOException;
}
