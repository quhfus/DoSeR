package doser.entitydisambiguation.algorithms.rules;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import doser.entitydisambiguation.algorithms.SurfaceForm;
import doser.entitydisambiguation.knowledgebases.EntityCentricKBDBpedia;
import doser.lucene.query.TermQuery;

class ContextRule extends AbstractRule {

	private static final int MINDISAMBIGUATEDSURFACEFORMS = 2;

	private static final int MINIMUMSURFACEFORMS = 10;

	private static final float SIMILARITYTHRESHOLD = 1.57f;
	private static final float SIMILARITYTHRESHOLDMISC = 1.53f;

	private EntityCentricKBDBpedia eckb;
	
	ContextRule(EntityCentricKBDBpedia eckb) {
		super(eckb);
		this.eckb = eckb;
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
						Set<String> levenshteinAdded = new HashSet<String>();
						for (String s : l) {
							String query = this.eckb.generateWord2VecFormatString(list, s);
							w2vFormatStrings.add(query);
							Map<String, Float> similarityMap = this.eckb.getWord2VecSimilarities(w2vFormatStrings);
							float simValue = similarityMap.get(query);
							// Check for Appropriate entities
							String candidateWithoutUrl = s.replaceAll("http://dbpedia.org/resource/", "").toLowerCase();
							if (levenshteinDistance(candidateWithoutUrl, sf.getSurfaceForm().toLowerCase()) <= 2) {
								System.out.println("LEVENSHTEIN DISTANCE ENTITY: " + s);
							}
							if (simValue > SIMILARITYTHRESHOLD
									|| (queryType(s).equalsIgnoreCase("Misc") && simValue > SIMILARITYTHRESHOLDMISC)) {
								bestCandidate.add(s);
							} else if (levenshteinDistance(candidateWithoutUrl,
									sf.getSurfaceForm().toLowerCase()) <= 2) {
								bestCandidate.add(s);
								levenshteinAdded.add(s);
							}
						}
						// Disambiguate and assign entity
						if (!bestCandidate.isEmpty()) {
							boolean notOnlyLevenshtein = false;
							for (String s : bestCandidate) {
								if (!levenshteinAdded.contains(s)) {
									notOnlyLevenshtein = true;
								}
							}
							if (notOnlyLevenshtein) {
								sf.setCandidates(bestCandidate);
								System.out.println("Es bleibt übrig SurfaceForm: " + sf.getSurfaceForm() + "   +"
										+ bestCandidate.toString());
							}
						}
					}
				}
			}
		}
		return false;
	}

	private String queryType(String url) {
		String type = "";
		IndexSearcher searcher = eckb.getSearcher();
		Query q = new TermQuery(new Term("Mainlink", url));
		try {
			TopDocs docs = searcher.search(q, 1);
			ScoreDoc[] scoredocs = docs.scoreDocs;
			if (scoredocs.length == 0) {
				type = "Misc";
			} else {
				int nr = scoredocs[0].doc;
				Document doc = searcher.getIndexReader().document(nr);
				type = doc.get("Type");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return type;
	}

	int levenshteinDistance(CharSequence lhs, CharSequence rhs) {
		int len0 = lhs.length() + 1;
		int len1 = rhs.length() + 1;

		// the array of distances
		int[] cost = new int[len0];
		int[] newcost = new int[len0];

		// initial cost of skipping prefix in String s0
		for (int i = 0; i < len0; i++)
			cost[i] = i;

		// dynamically computing the array of distances

		// transformation cost for each letter in s1
		for (int j = 1; j < len1; j++) {
			// initial cost of skipping prefix in String s1
			newcost[0] = j;

			// transformation cost for each letter in s0
			for (int i = 1; i < len0; i++) {
				// matching current letters in both strings
				int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;

				// computing cost for each transformation
				int cost_replace = cost[i - 1] + match;
				int cost_insert = cost[i] + 1;
				int cost_delete = newcost[i - 1] + 1;

				// keep minimum cost
				newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
			}

			// swap cost/newcost arrays
			int[] swap = cost;
			cost = newcost;
			newcost = swap;
		}

		// the distance is the cost for transforming all letters in both strings
		return cost[len0 - 1];
	}
}
