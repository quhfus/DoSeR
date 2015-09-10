package doser.word2vec.semanticCategories;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultEdge;

import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class Sampling {

	public static final String CATEGORYPURITY = "/home/zwicklbauer/word2vec/MSEDbpediaCategories_Min5.txt";

	public static final int MAXIMUMSAMPLENR = 5000;

	private Graph<String, DefaultEdge> graph;

	private String[] catSet;
	private HashSet<String> catHash;

	private Model m;

	private Random random;

	public Sampling() {
		super();
		BufferedReader reader = null;
		List<String> catList = new LinkedList<String>();
		this.catHash = new HashSet<String>();
		try {
			reader = new BufferedReader(
					new FileReader(new File(CATEGORYPURITY)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String splitter[] = line.split("\t");
				double score = Double.parseDouble(splitter[0]);
				if (score < 0.033 && score > -2) {
					catList.add(splitter[1]);
					this.catHash.add(splitter[1]);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		this.catSet = new String[catList.size()];
		this.catSet = catList.toArray(this.catSet);
		this.graph = createGraph();
		this.m = ModelFactory.createDefaultModel();
		this.m.read("/home/zwicklbauer/HDTGeneration/article_categories_en.nt");
		this.random = new Random();
		System.out.println(this.catHash.size());
	}

	public Map<Integer, HashSet<EntityPair>> generateCandidates(int maxDistance) {
		ConcurrentMap<Integer, HashSet<EntityPair>> map = new ConcurrentHashMap<Integer, HashSet<EntityPair>>();
		for (int i = 0; i < maxDistance; i++) {
			int stepsize = i;
			int counter = 0;
			while (true) {
				String randomCat = pickCategory();
				String basicCat = randomCat;
				String e1 = queryEntitiesFromCategory(randomCat);
				if (e1 != null) {
					boolean foundRelevantCategory = false;
					while (!foundRelevantCategory) {
						// Choose random category
						randomCat = performRandomStep(randomCat);
						if (catHash.contains(randomCat)) {
							foundRelevantCategory = true;
						}
					}

					if (randomCat != null) {
						String e2 = queryEntitiesFromCategory(randomCat);
						if (e2 != null) {
							List path = DijkstraShortestPath.findPathBetween(
									graph, basicCat, randomCat);
							if (path.size() == stepsize
									&& !e1.equalsIgnoreCase(e2)) {
								if (map.containsKey(stepsize)) {
									HashSet<EntityPair> set = map.get(stepsize);
									set.add(new EntityPair(e1, e2, basicCat,
											randomCat));
									counter++;
								} else {
									HashSet<EntityPair> set = new HashSet<EntityPair>();
									set.add(new EntityPair(e1, e2, basicCat,
											randomCat));
									map.put(stepsize, set);
									counter++;
								}
							}
						}
					}
				}
				System.out.println(counter);
				
				if (counter == MAXIMUMSAMPLENR) {
					break;
				}
			}
		}
		return map;
	}

	private String performRandomStep(String current) {
		if (current == null) {
			return null;
		}
		String jumpstep = null;
		Set<DefaultEdge> edges = graph.edgesOf(current);
		int max = edges.size();
		int ran = random.nextInt(max);
		int counter = 0;
		for (DefaultEdge e : edges) {
			if (ran == counter) {
				String source = graph.getEdgeSource(e);
				String target = graph.getEdgeTarget(e);
				if (source.equalsIgnoreCase(current)) {
					jumpstep = target;
				} else {
					jumpstep = source;
				}
				break;
			}
			counter++;
		}
		return jumpstep;
	}

	private String pickCategory() {
		int index = random.nextInt(catSet.length);
		return catSet[index];
	}

	public UndirectedGraph<String, DefaultEdge> createGraph() {
		Model model = ModelFactory.createDefaultModel();
		model.read("/home/zwicklbauer/HDTGeneration/skos_categories_en.nt");
		StmtIterator it = model.listStatements();
		UndirectedGraph<String, DefaultEdge> graph = new MiGrafo();
		Set<String> set = new HashSet<String>();

		int counter = 0;
		while (it.hasNext()) {
			Statement s = it.next();
			Resource r = s.getSubject();
			Property p = s.getPredicate();
			RDFNode n = s.getObject();
			if (p.getURI().equalsIgnoreCase(
					"http://www.w3.org/2004/02/skos/core#broader")
					&& n.isResource()) {
				set.add(r.getURI());
				Resource target = n.asResource();
				set.add(target.getURI());
				if (!graph.containsVertex(r.getURI())) {
					graph.addVertex(r.getURI());
				}
				if (!graph.containsVertex(target.getURI())) {
					graph.addVertex(target.getURI());
				}
				graph.addEdge(r.getURI(), target.getURI());
				if (counter % 10000 == 0) {
					System.out.println(counter);
				}
				counter++;
			}
		}
		return graph;
	}

	private String queryEntitiesFromCategory(final String catUri) {
		String res = null;

		final String query = "SELECT ?entities WHERE{ ?entities <http://purl.org/dc/terms/subject> <"
				+ catUri + ">. }";
		try {
			final com.hp.hpl.jena.query.Query cquery = QueryFactory
					.create(query);
			final QueryExecution qexec = QueryExecutionFactory
					.create(cquery, m);
			final ResultSet results = qexec.execSelect();
			List<String> entities = new LinkedList<String>();
			while (results.hasNext()) {
				final QuerySolution sol = results.nextSolution();
				entities.add(sol.getResource("entities").getURI());
			}
			if (entities.size() != 0) {
				int randomNr = this.random.nextInt(entities.size());
				return entities.get(randomNr);
			}

		} catch (final QueryException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
		return res;
	}

	class MiGrafo extends AbstractBaseGraph<String, DefaultEdge> implements
			UndirectedGraph<String, DefaultEdge> {

		private static final long serialVersionUID = 1L;

		MiGrafo() {
			super(new ClassBasedEdgeFactory<String, DefaultEdge>(
					DefaultEdge.class), true, true);

		}
	}

	public static void main(String[] args) {
		Sampling sampling = new Sampling();
		Map<Integer, HashSet<EntityPair>> map = sampling.generateCandidates(4);
		File file = new File("/home/zwicklbauer/samplingoutput.dat");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(file);
			for (Map.Entry<Integer, HashSet<EntityPair>> entry : map.entrySet()) {
				Integer key = entry.getKey();
				HashSet<EntityPair> value = entry.getValue();
				for (EntityPair p : value) {
					writer.println(String.valueOf(key) + "\t" + p.getEntity1()
							+ "\t" + p.getEntity2() + "\t" + p.getCategory1()
							+ "\t" + p.getCategory2());
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

}
