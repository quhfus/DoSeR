package experiments.table.limaye.corrected;



import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

import experiments.table.limaye.corrected.Table.Column;

public class LimayeAnnotationParserWebTables implements ContentHandler {
	private Table table;

	private StringBuilder currentValue;

	private boolean header;

	private int column;
	
	private Model m;

	private Model m_d;
	
	private Model m_l;
	
	public LimayeAnnotationParserWebTables(Model m, Model m_l, Model m_d) {
		this.table = new Table();
		this.column = -1;
		this.currentValue = new StringBuilder();
		this.header = false;
		
		this.m = m;
		this.m_d = m_d;
		this.m_l = m_l;
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
		if(localName.equals("header")) {
			this.header = true;
		}
		
		if(localName.equals("cell") && this.header) {
			this.table.addColumn("");
		}
		
		if(localName.equals("cell")) {
			column++;
			currentValue = new StringBuilder();
		}
		
		if(localName.equals("row")) {
			column = -1;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		if(localName.equals("html")) {
			String cellValue = escape(currentValue.toString().trim());
			Column c = table.getColumn(column);
			c.addCell(cellValue);
			currentValue = new StringBuilder();
		}
		
		if(localName.equals("wikipedia")) {
			String gt = "http://dbpedia.org/resource/"+currentValue.toString().trim();
			gt = checkRedirects(gt);
			gt = checkAvailability(gt);
//			if (!gt.equalsIgnoreCase("")) {
//				gt = checkDisambiguationPage(gt);
//			}
			
//			gt = checkAvailability(gt);
//			if (!gt.equalsIgnoreCase("")) {
//				System.out.println("Groundtruth"+gt);
//			}
			
			table.getColumn(column).addLastCellGT(gt);
			currentValue = new StringBuilder();
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (currentValue != null) {
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

	private String checkAvailability(String resource) {
		try {
			Query query = QueryFactory
					.create("SELECT ?label WHERE{ <"
							+ resource
							+ "> <http://www.w3.org/2000/01/rdf-schema#label> ?label. }");
			QueryExecution qe = QueryExecutionFactory.create(query, this.m_l);
			ResultSet results = qe.execSelect();
			if (results.hasNext()) {
				return resource;
			}
		} catch (Exception e) {
			return "";
		}
		return "";
	}

	private String checkRedirects(String resource) {
		String result = resource;
		try {

			Query query = QueryFactory
					.create("SELECT ?redirect WHERE{ <"
							+ resource
							+ "> <http://dbpedia.org/ontology/wikiPageRedirects> ?redirect. }");
			QueryExecution qe = QueryExecutionFactory.create(query, this.m);
			ResultSet results = qe.execSelect();
			while (results.hasNext()) {
				QuerySolution sol = results.nextSolution();
				result = sol.getResource("redirect").getURI();
			}
		} catch (Exception e) {
			return resource;
		}
		return result;
	}
}
