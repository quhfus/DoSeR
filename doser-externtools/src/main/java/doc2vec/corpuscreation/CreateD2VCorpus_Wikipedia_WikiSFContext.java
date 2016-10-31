package doc2vec.corpuscreation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
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

public class CreateD2VCorpus_Wikipedia_WikiSFContext {

	public static final String INDEX = "/home/zwicklbauer/NewIndexTryout";

	public static final String WIKIPEDIAPAGESDIR = "/mnt/storage/zwicklbauer/WikiParse/temp/plain_reduced/";

	private static String contextFile;
	private static String outputFilePath;

	private HashSet<String> relevantEntities;
	private HashMap<String, String> plainFiles;

	public static void main(String[] args) {
		contextFile = args[0];
		outputFilePath = args[1];
		CreateD2VCorpus_Wikipedia_WikiSFContext creation = new CreateD2VCorpus_Wikipedia_WikiSFContext();
		creation.action();
	}

	public CreateD2VCorpus_Wikipedia_WikiSFContext() {
		super();
		this.plainFiles = new HashMap<String, String>();
		this.relevantEntities = new HashSet<String>();
	}
	
	private String format(String s) {
		s = s.toLowerCase();
		s = s.replaceAll("[\\.\\,\\!\\? ]+", " ");
		return s;
	}

	public void action() {
		System.out.println("ExtractRelevantEntities");
		extractRelevantEntities();
		System.out.println("ReadWikiPages");
		readWikipediaPages();
		System.out.println("WriteOutput");
		createOutputFile();
	}
	
	public void finalize(PrintWriter writer) {
		for(String s : relevantEntities) {
			StringBuilder builder = new StringBuilder();
			builder.append(s);
			builder.append(" ");
			String wikiText = "";
			wikiText = getWikiText(s);
			builder.append(format(wikiText));
			if(!wikiText.equalsIgnoreCase("")) {
				writer.println(builder.toString());
			}
		}
	}

	public void createOutputFile() {
		File outputFile = new File(outputFilePath);
		PrintWriter writer = null;
		BufferedReader reader = null;
		try {
			writer = new PrintWriter(outputFile);
			File f = new File(contextFile);

			reader = new BufferedReader(new FileReader(f));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] splitter = line.split("\\t");
				StringBuilder builder = new StringBuilder();
				builder.append(splitter[0]);
				builder.append(" ");
				builder.append(format(splitter[1]));
				builder.append(" ");
				String wikiText = getWikiText(splitter[0]);
				builder.append(format(wikiText));
				writer.println(builder.toString());
				this.relevantEntities.remove(splitter[0]);
			}
			this.finalize(writer);
		} catch (IOException e) {
			e.printStackTrace();
//		}
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (writer != null) {
				writer.close();
			}
		}
		this.finalize(writer);
	}
	
	private String getWikiText(String entity) {
		if(this.plainFiles.containsKey(entity)) {
			return this.plainFiles.get(entity);
		} else {
			return "";
		}
	}


	public void readWikipediaPages() {
		File file = new File(WIKIPEDIAPAGESDIR);
		int counter = 0;
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			String finalLink = WikiPediaUriConverter
					.createConformDBpediaUrifromEncodedString(name.replaceAll(
							".html", "").replaceAll("'", "%"));
			if (relevantEntities.contains(finalLink)) {
				StringBuilder builder = new StringBuilder();
				String content = "";
				try {
					BufferedReader reader = new BufferedReader(new FileReader(files[i]));
					String line = null;
					while ((line = reader.readLine()) != null) {
						content += line;
					}
					reader.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					XMLReader xmlReader = XMLReaderFactory.createXMLReader();
					PlainTextHandler handler = new PlainTextHandler();
					InputSource inputSource = new InputSource(new StringReader(
							content));
					xmlReader.setContentHandler(handler);
					xmlReader.parse(inputSource);
					builder.append(handler.getDocumentText());
					this.plainFiles.put(finalLink, builder.toString());
//					System.out.println(builder.toString());
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(counter % 10000 == 0) {
				System.out.println(counter);
			}
			counter++;
		}
		System.out.println("Overall: "+relevantEntities.size() + "Matching: "+counter);
	}

	public void extractRelevantEntities() {
		IndexReader readerOldIndex = null;
		try {
			final Directory oldDir = FSDirectory.open(new File(INDEX));
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
