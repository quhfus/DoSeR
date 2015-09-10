package doser.tools.indexcreation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Zwar sehr langsam aber erstmal korrekt!
 * 
 * @author quh
 *
 */
public class WikiPediaUriConverter {

	public static String createConformDBpediaURI(String s) {
		return "http://dbpedia.org/resource/"
				+ convertStringToDBpediaConvention(s);
	}

	public static String createConformDBpediaUriEndingfromEncodedString(
			String string) {
		String decoded = "";
		try {
			decoded = URLDecoder.decode(string, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return convertStringToDBpediaConvention(decoded);
	}

	public static String createConformDBpediaUrifromEncodedString(String string) {
		return "http://dbpedia.org/resource/"
				+ createConformDBpediaUriEndingfromEncodedString(string);
	}

	public static String convertStringToDBpediaConvention(String s) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			builder.append(replaceCharacter(ch));
		}
		return builder.toString();
	}

	private static String replaceCharacter(char ch) {
		String res = String.valueOf(ch);
		if (ch == ' ') {
			res = "_";
		} else if (ch == '"') {
			res = "%22";
		} else if (ch == '#') {
			return "%23";
		} else if (ch == '%') {
			return "%25";
		} else if (ch == '<') {
			return "%3C";
		} else if (ch == '>') {
			return "%3E";
		} else if (ch == '?') {
			return "%3F";
		} else if (ch == '[') {
			return "%5B";
		} else if (ch == '\\') {
			return "%5C";
		} else if (ch == ']') {
			return "%5D";
		} else if (ch == '^') {
			return "%5E";
		} else if (ch == '`') {
			return "%60 ";
		} else if (ch == '{') {
			return "%7B";
		} else if (ch == '|') {
			return "%7C";
		} else if (ch == '}') {
			return "%7D";
		} else if (ch >= 128) {
			try {
				res = URLEncoder.encode(String.valueOf(ch), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	public static void main(String args[]) {
		char test = 'é';
		int i = test;
		System.out.println(i);

	}

}
