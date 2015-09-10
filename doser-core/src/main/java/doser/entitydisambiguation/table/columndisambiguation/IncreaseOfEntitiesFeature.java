package doser.entitydisambiguation.table.columndisambiguation;

import java.util.Set;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import doser.entitydisambiguation.table.logic.Type;

/**
 * Feature 2
 * 
 * Computes the increase of containing cells in relation to all cells available
 * in the column. Therefore we calculate the average of all incoming edges of
 * all types t - 1. The average of the predecessor nodes is compared with the
 * value of the current type. Additionally the hierarchy level is integrated via
 * log damping function:
 * 
 * Sqrt(|c| / a) * log(layer)
 * 
 * c = number of cells of type t a = average number of cells provided by all
 * types t - 1 layer = The layernumber
 * 
 * 
 * @author Stefan Zwicklbauer
 * 
 */
public class IncreaseOfEntitiesFeature extends AbstractTypeDisFeatures {

	private static final float WEIGHT = 0.685575f;

	public IncreaseOfEntitiesFeature(
			final DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> graph) {
		super(graph);
	}

	@Override
	public float computeFeature(final Type type) {

		final Set<DefaultWeightedEdge> set = this.graph.incomingEdgesOf(type);
		double sum = 0;
		for (final DefaultWeightedEdge defWeightedEdge2 : set) {
			final DefaultWeightedEdge defWeightedEdge = defWeightedEdge2;
			final Type pre = this.graph.getEdgeSource(defWeightedEdge);
			final Set<DefaultWeightedEdge> setpredecessor = this.graph
					.incomingEdgesOf(pre);
			if (setpredecessor.isEmpty()) {
				sum += 1.0;
			} else {
				sum += pre.getAccumulatedWeight();
			}
		}

		double avg = 0;
		if (set.isEmpty()) {
			avg = 1;
		} else {
			avg = sum / set.size();
		}
		final double inc = Math.sqrt(type.getAccumulatedWeight() / avg);
		return (float) (WEIGHT * (inc / (Math.sqrt(type.getLayer()))));
	}
}
