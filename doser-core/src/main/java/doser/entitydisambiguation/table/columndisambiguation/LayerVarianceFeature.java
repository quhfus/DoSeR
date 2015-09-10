package doser.entitydisambiguation.table.columndisambiguation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import doser.entitydisambiguation.table.logic.Type;

public class LayerVarianceFeature extends AbstractTypeDisFeatures {

	private final static float WEIGHT = 0.258219f;

	public LayerVarianceFeature(
			final DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> graph) {
		super(graph);
	}

	@Override
	public float computeFeature(final Type type) {
		final Set<DefaultWeightedEdge> set = this.graph.incomingEdgesOf(type);
		VarianceCalculator cal = new VarianceCalculator();
		for (final DefaultWeightedEdge defWeightedEdge2 : set) {
			final DefaultWeightedEdge defWeightedEdge = defWeightedEdge2;
			final Type ctype = this.graph.getEdgeSource(defWeightedEdge);
			cal.addValue(ctype.getAccumulatedWeight());
		}
		double var = 0;
		double standardDeviation = cal.standardDeviation();
		if (standardDeviation < 0.001) {
			var = 1.0f;
		} else {
			var = standardDeviation;
		}
		return (float) (WEIGHT * (1 / Math.sqrt(var)));
	}

	class VarianceCalculator {
		
		private List<Double> l; 
		
		double sum;
		
		VarianceCalculator() {
			super();
			l = new ArrayList<Double>();
			sum = 0;
		}
		
		void addValue(double val) {
			l.add(new Double(val));
			sum += val;
		}
		
		double variance() {
			double mean = sum / l.size();
			double res = 0;
			for(Double d : l) {
				res += Math.pow((d - mean), 2);
			}
			return res;
		}
		
		double standardDeviation() {
			return Math.sqrt(variance());
		}
	}
	
}
