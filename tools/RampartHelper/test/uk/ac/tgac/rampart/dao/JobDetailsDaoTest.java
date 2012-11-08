package uk.ac.tgac.rampart.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import uk.ac.tgac.rampart.data.JobDetails;

public class JobDetailsDaoTest {

	@Test
	public void testGetAllJobDetails() {
		List<JobDetails> jdl = new JobDetailsDao().getAllJobDetails();
		
		JobDetails row0 = jdl.get(0);
		JobDetails row1 = jdl.get(1);
		
		assertTrue(row0.getAuthor().equals("dan"));
		assertTrue(row1.getAuthor().equals("nizar"));
	}

	@Test
	public void testGetJobDetails() {
		JobDetails jd = new JobDetailsDao().getJobDetails(1L);
		
		assertTrue(jd.getAuthor().equals("dan"));
	}

}
