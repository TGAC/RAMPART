package uk.ac.tgac.rampart.dao;

import java.util.List;

import uk.ac.tgac.rampart.data.Job;

public interface JobDao {

	Job getJob(Long id);
	
	List<Job> getAllJobs();
	
	long count();
	
	void persist(Job jd);
}
