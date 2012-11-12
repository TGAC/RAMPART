package uk.ac.tgac.rampart.service;

import java.io.File;
import java.io.IOException;

import com.itextpdf.text.DocumentException;

public interface MassPlotService {

	void seperatePlots(File in, File outDir) throws IOException, DocumentException;
	
}
