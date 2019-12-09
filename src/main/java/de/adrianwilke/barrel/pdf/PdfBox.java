package de.adrianwilke.barrel.pdf;

import java.io.File;
import java.io.StringWriter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Extracts PDF text using Apache PDFBox.
 * 
 * @see https://pdfbox.apache.org/
 * 
 * @author Adrian Wilke
 */
public class PdfBox implements PdfTextReader {

	protected File file;

	public PdfBox setFile(String filePathName) {
		file = new File(filePathName);
		return this;
	}

	public PdfBox setFile(File file) {
		this.file = file;
		return this;
	}

	@Override
	public String getText() throws Exception {
		if (file == null) {
			throw new Exception("No file set.");
		} else if (!file.canRead()) {
			throw new Exception("Can not read file: " + file.getAbsolutePath());
		}

		try (PDDocument document = PDDocument.load(file)) {
			try (StringWriter stringWriter = new StringWriter()) {
				new PDFTextStripper().writeText(document, stringWriter);
				return stringWriter.toString();
			}
		}
	}
}