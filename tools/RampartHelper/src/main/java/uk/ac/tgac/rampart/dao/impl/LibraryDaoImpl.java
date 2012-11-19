package uk.ac.tgac.rampart.dao.impl;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import uk.ac.tgac.rampart.dao.LibraryDao;
import uk.ac.tgac.rampart.data.Library;
import uk.ac.tgac.rampart.data.Library.Dataset;
import uk.ac.tgac.rampart.util.RampartHibernate;

@Repository("libraryDaoImpl")
public class LibraryDaoImpl implements LibraryDao {

	@Autowired
    private SessionFactory sessionFactory;
	
	@Override
	public Library getLibrary(Long id) {
		Session session = this.sessionFactory.getCurrentSession();
		Library ld = (Library)session.load(Library.class, id);
		return ld;
	}
	
	@Override
	public List<Library> getAllLibraries() {	
		Session session = this.sessionFactory.getCurrentSession();
		Query q = session.createQuery("from Library");
		List<Library> libDetails = RampartHibernate.listAndCast(q);
		return libDetails;
	}
	
	@Override
	public List<Library> getLibraries(String name, Dataset dataset) {	
		Session session = this.sessionFactory.getCurrentSession();
		Query q = session.createQuery("from Library where name = :name and dataset = :dataset" );
		q.setParameter("name", name);
		q.setParameter("dataset", dataset);
		List<Library> libDetails = RampartHibernate.listAndCast(q);		
		return libDetails;
	}
	
	@Override
	public List<Library> getLibraries(Long job_id) {	
		Session session = this.sessionFactory.getCurrentSession();
		Query q = session.createQuery("from Library where job_id = :job_id" );
		q.setParameter("job_id", job_id);
		List<Library> libDetails = RampartHibernate.listAndCast(q);		
		return libDetails;
	}
	
	@Override
	public long count() {
		Session session = this.sessionFactory.getCurrentSession();
		Number c = (Number) session.createCriteria(Library.class).setProjection(Projections.rowCount()).uniqueResult();
		return c.longValue();
	}
	
	@Override
	public void persist(Library ld) {
		Session session = this.sessionFactory.getCurrentSession();
		session.saveOrUpdate(ld);
	}
}
