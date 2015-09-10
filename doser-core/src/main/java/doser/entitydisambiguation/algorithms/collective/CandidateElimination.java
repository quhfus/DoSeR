package doser.entitydisambiguation.algorithms.collective;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections15.Factory;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBaseDefault;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public class CandidateElimination extends Word2VecPageRank {

	private int maxCandidates;
	
	private List<CollectiveSFRepresentation> allDocumentCandidates;

	public CandidateElimination(List<CollectiveSFRepresentation> reps,
			EntityCentricKnowledgeBaseDefault eckb, int maxCandidates, List<CollectiveSFRepresentation> allDocumentCandidates) {
		super(eckb.getFeatureDefinition(), reps);
		this.maxCandidates = maxCandidates;
		this.allDocumentCandidates = allDocumentCandidates;
	}

	@Override
	public void setup(List<CollectiveSFRepresentation> rep) {
		this.graph = new DirectedSparseMultigraph<Vertex, Edge>();
		this.edgeWeights = new HashMap<Edge, Number>();
		this.edgeFactory = new Factory<Integer>() {
			int i = 0;

			public Integer create() {
				return i++;
			}
		};
		this.repList = rep;
	}

	@Override
	public boolean analyzeResults(PageRankWithPriors<Vertex, Edge> pr) {
		Collection<Vertex> vertexCol = graph.getVertices();
		for (int i = 0; i < repList.size(); i++) {
			if (repList.get(i).isACandidate()
					&& repList.get(i).getCandidates().size() > maxCandidates) {
				// Reduce Candidates
				List<String> allCandidates = repList.get(i).getCandidates();
				int qryNr = repList.get(i).getQueryNr();
				List<Candidate> candidateList = new LinkedList<Candidate>();
				for (Vertex v : vertexCol) {
					if (v.getEntityQuery() == qryNr && v.isCandidate()) {
						candidateList.add(new Candidate(v.getUris().get(0), pr
								.getVertexScore(v)));
					}
				}
				Collections.sort(candidateList, Collections.reverseOrder());
				List<Double> doubleList = new LinkedList<Double>();
				int counter = 0;
				while (doubleList.size() < 3 && counter < candidateList.size()) {
					Candidate c = candidateList.get(counter);
					if (c.getScore() > 0) {
						doubleList.add(c.getScore());
					}
					counter++;
				}
				if (analyzeValues(doubleList)) {
					Collection<String> retainList = new ArrayList<String>();
					for (int j = 0; j < maxCandidates; j++) {
						retainList.add(candidateList.get(j).getCandidate());
					}
					allCandidates.retainAll(retainList);
					repList.get(i).setCandidates(allCandidates);
				} else {
					List<String> retainList = computeSensePriorRankedList(
							qryNr, maxCandidates);
					allCandidates.retainAll(retainList);
//					allCandidates.addAll(determineAbbreviationCandidates(
//							repList.get(i).getSurfaceForm(),
//							allDocumentCandidates));
					repList.get(i).setCandidates(allCandidates);
				}
			}
		}
		return true;
	}

	private List<String> determineAbbreviationCandidates(String surfaceForm,
			List<CollectiveSFRepresentation> cans) {
		List<String> candidates = new LinkedList<String>();
		// Check Short surface form
		if (surfaceForm.length() <= 4
				&& (surfaceForm.replaceAll("[^a-zA-Z]", "")).length() <= 3
				&& surfaceForm.length() > 1) {
			String letters = surfaceForm.replaceAll("[^a-zA-Z]", "");
			for (CollectiveSFRepresentation sfrep : cans) {
				String sf = sfrep.getSurfaceForm();
				String splitter[] = sf.split(" ");
				if (splitter.length > 1) {
					boolean check = true;
					for (int i = 0; i < letters.length(); i++) {
						String letter = Character.toString(letters.charAt(i));
						if (!splitter[i].toLowerCase().startsWith(
								letter.toLowerCase())) {
							check = false;
							break;
						}
					}
					if (check) {
						candidates.addAll(sfrep.getCandidates());
					}
				}
			}
		}
		return candidates;
	}

	private boolean analyzeValues(List<Double> doubles) {
		if (doubles.size() < 3) {
			return false;
		}
		SummaryStatistics stats = new SummaryStatistics();
		for (Double d : doubles) {
			stats.addValue(d);
		}
		double standardDeviation = stats.getStandardDeviation();
		// System.out.println("StandardDeviation: " + standardDeviation);
		if (standardDeviation > 0.001) {
			return true;
		} else
			return false;
	}
}
