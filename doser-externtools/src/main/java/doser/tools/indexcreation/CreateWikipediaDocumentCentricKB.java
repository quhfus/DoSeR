package doser.tools.indexcreation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import doser.tools.indexcreation.CreateWikipediaDocumentCentricKB.Handler.Entry;
import doser.tools.indexcreation.CreateWikipediaDocumentCentricKB.Handler.Entry.Entity;

/**
 * Creates a document-centric knowledge base out of the extracted Wikipedia
 * pages (see S1HtmlToPlainTextWithEntities).
 * 
 * @author quh
 * 
 */

public class CreateWikipediaDocumentCentricKB {

	public CreateWikipediaDocumentCentricKB() {
		super();
	}

	public void create(String documentDirectory, String luceneOutput) {
		StandardAnalyzer analyzer = new StandardAnalyzer();
		try {
			MMapDirectory dir = new MMapDirectory(new File(luceneOutput));
			IndexWriterConfig config = new IndexWriterConfig(Version.LATEST,
					analyzer);
			IndexWriter writer = new IndexWriter(dir, config);

			File wikipediaFiles = new File(documentDirectory);
			File[] files = wikipediaFiles.listFiles();
			for (int i = 0; i < files.length; i++) {
				String content = "";
				try {
					BufferedReader reader = new BufferedReader(new FileReader(
							files[i]));
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
				Entry entry = null;
				try {
					XMLReader xmlReader = XMLReaderFactory.createXMLReader();
					Handler handler = new Handler();
					InputSource inputSource = new InputSource(new StringReader(
							content));
					xmlReader.setContentHandler(handler);
					xmlReader.parse(inputSource);
					entry = handler.getEntry();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (entry != null) {
					Document doc = new Document();
					doc.add(new StringField("Title", files[i].getName(),
							Field.Store.YES));
					doc.add(new TextField("Text", entry.text, Field.Store.YES));
					StringBuffer buffer = generateAnnotatedEntityStrings(entry
							.getEntitySet());
					doc.add(new StringField("Entities", buffer.toString(),
							Field.Store.YES));
					writer.addDocument(doc);
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private StringBuffer generateAnnotatedEntityStrings(List<Entity> entities) {
		StringBuffer buffer = new StringBuffer();
		for (Entity entity : entities) {
			buffer.append(entity.getLink());
		}
		return buffer;
	}

	class Handler implements ContentHandler {

		private String currentValue;
		private Entry entry;
		private StringBuffer documentText;
		private String link;
		private String mention;
		private int position;

		public Handler() {
			super();
			this.currentValue = "";
			this.entry = new Entry();
			this.documentText = new StringBuffer();
			this.link = null;
			this.mention = null;
			this.position = 0;
		}

		public Entry getEntry() {
			return this.entry;
		}

		@Override
		public void characters(char[] arg0, int arg1, int arg2)
				throws SAXException {
			this.currentValue = new String(arg0, arg1, arg2);
			this.documentText.append(new String(arg0, arg1, arg2));
		}

		@Override
		public void endDocument() throws SAXException {
			this.entry.setText(documentText.toString());
		}

		@Override
		public void endElement(String arg0, String arg1, String arg2)
				throws SAXException {
			if (arg1.equals("a")) {
				this.mention = currentValue;
				entry.addEntity(this.mention, this.link, this.position);
				this.mention = null;
				this.link = null;
			}
		}

		@Override
		public void endPrefixMapping(String arg0) throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
				throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void processingInstruction(String arg0, String arg1)
				throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void setDocumentLocator(Locator arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void skippedEntity(String arg0) throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void startDocument() throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void startElement(String arg0, String arg1, String arg2,
				Attributes arg3) throws SAXException {
			if (arg1.equals("a")) {
				this.link = arg3.getValue("href");
				this.position = documentText.length();
			}

		}

		@Override
		public void startPrefixMapping(String arg0, String arg1)
				throws SAXException {
			// TODO Auto-generated method stub

		}

		class Entry {

			class Entity {
				private int position;
				private String link;
				private String mention;

				public int getPosition() {
					return position;
				}

				public void setPosition(int position) {
					this.position = position;
				}

				public String getLink() {
					return link;
				}

				/*
				 * Bugfix! Links that were redirections in Step 1 miss ".html".
				 */
				public void setLink(String link) {
					if (!link.endsWith(".html")) {
						this.link = link + ".html";
					} else {
						this.link = link;
					}
				}

				public String getMention() {
					return mention;
				}

				public void setMention(String mention) {
					this.mention = mention;
				}
			}

			private List<Entity> entitySet;
			private String text;

			public Entry() {
				super();
				this.entitySet = new LinkedList<Entity>();
			}

			public List<Entity> getEntitySet() {
				return entitySet;
			}

			public void setEntitySet(List<Entity> entitySet) {
				this.entitySet = entitySet;
			}

			public String getText() {
				return text;
			}

			public void setText(String text) {
				this.text = text;
			}

			public void addEntity(String mention, String link, int position) {
				Entity e = new Entity();
				e.setLink(link);
				e.setMention(mention);
				e.setPosition(position);
				this.entitySet.add(e);
			}
		}
	}

	public static void main(String[] args) {
		CreateWikipediaDocumentCentricKB kb = new CreateWikipediaDocumentCentricKB();
		kb.create(args[0], args[1]);
	}

}
