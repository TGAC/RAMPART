package uk.ac.tgac.rampart.dao.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.tgac.rampart.dao.JobDao;
import uk.ac.tgac.rampart.dao.impl.JobDaoImpl;
import uk.ac.tgac.rampart.data.ImproverStats;
import uk.ac.tgac.rampart.data.Job;
import uk.ac.tgac.rampart.data.Library;
import uk.ac.tgac.rampart.data.RampartSettings;
import uk.ac.tgac.rampart.data.Library.Dataset;
import uk.ac.tgac.rampart.data.Library.Usage;
import uk.ac.tgac.rampart.data.MassStats;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext.xml"})
public class JobDaoImplTest {

	@Autowired 
	ApplicationContext ctx;
	
	private JobDao jd;
	
	@Before
	public void before() {
		this.jd = (JobDao)ctx.getAutowireCapableBeanFactory().createBean(JobDaoImpl.class);
	}
	
	@Test
	@Transactional
	public void testGetAllJobs() {
		List<Job> jl = jd.getAllJobs();
		
		Job row0 = jl.get(0);
		Job row1 = jl.get(1);
		
		assertTrue(row0.getAuthor().equals("dan"));
		assertTrue(row1.getAuthor().equals("nizar"));
	}

	@Test
	@Transactional
	public void testGetJob() {
		Job j = jd.getJob(1L);
		
		assertTrue(j.getAuthor().equals("dan"));
	}
	
	@Test
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Rollback(true)
	public void testPersist() {
		
		Job j = new Job();
		j.setAuthor("test_author");
		j.setCollaborator("test_collaborator");
		j.setInstitution("test_institution");
		j.setTitle("test_title");
		j.setJiraSeqinfoId(500L);
		j.setMisoId(500L);
				
		long count = jd.count(); 
		jd.persist(j);
		long newCount = jd.count();
		
		assertTrue(newCount == count+1);
		
	}
	
	
	@Test
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Rollback(true)
	public void testPersistCascade() {
		
		Library l1 = new Library();
		l1.setName("test_lib");
		l1.setDataset(Dataset.RAW);
		l1.setAverageInsertSize(500);
		l1.setInsertErrorTolerance(0.3);
		l1.setReadLength(new Integer(150));
		l1.setUsage(Usage.ASSEMBLY_AND_SCAFFOLDING);
		l1.setIndex(new Integer(1));
		
		List<Library> rawList = new ArrayList<Library>();
		rawList.add(l1);
		
		Library l2 = new Library();
		l2.setName("test_lib");
		l2.setDataset(Dataset.QT);
		l2.setAverageInsertSize(500);
		l2.setInsertErrorTolerance(0.3);
		l2.setReadLength(new Integer(150));
		l2.setUsage(Usage.ASSEMBLY_AND_SCAFFOLDING);
		l2.setIndex(new Integer(1));
		
		List<Library> qtList = new ArrayList<Library>();
		qtList.add(l2);
		
		MassStats m1 = new MassStats();
		m1.setDataset(Dataset.RAW.name());
				
		List<MassStats> msList = new ArrayList<MassStats>();
		msList.add(m1);
		
		ImproverStats i1 = new ImproverStats();
		i1.setStage(1);
				
		List<ImproverStats> isList = new ArrayList<ImproverStats>();
		isList.add(i1);
		
		RampartSettings rs = new RampartSettings();
		rs.setRampartVersion("0.2");
		rs.setQtTool("sickle");
		rs.setQtToolVersion("1.1");
		rs.setQtThreshold(0.3);
		rs.setQtMinLen(75);
		rs.setMassTool("abyss");
		rs.setMassToolVersion("1.3.4");
		rs.setMassKmin(41);
		rs.setMassKmax(95);
		rs.setImpScfTool("sspace");
		rs.setImpScfToolVersion("2.0-Basic");
		rs.setImpDegapTool("gapcloser");
		rs.setImpDegapToolVersion("1.12");
		rs.setImpClipMinLen(1000);
				
		
		Job j = new Job();
		j.setAuthor("test_author");
		j.setCollaborator("test_collaborator");
		j.setInstitution("test_institution");
		j.setTitle("test_title");
		j.setJiraSeqinfoId(500L);
		j.setMisoId(500L);
		j.setLibsRaw(rawList);
		j.setLibsQt(qtList);
		j.setMassStats(msList);
		j.setImproverStats(isList);
		j.setRampartSettings(rs);
		
				
		long count = jd.count(); 
		jd.persist(j);
		long newCount = jd.count();
		
		assertTrue(newCount == count+1);
		assertNotNull(j.getId());
	}
	
}
