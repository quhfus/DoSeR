package doser.entitydisambiguation.algorithms.collective;

import java.util.LinkedList;
import java.util.List;

import doser.entitydisambiguation.dpo.DisambiguatedEntity;
import doser.entitydisambiguation.dpo.Response;
import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBaseDefault;

public class EntityCentricAlgorithmCollectiveSolver {

	public static final int MAXSURFACEFORMSPERQUERY = 50;
	public static final int MULTIPLIER = 4;
	public static final int CLUSTERSIZE = 5;

	private Response[] currentResponse;

	private List<CollectiveSFRepresentation> rep;

	private EntityCentricKnowledgeBaseDefault eckb;

	public EntityCentricAlgorithmCollectiveSolver(Response[] res,
			List<CollectiveSFRepresentation> rep,
			EntityCentricKnowledgeBaseDefault eckb) {
		super();
		if (res.length != rep.size()) {
			throw new IllegalArgumentException();
		}
		this.currentResponse = res;
		this.rep = rep;
		this.eckb = eckb;
	}

	public void solve() {
		List<CollectiveSFRepresentation> finalList = new LinkedList<CollectiveSFRepresentation>();

		if (this.rep.size() > MAXSURFACEFORMSPERQUERY) {
			List<CollectiveSFRepresentation> disambiguatedSFs = new LinkedList<CollectiveSFRepresentation>();
			for (CollectiveSFRepresentation c : rep) {
				if (c.getCandidates().size() == 1) {
					disambiguatedSFs.add(c);
				}
			}
			int counter = 0;
			while (true) {
				if ((counter + MAXSURFACEFORMSPERQUERY) < this.rep.size()) {
					List<CollectiveSFRepresentation> subList = this.rep
							.subList(counter,
									(counter + MAXSURFACEFORMSPERQUERY));
					finalList.addAll(miniSolve(subList));
					counter += MAXSURFACEFORMSPERQUERY;
				} else {
					List<CollectiveSFRepresentation> subList = this.rep
							.subList(counter, this.rep.size());
					finalList.addAll(miniSolve(subList));
					break;
				}
			}

		} else {
			finalList.addAll(miniSolve(this.rep));
		}
		this.rep = finalList;
	}

	private List<CollectiveSFRepresentation> miniSolve(
			List<CollectiveSFRepresentation> rep) {
		List<CollectiveSFRepresentation> sol = new LinkedList<CollectiveSFRepresentation>();
		List<LinkedList<CollectiveSFRepresentation>> clusters = createDivideAndConquerClusters(rep);
		boolean reduced = false;
		while (clusters.size() > 1) {
			for (LinkedList<CollectiveSFRepresentation> cluster : clusters) {
				CandidateElimination elimination = new CandidateElimination(
						cluster, eckb, clusters.size() * MULTIPLIER, this.rep);
				elimination.solve();
			}
			clusters = merge(clusters);
			reduced = true;
		}

		LinkedList<CollectiveSFRepresentation> cluster = clusters.get(0);

		// If no CandidateElimination was performed due to only cluster is
		// available, we have to perform a CandidateElimination if more than one
		// surface form are available.
//		if (!reduced && cluster.size() > 1) {
//			int max = 0;
//			while ((max = computeMaxCandidates(cluster)) > 10) {
//				int nrCandidates = (int) Math.floor(((double) max) * 0.66);
//				CandidateElimination elimination = new CandidateElimination(
//						cluster, eckb, nrCandidates, this.rep);
//				elimination.solve();
//			}
//		}

		PageRankDisambiguator disambiguator = new PageRankDisambiguator(
				cluster, eckb.getFeatureDefinition());
		disambiguator.solve();
		sol.addAll(disambiguator.getRepresentation());
		return sol;
	}

	private int computeMaxCandidates(List<CollectiveSFRepresentation> reps) {
		int max = 0;
		for (CollectiveSFRepresentation sf : reps) {
			if (sf.getCandidates().size() > max) {
				max = sf.getCandidates().size();
			}
		}
		return max;
	}

	private List<LinkedList<CollectiveSFRepresentation>> merge(
			List<LinkedList<CollectiveSFRepresentation>> oldReps) {
		List<LinkedList<CollectiveSFRepresentation>> newClusters = new LinkedList<LinkedList<CollectiveSFRepresentation>>();
		for (int i = 0; i < oldReps.size(); i = i + 2) {
			LinkedList<CollectiveSFRepresentation> l = new LinkedList<CollectiveSFRepresentation>();
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

	private void addAllWithoutNoCandidates(
			LinkedList<CollectiveSFRepresentation> l,
			LinkedList<CollectiveSFRepresentation> old) {
		for (CollectiveSFRepresentation col : old) {
			if (col.isACandidate()) {
				l.add(col);
			}
		}
	}

	private List<LinkedList<CollectiveSFRepresentation>> createDivideAndConquerClusters(
			List<CollectiveSFRepresentation> rep) {
		int nrclusters = (int) Math.ceil((double) rep.size()
				/ (double) CLUSTERSIZE);
		System.out.println("Detected Clustersize: " + nrclusters);
		List<LinkedList<CollectiveSFRepresentation>> clusters = new LinkedList<LinkedList<CollectiveSFRepresentation>>();
		List<CollectiveSFRepresentation> unambiguous = detectDisambiguatedSufaceForms(rep);
		for (int i = 0; i < nrclusters; i++) {
			LinkedList<CollectiveSFRepresentation> list = new LinkedList<CollectiveSFRepresentation>();
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
		LinkedList<CollectiveSFRepresentation> lastCluster = clusters
				.get(clusters.size() - 1);
		if (clusters.size() > 1 && lastCluster.size() < CLUSTERSIZE) {
			LinkedList<CollectiveSFRepresentation> forelast = clusters
					.get(clusters.size() - 2);
			int counter = forelast.size() - 1;
			while (lastCluster.size() < CLUSTERSIZE) {
				CollectiveSFRepresentation col = forelast.get(counter);
				CollectiveSFRepresentation newCol = (CollectiveSFRepresentation) col
						.clone();
				newCol.setACandidate(false);
				lastCluster.add(newCol);
				counter--;
			}
		}
		return clusters;
	}

	private List<CollectiveSFRepresentation> detectDisambiguatedSufaceForms(
			List<CollectiveSFRepresentation> reps) {
		List<CollectiveSFRepresentation> unambiguous = new LinkedList<CollectiveSFRepresentation>();
		for (CollectiveSFRepresentation rep : reps) {
			if (rep.getCandidates().size() == 1) {
				CollectiveSFRepresentation clone = (CollectiveSFRepresentation) rep
						.clone();
				clone.setACandidate(false);
				unambiguous.add(clone);
			}
		}
		return unambiguous;
	}

	public void generateResult() {
		for (int i = 0; i < currentResponse.length; i++) {
			CollectiveSFRepresentation r = search(i);
			if (currentResponse[i] == null && r != null
					&& r.getCandidates().size() == 1) {
				Response res = new Response();
				List<DisambiguatedEntity> entList = new LinkedList<DisambiguatedEntity>();
				DisambiguatedEntity ent = new DisambiguatedEntity();
				ent.setEntityUri(r.getCandidates().get(0));
				ent.setText("ToDoText");
				entList.add(ent);
				res.setDisEntities(entList);
				res.setPosition(null);
				res.setSelectedText(r.getSurfaceForm());
				currentResponse[i] = res;
			}
		}
	}

	private CollectiveSFRepresentation search(int qryNr) {
		for (CollectiveSFRepresentation r : rep) {
			if (r.getQueryNr() == qryNr) {
				return r;
			}
		}
		return null;
	}

	class DivideAndConquerCluster {

		private List<CollectiveSFRepresentation> rep;

		DivideAndConquerCluster(List<CollectiveSFRepresentation> reps) {
			super();
			this.rep = reps;
		}

		List<CollectiveSFRepresentation> getCollectiveSFRepresentations() {
			return this.rep;
		}
	}
}