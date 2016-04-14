package doser.tools;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

import doser.entitydisambiguation.backend.DisambiguationMainService;
import doser.entitydisambiguation.table.logic.Type;

public final class RDFGraphOperations {

	private static RDFGraphOperations instance = null;

	public static Set<Type> getBroaderCategoriesOfCategory(final String category) {
		final Model model = DisambiguationMainService.getInstance()
				.getDBPediaArticleCategories();
		final Set<Type> types = new HashSet<Type>();
		final String query = "SELECT ?types WHERE{ <" + category
				+ "> <http://www.w3.org/2004/02/skos/core#broader> ?types. }";
		ResultSet results = null;
		QueryExecution qexec = null;
		try {
			final com.hp.hpl.jena.query.Query cquery = QueryFactory
					.create(query);
			qexec = QueryExecutionFactory.create(cquery, model);
			results = qexec.execSelect();
		} catch (final QueryException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		} finally {
			if (results != null) {
				while (results.hasNext()) {
					final QuerySolution sol = results.nextSolution();
					final String uri = sol.getResource("types").toString();
					final String name = queryDbPediaCategoryLabel(uri);
					final Type type = new Type(name, uri, true, 1);
					types.add(type);
				}
			}
			if (qexec != null) {
				qexec.close();
			}
		}
		return types;
	}
	
	public static Set<Type> getDbpediaCategoriesFromEntity(
			final String entityUri) {
		final Model model = DisambiguationMainService.getInstance()
				.getDBPediaArticleCategories();
		final Set<Type> types = new HashSet<Type>();
		final String query = "SELECT ?types WHERE{ <" + entityUri
				+ "> <http://purl.org/dc/terms/subject> ?types. }";
		ResultSet results = null;
		QueryExecution qexec = null;
		try {
			final com.hp.hpl.jena.query.Query cquery = QueryFactory
					.create(query);
			qexec = QueryExecutionFactory.create(cquery, model);
			results = qexec.execSelect();
		} catch (final QueryException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		} finally {
			if (results != null) {
				while (results.hasNext()) {
					final QuerySolution sol = results.nextSolution();
					final String uri = sol.getResource("types").toString();
					final String name = queryDbPediaCategoryLabel(uri);
					final Type type = new Type(name, uri, true, 1);
					types.add(type);
				}
			}
			if (qexec != null) {
				qexec.close();
			}
		}
		return types;
	}
	
	public static Set<Type> getDbpediaCategoriesFromEntity_GER(
			final String entityUri) {
		final Model model = DisambiguationMainService.getInstance()
				.getDBPediaArticleCategories_GER();
		final Set<Type> types = new HashSet<Type>();
		final String query = "SELECT ?types WHERE{ <" + entityUri
				+ "> <http://purl.org/dc/terms/subject> ?types. }";
		ResultSet results = null;
		QueryExecution qexec = null;
		try {
			final com.hp.hpl.jena.query.Query cquery = QueryFactory
					.create(query);
			qexec = QueryExecutionFactory.create(cquery, model);
			results = qexec.execSelect();
		} catch (final QueryException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		} finally {
			if (results != null) {
				while (results.hasNext()) {
					final QuerySolution sol = results.nextSolution();
					final String uri = sol.getResource("types").toString();
					final String name = queryDbPediaCategoryLabel(uri);
					final Type type = new Type(name, uri, true, 1);
					types.add(type);
				}
			}
			if (qexec != null) {
				qexec.close();
			}
		}
		return types;
	}
	
	public static Set<Type> getRDFTypesFromEntity(final String entityUri) {
		Set<Type> set = new HashSet<Type>();
		final Model model = DisambiguationMainService.getInstance().getDBPediaInstanceTypes();
		final String query = "SELECT ?types WHERE{ <" + entityUri
				+ "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?types. }";
		ResultSet results = null;
		QueryExecution qexec = null;
		try {
			final com.hp.hpl.jena.query.Query cquery = QueryFactory
					.create(query);
			qexec = QueryExecutionFactory.create(cquery, model);
			results = qexec.execSelect();
		} catch (final QueryException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		} finally {
			if (results != null) {
				while (results.hasNext()) {
					final QuerySolution sol = results.nextSolution();
					final String type = sol.getResource("types").toString();
					set.add(new Type("", type, true, 0));
				}
			}
		}
		return set;
	}

	public static String getDbPediaDescription(final String uri)
			throws QueryException {
		String res = null;
		final Model model = DisambiguationMainService.getInstance()
				.getDBPediaDescription();
		final String query = "SELECT ?description WHERE{ <" + uri
				+ "> <http://dbpedia.org/ontology/abstract> ?description. }";
		ResultSet results = null; // NOPMD by quh on 14.02.14 10:04
		QueryExecution qexec = null;
		try {
			final com.hp.hpl.jena.query.Query cquery = QueryFactory
					.create(query);
			qexec = QueryExecutionFactory.create(cquery, model);
			results = qexec.execSelect();
		} finally {
			if ((results != null) && results.hasNext()) {
				res = results.nextSolution().getLiteral("description")
						.getString();
			}
			if (qexec != null) {
				qexec.close();
			}
		}
		return res;
	}

	public static List<String> getDbPediaLabel(final String uri)
			throws QueryException {
		final List<String> labellist = new LinkedList<String>();

		final Model model = DisambiguationMainService.getInstance()
				.getDBPediaLabels();
		final String query = "SELECT ?label WHERE{ <" + uri
				+ "> <http://www.w3.org/2000/01/rdf-schema#label> ?label. }";
		ResultSet results = null; // NOPMD by quh on 14.02.14 10:04
		QueryExecution qexec = null;

		final com.hp.hpl.jena.query.Query cquery = QueryFactory.create(query);
		qexec = QueryExecutionFactory.create(cquery, model);
		results = qexec.execSelect();

		if (results != null) {
			while (results.hasNext()) {
				final QuerySolution sol = results.nextSolution();
				final String label = sol.getLiteral("label").getLexicalForm();
				labellist.add(label);
			}
			qexec.close();
		}
		return labellist;
	}
	
	public static List<String> getDbPediaLabel_GER(final String uri)
			throws QueryException {
		final List<String> labellist = new LinkedList<String>();

		final Model model = DisambiguationMainService.getInstance()
				.getDBPediaLabels_GER();
		final String query = "SELECT ?label WHERE{ <" + uri
				+ "> <http://www.w3.org/2000/01/rdf-schema#label> ?label. }";
		ResultSet results = null; // NOPMD by quh on 14.02.14 10:04
		QueryExecution qexec = null;

		final com.hp.hpl.jena.query.Query cquery = QueryFactory.create(query);
		qexec = QueryExecutionFactory.create(cquery, model);
		results = qexec.execSelect();

		if (results != null) {
			while (results.hasNext()) {
				final QuerySolution sol = results.nextSolution();
				final String label = sol.getLiteral("label").getLexicalForm();
				labellist.add(label);
			}
			qexec.close();
		}
		return labellist;
	}

	public static List<Type> getWordnetPredecessorTypesOfDbPediaCategory(
			final String dbpediaCat, final int layer) {
		// Transform Uri to yago wikicategoryUri!
		final String[] splitter = dbpediaCat.split(":");
		final String uri = "http://yago-knowledge.org/resource/wikicategory_"
				+ splitter[splitter.length - 1];

		// Query the supertype of this resource!
		final Model model = DisambiguationMainService.getInstance()
				.getYagoTaxonomy();
		final String query = "Select ?type WHERE{ <"
				+ uri
				+ "> <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?type. }";
		// System.out.println(query);
		final List<Type> resultList = new LinkedList<Type>();
		try {
			final com.hp.hpl.jena.query.Query que = QueryFactory.create(query);
			final QueryExecution qexec = QueryExecutionFactory.create(que,
					model);
			final ResultSet results = qexec.execSelect(); // NOPMD by quh on
															// 18.02.14 15:05
			while (results.hasNext()) {
				final QuerySolution sol = results.nextSolution();
				final String typeUri = sol.getResource("type").toString();
				final String name = queryYagoCategoryLabel(typeUri);
				final Type type = new Type(name, typeUri, true, layer);
				resultList.add(type);
			}
		} catch (final QueryException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
		return resultList;
	}

	private static String queryDbPediaCategoryLabel(final String catUri) {
		String res = null;
		final Model model = DisambiguationMainService.getInstance()
				.getDBPediaCategoryLabels();

		final String query = "SELECT ?label WHERE{ <" + catUri
				+ "> <http://www.w3.org/2000/01/rdf-schema#label> ?label. }";
		try {
			final com.hp.hpl.jena.query.Query cquery = QueryFactory
					.create(query);
			final QueryExecution qexec = QueryExecutionFactory.create(cquery,
					model);
			final ResultSet results = qexec.execSelect(); // NOPMD by quh on
															// 18.02.14 15:05
			while (results.hasNext()) {
				final QuerySolution sol = results.nextSolution();
				final String name = sol.getLiteral("label").getLexicalForm();
				res = name;
			}
		} catch (final QueryException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
		return res;
	}

	public static List<Type> queryWordnetTypePredecessor(final String uri,
			final int layer) {
		final List<Type> types = new LinkedList<Type>();
		final Model model = DisambiguationMainService.getInstance()
				.getYagoTaxonomy();
		final String query = "SELECT ?types WHERE{ <"
				+ uri
				+ "> <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?types. }";
		try {
			final com.hp.hpl.jena.query.Query cquery = QueryFactory
					.create(query);
			final QueryExecution qexec = QueryExecutionFactory.create(cquery,
					model);
			final ResultSet results = qexec.execSelect(); // NOPMD by quh on
															// 18.02.14 15:05
			while (results.hasNext()) {
				final QuerySolution sol = results.nextSolution();
				final String type = sol.getResource("types").toString();
				final String name = queryYagoCategoryLabel(type);
				types.add(new Type(name, type, true, layer));
				// System.out.println("Type: "+name+" Uri: "+type);
			}
		} catch (final QueryException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
		return types;
	}

	public static List<Type> queryYagoCategories(final String uri) {
		final Model model = DisambiguationMainService.getInstance()
				.getYagoTransitiveTypes();

		final List<Type> types = new LinkedList<Type>();
		final String query = "SELECT ?type WHERE{ <"
				+ uri
				+ "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type. }";
		try {
			final com.hp.hpl.jena.query.Query que = QueryFactory.create(query);
			final QueryExecution qexec = QueryExecutionFactory.create(que,
					model);
			final ResultSet results = qexec.execSelect(); // NOPMD by quh on
															// 18.02.14 15:05
			while (results.hasNext()) {
				final QuerySolution sol = results.nextSolution();
				final String name = sol.getResource("type").toString();
				types.add(new Type("", name, true, 0));
			}
		} catch (final QueryException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
		return types;
	}

	private static String queryYagoCategoryLabel(final String catUri) {
		String res = null;
		final Model model = DisambiguationMainService.getInstance()
				.getYagoCategoryLabels();

		final String query = "SELECT ?label WHERE{ <" + catUri
				+ "> <http://www.w3.org/2004/02/skos/core#> ?label. }";
		try {
			final com.hp.hpl.jena.query.Query que = QueryFactory.create(query);
			final QueryExecution qexec = QueryExecutionFactory.create(que,
					model);
			final ResultSet results = qexec.execSelect(); // NOPMD by quh on
															// 18.02.14 15:05
			while (results.hasNext()) {
				final QuerySolution sol = results.nextSolution();
				final String name = sol.getLiteral("label").getLexicalForm();
				res = name;
			}
		} catch (final QueryException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
		return res;
	}

	private RDFGraphOperations() {
		super();
	}

	public synchronized RDFGraphOperations getInstance() {
		if (instance == null) {
			instance = new RDFGraphOperations();
		}
		return instance;
	}
}
