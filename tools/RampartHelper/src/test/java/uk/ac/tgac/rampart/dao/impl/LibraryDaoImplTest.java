package uk.ac.tgac.rampart.dao.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uk.ac.tgac.rampart.dao.LibraryDao;
import uk.ac.tgac.rampart.data.Job;
import uk.ac.tgac.rampart.data.Library;
import uk.ac.tgac.rampart.data.Library.Dataset;
import uk.ac.tgac.rampart.data.Library.Usage;

public class LibraryDaoImplTest {

	private LibraryDao ld;
	
	@Before
	public void before() {
		this.ld = new LibraryDaoImpl();
	}
	
	@Test
	public void testGetLibraryLong() {
		Library l = ld.getLibrary(1L);
		
		assertTrue(l.getName().equals("LIB1782"));
		assertTrue(l.getDataset() == Dataset.RAW);
	}

	@Test
	public void testGetAllLibraries() {
		List<Library> ll = ld.getAllLibraries();
		
		Library row0 = ll.get(0);
		Library row1 = ll.get(1);
		
		assertTrue(row0.getName().equals("LIB1782"));
		assertTrue(row0.getDataset() == Dataset.RAW);
		
		assertTrue(row1.getName().equals("LIB1782"));
		assertTrue(row1.getDataset() == Dataset.QT);
	}

	@Test
	public void testGetLibraryStringDataset() {
		List<Library> ll = ld.getLibraries("LIB1782", Dataset.RAW);
		
		assertTrue(ll.size() == 2);		
		assertTrue(ll.get(0).getName().equals("LIB1782"));
		assertTrue(ll.get(0).getDataset() == Dataset.RAW);
		assertTrue(ll.get(0).getOrder() == 1);
		assertTrue(ll.get(1).getName().equals("LIB1782"));
		assertTrue(ll.get(1).getDataset() == Dataset.RAW);
		assertTrue(ll.get(1).getOrder() == 2);
	}

	@Test
	public void testSave() {
		Library l = new Library();
		l.setName("test_lib");
		l.setDataset(Dataset.RAW);
		l.setAverageInsertSize(500);
		l.setInsertErrorTolerance(0.3);
		//l.setReadLength(new Integer(150));
		//l.setUsage(Usage.ASSEMBLY_AND_SCAFFOLDING);
		//l.setOrder(new Integer(1));
		
		/*Job j = new Job();
		j.setAuthor("libtest_author");
		j.setCollaborator("libtest_collaborator");
		j.setInstitution("libtest_institution");
		j.setTitle("libtest_title");
		j.setJiraSeqinfoId(500L);
		j.setMisoId(500L);
		l.setJob(j);*/
		
		long count = ld.count(); 
		ld.persist(l);
		long newCount = ld.count();
		
		assertTrue(newCount == count+1);
	}

}
