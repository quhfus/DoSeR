package experiments.table.limaye;

import org.apache.commons.lang3.StringEscapeUtils;
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
			String cellValue = escape(currentValue.toString());
			table.getColumn(column).addCell(cellValue);
			currentValue = new StringBuilder();
			flag = false;
		}
		
		if (localName.equals("text") && content && !this.firstRow) {
			column++;
			String cellValue = escape(currentValue.toString());
			table.getColumn(column).addCell(cellValue);
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

	public static String removeAccents(String notNullSource) {
		return notNullSource;
		// return Normalizer.normalize(notNullSource,
		// Normalizer.Form.NFD).replaceAll(
		// "\\p{InCombiningDiacriticalMarks}+", "");

	}
	
	private String escape(String s) {
		String val = StringEscapeUtils.unescapeHtml4(s);
		val = val.replaceAll("&amp;", "&");
		return val.replaceAll("&apos;", "'");
	}
	
	public static void main(String args[]) {
		String test = "&apos;";
		System.out.println(StringEscapeUtils.unescapeHtml4(test).replaceAll("&apos;", "'"));
	}

}
