package uk.ac.tgac.rampart.dao.impl;

import static uk.ac.tgac.rampart.util.RampartHibernate.HBN_SESSION;

import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.tgac.rampart.dao.LibraryDao;
import uk.ac.tgac.rampart.data.Library;
import uk.ac.tgac.rampart.data.Library.Dataset;

@Repository
@Transactional
public class LibraryDaoImpl implements LibraryDao {

	@Override
	public Library getLibraryDetails(Long id) {
		Library ld = (Library)HBN_SESSION.getSession().load(Library.class, id);
		return ld;
	}
	
	@Override
	public List<Library> getAllLibraryDetails() {		
		Query q = HBN_SESSION.getSession().createQuery("from LibraryDetails");
		List<Library> libDetails = q.list();
		return libDetails;
	}
	
	@Override
	public List<Library> getLibraryDetails(String name, Dataset dataset) {		
		Query q = HBN_SESSION.getSession().createQuery("from LibraryDetails where name = :name and dataset = :dataset" );
		q.setParameter("name", name);
		q.setParameter("dataset", dataset);
		List<Library> libDetails = q.list();
		return libDetails;
	}
	
	@Override
	public void save(Library ld) {
		HBN_SESSION.getSession().saveOrUpdate(ld);
	}
}
