package de.adrianwilke.barrel;

import java.io.File;
import java.net.URI;

/**
 * Elasticsearch index.
 *
 * @author Adrian Wilke
 */
public class Index {

	public String id;
	public File directory;
	public URI web;

	Index setId(String id) {
		this.id = id;
		return this;
	}

	Index setDirectory(File directory) {
		this.directory = directory;
		return this;
	}

	Index setWeb(URI web) {
		this.web = web;
		return this;
	}

	File getTempDirectory() {
		return new File(Configuration.getTempDirectory(), id);
	}
}