package doser.server.actions.kbenrichment;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import doser.entitydisambiguation.modknowledgebase.dpo.DocumentToProcess;
import doser.entitydisambiguation.modknowledgebase.dpo.EntryToProcess;
import doser.entitydisambiguation.modknowledgebase.dpo.KBEnrichmentRequest;
import doser.entitydisambiguation.properties.Properties;

/**
 * Servlet implementation class StoreIndexModificationServlet
 */
public class StoreIndexModificationServlet extends HttpServlet {


	public static final String INDEX = Properties.getInstance().getDbPediaBiomedCopyKB();

	private static final long serialVersionUID = 1L;

	private KBEnrichmentService service;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public StoreIndexModificationServlet() {
		super();
		this.service = new KBEnrichmentService();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		KBEnrichmentRequest enrequest = new KBEnrichmentRequest();

		// Set knowledge base
		enrequest.setKburi(INDEX);

		// Set knowledge base
		enrequest.setPrimaryKeyField("Mainlink");

		// Set knowledge base operation
		enrequest.setCommand("UPDATEDOCUMENTS");

		// Set knowledge base entries
		final DocumentToProcess doc = new DocumentToProcess();
		List<EntryToProcess> entryList = new LinkedList<EntryToProcess>();

		EntryToProcess process1 = new EntryToProcess();
		process1.setFieldName("Label");
		process1.setValue(request.getParameter("label"));
		entryList.add(process1);

		EntryToProcess process2 = new EntryToProcess();
		process2.setFieldName("Description");
		process2.setValue(request.getParameter("description"));
		entryList.add(process2);

		EntryToProcess process3 = new EntryToProcess();
		process3.setFieldName("UniqueLabelString");
		process3.setValue(request.getParameter("synonyms"));
		entryList.add(process3);

		doc.setKey(request.getParameter("uri"));
		doc.setEntryList(entryList);

		List<DocumentToProcess> docList = new LinkedList<DocumentToProcess>();
		docList.add(doc);

		enrequest.setFieldAction("OVERRIDEFIELD");
		enrequest.setDocList(docList);
		service.enrich(enrequest);

		request.setAttribute("info", "IndexModification successful");
		ServletContext servletContext = getServletContext();
		RequestDispatcher requestDispatcher = servletContext
				.getRequestDispatcher("/DisplayEntityCandidatesServlet");
		requestDispatcher.forward(request, response);
	}

}
