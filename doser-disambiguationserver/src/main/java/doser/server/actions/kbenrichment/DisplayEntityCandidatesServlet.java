package doser.server.actions.kbenrichment;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import doser.entitydisambiguation.backend.DisambiguationMainService;
import doser.entitydisambiguation.backend.AbstractDisambiguationTask;
import doser.entitydisambiguation.backend.DisambiguationTaskSingle;
import doser.entitydisambiguation.dpo.DisambiguatedEntity;
import doser.entitydisambiguation.dpo.EntityDisambiguationDPO;
import doser.entitydisambiguation.dpo.Response;
import doser.entitydisambiguation.properties.Properties;

/**
 * Servlet implementation class DisplayEntityCandidatesServlet
 */
public class DisplayEntityCandidatesServlet extends HttpServlet {

	public static final String INDEX = Properties.getInstance()
			.getDBPediaIndex();

	public static final int RETURNEDDOCUMENTS = 20;

	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DisplayEntityCandidatesServlet() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String label = request.getParameter("searchlabel");
		Set<String> candidateSet = null;
		if (label == null) {
			candidateSet = new LinkedHashSet<String>();
		} else {
			candidateSet = checkLabelInIndex(label);
		}
		request.setAttribute("candidatesize",
				String.valueOf(candidateSet.size()));
		request.setAttribute("searchlabel", label);
		int counter = 0;
		for (String candidate : candidateSet) {
			request.setAttribute(
					(new StringBuffer("candidate").append(counter).toString()),
					candidate);
			counter++;
		}
		request.getRequestDispatcher("JSP/DisplayEntityCandidates.jsp")
				.forward(request, response);
	}

	private Set<String> checkLabelInIndex(String label) {
		Set<String> docSet = new LinkedHashSet<String>();
		List<AbstractDisambiguationTask> lst = new LinkedList<AbstractDisambiguationTask>();
		EntityDisambiguationDPO ent = new EntityDisambiguationDPO();
		String sfs = label;
		ent.setSelectedText(sfs);
		AbstractDisambiguationTask task = new DisambiguationTaskSingle(ent);
		task.setReturnNr(RETURNEDDOCUMENTS);
		task.setKbIdentifier("biomedcopy", "EntityCentric");
		task.setRetrieveDocClasses(true);
		lst.add(task);
		DisambiguationMainService.getInstance().disambiguate(lst);
		for (int i = 0; i < lst.size(); i++) {
			List<Response> res = lst.get(i).getResponse();
			List<DisambiguatedEntity> disList = res.get(0).getDisEntities();
			for (DisambiguatedEntity e : disList) {
				docSet.add(e.getEntityUri());
			}
		}
		return docSet;
	}
}
