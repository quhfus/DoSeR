package doser.entitydisambiguation.algorithms.collective.dbpedia;

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
import org.apache.commons.collections15.functors.MapTransformer;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import doser.entitydisambiguation.algorithms.SurfaceForm;
import doser.entitydisambiguation.algorithms.collective.AbstractWord2VecPageRank;
import doser.entitydisambiguation.algorithms.collective.Edge;
import doser.entitydisambiguation.algorithms.collective.Vertex;
import doser.entitydisambiguation.knowledgebases.EntityCentricKBDBpedia;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

class FinalEntityDisambiguation extends AbstractWord2VecPageRank {

	private static final int PREPROCESSINGCONTEXTSIZE = 500;


	public FinalEntityDisambiguation(EntityCentricKBDBpedia eckb,
			List<SurfaceForm> rep) {
		super(eckb, rep);
//		this.d2v = new Doc2Vec(rep, PREPROCESSINGCONTEXTSIZE);
	}

	@Override
	public void setup() {
		this.graph = new DirectedSparseMultigraph<Vertex, Edge>();
		this.edgeWeights = new HashMap<Edge, Number>();
		this.edgeFactory = new Factory<Integer>() {
			int i = 0;

			public Integer create() {
				return i++;
			}
		};

//		List<SurfaceForm> list = new LinkedList<SurfaceForm>();
//		for (SurfaceForm r : this.repList) {
//			list.add((SurfaceForm) r.clone());
//		}
//		Collections.sort(list);
//		this.repList = list;
		this.disambiguatedSurfaceForms = new BitSet(repList.size());
		for (int i = 0; i < repList.size(); i++) {
			if (repList.get(i).getCandidates().size() <= 1) {
				this.disambiguatedSurfaceForms.set(i);
			}
		}
		buildMainGraph();
	}

	@Override
	protected void buildMainGraph() {
		List<String> disambiguatedEntities = new LinkedList<String>();
		// Add Vertexes
		for (SurfaceForm rep : repList) {
			List<String> arrList = rep.getCandidates();
			for (String s : arrList) {
				int occs = this.eckb.getFeatureDefinition().getOccurrences(
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
						double weight = similarityMap.get(this.eckb
								.generateWord2VecFormatString(l1.get(0),
										l2.get(0)));
						// Add Doc2Vec Local Compatibility
						// First experiment: Harmonic mean
//						double localComp = (0.8*this.d2v.getDoc2VecSimilarity(
//								v2.getText(), v2.getContext(), l2.get(0)));
//						double hm = 2 * (localComp * weight)
//								/ (localComp + weight);
						// System.out.println(l1.get(0) + " "+l2.get(0)
						// +"  Connection: "+ weight+ " Localcomp: "+ localComp
						// + "HarmonicMean: "+ hm);
						
						// Testing
//						if(isAlreadyDisambiguated(v1) || isAlreadyDisambiguated(v2)) {
//							if(weight > 1.5) {
//								addEdge(v1, v2, edgeFactory.create(), weight);
//							} else {
//								System.out.println("Ich lasse hier Kanten weg!"+v1.getUris().get(0)+" and "+v1.getUris().get(0) + " and "+weight);
//							}
//						} else {
							addEdge(v1, v2, edgeFactory.create(), weight);
//						}
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
	
	private boolean isAlreadyDisambiguated(Vertex v) {
		boolean isDisambiguated = false;
		int qryNr = v.getEntityQuery();
		for(SurfaceForm sf : repList) {
			if(sf.getQueryNr() == qryNr) {
				int candidateSize = sf.getCandidates().size();
				if(candidateSize == 1) {
					isDisambiguated = true;
				}
				break;
			}
		}
		return isDisambiguated;
	}

	@Override
	protected PageRankWithPriors<Vertex, Edge> performPageRank() {
		PageRankWithPriors<Vertex, Edge> pr = new PageRankWithPriors<Vertex, Edge>(
				graph, MapTransformer.getInstance(edgeWeights),
				getRootPrior(graph.getVertices()), 0.13);
		pr.setMaxIterations(75);
		pr.evaluate();
		return pr;
	}

	@Override
	public boolean analyzeResults(PageRankWithPriors<Vertex, Edge> pr) {
		boolean disambiguationStop = true;
		Collection<Vertex> vertexCol = graph.getVertices();
		for (int i = 0; i < repList.size(); i++) {
			if (!disambiguatedSurfaceForms.get(i)) {
				int qryNr = repList.get(i).getQueryNr();
				double maxScore = 0;
				SummaryStatistics stats = new SummaryStatistics();
				String tempSolution = "";
				List<Candidate> scores = new ArrayList<Candidate>();
				for (Vertex v : vertexCol) {
					if (v.getEntityQuery() == qryNr && v.isCandidate()) {
						scores.add(new Candidate(pr.getVertexScore(v)));
						double score = Math.abs(pr.getVertexScore(v));
						stats.addValue(score);
						System.out.println("Score for: "+v.getUris().get(0)+"  :  "+score);
						if (score > maxScore) {
							tempSolution = v.getUris().get(0);
							maxScore = score;
						}
					}
				}
				SurfaceForm rep = repList.get(i);
				Collections.sort(scores, Collections.reverseOrder());
				double secondMax = scores.get(1).score;

				if (!Double.isInfinite(maxScore)) {
					double avg = stats.getMean();
					double threshold = computeThreshold(avg, maxScore);
//					if (secondMax < threshold) {
						updateGraph(rep.getCandidates(), tempSolution,
								rep.getQueryNr());
						rep.setDisambiguatedEntity(tempSolution);
						System.out.println("Ich disambiguiere: "+tempSolution);
						disambiguatedSurfaceForms.set(i);
						disambiguationStop = false;
						break;
					}
//				}
			}
		}
		return disambiguationStop;
	}

	/**
	 * Threshold Computation // IMPORTANT DISAMBIGUATION PARAMETER
	 * 
	 * @param avg
	 * @param highest
	 * @return
	 */
	private double computeThreshold(double avg, double highest) {
		double diff = highest - avg;
		double min = diff * 0.25;
		return highest - min;
	}

	class Candidate implements Comparable<Candidate> {
		private double score;

		Candidate(double score) {
			super();
			this.score = score;
		}

		@Override
		public int compareTo(Candidate o) {
			if (score < o.score) {
				return -1;
			} else if (score > o.score) {
				return 1;
			} else {
				return 0;
			}
		}
	}
}
