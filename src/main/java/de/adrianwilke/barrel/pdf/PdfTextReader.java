package de.adrianwilke.barrel.pdf;

/**
 * Components which can extract text from PDF files.
 *
 * @author Adrian Wilke
 */
public interface PdfTextReader {

	public String getText() throws Exception;

}