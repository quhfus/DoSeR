package word2vec.corpuscreation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import doser.tools.indexcreation.WikiPediaUriConverter;

public class CreateEntityCorpus {

	public static final boolean SEPERATELINES = true;
	
	public static final String CORPUSFILE = "/home/zwicklbauer/word2vec/corpus/wikientitycorpus.dat";

	public static final String DIRECTORY = "/mnt/storage/zwicklbauer/WikiParse/temp/plain_reduced";

	public CreateEntityCorpus() {
		super();
	}

	public void createCorpus() throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(new File(CORPUSFILE));
		File directory = new File(DIRECTORY);
		File[] files = directory.listFiles();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			String filecontent = "";
			try {
				BufferedReader reader = new BufferedReader(new FileReader(f));
				String line = null;
				while ((line = reader.readLine()) != null) {
					filecontent += line;
				}
				reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				XMLReader xmlReader = XMLReaderFactory.createXMLReader();
				Handler handler = new Handler();
				InputSource inputSource = new InputSource(new StringReader(
						filecontent));
				xmlReader.setContentHandler(handler);
				xmlReader.parse(inputSource);
				String fileText = handler.getString();
				print(writer, fileText);
				if(SEPERATELINES) {
					printNewLine(writer);
				}
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class Handler implements ContentHandler {

		private StringBuilder builder;

		Handler() {
			super();
			builder = new StringBuilder();
		}

		@Override
		public void setDocumentLocator(Locator locator) {
			// TODO Auto-generated method stub

		}

		@Override
		public void startDocument() throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void endDocument() throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void startPrefixMapping(String prefix, String uri)
				throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void endPrefixMapping(String prefix) throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes atts) throws SAXException {
			if (localName.equals("a")) {
				builder.append(WikiPediaUriConverter
						.createConformDBpediaUriEndingfromEncodedString(atts
								.getValue("href"))
						+ " ");
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			String val = new String(ch, start, length);
			if(val.contains(".")) {
				builder.append(". ");
			} else if(val.contains("?")) {
				builder.append("? ");
			} else if(val.contains("!")) {
				builder.append("! ");
			}
		}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length)
				throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void processingInstruction(String target, String data)
				throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void skippedEntity(String name) throws SAXException {
			// TODO Auto-generated method stub

		}

		public String getString() {
			return builder.toString();
		}

	}

	public void print(PrintWriter writer, String text) {
		writer.print(text);
		writer.flush();
	}
	
	public void printNewLine(PrintWriter writer) {
		writer.print(System.lineSeparator());
	}

	public static void main(String[] args) {
		CreateEntityCorpus corpus = new CreateEntityCorpus();
		try {
			corpus.createCorpus();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
