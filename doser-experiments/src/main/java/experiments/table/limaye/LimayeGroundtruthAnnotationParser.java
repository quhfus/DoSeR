package experiments.table.limaye;

import java.io.IOException;
import java.net.URLDecoder;

import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;
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
import com.hp.hpl.jena.rdf.model.ModelFactory;

import experiments.table.limaye.Table.Column;
import experiments.table.limaye.Table.Column.Cell;


public class LimayeGroundtruthAnnotationParser implements ContentHandler {

	private final static String REDIRECTS = "/home/quh/HDT/dbpedia_redirects.hdt";

	private final static String LABELS = "/home/quh/HDT/dbpedia_labels.hdt";

	private final static String DISAMBIGUATION = "/home/quh/HDT/dbpedia_disambiguation.hdt";

	private Table table;

	private StringBuilder currentValue;

	private boolean cellAnnotation;

	private boolean columnAnnotation;

	private int columnNr;

	private int column;

	private int row;

	private Model m;

	private Model m_l;

	private Model m_d;

	private boolean flag = false;

	public LimayeGroundtruthAnnotationParser(Table table) {
		this.table = table;
		this.cellAnnotation = false;
		this.columnAnnotation = false;
		this.columnNr = 0;
		this.column = -1;
		this.row = -1;
		this.currentValue = new StringBuilder();
		HDT hdt = null;
		HDT hdt_l = null;
		HDT hdt_d = null;
		try {
			hdt = HDTManager.mapIndexedHDT(REDIRECTS, null);
			hdt_l = HDTManager.mapIndexedHDT(LABELS, null);
			hdt_d = HDTManager.mapIndexedHDT(DISAMBIGUATION, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		HDTGraph graph = new HDTGraph(hdt);
		m = ModelFactory.createModelForGraph(graph);
		graph = new HDTGraph(hdt_d);
		m_d = ModelFactory.createModelForGraph(graph);
		graph = new HDTGraph(hdt_l);
		m_l = ModelFactory.createModelForGraph(graph);

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
		if (localName.equals("cellAnnotatoons")) {
			this.cellAnnotation = true;
		}

		if (localName.equals("columnAnnotations")) {
			this.columnAnnotation = true;
		}

		if (columnAnnotation && localName.equals("anno")) {
			String gt = atts.getValue("name");
			if (table.getColumn(columnNr) != null) {
//				table.getColumn(columnNr).addTypeGt("http://yago-knowledge.org/resource/"+gt);
			}
			// System.out.println(gt);
		}

		if (columnAnnotation && localName.equals("colAnnos")) {
			this.columnNr = Integer.parseInt(atts.getValue("col"));
		}

		if (cellAnnotation && localName.equals("row")) {
			row++;
			column = -1;
		}

		if (cellAnnotation && localName.equals("entity")) {
			flag = true;
			column++;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (localName.equals("cellAnnotatoons")) {
			this.cellAnnotation = false;
		}

		if (cellAnnotation && localName.equals("entity")) {
			Column c = table.getColumn(column);
			// Hack wegen Carriage Return bei self closing xml tag
			// Sollte aber nichts ausmachen beim Limaye Datensatz
			if (currentValue.toString().length() > 1
					&& currentValue.toString() != "NULL") {
				try {
					if (c != null) {
						Cell ce = c.getCellList().get(row);
						String gt = "http://dbpedia.org/resource/"
								+ URLDecoder
										.decode(checkRedirects(unescapeHTMLCharacters(currentValue
												.toString())), "UTF-8");
						gt = checkAvailability(gt);
						if (!gt.equalsIgnoreCase("")) {
							gt = checkDisambiguationPage(gt);
						}
						ce.setGt(gt);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				if (c != null) {
					c.getCellList().get(row).setGt("");
				}
			}
			flag = false;
			;
			currentValue = new StringBuilder();
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

	private String checkDisambiguationPage(String resource) {
		try {
			Query query = QueryFactory
					.create("SELECT ?dis WHERE{ <"
							+ resource
							+ "> <http://dbpedia.org/ontology/wikiPageDisambiguates> ?dis. }");
			QueryExecution qe = QueryExecutionFactory.create(query, this.m_d);
			ResultSet results = qe.execSelect();
			if (!results.hasNext()) {
				return resource;
			}
		} catch (Exception e) {
			return "";
		}
		return "";
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
					.create("SELECT ?redirect WHERE{ <http://dbpedia.org/resource/"
							+ resource
							+ "> <http://dbpedia.org/ontology/wikiPageRedirects> ?redirect. }");
			QueryExecution qe = QueryExecutionFactory.create(query, this.m);
			ResultSet results = qe.execSelect();
			while (results.hasNext()) {
				QuerySolution sol = results.nextSolution();
				result = sol.getResource("redirect").toString();
				String splitter[] = result.split("/");
				result = splitter[splitter.length - 1];
			}
		} catch (Exception e) {
			return resource;
		}
		return result;
	}

	private String unescapeHTMLCharacters(String resource) {
		String res = resource;
		if (res.contains("&amp;apos;")) {
			res = res.replace("&amp;apos;", "'");
		}
		if (res.contains("\\amp")) {
			res = res.replace("\\amp", "&");
		}
		return res;
	}
}
