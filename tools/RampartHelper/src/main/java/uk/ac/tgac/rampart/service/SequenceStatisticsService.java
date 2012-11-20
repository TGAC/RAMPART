package uk.ac.tgac.rampart.service;

import java.io.IOException;

import uk.ac.tgac.rampart.data.SeqFile;

public interface SequenceStatisticsService {
	
	void analyse(SeqFile in) throws IOException;
}
