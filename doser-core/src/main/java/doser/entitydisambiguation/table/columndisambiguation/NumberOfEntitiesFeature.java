package doser.entitydisambiguation.table.columndisambiguation;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import doser.entitydisambiguation.table.logic.Type;

/**
 * Feature 1
 * 
 * Calculates the amount of cells having the same type. The amount is divided
 * through the number of cells available within the column. It's perfect if all
 * disambiguated cells provide the same type.
 * 
 * Cells providing the same type / cells available in column
 * 
 * The edge weight w constitutes the number of cells belonging to this special
 * type t. Providing several incoming edges means that the special type t - 1
 * (n) (n > 0|amount of vertexes which are connected to type t) offers w cells
 * which are assigned to this type.
 * 
 * @author Stefan Zwicklbauer
 * 
 */
public class NumberOfEntitiesFeature extends AbstractTypeDisFeatures {

	private final static float WEIGHT = 2.0f;

	public NumberOfEntitiesFeature(
			final DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> graph) {
		super(graph);
	}

	@Override
	public float computeFeature(final Type type) {
		// if (type.getUri().contains("dbpedia")) {
		// return 0.0f;
		// } else {
		float sum = 0;
		int numberOfEntities = 0;
		final Set<Type> set = this.graph.vertexSet();
		for (final Type type3 : set) {
			final Type type2 = type3;
			if (!type2.isType()) {
				numberOfEntities++;
				Set<DefaultWeightedEdge> edgeSet = this.graph
						.outgoingEdgesOf(type2);
				boolean isFound = false;
				while ((!edgeSet.isEmpty()) && !isFound) {
					final Set<DefaultWeightedEdge> newHashSet = new HashSet<DefaultWeightedEdge>();
					for (final DefaultWeightedEdge defWeightedEdge2 : edgeSet) {
						final DefaultWeightedEdge defWeightedEdge = defWeightedEdge2;
						final Type type4 = this.graph
								.getEdgeTarget(defWeightedEdge);
						if (type4.getUri().equalsIgnoreCase(type.getUri())) {
							sum++;
							isFound = true;
							break;
						} else {
							newHashSet.addAll(this.graph.outgoingEdgesOf(type4));
						}
					}
					edgeSet = newHashSet;
				}
			}
		}
		final double ratio = (sum / numberOfEntities);
		return (float) (WEIGHT * (ratio / (type.getLayer() + 1)));
		// }
	}
}
