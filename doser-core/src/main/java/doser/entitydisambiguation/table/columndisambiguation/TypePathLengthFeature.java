package doser.entitydisambiguation.table.columndisambiguation;

import java.util.Set;

import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import doser.entitydisambiguation.table.logic.Type;

public class TypePathLengthFeature extends AbstractTypeDisFeatures {

	private final FloydWarshallShortestPaths<Type, DefaultWeightedEdge> floydWarshall;

	private final static float WEIGHT = 0.0245711f;

	public TypePathLengthFeature(
			final DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> graph,
			final FloydWarshallShortestPaths<Type, DefaultWeightedEdge> floydWarshall) {
		super(graph);
		this.floydWarshall = floydWarshall;
	}

	@Override
	public float computeFeature(final Type type) {
		final Set<Type> firstTypeSet = this.graph.vertexSet();
		double shortestPath = Double.MAX_VALUE;
		for (final Type ctype : firstTypeSet) {
			if (!ctype.isType()) {
				final double cPath = this.floydWarshall.shortestDistance(ctype, type);
				if (cPath < shortestPath) {
					shortestPath = cPath;
				}
			}
		}
		return (float) (WEIGHT * (1.0 / Math.sqrt(shortestPath)));
	}
}
