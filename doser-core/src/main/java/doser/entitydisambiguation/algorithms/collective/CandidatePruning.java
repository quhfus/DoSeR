package doser.entitydisambiguation.algorithms.collective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBaseDefault;
import doser.general.HelpfulMethods;

public class CandidatePruning {

	private static final int ENTITYTHRESHOLD = 35;

	private EntityCentricKnowledgeBaseDefault eckb;

	CandidatePruning(EntityCentricKnowledgeBaseDefault eckb) {
		super();
		this.eckb = eckb;
	}

	void prune(List<CollectiveSFRepresentation> rep) {
		for (CollectiveSFRepresentation c : rep) {
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
				}
				c.setCandidates(new ArrayList<String>(prunedCandidates));
			}
		}
	}
}