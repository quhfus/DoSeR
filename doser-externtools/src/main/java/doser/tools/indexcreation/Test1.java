package doser.tools.indexcreation;

import java.io.IOException;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class Test1 {

	public static final String MAPPINGPROPERTIESHDT = "/home/quh/Arbeitsfläche/mappingbased_properties_cleaned_en.hdt";

	public static void main(String[] args) {
		Test1 test = new Test1();
		System.out.println(test.addAdditionalPersonNameOccurrences("http://dbpedia.org/resource/Pat_Riley"));
	}

	private Model mappingbasedproperties;

	public Test1() {
		HDT mappingbasedproperties;
		try {
			mappingbasedproperties = HDTManager.mapIndexedHDT(
					MAPPINGPROPERTIESHDT, null);
			final HDTGraph instancemappingtypesgraph = new HDTGraph(
					mappingbasedproperties);
			this.mappingbasedproperties = ModelFactory
					.createModelForGraph(instancemappingtypesgraph);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public HashSet<String> addAdditionalPersonNameOccurrences(String res) {
		HashSet<String> names = new HashSet<String>();
		try {
			final String query = "SELECT ?surname WHERE{ <" + res
					+ "> <http://xmlns.com/foaf/0.1/surname> ?surname. }";
			ResultSet results = null;
			QueryExecution qexec = null;

			final com.hp.hpl.jena.query.Query cquery = QueryFactory
					.create(query);
			qexec = QueryExecutionFactory.create(cquery,
					this.mappingbasedproperties);
			results = qexec.execSelect();

			if (results != null) {
				while (results.hasNext()) {
					final QuerySolution sol = results.nextSolution();
					final String surname = sol.getLiteral("surname")
							.getLexicalForm();
					names.add(surname);
				}
				qexec.close();
			}
		} catch (QueryParseException e) {
			Logger.getRootLogger().info("Query parse Exception");
		}

		return names;
	}

}
