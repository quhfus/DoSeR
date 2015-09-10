package experiments.table.limaye;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class LimayeAnnotationParserWebTables implements ContentHandler {

	private Table table;

	private StringBuilder currentValue;

	private boolean content;

	private boolean flag = false;

	private int column;

	private boolean firstRow = true;
	
	public LimayeAnnotationParserWebTables() {
		this.table = new Table();
		this.content = false;
		this.column = -1;
		this.currentValue = new StringBuilder();
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
		if (localName.equals("row")) {
//			table.addColumn("");
			this.content = true;
//			column++;
		}
		
		if (localName.equals("text") && content) {
			flag = true;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(localName.equals("text") && this.firstRow && content) {
			table.addColumn("");
			column++;
			table.getColumn(column).addCell(currentValue.toString());
			currentValue = new StringBuilder();
			flag = false;
		}
		
		if (localName.equals("text") && content && !this.firstRow) {
			column++;
			table.getColumn(column).addCell(currentValue.toString());
			currentValue = new StringBuilder();
			flag = false;
		}

		if (localName.equals("row")) {
			column = -1;
			this.content = false;
			if(firstRow) {
				firstRow = false;
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (currentValue != null && flag) {
			for (int i = start; i < start + length; i++) {
				currentValue.append(ch[i]);
			}
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

	public Table getTable() {
		return table;
	}

	private String unescapeHTMLCharacters(String resource) {
		String res = resource;
		if (res.contains("&amp;apos;")) {
			res = res.replace("&amp;apos;", "'");
		}

		if (res.contains("&apos;")) {
			res = res.replace("&apos;", "'");
		}
		
		if(res.contains("&quot;")) {
			res = res.replace("&quot;", "");
		}
		return res;
	}

	public static String removeAccents(String notNullSource) {
		return notNullSource;
		// return Normalizer.normalize(notNullSource,
		// Normalizer.Form.NFD).replaceAll(
		// "\\p{InCombiningDiacriticalMarks}+", "");

	}

}
