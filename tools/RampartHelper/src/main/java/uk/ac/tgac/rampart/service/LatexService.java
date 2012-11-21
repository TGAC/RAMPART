package uk.ac.tgac.rampart.service;

import java.io.File;

public interface LatexService {

	void compileDocument(File texFile) throws Exception;
}
