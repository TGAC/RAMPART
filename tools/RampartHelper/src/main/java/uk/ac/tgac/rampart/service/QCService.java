package uk.ac.tgac.rampart.service;

import java.io.File;
import java.io.IOException;

import uk.ac.tgac.rampart.data.QCStats;

public interface QCService {

	QCStats load(File in) throws IOException;
}
