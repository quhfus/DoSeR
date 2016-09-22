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

public class Test {

	public static void main(String[] args) {
		try {
			Test t = new Test();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	Test() throws Exception {
		PrintWriter writer = new PrintWriter(new File("/mnt/ssd1/disambiguation/test/test.dat"));
		File alan = new File("/mnt/storage/zwicklbauer/WikiParse/temp/plain_reduced/Alan_Turing.html");
		String filecontent = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(alan));
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
			WikipediaFilesHandler handler = new WikipediaFilesHandler();
			InputSource inputSource = new InputSource(new StringReader(
					filecontent));
			xmlReader.setContentHandler(handler);
			xmlReader.parse(inputSource);
			String fileText = handler.getString();
			print(writer, fileText);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	public void print(PrintWriter writer, String text) {
		writer.print(text);
		writer.flush();
	}
	
	class WikipediaFilesHandler implements ContentHandler {

		private StringBuilder builder;

		private String currentValue;
		
		private boolean isOpen;
		
		WikipediaFilesHandler() {
			super();
			builder = new StringBuilder();
			this.isOpen = false;
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
				isOpen = true;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if(isOpen && localName.equals("a")) {
				builder.append(currentValue);
				isOpen = false;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			currentValue = new String(ch, start, length);
			if(!isOpen) {
//				currentValue = currentValue.replaceAll("[^a-zA-Z \\.\\?\\!]", ""); 
//				currentValue = currentValue.toLowerCase();
//				currentValue = currentValue.replaceAll("[\\.\\,\\!\\? ]+", " ");
				builder.append(currentValue);
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

}
