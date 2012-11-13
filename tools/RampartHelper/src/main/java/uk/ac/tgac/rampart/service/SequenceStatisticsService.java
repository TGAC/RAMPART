package uk.ac.tgac.rampart.service;

import java.io.File;
import java.io.IOException;

import uk.ac.tgac.rampart.data.SequenceFileStats;

public interface SequenceStatisticsService {
	
	SequenceFileStats analyse(File in) throws IOException;
}
