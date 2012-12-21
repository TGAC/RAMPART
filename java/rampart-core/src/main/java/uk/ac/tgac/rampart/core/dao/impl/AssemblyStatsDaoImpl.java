/**
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2013  Daniel Mapleson - TGAC
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package uk.ac.tgac.rampart.core.dao.impl;

import java.util.Collections;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import uk.ac.tgac.rampart.core.dao.AssemblyStatsDao;
import uk.ac.tgac.rampart.core.data.AssemblyStats;
import uk.ac.tgac.rampart.core.data.ImproverStats;
import uk.ac.tgac.rampart.core.data.MassStats;
import uk.ac.tgac.rampart.core.dao.RampartHibernate;

@Repository("assemblyStatsDaoImpl")
public class AssemblyStatsDaoImpl implements AssemblyStatsDao {

	@Autowired
    private SessionFactory sessionFactory;
	
	@Override
	public MassStats getMassStats(Long id) {
		Session session = this.sessionFactory.getCurrentSession();
		MassStats ms = (MassStats)session.load(MassStats.class, id);
		return ms;
	}

	@Override
	public ImproverStats getImproverStats(Long id) {
		Session session = this.sessionFactory.getCurrentSession();
		ImproverStats is = (ImproverStats)session.load(ImproverStats.class, id);
		return is;
	}

	@Override
	public List<AssemblyStats> getAllAssemblyStats() {
		Session session = this.sessionFactory.getCurrentSession();
		Query q = session.createQuery("from AssemblyStats");
		List<AssemblyStats> as = RampartHibernate.listAndCast(q);
		return as;
	}

	@Override
	public List<MassStats> getAllMassStats() {
		Session session = this.sessionFactory.getCurrentSession();
		Query q = session.createQuery("from MassStats");
		List<MassStats> ms = RampartHibernate.listAndCast(q);
		return ms;
	}

	@Override
	public List<ImproverStats> getAllImproverStats() {
		Session session = this.sessionFactory.getCurrentSession();
		Query q = session.createQuery("from ImproverStats");
		List<ImproverStats> is = RampartHibernate.listAndCast(q);
		return is;
	}
	
	

	@Override
	public long count() {
		Session session = this.sessionFactory.getCurrentSession();
		Number c = (Number) session.createCriteria(AssemblyStats.class).setProjection(Projections.rowCount()).uniqueResult();
		return c.longValue();
	}

	@Override
	public void persist(MassStats ms) {
		Session session = this.sessionFactory.getCurrentSession();
		session.saveOrUpdate(ms);
	}

	@Override
	public void persist(ImproverStats is) {
		Session session = this.sessionFactory.getCurrentSession();
		session.saveOrUpdate(is);
	}

	@Override
	public void persistMassStatsList(List<MassStats> massStatsList) {
		for(MassStats massStats : massStatsList) {
			persist(massStats);
		}
	}

	@Override
	public void persistImproverStatsList(List<ImproverStats> improverStatsList) {
		for(ImproverStats improverStats : improverStatsList) {
			persist(improverStats);
		}
	}

	@Override
	public MassStats getBestAssembly(Long jobId) {
		List<MassStats> jobMassStatsList = getMassStatsForJob(jobId);
		MassStats best = Collections.max(jobMassStatsList);
		return best;
	}

	@Override
	public List<MassStats> getMassStatsForJob(Long jobId) {
		Session session = this.sessionFactory.getCurrentSession();
		Query q = session.createQuery("from MassStats where job_id = :job_id");
		q.setParameter("job_id", jobId);
		List<MassStats> ms = RampartHibernate.listAndCast(q);
		return ms;
	}

	@Override
	public List<ImproverStats> getImproverStatsForJob(Long jobId) {
		Session session = this.sessionFactory.getCurrentSession();
		Query q = session.createQuery("from ImproverStats where job_id = :job_id");
		q.setParameter("job_id", jobId);
		List<ImproverStats> is = RampartHibernate.listAndCast(q);
		return is;
	}

}
