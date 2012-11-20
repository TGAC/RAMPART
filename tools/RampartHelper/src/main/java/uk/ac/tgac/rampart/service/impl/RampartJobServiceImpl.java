package uk.ac.tgac.rampart.service.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import uk.ac.tgac.rampart.dao.AssemblyStatsDao;
import uk.ac.tgac.rampart.dao.JobDao;
import uk.ac.tgac.rampart.data.ImproverStats;
import uk.ac.tgac.rampart.data.Job;
import uk.ac.tgac.rampart.data.Library;
import uk.ac.tgac.rampart.data.MassPlotFileStructure;
import uk.ac.tgac.rampart.data.MassStats;
import uk.ac.tgac.rampart.data.RampartConfiguration;
import uk.ac.tgac.rampart.data.RampartJobFileStructure;
import uk.ac.tgac.rampart.service.PdfOperationsService;
import uk.ac.tgac.rampart.service.RampartJobService;
import uk.ac.tgac.rampart.service.SequenceStatisticsService;
import au.com.bytecode.opencsv.CSVReader;

import com.itextpdf.text.DocumentException;

@Service
public class RampartJobServiceImpl implements RampartJobService {
	
	private static Logger log = Logger.getLogger(RampartJobServiceImpl.class.getName());
	
	// Services
	@Autowired private PdfOperationsService pdfOperationsService;
	@Autowired private SequenceStatisticsService sequenceStatisticsService;
	
	// DAOs
	@Autowired private JobDao jobDao;
	@Autowired private AssemblyStatsDao assemblyStatsDao;
	
	
	
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
	
	@Override
	public VelocityContext buildContext(File jobDir) throws IOException {

		RampartJobFileStructure jobFS = new RampartJobFileStructure(jobDir);
		
		// Get Configuration info from config files
		Job job = this.getRampartConfiguration(jobFS.getConfigFile()).getJobs();
		List<Library> libsRaw = this.getRampartConfiguration(jobFS.getConfigRawFile()).getLibs();
		List<Library> libsQt = this.getRampartConfiguration(jobFS.getConfigQtFile()).getLibs();
		
		// Analyse Read files and generate statistics for them.  Stats objects are already linked
		// to the seq files in the libs.
		calcReadStats(libsRaw);
		calcReadStats(libsQt);
		
		// Add libs to job object
		job.setLibsRaw(libsRaw);
		job.setLibsQt(libsQt);
		
		// Get Info from Assemblies (MASS)
		List<MassStats> massStats = this.getMassStats(jobFS.getMassStatsFile());
		List<ImproverStats> improverStats = this.getImproverStats(jobFS.getImproverStatsFile());
		
		// Get Info from Best Assembly statistics
		MassStats best = Collections.max(massStats);
		best.setBest(true);
		
		job.setMassStats(massStats);
		job.setImproverStats(improverStats);
		

		// Get the final assembly statistics
		ImproverStats finalAssembly = improverStats.get(improverStats.size() - 1);
		finalAssembly.setFinalAssembly(Boolean.TRUE);
		
		// Get final scaffold locations
		String finalAssemblyPath = finalAssembly.getFilePath();
		
		// Getting the structure of this is important for both report building and data persistence
		// Build context
		VelocityContext vc = new VelocityContext();
		vc.put("job", job);
		vc.put("fpa_path", finalAssemblyPath);
		
		return vc;
	}
	


	
	@Override
	public void calcReadStats(List<Library> libs) throws IOException {
		
		// This can be long process so log it
		log.info("Starting analysis of input library files");
		
		StopWatch stopWatch = new StopWatch("Input Library Analysis");
		
		for (Library l : libs) {
			
			stopWatch.start(l.getName() + " : " + l.getFilePaired1());
			this.sequenceStatisticsService.analyse(l.getFilePaired1());
			stopWatch.stop();
			
			stopWatch.start(l.getName() + " : " + l.getFilePaired2());
			this.sequenceStatisticsService.analyse(l.getFilePaired2());
			stopWatch.stop();
			
			if (l.getSeFile() != null && !l.getSeFile().getFilePath().isEmpty()) {
				stopWatch.start(l.getName() + " : " + l.getSeFile());
				this.sequenceStatisticsService.analyse(l.getSeFile());
				stopWatch.stop();
			}
		}
		
		log.info(stopWatch.prettyPrint());
	}
	
	
	@Override
	@Transactional
	public void persistContext(final VelocityContext vc, final boolean cascade) {
		
		Job j = (Job) vc.get("job");		
		jobDao.persist(j, cascade);
	}


	@Override
	public RampartConfiguration getRampartConfiguration(File in) throws IOException {
		
		RampartConfiguration rc = new RampartConfiguration(in);
		return rc;
	}

}
