package uk.ac.tgac.rampart.dao;

import java.util.List;

import uk.ac.tgac.rampart.data.RampartSettings;

public interface RampartSettingsDao {

	RampartSettings getRampartSettings(final Long id);
	
	List<RampartSettings> getAllRampartSettings();
	
	long count();
	
	void persist(RampartSettings rampartSettings);
}
