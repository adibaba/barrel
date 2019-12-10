package de.adrianwilke.barrel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Configuration.
 *
 * @author Adrian Wilke
 */
public class Configuration {

	public static final File PROPERTIES_FILE = new File("configuration.txt");

	public static final int CODE_OK = 0;
	public static final int CODE_EXIT = 1;
	public static final int CODE_CFG_FILE_NOT_FOUND = 2;
	public static final int CODE_CFG_FILE_NOT_READABLE = 3;
	public static final int CODE_CFG_PROPERTY_NOT_FOUND = 4;
	public static final int CODE_USER_INPUT = 5;

	public static final String INDEXES = "indexes";

	public static final String PREFIX_INDEX = "index.";
	public static final String SUFFIX_ID = ".id";
	public static final String SUFFIX_DIRECTORY = ".directory";
	public static final String SUFFIX_WEB = ".web";

	public static final String PREFIX_ELASTICSEARCH = "elasticsearch.";
	public static final String SUFFIX_HOSTNAME = "hostname";
	public static final String SUFFIX_PORT = "port";
	public static final String SUFFIX_SCHEME = "scheme";

	protected static final Logger LOGGER = LogManager.getLogger(Configuration.class);

	protected Properties properties;

	public HttpHost elasticsearchHost;
	public Map<String, Index> indexes = new HashMap<>();

	public Configuration load() {
		properties = new Properties();

		try (InputStream inputStream = new FileInputStream(PROPERTIES_FILE)) {
			properties.load(inputStream);
		} catch (FileNotFoundException e) {
			exitFileNotFound();
		} catch (IOException e) {
			exitFileNotReadable();
		}

		// Elasticsearch

		String elasticsearchHostname = getValueOrExit(PREFIX_ELASTICSEARCH + SUFFIX_HOSTNAME);
		int elasticsearchPort = -1;
		try {
			elasticsearchPort = Integer.valueOf(getValueOrExit(PREFIX_ELASTICSEARCH + SUFFIX_PORT));
		} catch (NumberFormatException e) {
			System.err.println("Not a valid port: " + getValueOrExit(PREFIX_ELASTICSEARCH + SUFFIX_PORT));
			System.exit(CODE_EXIT);
		}
		String elasticsearchScheme = getValueOrExit(PREFIX_ELASTICSEARCH + SUFFIX_SCHEME);
		elasticsearchHost = new HttpHost(elasticsearchHostname, elasticsearchPort, elasticsearchScheme);

		// Indexes

		String[] configIndexes = getValueOrExit(INDEXES).split(",");
		for (int i = 0; i < configIndexes.length; i++) {
			String id = getValueNeeded(PREFIX_INDEX + configIndexes[i].trim() + SUFFIX_ID);
			String directory = getValueNeeded(PREFIX_INDEX + configIndexes[i].trim() + SUFFIX_DIRECTORY);
			String web = getValue(PREFIX_INDEX + configIndexes[i].trim() + SUFFIX_WEB);
			if (id != null && directory != null) {
				File directoryFile = new File(directory);
				if (directoryFile.canRead()) {
					Index index = new Index().setId(id).setDirectory(directoryFile);
					if (web != null) {
						try {
							index.setWeb(new URI(web));
						} catch (URISyntaxException e) {
							LOGGER.warn("Not a valid URI: " + web);
						}
					}
					indexes.put(id, index);
				} else {
					LOGGER.warn("Can not read directory: " + directoryFile.getAbsolutePath());
					continue;
				}
			}
		}
		if (indexes.isEmpty()) {
			System.err.println("Exit: No indexes");
			System.exit(CODE_EXIT);
		}

		return this;
	}

	public String get(String key) {
		return properties.getProperty(key);
	}

	protected String getValue(String key) {
		String value = properties.getProperty(key);
		if (value == null || value.isEmpty()) {
			LOGGER.info("Could not find configuration setting: " + key);
		}
		return value;
	}

	protected String getValueNeeded(String key) {
		String value = properties.getProperty(key);
		if (value == null || value.isEmpty()) {
			LOGGER.warn("Could not find configuration setting: " + key);
		}
		return value;
	}

	protected String getValueOrExit(String key) {
		String value = properties.getProperty(key);
		if (value == null || value.isEmpty()) {
			exitPropertyNotFound(key);
			return new String();
		} else {
			return value;
		}
	}

	protected void exitPropertyNotFound(String key) {
		System.err.println("Exit: Could not find configuration setting: " + key);
		System.exit(CODE_CFG_PROPERTY_NOT_FOUND);
	}

	protected void exitFileNotReadable() {
		System.err.println("Exit: Could not read configuration file: " + PROPERTIES_FILE.getAbsolutePath());
		System.exit(CODE_CFG_FILE_NOT_READABLE);
	}

	protected void exitFileNotFound() {
		System.err.println("Exit: Could not find configuration file: " + PROPERTIES_FILE.getAbsolutePath());
		System.exit(CODE_CFG_FILE_NOT_FOUND);
	}

	public static File getTempDirectory() {
		return new File(System.getProperty("java.io.tmpdir"), Configuration.class.getPackageName());
	}

}