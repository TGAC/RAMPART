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

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import uk.ac.tgac.rampart.core.dao.SeqFileDao;
import uk.ac.tgac.rampart.core.data.SeqFile;
import uk.ac.tgac.rampart.core.dao.RampartHibernate;

@Repository("seqFileDaoImpl")
public class SeqFileDaoImpl implements SeqFileDao {
	
	@Autowired
    private SessionFactory sessionFactory;
	
	@Override
	public SeqFile getSeqFile(Long id) {
		Session session = this.sessionFactory.getCurrentSession();
		SeqFile sf = (SeqFile)session.load(SeqFile.class, id);
		return sf;
	}
	
	@Override
	public List<SeqFile> getAllSeqFiles() {
		Session session = this.sessionFactory.getCurrentSession();
		Query q = session.createQuery("from SeqFile");
		List<SeqFile> sfl = RampartHibernate.listAndCast(q);
		return sfl;
	}
	
	@Override
	public List<SeqFile> getSeqFileStatsByType(SeqFile.FileType type) {
		Session session = this.sessionFactory.getCurrentSession();
		Query q = session.createQuery("from SeqFile where file_type = :file_type" );
		q.setParameter("file_type", type);
		List<SeqFile> sfl = RampartHibernate.listAndCast(q);		
		return sfl;
	}
	
	@Override
	public long count() {
		Session session = this.sessionFactory.getCurrentSession();
		Number c = (Number) session.createCriteria(SeqFile.class).setProjection(Projections.rowCount()).uniqueResult();
		return c.longValue();
	}
	
	@Override
	public void persist(SeqFile sf) {
		Session session = this.sessionFactory.getCurrentSession();
		session.saveOrUpdate(sf);
	}
}
