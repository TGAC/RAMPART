package uk.ac.tgac.rampart.service;

import java.io.File;
import java.io.IOException;

import uk.ac.tgac.rampart.data.SeqFile;

public interface SequenceStatisticsService {
	
	SeqFile analyse(File in) throws IOException;
}
