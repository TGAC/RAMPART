package uk.ac.tgac.rampart.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

public class PdfOperations {

	private File pdfFile;

	public PdfOperations(File pdfFile) {
		this.setPdfFile(pdfFile);
	}

	public void extractPage(int pageNumber, File outPdf) {
		Document document = new Document();
		try {
			// Create a reader for the input file
			PdfReader reader = new PdfReader(new FileInputStream(pdfFile));
			
			if (pageNumber > reader.getNumberOfPages())
				throw new IndexOutOfBoundsException("Page number " + pageNumber + " does not exist in " + this.getPdfFile().getPath());
			
			// Create a copier for the output file
            PdfCopy copy = new PdfCopy(document, new FileOutputStream(outPdf));
            
            document.open();
            
            copy.addPage(copy.getImportedPage(reader, pageNumber));
            
            document.close();
        }
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public File getPdfFile() {
		return pdfFile;
	}

	public void setPdfFile(File pdfFile) {
		this.pdfFile = pdfFile;
	}
	
	public static void extractPage(File in, File out, int page) {
		PdfOperations pdfops = new PdfOperations(in);
		pdfops.extractPage(page, out);
	}
}
