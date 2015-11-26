package word2vec.corpuscreation;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class CreateRandomDBpediaModel {

	public static final int JUMPPROBABILITY = 14;

	public static final int STEPNR = 100000000;

	public static final String INFOBOXPROPERTIES = "/home/zwicklbauer/HDTGeneration/infobox_properties_en.nt";
	public static final String MAPPINGPROPERTIES = "/home/zwicklbauer/HDTGeneration/mappingbased_properties_cleaned_en.nt";
	public static final String ARTICLECATEGORIES = "/home/zwicklbauer/HDTGeneration/article_categories_en.nt";
	public static final String SKOSBROADER = "/home/zwicklbauer/HDTGeneration/skos_categories_en.nt";

	public static final String MODELPATH = "/home/zwicklbauer/word2vec/dbpediamodel_noCategories.dat";

	private Random random;
	private UndirectedGraph<String, DefaultEdge> graph;

	private String[] vertexes;

	public CreateRandomDBpediaModel() {
		super();
		this.graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
		this.random = new Random();
	}

	public void createDBpediaGraph() {
		System.out.println("Create DBpediaGraph");
		System.out.println("Add Facts");
		addFactsToGraph();
		// System.out.println("Add Categories");
		// addCategoriesToGraph();
		// System.out.println("Add SkosBroader");
		// addSkosBroaderToGraph();
		Set<String> v = graph.vertexSet();
		this.vertexes = new String[v.size()];
		this.vertexes = v.toArray(this.vertexes);
	}

	public void createWord2VecModel() {
		try {
			PrintWriter writer = new PrintWriter(MODELPATH);
			int counter = 0;
			String init = null;
			while (init == null) {
				init = performRandomJump();
			}
			writer.write(init.replaceAll("http://dbpedia.org/resource/", ""));
			while (counter < STEPNR) {
				init = performNextStep(init);
				String output = init.replaceAll("http://dbpedia.org/resource/",
						"");
				writer.write(output + " ");
				counter++;
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private String performNextStep(String current) {
		String result = null;
		String randomjump = performRandomJump();
		if (randomjump == null) {
			if (graph.containsVertex(current)) {
				Set<DefaultEdge> edgeSet = graph.edgesOf(current);

				DefaultEdge[] edges = new DefaultEdge[edgeSet.size()];
				edges = edgeSet.toArray(edges);
				DefaultEdge def = edges[random.nextInt(edges.length)];
				String source = graph.getEdgeSource(def);
				String target = graph.getEdgeTarget(def);
				String relevant = null;
				if (source.equalsIgnoreCase(current)) {
					relevant = target;
				} else {
					relevant = source;
				}
				result = relevant;
				if (relevant.contains("Category:")) {
					relevant = performNextStep(current);
					result = relevant;
				}
			} else {
				result = performSaveRandomJump();
			}
		} else {
			result = randomjump;
		}
		if (result.contains("__")) {
			String[] splitter = result.split("__");
			result = splitter[0];
		}
		return result;
	}

	private String performRandomJump() {
		String result = null;
		int randomint = random.nextInt(100);
		if (randomint < JUMPPROBABILITY) {
			int jump = random.nextInt(vertexes.length);
			result = vertexes[jump];
			while (result.contains("Category:")) {
				jump = random.nextInt(vertexes.length);
				result = vertexes[jump];
			}
		}
		return result;
	}

	private String performSaveRandomJump() {
		String result = null;
		int jump = random.nextInt(vertexes.length);
		result = vertexes[jump];
		while (result.contains("Category:")) {
			jump = random.nextInt(vertexes.length);
			result = vertexes[jump];
		}
		return result;
	}

	private void addCategoriesToGraph() {
		Model m = ModelFactory.createDefaultModel();
		m.read(ARTICLECATEGORIES);
		StmtIterator it = m.listStatements();
		int counter = 0;
		while (it.hasNext()) {
			if (counter % 10000 == 0) {
				System.out.println(counter);
			}
			Statement s = it.next();
			Resource subject = s.getSubject();
			Property pra = s.getPredicate();
			RDFNode object = s.getObject();
			if (object.isResource()) {
				Resource obj = object.asResource();
				if (pra.isResource()
						&& obj.getURI().startsWith(
								"http://dbpedia.org/resource/")) {
					if (!subject.getURI().equalsIgnoreCase(obj.getURI())) {
						graph.addVertex(subject.getURI());
						graph.addVertex(obj.getURI());
						graph.addEdge(subject.getURI(), obj.getURI());
					}
				}
			}
		}
		counter++;
	}

	private void addSkosBroaderToGraph() {
		Model m = ModelFactory.createDefaultModel();
		m.read(SKOSBROADER);
		StmtIterator it = m.listStatements();
		while (it.hasNext()) {
			Statement s = it.next();
			Resource subject = s.getSubject();
			Property pra = s.getPredicate();
			RDFNode object = s.getObject();
			if (object.isResource()) {
				Resource obj = object.asResource();
				if (pra.isResource()
						&& obj.getURI().startsWith(
								"http://dbpedia.org/resource/")) {
					if (!subject.getURI().equalsIgnoreCase(obj.getURI())) {
						graph.addVertex(subject.getURI());
						graph.addVertex(obj.getURI());
						graph.addEdge(subject.getURI(), obj.getURI());
					}
				}
			}
		}
	}

	private void addFactsToGraph() {
		Model m = ModelFactory.createDefaultModel();
		m.read(INFOBOXPROPERTIES);
		StmtIterator it = m.listStatements();
		int counter = 0;
		while (it.hasNext()) {
			if (counter % 10000 == 0) {
				System.out.println(counter);
			}
			Statement s = it.next();
			Resource subject = s.getSubject();
			Property pra = s.getPredicate();
			RDFNode object = s.getObject();
			if (object.isResource()) {
				Resource obj = object.asResource();
				if (pra.isResource()
						&& obj.getURI().startsWith(
								"http://dbpedia.org/resource/")) {
					if (!subject.getURI().equalsIgnoreCase(obj.getURI())) {
						graph.addVertex(subject.getURI());
						graph.addVertex(obj.getURI());
						graph.addEdge(subject.getURI(), obj.getURI());
					}
				}
			}
			counter++;
		}
		m = ModelFactory.createDefaultModel();
		m.read(MAPPINGPROPERTIES);
		it = m.listStatements();
		counter = 0;
		while (it.hasNext()) {
			if (counter % 10000 == 0) {
				System.out.println(counter);
			}
			Statement s = it.next();
			Resource subject = s.getSubject();
			Property pra = s.getPredicate();
			RDFNode object = s.getObject();
			if (object.isResource()) {
				Resource obj = object.asResource();
				if (pra.isResource()
						&& obj.getURI().startsWith(
								"http://dbpedia.org/resource/")) {
					if (!subject.getURI().equalsIgnoreCase(obj.getURI())) {
						graph.addVertex(subject.getURI());
						graph.addVertex(obj.getURI());
						graph.addEdge(subject.getURI(), obj.getURI());
					}
				}
			}
			counter++;
		}
	}

	public static void main(String[] args) {
		CreateRandomDBpediaModel model = new CreateRandomDBpediaModel();
		model.createDBpediaGraph();
		model.createWord2VecModel();
	}

}
