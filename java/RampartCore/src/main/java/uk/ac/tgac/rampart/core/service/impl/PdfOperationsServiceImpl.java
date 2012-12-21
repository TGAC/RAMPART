/**
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2013  Daniel Mapleson - TGAC
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package uk.ac.tgac.rampart.core.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import uk.ac.tgac.rampart.core.service.PdfOperationsService;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

@Service
public class PdfOperationsServiceImpl implements PdfOperationsService {
	
	private static Logger log = LoggerFactory.getLogger(PdfOperationsServiceImpl.class);
		
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
