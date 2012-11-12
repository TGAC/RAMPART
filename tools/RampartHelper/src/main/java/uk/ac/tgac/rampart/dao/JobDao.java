package uk.ac.tgac.rampart.dao;

import java.util.List;

import uk.ac.tgac.rampart.data.Job;

public interface JobDao {

	Job getJobDetails(Long id);
	
	List<Job> getAllJobDetails();
	
	void save(Job jd);
}
