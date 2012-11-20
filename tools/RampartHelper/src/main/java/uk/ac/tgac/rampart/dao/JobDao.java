package uk.ac.tgac.rampart.dao;

import java.util.List;

import uk.ac.tgac.rampart.data.Job;

public interface JobDao {

	Job getJob(final Long id);
	
	List<Job> getAllJobs();
	
	long count();
	
	void persist(final Job job, final boolean cascade);
}
