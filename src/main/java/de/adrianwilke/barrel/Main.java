package de.adrianwilke.barrel;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

import de.adrianwilke.barrel.pdf.PdfBox;

/**
 * A barrel full of PDF data.
 *
 * @author Adrian Wilke
 */
public class Main {

	protected static final Logger LOGGER = LogManager.getLogger();

	public static final String MODE_LIST = "list";
	public static final String MODE_INDEX = "index";
	public static final String MODE_SEARCH = "search";

	public static final String PARAMETER_INDEX_ID = "indexId";
	public static final String PARAMETER_ALL = "all";
	public static final String PARAMETER_QUERY = "query";

	protected Configuration configuration = new Configuration();

	public static void main(String[] args) {
		try {
			new Main().run(args);
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}

	public void run(String[] args) throws Exception {
		configuration.load();

		if (args.length == 0) {
			printInfo();
			System.exit(Configuration.CODE_OK);

		} else if (args[0].equals(MODE_LIST) && args.length == 2) {
			getPdfFiles(getIndexOrExit(args[1])).forEach(System.out::println);

		} else if (args[0].equals(MODE_INDEX) && args.length >= 2) {
			boolean ignoreExisting = true;
			if (args.length > 2) {
				if (args[2].equals(PARAMETER_ALL)) {
					ignoreExisting = false;
				}
			}
			indexFiles(getIndexOrExit(args[1]), ignoreExisting);

		} else if (args[0].equals(MODE_SEARCH) && args.length == 3) {
			search(getIndexOrExit(args[1]), args[2]);

		} else {
			System.err.println("Could not parse input: " + Arrays.asList(args).toString());
			System.exit(Configuration.CODE_USER_INPUT);
		}

	}

	protected Index getIndexOrExit(String indexId) {
		Index index = configuration.indexes.get(indexId);
		if (index == null) {
			System.err.println("Unknown index: " + indexId);
			System.exit(Configuration.CODE_USER_INPUT);
		}
		return index;
	}

	protected void printInfo() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append("Configuration file:");
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append("  " + Configuration.PROPERTIES_FILE.getAbsolutePath());
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append("Usage:");
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append("  " + MODE_LIST + "   <" + PARAMETER_INDEX_ID + ">");
		stringBuilder.append("          Lists PDF files");
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append("  " + MODE_INDEX + "  <" + PARAMETER_INDEX_ID + ">");
		stringBuilder.append("          Indexes unknown PDF files");
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append("  " + MODE_INDEX + "  <" + PARAMETER_INDEX_ID + "> " + PARAMETER_ALL);
		stringBuilder.append("      Indexes all PDF files");
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append("  " + MODE_SEARCH + " <" + PARAMETER_INDEX_ID + "> <" + PARAMETER_QUERY + ">");
		stringBuilder.append("  Searches for query");
		stringBuilder.append(System.lineSeparator());
		System.out.println(stringBuilder.toString());
	}

	protected void search(Index index, String query) throws IOException {
		SearchResponse searchResponse = Elasticsearch.search(configuration.elasticsearchHost, index.id, query);
		StringBuilder stringBuilder = new StringBuilder();
		for (SearchHit hit : searchResponse.getHits()) {
			stringBuilder.append(hit.getId() + "<br/>");
			LOGGER.info(hit.getId());
			stringBuilder.append(hit.getScore() + "<br/>");
			LOGGER.info(hit.getScore());
			for (HighlightField field : hit.getHighlightFields().values()) {
				String highlight = Arrays.toString(field.getFragments()).replaceAll("\r", "").replaceAll("\n", "");
				stringBuilder.append(highlight + "<br/>");
				LOGGER.info(highlight);
			}
			File file = new File(index.directory, hit.getId());
			stringBuilder.append("<a href = \"" + file + "\">" + file + "</a><br/>");
			LOGGER.info(file);
			if (index.web != null) {
				try {
					URI uri = new URI(index.web.toString() + hit.getId());
					stringBuilder.append("<a href = \"" + uri + "\">" + uri + "</a><br/>");
					LOGGER.info(uri);
				} catch (URISyntaxException e) {
					// Ignore
				}
			}
			stringBuilder.append("<br/>");
			System.out.println();
		}
		File resultsFile = new File(Configuration.getTempDirectory(), "search.htm");
		Filesystem.writeFile(stringBuilder.toString(), resultsFile);
		LOGGER.info(searchResponse.getHits().getTotalHits().value);
		LOGGER.info(resultsFile.getAbsolutePath());
	}

	protected void indexFiles(Index index, boolean ignoreExisting) throws IOException {
		extractText(index, getPdfFiles(index), ignoreExisting);

		List<File> txtFiles = Filesystem.getFiles(index.getTempDirectory(),
				Filesystem.getPathMatcherSuffix(".pdf.txt"));

		List<File> contentFiles = txtFiles.stream().filter(new Predicate<File>() {
			@Override
			public boolean test(File file) {
				return file.length() > 1000 ? true : false;
			}
		}).collect(Collectors.toList());

		for (File contentFile : contentFiles) {
			String id = getId(contentFile, index);
			if (id != null) {
				Elasticsearch.write(configuration.elasticsearchHost, index.id, id, Filesystem.readFile(contentFile));
			} else {
				LOGGER.warn("Could not get ID for " + contentFile.getAbsolutePath());
			}
		}
	}

	String getId(File textFile, Index index) {
		String id = null;
		if (textFile.getAbsolutePath().startsWith(index.getTempDirectory().getAbsolutePath())) {
			id = textFile.getAbsolutePath().substring((int) index.getTempDirectory().getAbsolutePath().length());
		} else {
			return null;
		}
		if (id.startsWith(index.directory.getAbsolutePath())) {
			id = id.substring(index.directory.getAbsolutePath().length());
		} else {
			return null;
		}
		String suffix = ".txt";
		if (id.endsWith(suffix)) {
			id = id.substring(0, id.length() - suffix.length());
		}
		return id;
	}

	protected void extractText(Index index, List<File> pdfFiles, boolean ignoreExisting) {
		int counter = 0;
		for (File pdfFile : pdfFiles) {
			File outFile = new File(index.getTempDirectory(), pdfFile.getAbsolutePath() + ".txt");
			if (ignoreExisting && outFile.exists()) {
				continue;
			} else {
				outFile.getParentFile().mkdirs();
				try {
					Filesystem.writeFile(new PdfBox().setFile(pdfFile).getText(), outFile);
				} catch (Exception e) {
					LOGGER.error("Could not extract " + pdfFile.getAbsolutePath(), e);
				}
				counter++;
			}
		}
		LOGGER.info("Extracted text of " + counter + " PDF files to " + index.getTempDirectory());
	}

	protected List<File> getPdfFiles(Index index) throws IOException {
		List<File> files = Filesystem.getFiles(index.directory, Filesystem.getPathMatcherSuffix(".pdf"));
		LOGGER.info("Found " + files.size() + " PDF files in " + index.directory.getAbsolutePath());
		return files;
	}
}