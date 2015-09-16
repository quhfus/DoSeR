package doser.webclassify.algorithm;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import doser.entitydisambiguation.dpo.DisambiguatedEntity;
import doser.entitydisambiguation.table.logic.Type;
import doser.tools.RDFGraphOperations;
import edu.uci.ics.jung.algorithms.scoring.HITS;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class EntitySignificanceAlgorithmHITSRelations {

	private static final int MAXGRAPHDEPTH = 4;

	public EntitySignificanceAlgorithmHITSRelations() {
		super();
	}

	public void process(Map<DisambiguatedEntity, Integer> map) {
		UndirectedSparseGraph<String, MyEdge> graph = new UndirectedSparseGraph<String, MyEdge>();

		Set<String> linkSet = new HashSet<String>();

		// Set graph vertexes
		for (Map.Entry<DisambiguatedEntity, Integer> entry : map.entrySet()) {
			String uri = entry.getKey().getEntityUri();
			graph.addVertex(uri);
			linkSet.add(uri);
		}

		// Set graph edges
		for (String s1 : linkSet) {
			Set<Type> types1 = RDFGraphOperations
					.getDbpediaCategoriesFromEntity(s1);
			int counter = 1;
			while(counter < MAXGRAPHDEPTH) {
				Set<Type> newSet = new HashSet<Type>();
				newSet.addAll(types1);
				for (Type t : types1) {
					Set<Type> s = RDFGraphOperations
							.getBroaderCategoriesOfCategory(t.getUri());
					newSet.addAll(s);
				}
				types1 = newSet;
				counter++;
			}
				
			for (String s2 : linkSet) {
				if (s1.hashCode() != s2.hashCode() && checkDependency(types1, s2)) {
					graph.addEdge(new MyEdge(), s1, s2);
				}
			}
		}
		HITS<String, MyEdge> hitsAlgorithm = new HITS<String, MyEdge>(graph);
		hitsAlgorithm.initialize();
		hitsAlgorithm.setTolerance(0.000001);
		hitsAlgorithm.setMaxIterations(200);
		hitsAlgorithm.evaluate();
		for (String s : graph.getVertices()) {
			System.out.println(s + "  \th:"
					+ hitsAlgorithm.getVertexScore(s).hub + "\ta:"
					+ hitsAlgorithm.getVertexScore(s).authority);
		}
		
		
	}

	private boolean checkDependency(Set<Type> set1, String s2) {
		Set<Type> types2 = RDFGraphOperations
				.getDbpediaCategoriesFromEntity(s2);
		int currentIteration = 0;
		boolean ret = false;
		while (currentIteration < MAXGRAPHDEPTH) {
			if (!Collections.disjoint(set1, types2)) {
				ret = true;
				break;
			}
			Set<Type> newSet = new HashSet<Type>();
			newSet.addAll(types2);
			for (Type t : types2) {
				Set<Type> s = RDFGraphOperations
						.getBroaderCategoriesOfCategory(t.getUri());
				newSet.addAll(s);
			}
			types2 = newSet;
			currentIteration++;
		}
		return ret;
	}

	class MyEdge {
	}

}
