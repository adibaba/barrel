package de.adrianwilke.barrel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filesystem utilities.
 *
 * @author Adrian Wilke
 */
public abstract class Filesystem {

	public static PathMatcher getPathMatcherSuffix(String fileSuffix) {
		return new PathMatcher() {

			@Override
			public boolean matches(Path path) {
				return path.toString().toLowerCase().endsWith(fileSuffix);
			}
		};
	}

	public static List<File> getFiles(File directory, final PathMatcher pathMatcher) throws IOException {

		// https://stackoverflow.com/questions/2056221/recursively-list-files-in-java/24006711#24006711
		// https://stackoverflow.com/questions/29316310/java-8-lambda-expression-for-filenamefilter/29316408#29316408
		// https://stackoverflow.com/questions/42257987/java-8-stream-populating-a-list-of-objects-instantiated-using-values-in-a-hashm/42258210#42258210
		// https://stackoverflow.com/questions/39840749/casting-types-in-java-8-streams/39840976#39840976

		return Files.walk(Paths.get(directory.getPath()))

				.filter(Files::isRegularFile)

				.filter(pathMatcher::matches)

				.map(Path::toFile)

				.collect(Collectors.toList());
	}

	public static void writeFile(String string, File file) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(string);
		} 
	}

	public static String readFile(File file) throws IOException {
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
			StringBuilder stringBuilder = new StringBuilder();
			String line = bufferedReader.readLine();
			while (line != null) {
				stringBuilder.append(line);
				stringBuilder.append(System.lineSeparator());
				line = bufferedReader.readLine();
			}
			return stringBuilder.toString();
		} 
	}
}