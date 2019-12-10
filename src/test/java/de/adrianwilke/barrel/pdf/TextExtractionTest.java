package de.adrianwilke.barrel.pdf;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Assume;
import org.junit.Test;

public class TextExtractionTest {

	public static final String PDF_FILE = "/tmp/public_title.pdf";

	@Test
	public void testWithTitle() throws Exception {
		File file = new File(PDF_FILE);
		Assume.assumeTrue(file.canRead());

		String text;
		try (PdfBox pdfBox = new PdfBox()) {
			text = pdfBox.setFile(file).getText();
		}

		System.out.println(text);
		assertTrue(text != null);
	}

}