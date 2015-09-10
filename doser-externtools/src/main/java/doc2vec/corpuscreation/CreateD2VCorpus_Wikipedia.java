package doc2vec.corpuscreation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.HashSet;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import doser.tools.indexcreation.WikiPediaUriConverter;

public class CreateD2VCorpus_Wikipedia {

	public static final String INDEX = "/mnt/ssd1/disambiguation/LuceneIndex/Wikipedia_Default_AidaNew/";

	public static final String WIKIPEDIAPAGESDIR = "/mnt/storage/zwicklbauer/WikiParse/temp/plain_reduced/";

	private static String outputFilePath;

	private HashSet<String> relevantEntities;

	public static void main(String[] args) {
		outputFilePath = args[0];
		CreateD2VCorpus_Wikipedia creation = new CreateD2VCorpus_Wikipedia();
		creation.action();
	}

	public CreateD2VCorpus_Wikipedia() {
		super();
		this.relevantEntities = new HashSet<String>();
	}

	public void action() {
		System.out.println("ExtractRelevantEntities");
		extractRelevantEntities();
		System.out.println("CreateOutputFile");
		createOutputFile();
	}

	public void createOutputFile() {
		File outputFile = new File(outputFilePath);
		PrintWriter writer = null;
		BufferedReader reader = null;
		File file = new File(WIKIPEDIAPAGESDIR);
		File[] files = file.listFiles();
		try {
			writer = new PrintWriter(outputFile);
			for (int i = 0; i < files.length; i++) {
				String name = files[i].getName();
				String finalLink = WikiPediaUriConverter
						.createConformDBpediaUrifromEncodedString(name
								.replaceAll(".html", "").replaceAll("'", "%"));
				if (relevantEntities.contains(finalLink)) {
					StringBuilder builder = new StringBuilder();
					String content = "";
					try {
						reader = new BufferedReader(new FileReader(files[i]));
						String line = null;
						while ((line = reader.readLine()) != null) {
							content += line;
						}
						reader.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					try {
						XMLReader xmlReader = XMLReaderFactory
								.createXMLReader();
						PlainTextHandler handler = new PlainTextHandler();
						InputSource inputSource = new InputSource(
								new StringReader(content));
						xmlReader.setContentHandler(handler);
						xmlReader.parse(inputSource);
						builder.append(handler.getDocumentText());
						String wikitext = builder.toString();
						wikitext = wikitext.toLowerCase();
						wikitext = wikitext.replaceAll("\\.", " ");
						wikitext = wikitext.replaceAll("\\,", " ");
						wikitext = wikitext.replaceAll("\\!", " ");
						wikitext = wikitext.replaceAll("\\?", " ");
						wikitext = wikitext.replaceAll(" +", " ");
						if (!wikitext.equalsIgnoreCase("") && !finalLink.equalsIgnoreCase("") && !finalLink.equalsIgnoreCase(" ") && !finalLink.equalsIgnoreCase("http://dbpedia.org/resource/")) {
							writer.println(finalLink + " " + wikitext);
						}
					} catch (SAXException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	public void extractRelevantEntities() {
		File oldIndexFile = new File(INDEX);
		IndexReader readerOldIndex = null;
		try {
			final Directory oldDir = FSDirectory.open(oldIndexFile);
			readerOldIndex = DirectoryReader.open(oldDir);
			for (int j = 0; j < readerOldIndex.maxDoc(); ++j) {
				Document oldDoc = readerOldIndex.document(j);
				String ent = oldDoc.get("Mainlink");
				this.relevantEntities.add(ent);
			}
			readerOldIndex.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (readerOldIndex != null) {
				try {
					readerOldIndex.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class PlainTextHandler implements ContentHandler {

		private StringBuffer documentText;

		public PlainTextHandler() {
			super();
			this.documentText = new StringBuffer();
		}

		@Override
		public void characters(char[] arg0, int arg1, int arg2)
				throws SAXException {
			this.documentText.append(new String(arg0, arg1, arg2));
		}

		@Override
		public void endDocument() throws SAXException {
		}

		@Override
		public void endElement(String arg0, String arg1, String arg2)
				throws SAXException {
		}

		@Override
		public void endPrefixMapping(String arg0) throws SAXException {
		}

		@Override
		public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
				throws SAXException {
		}

		@Override
		public void processingInstruction(String arg0, String arg1)
				throws SAXException {
		}

		@Override
		public void setDocumentLocator(Locator arg0) {
		}

		@Override
		public void skippedEntity(String arg0) throws SAXException {
		}

		@Override
		public void startDocument() throws SAXException {
		}

		@Override
		public void startElement(String arg0, String arg1, String arg2,
				Attributes arg3) throws SAXException {
		}

		@Override
		public void startPrefixMapping(String arg0, String arg1)
				throws SAXException {
		}

		public String getDocumentText() {
			return documentText.toString();
		}
	}

}
