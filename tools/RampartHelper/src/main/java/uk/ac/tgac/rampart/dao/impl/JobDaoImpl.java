package uk.ac.tgac.rampart.dao.impl;

import static uk.ac.tgac.rampart.util.RampartHibernate.HBN_SESSION;

import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.tgac.rampart.dao.JobDao;
import uk.ac.tgac.rampart.data.Job;

@Repository
@Transactional
public class JobDaoImpl implements JobDao {
	
	@Override
	public Job getJob(Long id) {
		Job jd = (Job)HBN_SESSION.getSession().load(Job.class, id);
		return jd;
	}
	
	@Override
	public List<Job> getAllJobs() {		
		Query q = HBN_SESSION.getSession().createQuery("from Job");
		List<Job> jobDetails = q.list();
		return jobDetails;
	}
	
	@Override
	public void save(Job jd) {
		HBN_SESSION.getSession().saveOrUpdate(jd);
	}
}
