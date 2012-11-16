package uk.ac.tgac.rampart.dao;

import java.util.List;

import uk.ac.tgac.rampart.data.Library;
import uk.ac.tgac.rampart.data.Library.Dataset;

public interface LibraryDao {

	Library getLibrary(Long id);
	
	List<Library> getAllLibraries();
	
	List<Library> getLibraries(String name, Dataset dataset);
	
	List<Library> getLibraries(Long job_id);
	
	long count();
	
	void persist(Library ld);
}
