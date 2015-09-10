package doc2vec.corpuscreation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

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

import com.google.gson.Gson;

import doc2vec.corpuscreation.ExtractContextOfWikipediaPages.Handler.Entry;
import doc2vec.corpuscreation.ExtractContextOfWikipediaPages.Handler.Entry.Entity;
import doser.tools.indexcreation.WikiPediaUriConverter;

public class ExtractContextOfWikipediaPages {

	public static int CHARSTOMOVE = 200;

	public static final String INDEX = "/home/zwicklbauer/NewIndexTryout";

	private HashSet<String> relevantEntities;

	public static void main(String[] args) {
		ExtractContextOfWikipediaPages p = new ExtractContextOfWikipediaPages();
		p.extractRelevantEntities();
		try {
			p.doAction(args[0], args[1]);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ExtractContextOfWikipediaPages() {
		super();
		this.relevantEntities = new HashSet<String>();
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

	public void doAction(String maindir, String outputfile)
			throws FileNotFoundException, IOException {
		File outputFile = new File(outputfile);
		PrintWriter pWriter = new PrintWriter(new FileWriter(outputFile, true));
		File d = new File(maindir);
		String[] files = d.list();
		Gson gson = new Gson();
		for (int i = 0; i < files.length; i++) {
			File cFile = new File(maindir + files[i]);
			String content = "";
			try {
				BufferedReader reader = new BufferedReader(
						new FileReader(cFile));
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
				Handler handler = new Handler();
				InputSource inputSource = new InputSource(new StringReader(
						content));
				xmlReader.setContentHandler(handler);
				xmlReader.parse(inputSource);
				Entry entry = handler.getEntry();
				printObject(pWriter, entry, gson);
				pWriter.flush();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		pWriter.close();
	}

	private void printObject(PrintWriter writer, Entry entry, Gson gson) {
		List<Entity> l = entry.getEntitySet();
		for (Entity e : l) {
			int position = e.getPosition();
			String text = entry.getText();
			int start = position - CHARSTOMOVE;
			start = getCorrectLeftWordBound(text, start);
			int end = position + CHARSTOMOVE;
			end = getCorrectRightWordBound(text, end);
			if (end > text.length()) {
				end = text.length() - 1;
			}
			try {
				String content = text.substring(start, position)
						+ " "
						+ e.getMention()
						+ " "
						+ text.substring(position + e.getMention().length(),
								end);
				Output out = new Output();
				content.replaceAll(" +", "");
				out.setContent(content);
				out.setUrl(e.getLink());
				out.setMention(e.getMention());
				// System.out.println(out.getEntity());
				if (relevantEntities.contains(out.getEntity())) {
					writer.println(gson.toJson(out, Output.class));
				}
			} catch (StringIndexOutOfBoundsException exception) {
				break;
			}
		}
	}

	private static int getCorrectLeftWordBound(String text, int pos) {
		if (pos < 0) {
			pos = 0;
		}
		while (true) {
			if (pos > 0) {
				char[] chars = { text.charAt(pos) };
				String t = new String(chars);
				if (t.equalsIgnoreCase(" ")) {
					++pos;
					break;
				}
				pos--;
			} else {
				break;
			}
		}
		return pos;
	}

	private static int getCorrectRightWordBound(String text, int pos) {
		if (pos > text.length()) {
			pos = text.length();
		}
		while (true) {
			if (pos < text.length()) {
				char[] chars = { text.charAt(pos) };
				String t = new String(chars);
				if (t.equalsIgnoreCase(" ")) {
					break;
				}
				pos++;
			} else {
				break;
			}
		}
		return pos;
	}

	public class Output {
		private String entity;
		private String content;
		private String mention;

		public String getEntity() {
			return entity;
		}

		public void setUrl(String url) {
			this.entity = url;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getMention() {
			return mention;
		}

		public void setMention(String mention) {
			this.mention = mention;
		}
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

				public void setLink(String link) {
					String withoutending = link.replaceAll(".html", "");
					String finalLink = WikiPediaUriConverter
							.createConformDBpediaUrifromEncodedString(withoutending);
					this.link = finalLink;
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
}
