package doser.entitydisambiguation.algorithms.collective;

import java.util.LinkedList;
import java.util.List;

import doser.entitydisambiguation.dpo.DisambiguatedEntity;
import doser.entitydisambiguation.dpo.Response;
import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBaseDefault;

public abstract class AlgorithmDriver {

	protected Response[] currentResponse;

	protected List<SurfaceForm> rep;

	protected EntityCentricKnowledgeBaseDefault eckb;

	public AlgorithmDriver(Response[] res,
			List<SurfaceForm> rep,
			EntityCentricKnowledgeBaseDefault eckb) {
		if (res.length != rep.size()) {
			throw new IllegalArgumentException();
		}
		this.currentResponse = res;
		this.rep = rep;
		this.eckb = eckb;
	}

	public void generateResult() {
		for (int i = 0; i < currentResponse.length; i++) {
			SurfaceForm r = search(i);
			if (currentResponse[i] == null && r != null
					&& r.getCandidates().size() == 1) {
				Response res = new Response();
				List<DisambiguatedEntity> entList = new LinkedList<DisambiguatedEntity>();
				DisambiguatedEntity ent = new DisambiguatedEntity();
				ent.setEntityUri(r.getCandidates().get(0));
				ent.setText("ToDoText");
				entList.add(ent);
				res.setDisEntities(entList);
				res.setPosition(null);
				res.setSelectedText(r.getSurfaceForm());
				currentResponse[i] = res;
			}
		}
	}

	private SurfaceForm search(int qryNr) {
		for (SurfaceForm r : rep) {
			if (r.getQueryNr() == qryNr) {
				return r;
			}
		}
		return null;
	}

	public abstract void solve();

}
