package doser.entitydisambiguation.algorithms.collective.hybrid;

import java.util.List;

import doser.entitydisambiguation.algorithms.collective.AlgorithmDriver;
import doser.entitydisambiguation.algorithms.collective.SurfaceForm;
import doser.entitydisambiguation.algorithms.collective.rules.RuleAdapation;
import doser.entitydisambiguation.dpo.Response;
import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBaseDefault;

public class CollectiveAndContextDriver extends AlgorithmDriver {

	static final int PREPROCESSINGCONTEXTSIZE = 350;

	private Word2Vec w2v;

	public CollectiveAndContextDriver(Response[] res, List<SurfaceForm> rep,
			EntityCentricKnowledgeBaseDefault eckb) {
		super(res, rep, eckb);
		this.w2v = new Word2Vec(rep, PREPROCESSINGCONTEXTSIZE);
	}

	@Override
	public void solve() {
		// First candidate pruning
		CandidatePruning pruning = new CandidatePruning(w2v, eckb);
		pruning.prune(rep);
		LocationDisambiguation locationDis = new LocationDisambiguation(w2v,
				eckb);
		locationDis.solve(rep);
		RuleAdapation rules = new RuleAdapation(eckb);
		rules.performRuleChainBeforeCandidateSelection(rep);
		FinalSolving finalSolve = new FinalSolving(rep, eckb);
		finalSolve.solve();
	}
}
