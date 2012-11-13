package uk.ac.tgac.rampart.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.bytecode.opencsv.CSVReader;

import com.itextpdf.text.DocumentException;

import uk.ac.tgac.rampart.data.ImproverStats;
import uk.ac.tgac.rampart.data.MassPlotFileStructure;
import uk.ac.tgac.rampart.data.MassStats;
import uk.ac.tgac.rampart.service.RampartJobService;
import uk.ac.tgac.rampart.service.PdfOperationsService;

@Service
public class RampartJobServiceImpl implements RampartJobService {
	
	@Autowired
	private PdfOperationsService pdfOperationsService;
	
	
	@Override
	public void seperatePlots(File in, File outDir) throws IOException, DocumentException {		
		MassPlotFileStructure mpfs = new MassPlotFileStructure(in, outDir);
		
		File plots = mpfs.getPlots();
		
		this.pdfOperationsService.extractPage(plots, mpfs.getMassPlotNbContigsFile(), 1);
		this.pdfOperationsService.extractPage(plots, mpfs.getMassPlotNPcFile(), 6);
		this.pdfOperationsService.extractPage(plots, mpfs.getMassPlotNbBasesFile(), 7);
		this.pdfOperationsService.extractPage(plots, mpfs.getMassPlotAvgLenFile(), 10);
		this.pdfOperationsService.extractPage(plots, mpfs.getMassPlotMaxLenFile(), 9);
		this.pdfOperationsService.extractPage(plots, mpfs.getMassPlotN50File(), 11);
	}

	
	protected List<String[]> getStats(File in) throws IOException {
		CSVReader reader = new CSVReader(new FileReader(in), '|');
		List<String[]> stats = reader.readAll();
		reader.close();
		return stats;
	}

	@Override
	public List<MassStats> getMassStats(File in) throws IOException {
		
		List<String[]> stats = getStats(in);
		List<MassStats> massStats = new ArrayList<MassStats>();
		
		for(String[] rawStats : stats.subList(1, stats.size()-1)) {
			massStats.add(new MassStats(rawStats));
		}
		
		return massStats;
	}
	
	@Override
	public List<ImproverStats> getImproverStats(File in) throws IOException {
		List<String[]> stats = getStats(in);
		List<ImproverStats> improverStats = new ArrayList<ImproverStats>();
		
		for(String[] rawStats : stats.subList(1, stats.size()-1)) {
			improverStats.add(new ImproverStats(rawStats));
		}
		
		return improverStats;
	}

}
