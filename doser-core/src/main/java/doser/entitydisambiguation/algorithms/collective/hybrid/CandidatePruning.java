package doser.entitydisambiguation.algorithms.collective.hybrid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBaseDefault;
import doser.general.HelpfulMethods;

public class CandidatePruning {

	private static final int ENTITYTHRESHOLD = 6;

	private static final int MINIMUMSURFACEFORMS = 3;

	private static final float WORD2VECTHRESHOLD = 1.6f;

	private Doc2Vec d2v;
	private Word2Vec w2v;

	private EntityCentricKnowledgeBaseDefault eckb;

	CandidatePruning(Word2Vec w2v, Doc2Vec d2v, EntityCentricKnowledgeBaseDefault eckb) {
		super();
		this.d2v = d2v;
		this.eckb = eckb;
		this.w2v = w2v;
	}

	void prune(List<SurfaceForm> rep) {
		List<SurfaceForm> unambiguous = new LinkedList<SurfaceForm>();
		for (SurfaceForm c : rep) {
			if (c.getCandidates().size() == 1) {
				unambiguous.add(c);
			}
		}

		List<String> list = new LinkedList<String>();
		for (SurfaceForm sf : rep) {
			if (rep.size() > 1 && sf.getCandidates().size() == 1 && sf.isInitial()) {
				list.add(sf.getCandidates().get(0));
			}
		}

		for (SurfaceForm c : rep) {
			List<String> candidates = c.getCandidates();
			if (candidates.size() > ENTITYTHRESHOLD) {
				Set<String> prunedCandidates = new HashSet<String>();

				// Sense Prior
				Map<String, Integer> map = new HashMap<String, Integer>();
				for (String candidate : candidates) {
					map.put(candidate, eckb.getFeatureDefinition().getOccurrences(c.getSurfaceForm(), candidate));
				}
				@SuppressWarnings("deprecation")
				List<Map.Entry<String, Integer>> l = HelpfulMethods.sortByValue(map);
				for (int i = 0; i < ENTITYTHRESHOLD; ++i) {
					prunedCandidates.add(l.get(i).getKey());
					// System.out.println("SensePrior ADd: "+l.get(i).getKey()+"
					// "+l.get(i).getValue());
				}

				// Doc2Vec ContextSimilarity
				Map<String, Float> map_doc2vec = new HashMap<String, Float>();
				for (String candidate : candidates) {

					map_doc2vec.put(candidate, d2v.getDoc2VecSimilarity(c.getSurfaceForm(), c.getContext(), candidate));
				}
				@SuppressWarnings("deprecation")
				List<Map.Entry<String, Float>> l_doc2vec = HelpfulMethods.sortByValue(map_doc2vec);
				for (int i = 0; i < ENTITYTHRESHOLD; ++i) {
					prunedCandidates.add(l_doc2vec.get(i).getKey());
				}

				// Check for very relevant Candidates via given Word2Vec
				// similarities
				if (list.size() >= MINIMUMSURFACEFORMS) {
					Set<String> w2vFormatStrings = new HashSet<String>();
					for (String can : candidates) {
						if (!prunedCandidates.contains(can)) {
							String query = this.w2v.generateWord2VecFormatString(list, can);
							w2vFormatStrings.add(query);
						}
					}

					Map<String, Float> similarityMap = this.w2v.getWord2VecSimilarities(w2vFormatStrings);
					for (String can : candidates) {
						if (!prunedCandidates.contains(can)) {
							String query = this.w2v.generateWord2VecFormatString(list, can);
							float val = similarityMap.get(query);
							if (val > WORD2VECTHRESHOLD) {
								System.out.println("Ich add noch die SurfaceForm: " + can + "  " + val);
								prunedCandidates.add(can);
							}
						}
					}
				}

				c.setCandidates(new ArrayList<String>(prunedCandidates));
			}
		}
	}
}
