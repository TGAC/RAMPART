package uk.ac.tgac.rampart.service;

import java.io.File;
import java.io.IOException;

import com.itextpdf.text.DocumentException;

public interface PdfOperationsService {

	void extractPage(File in, File out, int page) throws IOException, DocumentException;
	
}
