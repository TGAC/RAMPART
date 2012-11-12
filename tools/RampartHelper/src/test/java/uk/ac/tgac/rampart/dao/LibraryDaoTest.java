package uk.ac.tgac.rampart.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import uk.ac.tgac.rampart.dao.impl.LibraryDaoImpl;
import uk.ac.tgac.rampart.data.Job;
import uk.ac.tgac.rampart.data.Library;
import uk.ac.tgac.rampart.data.Library.Dataset;

public class LibraryDaoTest {

	@Test
	public void testGetLibraryDetailsLong() {
		Library ld = new LibraryDaoImpl().getLibraryDetails(1L);
		
		assertTrue(ld.getName().equals("LIB1782"));
		assertTrue(ld.getDataset() == Dataset.RAW);
	}

	@Test
	public void testGetAllLibraryDetails() {
		List<Library> jdl = new LibraryDaoImpl().getAllLibraryDetails();
		
		Library row0 = jdl.get(0);
		Library row1 = jdl.get(1);
		
		assertTrue(row0.getName().equals("LIB1782"));
		assertTrue(row0.getDataset() == Dataset.RAW);
		
		assertTrue(row1.getName().equals("LIB1896"));
		assertTrue(row1.getDataset() == Dataset.RAW);
	}

	@Test
	public void testGetLibraryDetailsStringDataset() {
		List<Library> ld = new LibraryDaoImpl().getLibraryDetails("LIB1782", Dataset.RAW);
		
		assertTrue(ld.size() == 1);		
		assertTrue(ld.get(0).getName().equals("LIB1782"));
		assertTrue(ld.get(0).getDataset() == Dataset.RAW);
	}

	@Test
	public void testSave() {
		fail("Not yet implemented");
	}

}
