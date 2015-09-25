package doser.entitydisambiguation.algorithms.collective.hybrid;

import java.util.List;

import doser.entitydisambiguation.algorithms.collective.AlgorithmDriver;
import doser.entitydisambiguation.algorithms.collective.CollectiveSFRepresentation;
import doser.entitydisambiguation.dpo.Response;
import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBaseDefault;

public class CollectiveAndContextDriver extends AlgorithmDriver {

	private Word2Vec w2v;
	
	public CollectiveAndContextDriver(Response[] res,
			List<CollectiveSFRepresentation> rep,
			EntityCentricKnowledgeBaseDefault eckb) {
		super(res, rep, eckb);
		this.w2v = new Word2Vec(rep);
	}

	@Override
	public void solve() {
		// First candidate pruning
		CandidatePruning pruning = new CandidatePruning(w2v, eckb);
		pruning.prune(rep);
//		DisambiguateSimpleCases simpleCases = new DisambiguateSimpleCases(w2v);
//		simpleCases.solve(rep);
	}
}
