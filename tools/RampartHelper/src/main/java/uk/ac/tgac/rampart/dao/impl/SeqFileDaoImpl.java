package uk.ac.tgac.rampart.dao.impl;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import uk.ac.tgac.rampart.dao.SeqFileDao;
import uk.ac.tgac.rampart.data.SeqFile;
import uk.ac.tgac.rampart.util.RampartHibernate;

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
