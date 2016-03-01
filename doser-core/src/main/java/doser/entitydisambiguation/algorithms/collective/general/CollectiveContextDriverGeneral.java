package doser.entitydisambiguation.algorithms.collective.general;

import java.util.LinkedList;
import java.util.List;

import doser.entitydisambiguation.algorithms.SurfaceForm;
import doser.entitydisambiguation.algorithms.collective.CandidatePruning;
import doser.entitydisambiguation.algorithms.rules.RuleAdapation;
import doser.entitydisambiguation.dpo.DisambiguatedEntity;
import doser.entitydisambiguation.dpo.Response;
import doser.entitydisambiguation.knowledgebases.AbstractEntityCentricKBGeneral;

class CollectiveContextDriverGeneral {

	static final int PREPROCESSINGCONTEXTSIZE = 200;
	
	private Response[] currentResponse;
	private List<SurfaceForm> rep;
	private AbstractEntityCentricKBGeneral eckb;
	
	CollectiveContextDriverGeneral(Response[] res, List<SurfaceForm> rep, AbstractEntityCentricKBGeneral eckb) {
		super();
		this.currentResponse = res;
		this.rep = rep;
		this.eckb = eckb;
	}
	
	void solve() {
		// First candidate pruning
		CandidatePruning pruning = new CandidatePruning(eckb);
		pruning.prune(rep);

		RuleAdapation rules = new RuleAdapation();
		rules.addNoCandidatesCheckPluralRule(eckb);
		rules.addNoCandidatesExpansionRule(eckb);
		rules.performRuleChainBeforeCandidateSelection(rep);

		CandidateReductionGeneralW2V w2vreduction = new CandidateReductionGeneralW2V(eckb, rep, 20, 5, 125, false, false);
		w2vreduction.solve();
		rep = w2vreduction.getRep();

		w2vreduction = new CandidateReductionGeneralW2V(eckb, rep, 45, 5, 250, true, true);
		w2vreduction.solve();
		rep = w2vreduction.getRep();
		FinalEntityDisambiguation finalDis = new FinalEntityDisambiguation(eckb, rep);
		finalDis.setup();
		finalDis.solve();
	}
	
	void generateResult() {
		for (int i = 0; i < currentResponse.length; i++) {
			SurfaceForm r = search(i);
			if (currentResponse[i] == null && r != null && r.getCandidates().size() == 1) {
				Response res = new Response();
				List<DisambiguatedEntity> entList = new LinkedList<DisambiguatedEntity>();
				DisambiguatedEntity ent = new DisambiguatedEntity();
				ent.setEntityUri(r.getCandidates().get(0));
				ent.setText("ToDoText");
				entList.add(ent);
				res.setDisEntities(entList);
				res.setStartPosition(-1);
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
	
}
