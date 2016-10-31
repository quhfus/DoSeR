package doc2vec.corpuscreation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import doser.tools.indexcreation.WikiPediaUriConverter;

public class WikiParagraphCreator {

	public static final String luceneIndex = "/mnt/ssd1/disambiguation/LuceneIndex/Current_Wiki_Index/";

	public static final String input = "/home/zwicklbauer/wiki_data.dat";
	public static final String output = "/home/zwicklbauer/doc2vec_input_standardanalyzer.dat";

	public static void main(String[] args) {
		WikiParagraphCreator cr = new WikiParagraphCreator();
		cr.checkLuceneIndex();
		System.out.println("Check Ready");
		File inputFile = new File(input);
		cr.action(inputFile);

	}

	private String currentTitle;

	private PrintWriter writer;

	private int paragraphIterator;

	private boolean isInIndex = false;

	private Set<String> luceneWikiFiles;

	private int entitiesOverall;
	private int paragraphsOverall;

	public WikiParagraphCreator() {
		super();

		this.currentTitle = "";
		try {
			this.writer = new PrintWriter(new File(output));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void action(File file) {
		BufferedReader reader = null;
		try {
			String line = null;
			reader = new BufferedReader(new FileReader(file));
			StringBuilder builder = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("<doc id=")) {
					this.isInIndex = false;
					// Write Old paragraph
					String s = builder.toString();
					if (s.length() > 0) {
						writer.println(
								currentTitle + "_" + paragraphIterator + "\t" + analyze(builder.toString()));
						paragraphsOverall++;
						writer.flush();
					}
					paragraphIterator = 0;

					// Extract Title
					String split[] = line.split("title=\"");
					String split2[] = split[1].split("\"");
					currentTitle = convertTitle(split2[0]);
					if (luceneWikiFiles.contains("http://dbpedia.org/resource/" + currentTitle)) {
						this.isInIndex = true;
						this.entitiesOverall++;
					}

					builder = new StringBuilder();
				} else if (line.equals("</doc>")) {
					//
				} else if (line.length() < 100 && builder.length() > 400 && isInIndex) {
					writer.println(currentTitle + "_" + paragraphIterator + "\t"
							+ analyze(builder.toString()));
					writer.flush();
					paragraphIterator++;
					builder = new StringBuilder();
					paragraphsOverall++;
				} else if (line.length() > 100) {
					builder.append(line);
				}
			}
			System.out.println("Parsed Entities: " + entitiesOverall);
			System.out.println("Parsed Paragraphs: " + paragraphsOverall);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String analyze(String text) {
		Analyzer ana = new StandardAnalyzer();
		StringBuilder builder = new StringBuilder();
		try {
			TokenStream stream = ana.tokenStream("", new StringReader(text));

			stream.reset();
			while (stream.incrementToken()) {
				builder.append(stream.getAttribute(CharTermAttribute.class).toString()+" ");
//				result.add(stream.getAttribute(CharTermAttribute.class).toString());
			}
		} catch (IOException e) {
			// not thrown b/c we're using a string reader...
		}
		ana.close();
		return builder.toString().trim();
	}

	private void checkLuceneIndex() {
		Set<String> set = new HashSet<String>();
		File oldIndexFile = new File(luceneIndex);
		IndexReader readerOldIndex = null;
		try {
			final Directory oldDir = FSDirectory.open(oldIndexFile);
			readerOldIndex = DirectoryReader.open(oldDir);
			for (int j = 0; j < readerOldIndex.maxDoc(); ++j) {
				Document oldDoc = readerOldIndex.document(j);
				set.add(oldDoc.get("Mainlink"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.luceneWikiFiles = set;
	}

	private String convertTitle(String t) {
		return WikiPediaUriConverter.convertStringToDBpediaConvention(t);
	}
}
