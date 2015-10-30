package doser.webclassify.algorithm;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.functors.MapTransformer;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import doser.entitydisambiguation.algorithms.collective.hybrid.Edge;
import doser.entitydisambiguation.algorithms.collective.hybrid.Vertex;
import doser.entitydisambiguation.dpo.DisambiguatedEntity;
import doser.webclassify.dpo.Paragraph;
import doser.word2vec.Word2VecJsonFormat;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public class EntitySignificanceAlgorithmPR_W2V implements
		EntityRelevanceAlgorithm {

	private Map<String, Float> word2vecsimilarities;
	private Map<Edge, Number> edgeWeights;
	private Factory<Integer> edgeFactory;

	@Override
	public String process(Map<DisambiguatedEntity, Integer> map, Paragraph p) {
		Set<String> entitySet = new HashSet<String>();
		List<String> entities = new LinkedList<String>();
		for (Map.Entry<DisambiguatedEntity, Integer> entry : map.entrySet()) {
			entities.add(entry.getKey().getEntityUri());
			entitySet.add(entry.getKey().getEntityUri());
		}
		if (entities.size() == 0) {
			return "";
		} else {
			computeWord2VecSimilarities(entitySet);
			DirectedGraph<Vertex, Edge> graph = buildGraph(entities);

			PageRank<Vertex, Edge> pr = new PageRank<Vertex, Edge>(graph,
					MapTransformer.getInstance(edgeWeights), 0.1);
			pr.setMaxIterations(100);
			pr.evaluate();
			Collection<Vertex> vertexCol = graph.getVertices();
			String topEntity = null;
			double max = 0;
			for (Vertex v : vertexCol) {
				Double score = pr.getVertexScore(v);
				if (score > max) {
					topEntity = v.getUris().get(0);
					max = score;
				}
			}
			return topEntity;
		}
	}

	private float getWord2VecSimilarity(String source, String target) {
		source = source.replaceAll("http://dbpedia.org/resource/", "");
		target = target.replaceAll("http://dbpedia.org/resource/", "");
		int c = source.compareToIgnoreCase(target);
		String res = "";
		if (c < 0) {
			res = source + "|" + target;
		} else if (c == 0) {
			res = source + "|" + target;
		} else {
			res = target + "|" + source;
		}

		float result = 0;
		if (this.word2vecsimilarities.containsKey(res)) {
			result = this.word2vecsimilarities.get(res) + 1.0f;
		}
		return result;
	}

	private void computeWord2VecSimilarities(Set<String> entities) {
		this.word2vecsimilarities = new HashMap<String, Float>();
		Set<String> combinations = new HashSet<String>();
		for (String s1 : entities) {
			for (String s2 : entities) {
				combinations.add(s1.replaceAll("http://dbpedia.org/resource/",
						"")
						+ "|"
						+ s2.replaceAll("http://dbpedia.org/resource/", ""));
			}
		}

		Word2VecJsonFormat format = new Word2VecJsonFormat();
		format.setData(combinations);
		JSONArray res = Word2VecJsonFormat.performquery(format, "w2vsim");
		for (int i = 0; i < res.length(); i++) {
			try {
				JSONObject obj = res.getJSONObject(i);
				String ents = obj.getString("ents");
				float sim = (float) obj.getDouble("sim");
				this.word2vecsimilarities.put(ents, sim);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private DirectedGraph<Vertex, Edge> buildGraph(List<String> entities) {
		this.edgeWeights = new HashMap<Edge, Number>();
		this.edgeFactory = new Factory<Integer>() {
			int i = 0;

			public Integer create() {
				return i++;
			}
		};
		DirectedGraph<Vertex, Edge> graph = new DirectedSparseMultigraph<Vertex, Edge>();
		for (String e : entities) {
			Vertex v = new Vertex();
			v.addUri(e);
			graph.addVertex(v);
		}

		Collection<Vertex> vertexes = graph.getVertices();
		for (Vertex v1 : vertexes) {
			for (Vertex v2 : vertexes) {
				float similarity = this.getWord2VecSimilarity(
						v1.getUris().get(0), v2.getUris().get(0));
				Edge edge = new Edge(this.edgeFactory.create(), v2, similarity);
				v1.addOutGoingEdge(edge);
				graph.addEdge(edge, v1, v2);
			}
		}

		vertexes = graph.getVertices();
		for (Vertex v : vertexes) {
			Set<Edge> edges = v.getOutgoingEdges();
			for (Edge e : edges) {
				edgeWeights.put(e, e.getProbability());
			}
		}
		return graph;
	}
}
