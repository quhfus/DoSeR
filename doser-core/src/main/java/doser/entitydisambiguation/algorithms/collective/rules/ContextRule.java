package doser.entitydisambiguation.algorithms.collective.rules;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import doser.entitydisambiguation.algorithms.collective.hybrid.SurfaceForm;
import doser.entitydisambiguation.algorithms.collective.hybrid.Word2Vec;
import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBaseDefault;

public class ContextRule extends Rule {

	private static final int MINDISAMBIGUATEDSURFACEFORMS = 2;

	private static final int MINIMUMSURFACEFORMS = 10;

	private static final float SIMILARITYTHRESHOLD = 1.57f;

	private Word2Vec w2v;

	public ContextRule(EntityCentricKnowledgeBaseDefault eckb, Word2Vec w2v) {
		super(eckb);
		this.w2v = w2v;
	}

	@Override
	public boolean applyRule(List<SurfaceForm> rep) {
		if (rep.size() > MINIMUMSURFACEFORMS) {
			List<String> list = new LinkedList<String>();
			for (SurfaceForm sf : rep) {
				if (rep.size() > 1 && sf.getCandidates().size() == 1 && sf.isInitial()) {
					list.add(sf.getCandidates().get(0));
				}
			}
			if (list.size() >= MINDISAMBIGUATEDSURFACEFORMS) {
				Set<String> w2vFormatStrings = new HashSet<String>();
				for (SurfaceForm sf : rep) {
					if (rep.size() > 1 && sf.getCandidates().size() > 1) {
						List<String> l = sf.getCandidates();
						List<String> bestCandidate = new LinkedList<String>();
//						System.out.println("CANDIDATES SURFACEFORM: "+sf.getSurfaceForm()+"    "+l.toString());
						for (String s : l) {
							String query = this.w2v.generateWord2VecFormatString(list, s);
							w2vFormatStrings.add(query);
							Map<String, Float> similarityMap = this.w2v.getWord2VecSimilarities(w2vFormatStrings);
							float simValue = similarityMap.get(query);
							// Check for Appropriate entities
//							System.out.println("RESULTS: "+list.toString() + "  "+s+"     "+simValue);
							if (simValue > SIMILARITYTHRESHOLD) {
								bestCandidate.add(s);
							}
						}
						// Disambiguate and assign entity
						if (!bestCandidate.isEmpty()) {
							sf.setCandidates(bestCandidate);
							System.out.println("Es bleibt übrig SurfaceForm: "+sf.getSurfaceForm() + "   +"+bestCandidate.toString());
						}
					}
				}
			}
		}
		return false;
	}

	// Make it faster maybe
	private void disambiguateTerms(String str, List<SurfaceForm> rep) {
		List<String> list = new LinkedList<String>();
		for (SurfaceForm sf : rep) {
			if (rep.size() > 1 && sf.getCandidates().size() == 1) {
				list.add(sf.getCandidates().get(0));
			}
		}

		if (list.size() >= MINDISAMBIGUATEDSURFACEFORMS) {
			Set<String> w2vFormatStrings = new HashSet<String>();
			for (SurfaceForm sf : rep) {
				if (rep.size() > 1 && sf.getCandidates().size() > 1) {
					List<String> l = sf.getCandidates();
					List<String> bestCandidate = new LinkedList<String>();
					for (String s : l) {
						String st = s.replaceAll("http://dbpedia.org/resource/", "").toLowerCase();
						if (st.contains("_" + str)) {
							w2vFormatStrings.clear();
							String query = this.w2v.generateWord2VecFormatString(list, s);
							w2vFormatStrings.add(query);
							Map<String, Float> similarityMap = this.w2v.getWord2VecSimilarities(w2vFormatStrings);
							float simValue = similarityMap.get(query);
							// Check for Appropriate entities
							System.out.println(
									"SurfaceForm" + sf.getSurfaceForm() + "Candidate: " + s + "    " + simValue);
							if (simValue > SIMILARITYTHRESHOLD) {
								bestCandidate.add(s);
							}
						}
					}
					// Disambiguate and assign entity
					if (!bestCandidate.isEmpty()) {
						sf.setCandidates(bestCandidate);
					}
				}
			}
		}
	}
}
