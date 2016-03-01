package doser.entitydisambiguation.algorithms.collective;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.MapTransformer;

import doser.entitydisambiguation.algorithms.SurfaceForm;
import doser.entitydisambiguation.knowledgebases.AbstractEntityCentricKBGeneral;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public abstract class AbstractWord2VecPageRank {

	protected AbstractEntityCentricKBGeneral eckb;

	protected Map<Edge, Number> edgeWeights;

	protected DirectedGraph<Vertex, Edge> graph;

	protected Factory<Integer> edgeFactory;

	protected BitSet disambiguatedSurfaceForms;

	protected List<SurfaceForm> allCandidates;

	protected List<SurfaceForm> repList;

	public AbstractWord2VecPageRank(AbstractEntityCentricKBGeneral featureDefinition,
			List<SurfaceForm> rep) {
		super();
		this.eckb = featureDefinition;
		this.repList = rep;
	}

	public void solve() {
		while (true) {
			PageRankWithPriors<Vertex, Edge> pr = performPageRank();
			if (analyzeResults(pr)) {
				break;
			}
		}
	}

	protected PageRankWithPriors<Vertex, Edge> performPageRank() {
		PageRankWithPriors<Vertex, Edge> pr = new PageRankWithPriors<Vertex, Edge>(
				graph, MapTransformer.getInstance(edgeWeights),
				getRootPrior(graph.getVertices()), 0.15);
		pr.setMaxIterations(200);
		pr.evaluate();
		return pr;
	}

	public void setup() {
		this.graph = new DirectedSparseMultigraph<Vertex, Edge>();
		this.edgeWeights = new HashMap<Edge, Number>();
		this.edgeFactory = new Factory<Integer>() {
			int i = 0;

			public Integer create() {
				return i++;
			}
		};

		List<SurfaceForm> list = new LinkedList<SurfaceForm>();
		for (SurfaceForm r : this.repList) {
			list.add((SurfaceForm) r.clone());
		}
		Collections.sort(list);
		this.repList = list;
		this.disambiguatedSurfaceForms = new BitSet(repList.size());
		for (int i = 0; i < repList.size(); i++) {
			if (repList.get(i).getCandidates().size() <= 1) {
				this.disambiguatedSurfaceForms.set(i);
			}
		}
		buildMainGraph();
	}

	protected void buildMainGraph() {
		List<String> disambiguatedEntities = new LinkedList<String>();
		// Add Vertexes
		for (SurfaceForm rep : repList) {
			List<String> arrList = rep.getCandidates();
			for (String s : arrList) {
				int occs = eckb.getFeatureDefinition().getOccurrences(
						rep.getSurfaceForm(), s);
				List<String> l = new LinkedList<String>();
				l.add(s);
				if (rep.getCandidates().size() == 1) {
					disambiguatedEntities.add(rep.getCandidates().get(0));
					addVertex(l, rep.getSurfaceForm(), rep.getQueryNr(), true,
							20000, rep.getContext());
				} else {
					addVertex(l, rep.getSurfaceForm(), rep.getQueryNr(), true,
							occs, rep.getContext());
				}
			}
		}

		// Add Document AsVertex
		addVertex(disambiguatedEntities, "", -1, true, 50000, "");

		// Add Edges
		List<Vertex> vertexList = new ArrayList<Vertex>(graph.getVertices());

		// Create Word2Vec Queries
		Set<String> w2vFormatStrings = new HashSet<String>();
		for (Vertex v1 : vertexList) {
			for (Vertex v2 : vertexList) {
				if (!v1.equals(v2) && !areCandidatesofSameSF(v1, v2)) {
					List<String> l1 = v1.getUris();
					List<String> l2 = v2.getUris();
					if (l1.size() == 1 && l2.size() == 1) {
						String format = this.eckb.generateWord2VecFormatString(
								l1.get(0), l2.get(0));
						w2vFormatStrings.add(format);
					}
				}
			}
		}
		Map<String, Float> similarityMap = this.eckb
				.getWord2VecSimilarities(w2vFormatStrings);

		for (Vertex v1 : vertexList) {
			for (Vertex v2 : vertexList) {
				if (!v1.equals(v2) && !areCandidatesofSameSF(v1, v2)) {
					List<String> l1 = v1.getUris();
					List<String> l2 = v2.getUris();
					if (l1.size() == 1 && l2.size() == 1) {
						double weight = similarityMap.get(this.eckb.generateWord2VecFormatString(l1.get(0), l2.get(0)));
						if(weight < 0.00000001) {
							System.out.println(weight + " "+l1.get(0) + "   "+l2.get(0));
						}
						// Add Doc2Vec Local Compatibility
						// First experiment: Harmonic mean
						// double localComp = super.getDoc2VecSimilarity(
						// v2.getText(), v2.getContext(), l2.get(0));
						// double hm = 2 * (localComp * weight)
						// / (localComp + weight);
						// System.out.println(l1.get(0) + " "+l2.get(0)
						// +"  Connection: "+ weight+ " Localcomp: "+ localComp
						// + "HarmonicMean: "+ hm);
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
				// System.out.println("From: " + v.getUris().get(0) +
				// " To: "+e.getTarget().getUris().get(0)+
				// " Probability: "+e.getProbability());
				edgeWeights.put(e, e.getProbability());
			}
		}
	}

	protected void addVertex(List<String> uri, String sf, int qryNr,
			boolean isCandidate, int occurrences, String context) {
		Vertex v = new Vertex();
		for (String u : uri) {
			v.addUri(u);
		}
		v.setCandidate(isCandidate);
		v.setText(sf);
		v.setEntityQuery(qryNr);
		v.setOccurrences(occurrences);
		v.setContext(context);
		graph.addVertex(v);
	}

	protected void addEdge(Vertex out, Vertex in, int edgeNr, double transition) {
		Edge edge = new Edge(edgeNr, in, transition);
		out.addOutGoingEdge(edge);
		graph.addEdge(edge, out, in);
	}

	protected void removeVertex(Vertex rem) {
		Set<Edge> outs = rem.getOutgoingEdges();
		for (Edge e : outs) {
			removeEdge(e);
		}
		rem.removeAllOutgoingEdges();

		Collection<Vertex> n = graph.getNeighbors(rem);
		// BugFix
		if (n != null) {
			for (Vertex v : n) {
				removeEdge(v, rem);
			}
		}

		graph.removeVertex(rem);
	}

	protected void updateGraph(List<String> candidates,
			String disambiguatedEntity, int entityQry) {
		Collection<Vertex> vertexCol = graph.getVertices();
		List<Vertex> relVertexes = new ArrayList<Vertex>();
		for (Vertex v : vertexCol) {
			if (v.getEntityQuery() == entityQry) {
				relVertexes.add(v);
			}
		}

		for (String s : candidates) {
			if (!s.equalsIgnoreCase(disambiguatedEntity)) {
				for (Vertex v : relVertexes) {
					if (v.getUris().get(0).equalsIgnoreCase(s)) {
						removeVertex(v);
					}
				}
			}
		}
	}

	public List<SurfaceForm> getRepresentation() {
		return this.repList;
	}

	/**
	 * Assigns a probability of 1/<code>roots.size()</code> to each of the
	 * elements of <code>roots</code>.
	 * 
	 * @param <V>
	 *            the vertex type
	 * @param roots
	 *            the vertices to be assigned nonzero prior probabilities
	 * @return
	 */
	protected Transformer<Vertex, Double> getRootPrior(Collection<Vertex> roots) {
		final Collection<Vertex> inner_roots = roots;
		double sum = 0;
		for (Vertex v : inner_roots) {
			sum += v.getOccurrences();
		}
		final double overallOccs = sum;
		Transformer<Vertex, Double> distribution = new Transformer<Vertex, Double>() {
			public Double transform(Vertex input) {
				if (inner_roots.contains(input)) {
					double d = new Double(input.getOccurrences()
							/ (double) overallOccs);
					return d;
				} else {
					return 0.0;
				}
			}
		};
		return distribution;
	}

	protected List<SurfaceForm> getCollectiveSFRepresentations() {
		return this.repList;
	}

	protected List<String> computeSensePriorRankedList(int qryNr, int bestOf) {
		List<Candidate> canList = new LinkedList<Candidate>();
		Collection<Vertex> vertexCol = graph.getVertices();
		for (Vertex c : vertexCol) {
			if (c.getEntityQuery() == qryNr && c.isCandidate()) {
				canList.add(new Candidate(c.getUris().get(0), c
						.getOccurrences()));
			}
		}
		Collections.sort(canList, Collections.reverseOrder());
		List<String> strList = new LinkedList<String>();
		for (Candidate c : canList.subList(0, bestOf)) {
			strList.add(c.candidate);
		}
		return strList;
	}

	private void removeEdge(Edge e) {
		graph.removeEdge(e);
		edgeWeights.remove(e);
	}

	private void removeEdge(Vertex out, Vertex in) {
		Edge e = out.removeOutgoingEdge(in, edgeWeights);
		if (e != null) {
			graph.removeEdge(e);
			edgeWeights.remove(e);
		}
	}

	protected boolean areCandidatesofSameSF(Vertex v1, Vertex v2) {
		int qryNr1 = v1.getEntityQuery();
		int qryNr2 = v2.getEntityQuery();
		if (qryNr1 == -1 || qryNr2 == -1
				|| v1.getEntityQuery() != v2.getEntityQuery()) {
			return false;
		}
		return true;
	}

	public abstract boolean analyzeResults(PageRankWithPriors<Vertex, Edge> pr);

	protected class Candidate implements Comparable<Candidate> {

		private String candidate;
		private double score;

		protected Candidate(String candidate, double score) {
			super();
			this.candidate = candidate;
			this.score = score;
		}

		@Override
		public int compareTo(Candidate o) {
			if (this.score < o.score) {
				return -1;
			} else if (this.score > o.score) {
				return 1;
			} else {
				return 0;
			}
		}

		protected String getCandidate() {
			return candidate;
		}

		protected double getScore() {
			return score;
		}
	}
}
