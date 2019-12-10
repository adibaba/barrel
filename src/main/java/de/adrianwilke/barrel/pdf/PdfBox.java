package de.adrianwilke.barrel.pdf;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;

/**
 * Extracts PDF text using Apache PDFBox.
 * 
 * @see https://pdfbox.apache.org/
 * 
 * @author Adrian Wilke
 */
public class PdfBox implements PdfTextReader, PdfTitleReader, Closeable {

	protected File file;
	protected PDDocument pdDocument;

	public PdfBox setFile(String filePathName) {
		file = new File(filePathName);
		return this;
	}

	public PdfBox setFile(File file) {
		this.file = file;
		return this;
	}

	private void initialize() throws Exception {
		if (file == null) {
			throw new Exception("No file set.");
		} else if (!file.canRead()) {
			throw new Exception("Can not read file: " + file.getAbsolutePath());
		}

		pdDocument = PDDocument.load(file);
	}

	@Override
	public String getText() throws Exception {
		if (pdDocument == null) {
			initialize();
		}

		try (StringWriter stringWriter = new StringWriter()) {
			new PDFTextStripper().writeText(pdDocument, stringWriter);
			return stringWriter.toString();
		}
	}

	@Override
	public String getTitle() throws Exception {
		if (pdDocument == null) {
			initialize();
		}

		PDMetadata pdMetadata = pdDocument.getDocumentCatalog().getMetadata();
		if (pdMetadata != null) {
			DomXmpParser domXmpParser = new DomXmpParser();
			try {
				XMPMetadata xmpMetadata = domXmpParser.parse(pdMetadata.createInputStream());
				DublinCoreSchema dublinCoreSchema = xmpMetadata.getDublinCoreSchema();
				if (dublinCoreSchema != null) {
					return dublinCoreSchema.getTitle();
				}
			} catch (XmpParsingException e) {
				// Ignore
			}
		}

		return pdDocument.getDocumentInformation().getTitle();
	}

	@Override
	public void close() throws IOException {
		if (pdDocument != null) {
			pdDocument.close();
		}
	}
}