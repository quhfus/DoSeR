package DBpediaCategoryCorrection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

public class DBpediaCategoryCorrection {

	public static String SKOSHDT;
	public static String MAINFILE;
	public static String OUTPUTFILE;

	private Model categorySkosModel;

	private HashSet<String> prominentCategories;
	
	public DBpediaCategoryCorrection(String prominentFile) {
		super();

		this.categorySkosModel = ModelFactory.createDefaultModel();
		this.categorySkosModel.read(SKOSHDT);

		// Create ProminentHashSet
		this.prominentCategories = new HashSet<String>();
		File f =  new File(prominentFile);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String category = "";
			while ((category = reader.readLine()) != null) {
				String cat = "http://dbpedia.org/resource/Category:"+category;
				prominentCategories.add(cat);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if (args.length < 4 || args.length > 4) {
			System.out
					.println("Bitte Parameter korrekt eingeben: Promising Categories - OutputFile - EntityCategories - SKOSCategories");
		} else {
			DBpediaCategoryCorrection.MAINFILE = args[0];
			DBpediaCategoryCorrection.OUTPUTFILE = args[2];
			DBpediaCategoryCorrection.SKOSHDT = args[3];
			
			DBpediaCategoryCorrection correction = new DBpediaCategoryCorrection(args[1]);
			correction.correctCategories();
		}
	}

	public void correctCategories() {
		File outputFile = new File(OUTPUTFILE);
		File f = new File(MAINFILE);
		try {
			PrintWriter writer = new PrintWriter(outputFile);
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String category = "";
			while ((category = reader.readLine()) != null) {
				HashSet<String> s = new HashSet<String>();
				s.add(category);
				Set<String> set = recursiveIteration(s, 0);
				for (String str : set) {
					writer.println(category+"\t"+str);
				}
			}
			writer.close();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Set<String> recursiveIteration(Set<String> stack, int depth) {
		Set<String> result = new HashSet<String>();
		for(String s : stack) {
			if(prominentCategories.contains(s)) {
				result.add(s);
			}
		}
		if(result.size() == 0 && depth < 20) {
			Set<String> newStack = new HashSet<String>();
			for(String s : stack) {
				Set<String> set = querySubCategories(s);
				System.out.println("Size: "+set.size());
				newStack.addAll(set);
			}
			return recursiveIteration(newStack, depth + 1);
		} else {
			return result;
		}
	}
	
	public Set<String> querySubCategories(final String uri) {
		final Set<String> types = new HashSet<String>();
		final String query = "SELECT ?sub WHERE { <"+uri+"> <http://www.w3.org/2004/02/skos/core#broader> ?sub }";

		try {
			final com.hp.hpl.jena.query.Query que = QueryFactory.create(query);
			final QueryExecution qexec = QueryExecutionFactory.create(que,
					categorySkosModel);
			final ResultSet results = qexec.execSelect();

			while (results.hasNext()) {
				final QuerySolution sol = results.nextSolution();
				final String name = sol.getResource("sub").toString();
				types.add(new String(name));
			}
		} catch (final QueryException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
		return types;
	}
}
