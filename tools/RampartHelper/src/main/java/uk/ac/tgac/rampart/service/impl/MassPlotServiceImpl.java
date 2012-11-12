package uk.ac.tgac.rampart.service.impl;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.text.DocumentException;

import uk.ac.tgac.rampart.service.MassPlotService;
import uk.ac.tgac.rampart.service.PdfOperationsService;

@Service
public class MassPlotServiceImpl implements MassPlotService {

	private static final String FILENAME_NB_CONTIGS = "Mass_NBC.pdf";	
	private static final String FILENAME_NB_BASES 	= "Mass_TB.pdf";
	private static final String FILENAME_N_PERC 	= "Mass_N.pdf";
	private static final String FILENAME_AVG_LEN 	= "Mass_AL.pdf";
	private static final String FILENAME_MAX_LEN 	= "Mass_ML.pdf";
	private static final String FILENAME_N50 		= "Mass_N50.pdf";
	
	private File plots;
	private File outputDir;
	
	private File massPlotNbContigsFile;
	private File massPlotNbBasesFile;
	private File massPlotNPcFile;
	private File massPlotAvgLenFile;
	private File massPlotMaxLenFile;
	private File massPlotN50File;
	
	private PdfOperationsService pdfOperationsService;
	
	@Autowired
	public void setPdfOperationsService(PdfOperationsService pdfOperationsService) {
		this.pdfOperationsService = pdfOperationsService;
	}
	
	
	public MassPlotServiceImpl(File plots, File outputDir) {
		this.plots = plots;
		this.outputDir = outputDir;
		
		setupFiles();
	}
	
	protected void setupFiles() {
		this.massPlotNbContigsFile = new File(this.outputDir.getPath() + "/" + FILENAME_NB_CONTIGS);
		this.massPlotNbBasesFile = new File(this.outputDir.getPath() + "/" + FILENAME_NB_BASES);
		this.massPlotNPcFile = new File(this.outputDir.getPath() + "/" + FILENAME_N_PERC);
		this.massPlotAvgLenFile = new File(this.outputDir.getPath() + "/" + FILENAME_AVG_LEN);
		this.massPlotMaxLenFile = new File(this.outputDir.getPath() + "/" + FILENAME_MAX_LEN);
		this.massPlotN50File = new File(this.outputDir.getPath() + "/" + FILENAME_N50);
	}

	public File getPlots() {
		return plots;
	}
	
	public File getOutputDir() {
		return outputDir;
	}

	public File getMassPlotNbContigsFile() {
		return massPlotNbContigsFile;
	}

	public File getMassPlotNbBasesFile() {
		return massPlotNbBasesFile;
	}

	public File getMassPlotNPcFile() {
		return massPlotNPcFile;
	}

	public File getMassPlotAvgLenFile() {
		return massPlotAvgLenFile;
	}

	public File getMassPlotMaxLenFile() {
		return massPlotMaxLenFile;
	}

	public File getMassPlotN50File() {
		return massPlotN50File;
	}
	
	public void seperatePlots() throws IOException, DocumentException {
		
		this.pdfOperationsService.extractPage(this.plots, this.massPlotNbContigsFile, 1);
		this.pdfOperationsService.extractPage(this.plots, this.massPlotNPcFile, 6);
		this.pdfOperationsService.extractPage(this.plots, this.massPlotNbBasesFile, 7);
		this.pdfOperationsService.extractPage(this.plots, this.massPlotAvgLenFile, 10);
		this.pdfOperationsService.extractPage(this.plots, this.massPlotMaxLenFile, 9);
		this.pdfOperationsService.extractPage(this.plots, this.massPlotN50File, 11);
	}
	
	
	
	@Override
	public void seperatePlots(File in, File outDir) throws IOException, DocumentException {		
		MassPlotServiceImpl mps = new MassPlotServiceImpl(in, outDir);
		mps.seperatePlots();
	}

}
