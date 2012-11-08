package uk.ac.tgac.rampart.dao;

import static uk.ac.tgac.rampart.util.RampartHibernate.HBN_SESSION;

import java.util.List;

import org.hibernate.Query;

import uk.ac.tgac.rampart.data.JobDetails;

public class JobDetailsDao {
	
	public JobDetails getJobDetails(Long id) {
		JobDetails jd = (JobDetails)HBN_SESSION.getSession().load(JobDetails.class, id);
		return jd;
	}
	
	public List<JobDetails> getAllJobDetails() {		
		Query q = HBN_SESSION.getSession().createQuery("from JobDetails");
		List<JobDetails> jobDetails = q.list();
		return jobDetails;
	}
	
	public void save(JobDetails jd) {
		HBN_SESSION.getSession().saveOrUpdate(jd);
	}
}
