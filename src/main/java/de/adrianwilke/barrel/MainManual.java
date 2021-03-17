package de.adrianwilke.barrel;

/**
 * Manual run with arguments set in Java code.
 *
 * @author Adrian Wilke
 */
public class MainManual {

	@SuppressWarnings("unused")
	public static void main(String[] args) {

		String indexId = "barrel";
		String searchQuery = "test";
		String filenameFilter = "";

		// Indexes unknown PDF files
//		args = ("index," + indexId).split(",");

		// List files
//		args = ("list," + indexId).split(",");

		// Search with filter (or empty filter)
//		args = ("search," + indexId + ",'" + searchQuery + "'," + filenameFilter).split(",");

		Main.main(args);
	}
}