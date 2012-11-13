package uk.ac.tgac.rampart.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import uk.ac.tgac.rampart.dao.impl.JobDaoImpl;
import uk.ac.tgac.rampart.data.Job;

public class JobDaoTest {

	@Test
	public void testGetAllJobs() {
		List<Job> jdl = new JobDaoImpl().getAllJobs();
		
		Job row0 = jdl.get(0);
		Job row1 = jdl.get(1);
		
		assertTrue(row0.getAuthor().equals("dan"));
		assertTrue(row1.getAuthor().equals("nizar"));
	}

	@Test
	public void testGetJob() {
		Job jd = new JobDaoImpl().getJob(1L);
		
		assertTrue(jd.getAuthor().equals("dan"));
	}

}
