package uk.ac.tgac.rampart.service;

import java.io.File;
import java.io.IOException;

import uk.ac.tgac.rampart.data.SeqFileStats;

public interface SequenceStatisticsService {
	
	SeqFileStats analyse(File in) throws IOException;
}
