package doser.entitydisambiguation.table.columndisambiguation;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import doser.entitydisambiguation.table.logic.Type;

abstract class AbstractTypeDisFeatures {

	protected DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> graph;

	AbstractTypeDisFeatures(
			final DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> graph) {
		super();
		this.graph = graph;
	}

	abstract float computeFeature(Type type);
}
