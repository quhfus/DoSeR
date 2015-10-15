package doser.entitydisambiguation.algorithms.collective.hybrid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.functors.MapTransformer;

import doser.entitydisambiguation.algorithms.collective.SurfaceForm;
import doser.entitydisambiguation.algorithms.collective.Edge;
import doser.entitydisambiguation.algorithms.collective.Vertex;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

class DisambiguateSimpleCases {

	private static final int SFSPERPAGERANK = 10;

	private Word2Vec w2v;

	DisambiguateSimpleCases(Word2Vec w2v) {
		super();
		this.w2v = w2v;
	}

	void solve(List<SurfaceForm> reps) {
		if (reps.size() > SFSPERPAGERANK) {
			List<SurfaceForm> disambiguatedSFs = new LinkedList<SurfaceForm>();
			for (SurfaceForm c : reps) {
				if (c.getCandidates().size() == 1) {
					disambiguatedSFs.add(c);
				}
			}
			int counter = 0;
			while (true) {
				if ((counter + SFSPERPAGERANK) < reps.size()) {
					List<SurfaceForm> subList = new ArrayList<SurfaceForm>();
					for (SurfaceForm c : reps.subList(counter,
							(counter + SFSPERPAGERANK))) {
						if (c.getCandidates().size() > 1) {
							subList.add(c);
						}
					}
					subList.addAll(disambiguatedSFs);
					SimpleCollectivePageRank simplePR = new SimpleCollectivePageRank(
							subList, createMustMatchCandidates(subList));
					simplePR.solve();
					counter += SFSPERPAGERANK;
				} else {
					List<SurfaceForm> subList = new ArrayList<SurfaceForm>();
					for (SurfaceForm c : reps.subList(counter,
							reps.size())) {
						if (c.getCandidates().size() > 1) {
							subList.add(c);
						}
					}
					subList.addAll(disambiguatedSFs);
					SimpleCollectivePageRank simplePR = new SimpleCollectivePageRank(
							subList, createMustMatchCandidates(subList));
					simplePR.solve();
					break;
				}
			}
		} else {
			List<SurfaceForm> l = new ArrayList<SurfaceForm>();
			l.addAll(reps);
			SimpleCollectivePageRank simplePR = new SimpleCollectivePageRank(l,
					createMustMatchCandidates(l));
			simplePR.solve();
		}
	}

	private List<String> createMustMatchCandidates(
			List<SurfaceForm> reps) {
		List<String> mustMatchCandidates = new ArrayList<String>();
		for (SurfaceForm c : reps) {
			if (c.getCandidates().size() == 1) {
				mustMatchCandidates.add(c.getCandidates().get(0));
			} else if (c.getCandidates().size() == 0) {
				mustMatchCandidates.add("");
			} else {
				List<String> cans = c.getCandidates();
				List<Candidate> candidates = new ArrayList<Candidate>();
				for (String s : cans) {
					candidates.add(new Candidate(s, this.w2v
							.getDoc2VecSimilarity(c.getSurfaceForm(),
									c.getContext(), s)));
				}
				Collections.sort(candidates, Collections.reverseOrder());
				mustMatchCandidates.add(candidates.get(0).getCandidate());

			}
		}
		return mustMatchCandidates;
	}

	class SimpleCollectivePageRank {

		private List<String> mustMatchCandidates;
		private List<SurfaceForm> reps;
		private DirectedGraph<Vertex, Edge> graph;
		private Map<Edge, Number> edgeWeights;
		private Factory<Integer> edgeFactory;

		SimpleCollectivePageRank(List<SurfaceForm> reps,
				List<String> mustMatchCandidates) {
			super();
			this.reps = reps;
			this.mustMatchCandidates = mustMatchCandidates;
			w2v.computeWord2VecSimilarities(reps);
			setup();
			buildMainGraph();
		}

		void solve() {
			PageRank<Vertex, Edge> pr = new PageRank<Vertex, Edge>(graph,
					MapTransformer.getInstance(edgeWeights), 0.1);
			pr.setMaxIterations(100);
			pr.evaluate();
			disambiguate(pr);
		}

		private void disambiguate(PageRank<Vertex, Edge> pr) {
			Collection<Vertex> vertexCol = graph.getVertices();
			for (int i = 0; i < this.reps.size(); i++) {
				SurfaceForm r = this.reps.get(i);
				int qryNr = r.getQueryNr();
				List<Candidate> candidateList = new ArrayList<Candidate>();
				if (r.isACandidate() && r.getCandidates().size() > 1) {
					for (Vertex v : vertexCol) {
						if (v.getEntityQuery() == qryNr && v.isCandidate()) {
							candidateList.add(new Candidate(v.getUris().get(0),
									pr.getVertexScore(v)));
						}
					}
					Collections.sort(candidateList, Collections.reverseOrder());
					List<Double> doubleList = new LinkedList<Double>();
					for (Candidate c : candidateList) {
						doubleList.add(c.getScore());
					}

					if (analyzeValues(doubleList, candidateList,
							mustMatchCandidates.get(i))) {
						r.setDisambiguatedEntity(mustMatchCandidates.get(i));
						System.out.println("DISAMBIGUATION Surface Form: "
								+ r.getSurfaceForm() + " Entity :"
								+ mustMatchCandidates.get(i));
					}
				}
			}
		}

		private boolean analyzeValues(List<Double> dList,
				List<Candidate> candidates, String mustMatch) {
			System.out.println("MATCHING: " + candidates.get(0).getCandidate()
					+ "to: " + mustMatch);
			if (candidates.get(0).getCandidate().equals(mustMatch)) {
				return true;
			}
			return false;
		}

		private void setup() {
			this.graph = new DirectedSparseMultigraph<Vertex, Edge>();
			this.edgeWeights = new HashMap<Edge, Number>();
			this.edgeFactory = new Factory<Integer>() {
				int i = 0;

				public Integer create() {
					return i++;
				}
			};
		}

		private void buildMainGraph() {
			List<String> disambiguatedEntities = new LinkedList<String>();
			// Add Vertexes
			for (SurfaceForm rep : reps) {
				List<String> arrList = rep.getCandidates();
				for (String s : arrList) {
					List<String> l = new LinkedList<String>();
					l.add(s);
					if (rep.getCandidates().size() == 1) {
						disambiguatedEntities.add(rep.getCandidates().get(0));
						addVertex(l, rep.getSurfaceForm(), rep.getQueryNr(),
								true, rep.getContext());
					} else {
						addVertex(l, rep.getSurfaceForm(), rep.getQueryNr(),
								true, rep.getContext());
					}
				}
			}

			// Add Document AsVertex
			addVertex(disambiguatedEntities, "", -1, true, "");

			// Add Edges
			List<Vertex> vertexList = new ArrayList<Vertex>(graph.getVertices());
			for (Vertex v1 : vertexList) {
				for (Vertex v2 : vertexList) {
					if (!v1.equals(v2) && !areCandidatesofSameSF(v1, v2)) {
						List<String> l1 = v1.getUris();
						List<String> l2 = v2.getUris();
						if (l1.size() == 1 && l2.size() == 1) {
							double weight = w2v.getWord2VecSimilarity(
									l1.get(0), l2.get(0));
							addEdge(v1, v2, edgeFactory.create(), weight);
						}
					}
				}
			}

			// Set Edge Probabilities
			Collection<Vertex> vertexes = graph.getVertices();
			for (Vertex v : vertexes) {
				Set<Edge> edges = v.getOutgoingEdges();
				for (Edge e : edges) {
					edgeWeights.put(e, e.getProbability());
				}
			}
		}

		private void addVertex(List<String> uri, String sf, int qryNr,
				boolean isCandidate, String context) {
			Vertex v = new Vertex();
			for (String u : uri) {
				v.addUri(u);
			}
			v.setCandidate(isCandidate);
			v.setText(sf);
			v.setEntityQuery(qryNr);
			v.setContext(context);
			graph.addVertex(v);
		}

		private void addEdge(Vertex out, Vertex in, int edgeNr,
				double transition) {
			Edge edge = new Edge(edgeNr, in, transition);
			out.addOutGoingEdge(edge);
			graph.addEdge(edge, out, in);
		}

		private boolean areCandidatesofSameSF(Vertex v1, Vertex v2) {
			int qryNr1 = v1.getEntityQuery();
			int qryNr2 = v2.getEntityQuery();
			if (qryNr1 == -1 || qryNr2 == -1
					|| v1.getEntityQuery() != v2.getEntityQuery()) {
				return false;
			}
			return true;
		}
	}
}
