package doser.word2vec.dbpediaGraphThinning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
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

import doser.word2vec.Word2VecModel;

public class FullyEvaluateCategories {

	public static final int NROFCHOSENCATEGORIES = 50000;
	
	private Word2VecModel w2c;
	
	private Random random;
	
	private Model m;
	
	public FullyEvaluateCategories() {
		super();
		this.random = new Random();
		this.m = ModelFactory.createDefaultModel();
		this.m.read("/home/zwicklbauer/HDTGeneration/article_categories_en.nt");
		this.w2c = Word2VecModel.createWord2VecModel("/mnt/ssd1/disambiguation/word2vec/wikientitymodel_min5.seq");
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
					"/home/zwicklbauer/purityInCategories.dat"));
			int counter = 0;
			while (counter < NROFCHOSENCATEGORIES) {
				String cat = categories[this.random.nextInt(categories.length)];
				Set<String> set = queryEntitiesFromCategory(cat);
				float sum = 0;
				float comp = 0;
				for(String e1 : set) {
					for(String e2 : set) {
						if(!e1.equalsIgnoreCase(e2)) {
							float score = w2c.computeSimilarity(e1, e2);
							if(score > -2) {
								sum += score;
								comp++;
							}
						}
					}
				}
				System.out.println(cat + "\t"+(sum/comp));
				
				writer.println("0\t"+(sum/comp));
					counter++;
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
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
	
	public static void main(String[] args) {
		FullyEvaluateCategories ev = new FullyEvaluateCategories();
		ev.evaluate(ev.createCategorySet());
	}
	
}
