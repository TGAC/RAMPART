package uk.ac.tgac.rampart.dao.impl;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uk.ac.tgac.rampart.dao.JobDao;
import uk.ac.tgac.rampart.dao.impl.JobDaoImpl;
import uk.ac.tgac.rampart.data.Job;

public class JobDaoImplTest {

	private JobDao jd;
	
	@Before
	public void before() {
		this.jd = new JobDaoImpl();
	}
	
	@Test
	public void testGetAllJobs() {
		List<Job> jdl = jd.getAllJobs();
		
		Job row0 = jdl.get(0);
		Job row1 = jdl.get(1);
		
		assertTrue(row0.getAuthor().equals("dan"));
		assertTrue(row1.getAuthor().equals("nizar"));
	}

	@Test
	public void testGetJob() {
		Job j = jd.getJob(1L);
		
		assertTrue(j.getAuthor().equals("dan"));
	}
	
	@Test
	public void testSave() {
		
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

}
