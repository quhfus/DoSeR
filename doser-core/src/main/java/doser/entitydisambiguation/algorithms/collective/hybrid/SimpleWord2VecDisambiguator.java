package doser.entitydisambiguation.algorithms.collective.hybrid;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.functors.MapTransformer;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import doser.lucene.features.EnCenExtFeatures;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public class SimpleWord2VecDisambiguator extends Word2VecPageRank {

	public SimpleWord2VecDisambiguator(EnCenExtFeatures featureDefinition,
			List<SurfaceForm> rep, Word2Vec w2v) {
		super(featureDefinition, rep, w2v);
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

		this.disambiguatedSurfaceForms = new BitSet(repList.size());
		for (int i = 0; i < repList.size(); i++) {
			if (repList.get(i).getCandidates().size() <= 1) {
				this.disambiguatedSurfaceForms.set(i);
			}
		}
	}

	@Override
	protected PageRankWithPriors<Vertex, Edge> performPageRank() {
		PageRankWithPriors<Vertex, Edge> pr = new PageRankWithPriors<Vertex, Edge>(
				graph, MapTransformer.getInstance(edgeWeights),
				getRootPrior(graph.getVertices()), 0.08);
		pr.setMaxIterations(100);
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
				Collection<Double> scores = new ArrayList<Double>();
				for (Vertex v : vertexCol) {
					if (v.getEntityQuery() == qryNr && v.isCandidate()) {
						scores.add(pr.getVertexScore(v));
						double score = Math.abs(pr.getVertexScore(v));
						stats.addValue(score);
						System.out.println("Score für: " + v.getUris() + "  "
								+ score);
						if (score > maxScore) {
							tempSolution = v.getUris().get(0);
							maxScore = score;
						}
					}
				}
				double secondMax = computeSecondMaxScore(scores);
				SurfaceForm rep = repList.get(i);
				if (!Double.isInfinite(maxScore)) {
					double avg = stats.getMean();
					double threshold = computeThreshold(avg, maxScore);
					System.out.println(secondMax + "    " + threshold);
					if (secondMax < threshold) {
						updateGraph(rep.getCandidates(), tempSolution,
								rep.getQueryNr());
						rep.setDisambiguatedEntity(tempSolution);
						System.out.println("Ich setze die Lösung: "
								+ tempSolution);
						disambiguatedSurfaceForms.set(i);
						disambiguationStop = false;
						break;
					}
				}
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
		double min = diff * 0.5;
		return highest - min;
	}

	private double computeSecondMaxScore(Collection<Double> col) {
		PriorityQueue<Double> topN = new PriorityQueue<Double>(2,
				new Comparator<Double>() {
					@Override
					public int compare(Double d1, Double d2) {
						return Double.compare(d1, d2);
					}
				});
		for (Double d : col) {
			if (topN.size() < 2) {
				topN.add(d);
			} else if (topN.peek() < d) {
				topN.poll();
				topN.add(d);
			}
		}
		return topN.peek();
	}
}
