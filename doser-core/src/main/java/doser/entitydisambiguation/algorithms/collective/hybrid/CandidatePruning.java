package doser.entitydisambiguation.algorithms.collective.hybrid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import doser.entitydisambiguation.algorithms.collective.SurfaceForm;
import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBaseDefault;
import doser.general.HelpfulMethods;

public class CandidatePruning {

	private static int ENTITYTHRESHOLD = 10;

	private Word2Vec w2v;

	private EntityCentricKnowledgeBaseDefault eckb;

	CandidatePruning(Word2Vec w2v, EntityCentricKnowledgeBaseDefault eckb) {
		super();
		this.w2v = w2v;
		this.eckb = eckb;
	}

	void prune(List<SurfaceForm> rep) {
		for (SurfaceForm c : rep) {
			List<String> candidates = c.getCandidates();

			if (candidates.size() > ENTITYTHRESHOLD) {
				Set<String> prunedCandidates = new HashSet<String>();

				// Sense Prior
				Map<String, Integer> map = new HashMap<String, Integer>();
				for (String candidate : candidates) {
					map.put(candidate, eckb.getFeatureDefinition()
							.getOccurrences(c.getSurfaceForm(), candidate));
				}
				@SuppressWarnings("deprecation")
				List<Map.Entry<String, Integer>> l = HelpfulMethods
						.sortByValue(map);
				for (int i = 0; i < ENTITYTHRESHOLD; ++i) {
					prunedCandidates.add(l.get(i).getKey());
					//System.out.println("SensePrior ADd: "+l.get(i).getKey()+"  "+l.get(i).getValue());
				}

				// Doc2Vec ContextSimilarity
				Map<String, Float> map_doc2vec = new HashMap<String, Float>();
				for (String candidate : candidates) {
					map_doc2vec.put(
							candidate,
							w2v.getDoc2VecSimilarity(c.getSurfaceForm(),
									c.getContext(), candidate));
				}
				@SuppressWarnings("deprecation")
				List<Map.Entry<String, Float>> l_doc2vec = HelpfulMethods
						.sortByValue(map_doc2vec);
				for (int i = 0; i < ENTITYTHRESHOLD; ++i) {
					prunedCandidates.add(l_doc2vec.get(i).getKey());
				}

				c.setCandidates(new ArrayList<String>(prunedCandidates));
			}
		}
	}
}
