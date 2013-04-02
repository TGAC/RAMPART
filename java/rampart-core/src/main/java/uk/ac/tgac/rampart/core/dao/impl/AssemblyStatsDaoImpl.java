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

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.tgac.rampart.core.dao.AssemblyStatsDao;
import uk.ac.tgac.rampart.core.dao.RampartHibernate;
import uk.ac.tgac.rampart.core.data.AssemblyStats;

import java.util.Collections;
import java.util.List;

@Repository("assemblyStatsDaoImpl")
public class AssemblyStatsDaoImpl implements AssemblyStatsDao {

	@Autowired
    private SessionFactory sessionFactory;
	
	@Override
	public AssemblyStats getStats(Long id) {
		Session session = this.sessionFactory.getCurrentSession();
		AssemblyStats s = (AssemblyStats)session.load(AssemblyStats.class, id);
		return s;
	}

    @Override
	public long count() {
		Session session = this.sessionFactory.getCurrentSession();
		Number c = (Number) session.createCriteria(AssemblyStats.class).setProjection(Projections.rowCount()).uniqueResult();
		return c.longValue();
	}

	@Override
	public void persist(AssemblyStats s) {
		Session session = this.sessionFactory.getCurrentSession();
		session.saveOrUpdate(s);
	}

	@Override
	public void persistStatsList(List<AssemblyStats> statsList) {
		for(AssemblyStats stats : statsList) {
			persist(stats);
		}
	}

	@Override
	public AssemblyStats getBestAssembly(Long jobId) {
		List<AssemblyStats> jobStatsList = getStatsForJob(jobId);
		AssemblyStats best = Collections.max(jobStatsList);
		return best;
	}

	@Override
	public List<AssemblyStats> getStatsForJob(Long jobId) {
		Session session = this.sessionFactory.getCurrentSession();
		Query q = session.createQuery("from AssemblyStats where job_id = :job_id");
		q.setParameter("job_id", jobId);
		List<AssemblyStats> ms = RampartHibernate.listAndCast(q);
		return ms;
	}

}
