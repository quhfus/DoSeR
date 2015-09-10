package doser.entitydisambiguation.algorithms.collective;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections15.functors.MapTransformer;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import doser.lucene.features.EnCenExtFeatures;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;

public class PageRankDisambiguator extends Word2VecPageRank {

	private List<CollectiveSFRepresentation> sustain;

	public PageRankDisambiguator(List<CollectiveSFRepresentation> reps,
			EnCenExtFeatures featureDefinition) {
		super(featureDefinition, reps);
		this.sustain = new LinkedList<CollectiveSFRepresentation>();
	}

	@Override
	protected PageRankWithPriors<Vertex, Edge> performPageRank() {
		PageRankWithPriors<Vertex, Edge> pr = new PageRankWithPriors<Vertex, Edge>(
				graph, MapTransformer.getInstance(edgeWeights),
				getRootPrior(graph.getVertices()), 0.13);
		pr.setMaxIterations(250);
		pr.evaluate();
		return pr;
	}

	/**
	 * Disambiguation result process
	 * 
	 * Order surface forms according to ambiguity and score difference. Entities
	 * which obviously belong to a surface form (larger score margin between
	 * first and second entity candidate) will be disambiguated first.
	 * 
	 * ToDo: Nil Identifier
	 */
	@Override
	public boolean analyzeResults(PageRankWithPriors<Vertex, Edge> pr) {
		boolean disambiguationStop = true;
		Collection<Vertex> vertexCol = graph.getVertices();

		for (int i = 0; i < repList.size(); i++) {
			if (!disambiguatedSurfaceForms.get(i)
					&& !sustain.contains(repList.get(i))) {
				// Hier muss noch disambiguiert werden
				System.out
						.println("Distance:" + repList.get(i).getDifference());
				int qryNr = repList.get(i).getQueryNr();
				double maxScore = 0;
				double secondMaxScore = 0;
				SummaryStatistics stats = new SummaryStatistics();
				String tempSolution = "";
				for (Vertex v : vertexCol) {
					if (v.getEntityQuery() == qryNr && v.isCandidate()) {
						double score = Math.abs(pr.getVertexScore(v));
						stats.addValue(score);
						System.out.println("Score für: " + v.getUris() + "  "
								+ score);
						if (score > maxScore) {
							tempSolution = v.getUris().get(0);
							secondMaxScore = maxScore;
							maxScore = score;
						}
					}
				}
				CollectiveSFRepresentation rep = repList.get(i);
				// PageRank not solvable
				if (Double.isInfinite(maxScore)) {
					List<String> l = computeSensePriorRankedList(qryNr, 1);
					tempSolution = l.get(0);
					updateGraph(rep.getCandidates(), tempSolution,
							rep.getQueryNr());
					rep.setDisambiguatedEntity(tempSolution);
					disambiguatedSurfaceForms.set(i);
					disambiguationStop = false;
					break;
				} else {
					double avg = stats.getMean();
					double threshold = computeThreshold(avg, maxScore);
					System.out.println("THRESHOLD: " + threshold);
					// if(secondMaxScore < threshold) {
					updateGraph(rep.getCandidates(), tempSolution,
							rep.getQueryNr());
					rep.setDisambiguatedEntity(tempSolution);
					disambiguatedSurfaceForms.set(i);
					disambiguationStop = false;
					break;
					// } else {
					// this.sustain.add(repList.get(i));
					// disambiguatedSurfaceForms.set(i);
					// rep.clearList();
					// disambiguationStop = false;
					// break;
					// }
				}
			}
		}
		return disambiguationStop;
	}

	private double computeThreshold(double avg, double highest) {
		double diff = highest - avg;
		double min = diff * 0.1;
		return highest - min;
	}
}
