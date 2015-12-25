package doser.entitydisambiguation.algorithms.collective.hybrid;

import java.util.List;

import doser.entitydisambiguation.algorithms.collective.rules.RuleAdapation;
import doser.entitydisambiguation.dpo.Response;
import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBaseDefault;

public class CollectiveAndContextDriver extends AlgorithmDriver {

	static final int PREPROCESSINGCONTEXTSIZE = 200;

	private Doc2Vec d2v;

	public CollectiveAndContextDriver(Response[] res, List<SurfaceForm> rep,
			EntityCentricKnowledgeBaseDefault eckb) {
		super(res, rep, eckb);
		this.d2v = new Doc2Vec(rep, PREPROCESSINGCONTEXTSIZE);
	}

	@Override
	public void solve() {
		// First candidate pruning
		CandidatePruning pruning = new CandidatePruning(d2v, eckb);
		pruning.prune(rep);
		LocationDisambiguation locationDis = new LocationDisambiguation(d2v,
				eckb);
		locationDis.solve(rep);
		RuleAdapation rules = new RuleAdapation(eckb);
		rules.performRuleChainBeforeCandidateSelection(rep);

		Word2Vec w2v = new Word2Vec();

		Word2VecDisambiguator simple = new Word2VecDisambiguator(
				eckb.getFeatureDefinition(), rep, w2v);
		simple.setup();
		simple.solve();
		rep = simple.getRepresentation();
		FinalEntityDisambiguation finalDis = new FinalEntityDisambiguation(
				eckb.getFeatureDefinition(), rep, w2v);
		finalDis.setup();
		finalDis.solve();
	}
}
