package doser.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

public final class NTToDbPediaUrlEncoding {

	private NTToDbPediaUrlEncoding() {
		super();
	}
	
	public static String dbpediaEncoding(final String url) {
		final StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < url.length(); i++) {
			final String str = String.valueOf(url.charAt(i));
			if (str.equalsIgnoreCase("!")) {
				buffer.append('!');
			} else if (str.equalsIgnoreCase("$")) {
				buffer.append('$');
			} else if (str.equalsIgnoreCase("&")) {
				buffer.append('&');
			} else if (str.equalsIgnoreCase("'")) {
				buffer.append('\'');
			} else if (str.equalsIgnoreCase("(")) {
				buffer.append('(');
			} else if (str.equalsIgnoreCase(")")) {
				buffer.append(')');
			} else if (str.equalsIgnoreCase("*")) {
				buffer.append('*');
			} else if (str.equalsIgnoreCase("+")) {
				buffer.append('+');
			} else if (str.equalsIgnoreCase(",")) {
				buffer.append(',');
			} else if (str.equalsIgnoreCase("-")) {
				buffer.append('-');
			} else if (str.equalsIgnoreCase("/")) {
				buffer.append('/');
			} else if (str.equalsIgnoreCase(":")) {
				buffer.append(':');
			} else if (str.equalsIgnoreCase(";")) {
				buffer.append(';');
			} else if (str.equalsIgnoreCase("=")) {
				buffer.append('=');
			} else if (str.equalsIgnoreCase("@")) {
				buffer.append('@');
			} else if (str.equalsIgnoreCase("_")) {
				buffer.append('_');
			} else if (str.equalsIgnoreCase("~")) {
				buffer.append('~');
			} else {
				try {
					buffer.append(URLEncoder.encode(str, "UTF-8"));
				} catch (final UnsupportedEncodingException e) {
					Logger.getRootLogger().error(e.getStackTrace());
				}
			}
		}
		return buffer.toString();
	}

	public static void main(final String[] args) throws IOException {
		final String fileInput = args[0];
		final String fileOutput = args[1];
		final File fileIn = new File(fileInput);
		final File fileOut = new File(fileOutput);
		final Writer writer = new FileWriter(fileOut);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fileIn));
		} catch (final FileNotFoundException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
		String line = null;
		while ((line = reader.readLine()) != null) {
			line = line.replaceAll("[ ]+", " ");
			final String splitter[] = line.split(" ");
			final StringBuffer buffer = new StringBuffer();

			// Subject
			String url = splitter[0].substring(1, splitter[0].length() - 1);
			String sLine = StringEscapeUtils.unescapeJava(url);
			buffer.append("<" + dbpediaEncoding(sLine) + "> ");

			// Predicate
			buffer.append(splitter[1] + " ");

			// Object
			if (splitter[2].startsWith("<")) {
				url = splitter[2].substring(1, splitter[2].length() - 1);
				sLine = StringEscapeUtils.unescapeJava(url);
				buffer.append("<" + dbpediaEncoding(sLine) + ">");
			} else {
				buffer.append(splitter[2]);
			}
			writer.write(buffer.toString());
			writer.write(System.getProperty("line.separator"));
			writer.flush();
		}
		writer.close();
		reader.close();
	}

}
