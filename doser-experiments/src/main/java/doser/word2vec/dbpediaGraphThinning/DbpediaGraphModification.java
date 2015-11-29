package doser.word2vec.dbpediaGraphThinning;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

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

import word2vec.tools.Word2VecModel;


public class DbpediaGraphModification {

	public final static String OUTPUTFILE = "/home/zwicklbauer/word2vec/MSEDbpediaCategories.txt";

	private Model m;
	
	private Model skosModel;

	private Word2VecModel w2v;

	public DbpediaGraphModification() {
		super();
		this.m = ModelFactory.createDefaultModel();
		this.m.read("/home/zwicklbauer/HDTGeneration/article_categories_en.nt");
		this.skosModel = ModelFactory.createDefaultModel();
		this.skosModel.read("/home/zwicklbauer/HDTGeneration/skos_categories_en.nt");
		this.w2v = Word2VecModel
				.createWord2VecModel("/mnt/ssd1/disambiguation/word2vec/wikientitymodel_min5.seq");
	}

	public Set<String> initializeCategories() {
		Model model = ModelFactory.createDefaultModel();
		model.read("/home/zwicklbauer/HDTGeneration/skos_categories_en.nt");
		StmtIterator it = model.listStatements();
		Set<String> set = new HashSet<String>();
		
		System.out.println("Los gehts");
		while (it.hasNext()) {
			Statement s = it.next();
			Resource r = s.getSubject();
			Property p = s.getPredicate();
			RDFNode n = s.getObject();
			if (p.getURI().equalsIgnoreCase(
					"http://www.w3.org/2004/02/skos/core#broader")
					&& n.isResource()) {
				Resource target = n.asResource();
				if(!hasSubCategory(target.getURI())) 
				set.add(target.getURI());
				if(!hasSubCategory(r.getURI())) 
				set.add(r.getURI());
			}
		}
		return set;
	}
	
	private boolean hasSubCategory(String uri) {
		final String query = "SELECT ?entities WHERE{ ?types <http://www.w3.org/2004/02/skos/core#broader> <"
				+ uri + ">. }";
		boolean hasSubtype = false;
		try {
			final com.hp.hpl.jena.query.Query cquery = QueryFactory
					.create(query);
			final QueryExecution qexec = QueryExecutionFactory
					.create(cquery, skosModel);
			final ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				hasSubtype = true;
				break;
			}

		} catch (final QueryException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
		return hasSubtype;
	}

	private Set<String> queryEntitiesFromCategory(final String catUri) {
		Set<String> set = new HashSet<String>();
		final String query = "SELECT ?entities WHERE{ ?entities <http://purl.org/dc/terms/subject> <"
				+ catUri + ">. }";
		try {
			final com.hp.hpl.jena.query.Query cquery = QueryFactory
					.create(query);
			final QueryExecution qexec = QueryExecutionFactory
					.create(cquery, m);
			final ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				final QuerySolution sol = results.nextSolution();
				set.add(sol.getResource("entities").getURI()
						.replaceAll("http://dbpedia.org/resource/", ""));
			}

		} catch (final QueryException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
		return set;
	}

	public float computeDistance(String[] words) {
		return this.w2v.computeMSE(words);
	}

	public static void main(String[] args) {
		File file = new File(OUTPUTFILE);
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(file);
			DbpediaGraphModification mod = new DbpediaGraphModification();
			Set<String> categories = mod.initializeCategories();
			for (String cat : categories) {
				Set<String> entities = mod.queryEntitiesFromCategory(cat);
				String[] entArr = new String[entities.size()];
				entArr = entities.toArray(entArr);
				float distance = mod.computeDistance(entArr);
				writer.println(distance+"\t"+cat);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
