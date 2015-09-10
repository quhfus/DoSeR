package experiments.webclassify.firstexperiments;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class ExtractWikipediaText implements ContentHandler {

	private StringBuffer documentText;

	public ExtractWikipediaText() {
		super();
		this.documentText = new StringBuffer();
	}

	@Override
	public void characters(char[] arg0, int arg1, int arg2)
			throws SAXException {
		documentText.append(new String(arg0, arg1, arg2));
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
	}

	@Override
	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
		// TODO Auto-generated method stub
	}

	public String getDocumentText() {
		return documentText.toString();
	}
}
