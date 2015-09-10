package doser.server.actions.kbenrichment;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import doser.entitydisambiguation.properties.Properties;
import doser.lucene.analysis.DoserIDAnalyzer;

/**
 * Servlet implementation class ShowIndexEntityServlet
 * 
 * This class provides the necessary operations to query a specific document in
 * our DbPediaBiomedCopy index. The propertie fields label, descriptions and
 * synonyms are extracted.
 */
public class ShowIndexEntityServlet extends HttpServlet {

	public static final String INDEX = Properties.getInstance()
			.getDbPediaBiomedCopyKB();

	private static final String INDEXMAINFIELD = "Mainlink";

	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ShowIndexEntityServlet() {
		super();
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String uri = request.getParameter("newurl");
		Document doc = checkUriInIndex(uri);
		String label = extractLabel(doc);
		String description = extractDescription(doc);
		String synonymsHtml = extractSynonyms(doc);

		request.setAttribute("uri", uri);
		request.setAttribute("searchlabel", request.getParameter("searchlabel"));
		request.setAttribute("label", label);
		request.setAttribute("description", description);
		request.setAttribute("synonyms", synonymsHtml);
		if(doc == null) {
			request.setAttribute("mode", "new");
			request.setAttribute("newid", generateNewDocumentID());
		}

		request.getRequestDispatcher("JSP/ModifyEntity.jsp").forward(request,
				response);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String uri = request.getParameter("entry");
		Document doc = checkUriInIndex(uri);
		
		String label = extractLabel(doc);
		String description = extractDescription(doc);
		String synonymsHtml = extractSynonyms(doc);

		request.setAttribute("uri", uri);
		request.setAttribute("searchlabel", request.getParameter("searchlabel"));
		request.setAttribute("label", label);
		request.setAttribute("description", description);
		request.setAttribute("synonyms", synonymsHtml);

		request.getRequestDispatcher("JSP/ModifyEntity.jsp").forward(request,
				response);
	}

	private Document checkUriInIndex(String uri) {
		/* This Uri is not URL encoded for knowledge base matching */
		String[] splitter = uri.split("/");
		String entity = splitter[splitter.length - 1];
		StringBuffer buffer = new StringBuffer();
		try {
			for (int i = 0; i < splitter.length - 1; i++) {
				buffer.append(splitter[i]);
				buffer.append("/");
			}
			buffer.append(URLEncoder.encode(entity, "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			Logger.getRootLogger().error(e1.getStackTrace());
		}
		
		Document doc = null;
		try {
			final Directory dir = FSDirectory.open(new File(INDEX));
			final IndexReader reader = DirectoryReader.open(dir);
			final IndexSearcher searcher = new IndexSearcher(reader);
			final QueryParser qp = new QueryParser(INDEXMAINFIELD, new DoserIDAnalyzer());
			final TopDocs top = searcher.search(
					qp.parse(QueryParserBase.escape(buffer.toString())), 1);
			final ScoreDoc[] scores = top.scoreDocs;
			if (scores.length == 1) {
				doc = reader.document(scores[0].doc);
			}
			reader.close();
		} catch (IOException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		} catch (ParseException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
		return doc;
	}

	private static String extractLabel(Document doc) {
		StringBuffer result = new StringBuffer();
		if (doc != null) {
			String s = doc.get("Label");
			if (s != null) {
				result.append(s);
			}
		}
		return result.toString();
	}

	private static String extractDescription(Document doc) {
		StringBuffer result = new StringBuffer();
		if (doc != null) {
			String s = doc.get("Description");
			if (s != null) {
				result.append(s);
			}
		}
		return result.toString();
	}

	private static String extractSynonyms(Document doc) {
		StringBuffer result = new StringBuffer();
		if (doc != null) {
			IndexableField[] fields = doc.getFields("UniqueLabelString");
			if (fields != null) {
				for (int i = 0; i < fields.length; i++) {
					IndexableField f = fields[i];
					result.append(f.stringValue());
					if ((i + 1) != fields.length) {
						result.append("&#13;&#10;");
					}
				}
			}
		}
		return result.toString();
	}
	
	/**
	 * Id generator for our new document!
	 * 
	 * @return The id for our new document
	 */
	private String generateNewDocumentID() {
		String id = null;
		Random random = new Random();
		id = "Manual_"+String.valueOf(random.nextInt());
		while(checkIdExistence(id)) {
			id = "Manual_"+String.valueOf(random.nextInt());
		}
		return id;
	}
	
	private boolean checkIdExistence(String id) {
		Document doc = null;
		try {
			final Directory dir = FSDirectory.open(new File(INDEX));
			final IndexReader reader = DirectoryReader.open(dir);
			final IndexSearcher searcher = new IndexSearcher(reader);
			final QueryParser qp = new QueryParser("ID", new DoserIDAnalyzer());
			final TopDocs top = searcher.search(
					qp.parse(QueryParserBase.escape(id)), 1);
			final ScoreDoc[] scores = top.scoreDocs;
			if (scores.length == 1) {
				doc = reader.document(scores[0].doc);
			}
			reader.close();
		} catch (IOException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		} catch (ParseException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
		return (doc == null) ? false : true;
	}
}
