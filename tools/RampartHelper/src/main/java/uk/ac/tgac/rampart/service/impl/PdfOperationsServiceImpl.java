package uk.ac.tgac.rampart.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import uk.ac.tgac.rampart.service.PdfOperationsService;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

@Service
public class PdfOperationsServiceImpl implements PdfOperationsService {
	
	private static Logger log = Logger.getLogger(PdfOperationsServiceImpl.class.getName());
		
	@Override
	public void extractPage(File in, File out, int page) throws IOException, DocumentException {
		
		log.debug("Starting PDF page extraction");
		
		Document document = new Document();
		
		// Create a reader for the input file
		PdfReader reader = new PdfReader(new FileInputStream(in));
		
		if (page > reader.getNumberOfPages())
			throw new IndexOutOfBoundsException("Page number " + page + " does not exist in " + in.getPath());
		
		// Create a copier for the output file
        PdfCopy copy = new PdfCopy(document, new FileOutputStream(out));
        
        log.debug("PDF extraction resources created");
        
        document.open();
        
        copy.addPage(copy.getImportedPage(reader, page));
        
        document.close();
        
        log.debug("Starting PDF page extracted successfully");
	}
}
