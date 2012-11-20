package uk.ac.tgac.rampart.dao.impl;

import static org.junit.Assert.*;

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
import uk.ac.tgac.rampart.data.Job;

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
	public void testSave() {
		
		Job j = new Job();
		j.setAuthor("test_author");
		j.setCollaborator("test_collaborator");
		j.setInstitution("test_institution");
		j.setTitle("test_title");
		j.setJiraSeqinfoId(500L);
		j.setMisoId(500L);
				
		long count = jd.count(); 
		jd.persist(j, false);
		long newCount = jd.count();
		
		assertTrue(newCount == count+1);
		
	}

}
