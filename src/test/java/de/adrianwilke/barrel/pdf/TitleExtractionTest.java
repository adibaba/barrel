package de.adrianwilke.barrel.pdf;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Assume;
import org.junit.Test;

public class TitleExtractionTest {

	public static final String PDF_FILE_1 = "/tmp/public_title.pdf";
	public static final String PDF_FILE_2 = "/tmp/public_no_title.pdf";

	@Test
	public void testWithTitle() throws Exception {
		File file = new File(PDF_FILE_1);
		Assume.assumeTrue(file.canRead());

		String title;
		try (PdfBox pdfBox = new PdfBox()) {
			title = pdfBox.setFile(file).getTitle();
		}

		System.out.println(title);
		assertTrue(title != null);
	}

	@Test
	public void testWithoutTitle() throws Exception {
		File file = new File(PDF_FILE_2);
		Assume.assumeTrue(file.canRead());

		String title;
		try (PdfBox pdfBox = new PdfBox()) {
			title = pdfBox.setFile(file).getTitle();
		}

		System.out.println(title);
		assertTrue(title == null);
	}

}