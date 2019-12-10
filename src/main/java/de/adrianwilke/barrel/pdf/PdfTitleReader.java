package de.adrianwilke.barrel.pdf;

/**
 * Components which can extract text from PDF files.
 *
 * @author Adrian Wilke
 */
public interface PdfTitleReader {

	public String getTitle() throws Exception;

}