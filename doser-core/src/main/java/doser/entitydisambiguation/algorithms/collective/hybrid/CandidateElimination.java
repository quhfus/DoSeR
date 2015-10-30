package doser.entitydisambiguation.algorithms.collective.hybrid;

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

public class CandidateElimination {

	public static final int MAXSURFACEFORMSPERQUERY = 50;
	public static final int MULTIPLIER = 4;
	public static final int CLUSTERSIZE = 5;

	private EntityCentricKnowledgeBaseDefault eckb;
	private List<SurfaceForm> rep;
	private Word2Vec w2v;

	public CandidateElimination(List<SurfaceForm> reps,
			EntityCentricKnowledgeBaseDefault eckb, Word2Vec w2v) {
		super();
		this.eckb = eckb;
		this.rep = reps;
		this.w2v = w2v;
	}

	public List<SurfaceForm> solve() {
		List<SurfaceForm> finalList = new LinkedList<SurfaceForm>();

		if (this.rep.size() > MAXSURFACEFORMSPERQUERY) {
			List<SurfaceForm> disambiguatedSFs = new LinkedList<SurfaceForm>();
			for (SurfaceForm c : this.rep) {
				if (c.getCandidates().size() == 1) {
					disambiguatedSFs.add(c);
				}
			}
			int counter = 0;
			while (true) {
				if ((counter + MAXSURFACEFORMSPERQUERY) < this.rep.size()) {
					List<SurfaceForm> subList = this.rep.subList(counter,
							(counter + MAXSURFACEFORMSPERQUERY));
					finalList.addAll(miniSolve(subList));
					counter += MAXSURFACEFORMSPERQUERY;
				} else {
					List<SurfaceForm> subList = this.rep.subList(counter,
							this.rep.size());
					finalList.addAll(miniSolve(subList));
					break;
				}
			}

		} else {
			finalList.addAll(miniSolve(this.rep));
		}
		return finalList;
	}

	private List<SurfaceForm> miniSolve(List<SurfaceForm> rep) {
		List<LinkedList<SurfaceForm>> clusters = createDivideAndConquerClusters(rep);
		while (clusters.size() > 1) {
			for (LinkedList<SurfaceForm> cluster : clusters) {
				SubElimination elimination = new SubElimination(cluster, eckb,
						clusters.size() * MULTIPLIER, this.w2v);
				elimination.solve();
			}
			clusters = merge(clusters);
		}

		return clusters.get(0);
	}

	private List<LinkedList<SurfaceForm>> merge(
			List<LinkedList<SurfaceForm>> oldReps) {
		List<LinkedList<SurfaceForm>> newClusters = new LinkedList<LinkedList<SurfaceForm>>();
		for (int i = 0; i < oldReps.size(); i = i + 2) {
			LinkedList<SurfaceForm> l = new LinkedList<SurfaceForm>();
			if (i < oldReps.size()) {
				addAllWithoutNoCandidates(l, oldReps.get(i));
			}
			if ((i + 1) < oldReps.size()) {
				addAllWithoutNoCandidates(l, oldReps.get(i + 1));
			}
			newClusters.add(l);
		}
		return newClusters;
	}

	private void addAllWithoutNoCandidates(LinkedList<SurfaceForm> l,
			LinkedList<SurfaceForm> old) {
		for (SurfaceForm col : old) {
			if (col.isACandidate()) {
				l.add(col);
			}
		}
	}

	private List<LinkedList<SurfaceForm>> createDivideAndConquerClusters(
			List<SurfaceForm> rep) {
		int nrclusters = (int) Math.ceil((double) rep.size()
				/ (double) CLUSTERSIZE);
		List<LinkedList<SurfaceForm>> clusters = new LinkedList<LinkedList<SurfaceForm>>();
		List<SurfaceForm> unambiguous = detectDisambiguatedSufaceForms(rep);
		for (int i = 0; i < nrclusters; i++) {
			LinkedList<SurfaceForm> list = new LinkedList<SurfaceForm>();
			int counter = 0;
			while (true) {
				if (counter > 0
						&& (counter % CLUSTERSIZE == 0 || (i * CLUSTERSIZE + counter) == rep
								.size())) {
					break;
				}
				list.add(rep.get(i * CLUSTERSIZE + counter));
				counter++;
			}
			list.addAll(unambiguous);
			clusters.add(list);
		}

		// Fill Last Cluster with an appropriate amount of Surface Forms
		LinkedList<SurfaceForm> lastCluster = clusters.get(clusters.size() - 1);
		if (clusters.size() > 1 && lastCluster.size() < CLUSTERSIZE) {
			LinkedList<SurfaceForm> forelast = clusters
					.get(clusters.size() - 2);
			int counter = forelast.size() - 1;
			while (lastCluster.size() < CLUSTERSIZE) {
				SurfaceForm col = forelast.get(counter);
				SurfaceForm newCol = (SurfaceForm) col.clone();
				newCol.setACandidate(false);
				lastCluster.add(newCol);
				counter--;
			}
		}
		return clusters;
	}

	private List<SurfaceForm> detectDisambiguatedSufaceForms(
			List<SurfaceForm> reps) {
		List<SurfaceForm> unambiguous = new LinkedList<SurfaceForm>();
		for (SurfaceForm rep : reps) {
			if (rep.getCandidates().size() == 1) {
				SurfaceForm clone = (SurfaceForm) rep.clone();
				clone.setACandidate(false);
				unambiguous.add(clone);
			}
		}
		return unambiguous;
	}

	class DivideAndConquerCluster {

		private List<SurfaceForm> rep;

		DivideAndConquerCluster(List<SurfaceForm> reps) {
			super();
			this.rep = reps;
		}

		List<SurfaceForm> getCollectiveSFRepresentations() {
			return this.rep;
		}
	}

	class SubElimination extends Word2VecPageRank {

		private int maxCandidates;

		public SubElimination(List<SurfaceForm> reps,
				EntityCentricKnowledgeBaseDefault eckb, int maxCandidates,
				Word2Vec w2v) {
			super(eckb.getFeatureDefinition(), reps, w2v);
			this.maxCandidates = maxCandidates;
			System.out.println("SOVIEL KANDIDATEN HABEN WIR: "+this.maxCandidates);
		}

		@Override
		public void setup(List<SurfaceForm> rep) {
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
							candidateList.add(new Candidate(v.getUris().get(0),
									pr.getVertexScore(v)));
						}
					}
					Collections.sort(candidateList, Collections.reverseOrder());
					List<Double> doubleList = new LinkedList<Double>();
					int counter = 0;
					while (doubleList.size() < 3
							&& counter < candidateList.size()) {
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
						repList.get(i).setCandidates(allCandidates);
					}
				}
			}
			return true;
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
			if (standardDeviation > 0.001) {
				return true;
			} else
				return false;
		}
	}
}
