package doser.word2vec.dbpediaGraphThinning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import word2vec.tools.Word2VecModel;


public class EvaluatePureDbpediaCategories {

	public static final int RANDOMDRAWS = 50000;

	private Random random;

	private Word2VecModel w2c;

	private Model m;

	public EvaluatePureDbpediaCategories() {
		super();
		this.random = new Random();
		// this.w2c =
		// Word2VecModel.createWord2VecModel("/mnt/ssd1/disambiguation/word2vec/wikientitymodel_min5.seq");
		this.m = ModelFactory.createDefaultModel();
		this.m.read("/home/zwicklbauer/HDTGeneration/article_categories_en.nt");
	}

	public String[] createCategorySet() {
		File file = new File(
				"/home/zwicklbauer/word2vec/MSEDbpediaCategories.txt");
		BufferedReader reader = null;
		String line = null;
		List<String> set = new LinkedList<String>();
		int counter = 0;
		try {
			reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine()) != null) {
				String splitter[] = line.split("\\t");
				float score = Float.valueOf(splitter[0]);
				if (score > 0 && score < 0.04) {
					set.add(splitter[1]);
				}
				if (score > 0) {
					counter++;
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
		System.out.println("Category Relation: " + set.size() + " of "
				+ counter);
		String[] arr = new String[set.size()];
		arr = set.toArray(arr);
		return arr;
	}

	public void evaluate(String[] categories) {
		try {
			PrintWriter writer = new PrintWriter(new File(
					"/home/zwicklbauer/samplingoutput.dat"));
			int counter = 0;
			while (counter < RANDOMDRAWS) {
				String cat = categories[this.random.nextInt(categories.length)];
				String e1 = queryEntitiesFromCategory(cat);
				String e2 = queryEntitiesFromCategory(cat);
				if (!e1.equalsIgnoreCase(e2)) {
					writer.println("0\t" + e1 + "\t" + e2 + "\t" + cat + "\t"
							+ cat);
					counter++;
				}
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
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

	public static void main(String[] args) {
		EvaluatePureDbpediaCategories ev = new EvaluatePureDbpediaCategories();
		ev.evaluate(ev.createCategorySet());
	}

}
