package uk.ac.tgac.rampart.dao;

import java.util.List;

import uk.ac.tgac.rampart.data.Library;
import uk.ac.tgac.rampart.data.Library.Dataset;

public interface LibraryDao {

	Library getLibraryDetails(Long id);
	
	List<Library> getAllLibraryDetails();
	
	List<Library> getLibraryDetails(String name, Dataset dataset);
	
	void save(Library ld);
}
