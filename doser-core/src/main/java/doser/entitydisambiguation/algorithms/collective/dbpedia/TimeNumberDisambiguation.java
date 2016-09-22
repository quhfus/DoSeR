package doser.entitydisambiguation.algorithms.collective.dbpedia;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

import doser.entitydisambiguation.algorithms.SurfaceForm;
import doser.entitydisambiguation.backend.DisambiguationMainService;
import doser.entitydisambiguation.knowledgebases.EntityCentricKBDBpedia;

class TimeNumberDisambiguation {

	private static final HashMap<String, String> TIMEANDNUMBERS = new HashMap<String, String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			put("monday", "http://dbpedia.org/resource/Monday");
			put("tuesday", "http://dbpedia.org/resource/Tuesday");
			put("wednesday", "http://dbpedia.org/resource/Wednesday");
			put("thursday", "http://dbpedia.org/resource/Thursday");
			put("friday", "http://dbpedia.org/resource/Friday");
			put("saturday", "http://dbpedia.org/resource/Saturday");
			put("sunday", "http://dbpedia.org/resource/Sunday");
			put("one", "http://dbpedia.org/resource/1_(number)");
			put("two", "http://dbpedia.org/resource/2_(number)");
			put("three", "http://dbpedia.org/resource/3_(number)");
			put("four", "http://dbpedia.org/resource/4_(number)");
			put("five", "http://dbpedia.org/resource/5_(number)");
			put("six", "http://dbpedia.org/resource/6_(number)");
			put("seven", "http://dbpedia.org/resource/7(number)");
			put("eight", "http://dbpedia.org/resource/8_(number)");
			put("nine", "http://dbpedia.org/resource/9_(number)");
			put("ten", "http://dbpedia.org/resource/10_(number)");
			put("eleven", "http://dbpedia.org/resource/11_(number)");
			put("twelve", "http://dbpedia.org/resource/12_(number)");
			put("thirteen", "http://dbpedia.org/resource/13_(number)");
			put("fourteen", "http://dbpedia.org/resource/14_(number)");
			put("fifteen", "http://dbpedia.org/resource/15_(number)");
			put("sixteen", "http://dbpedia.org/resource/16_(number)");
			put("seventeen", "http://dbpedia.org/resource/17_(number)");
			put("eighteen", "http://dbpedia.org/resource/18_(number)");
			put("nineteen", "http://dbpedia.org/resource/19_(number)");
			put("twenty", "http://dbpedia.org/resource/20_(number)");
			put("thirty", "http://dbpedia.org/resource/30_(number)");
			put("forty", "http://dbpedia.org/resource/40_(number)");
			put("fifty", "http://dbpedia.org/resource/50_(number)");
			put("sixty", "http://dbpedia.org/resource/60_(number)");
			put("seventy", "http://dbpedia.org/resource/70_(number)");
			put("eighty", "http://dbpedia.org/resource/80_(number)");
			put("ninety", "http://dbpedia.org/resource/90_(number)");
			put("hundred", "http://dbpedia.org/resource/100_(number)");
			put("year", "http://dbpedia.org/resource/Year");
			put("years", "http://dbpedia.org/resource/Year");
			put("january", "http://dbpedia.org/resource/January");
			put("february", "http://dbpedia.org/resource/February");
			put("march", "http://dbpedia.org/resource/March");
			put("april", "http://dbpedia.org/resource/April");
			put("may", "http://dbpedia.org/resource/May");
			put("june", "http://dbpedia.org/resource/June");
			put("july", "http://dbpedia.org/resource/July");
			put("august", "http://dbpedia.org/resource/August");
			put("september", "http://dbpedia.org/resource/September");
			put("october", "http://dbpedia.org/resource/October");
			put("november", "http://dbpedia.org/resource/November");
			put("december", "http://dbpedia.org/resource/December");
			put("mile", "http://dbpedia.org/resource/Mile");
			put("miles", "http://dbpedia.org/resource/Mile");
			put("hour", "http://dbpedia.org/resource/Hour");
			put("hours", "http://dbpedia.org/resource/Hour");
			put("second", "http://dbpedia.org/resource/Second");
			put("week", "http://dbpedia.org/resource/Week");
			put("weeks", "http://dbpedia.org/resource/Week");
//			put("socialist party", "http://dbpedia.org/resource/Socialist_Party_of_Serbia");
		}
	};

	private EntityCentricKBDBpedia eckb;

	public TimeNumberDisambiguation(EntityCentricKBDBpedia eckb) {
		super();
		this.eckb = eckb;
	}

	void solve(List<SurfaceForm> reps) {
		for (SurfaceForm sf : reps) {
			String s = sf.getSurfaceForm().toLowerCase();
			String redirect = null;
			if (TIMEANDNUMBERS.containsKey(s)) {
				sf.setDisambiguatedEntity(TIMEANDNUMBERS.get(s));
			} else if (isInteger(s, 10)) {
				String url = "http://dbpedia.org/resource/" + s + "_(number)";
				if (isInIndex(url)) {
					sf.setDisambiguatedEntity(url);
				} else if ((redirect = getRedirect(url)) != null) {
					sf.setDisambiguatedEntity(redirect);
				}
			}
		}
	}

	private static boolean isInteger(String s, int radix) {
		if (s.isEmpty())
			return false;
		for (int i = 0; i < s.length(); i++) {
			if (i == 0 && s.charAt(i) == '-') {
				if (s.length() == 1)
					return false;
				else
					continue;
			}
			if (Character.digit(s.charAt(i), radix) < 0)
				return false;
		}
		return true;
	}

	private boolean isInIndex(String url) {
		IndexSearcher searcher = this.eckb.getSearcher();
		Query query = new TermQuery(new Term("Mainlink", url));
		try {
			TopDocs topdocs = searcher.search(query, 1);
			ScoreDoc[] scoredoc = topdocs.scoreDocs;
			if (scoredoc.length > 0) {
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private String getRedirect(String uri) {
		final Model model = DisambiguationMainService.getInstance().getDBpediaRedirects();
		final String query = "SELECT ?label WHERE{ <" + uri
				+ "> <http://dbpedia.org/ontology/wikiPageRedirects> ?label. }";
		ResultSet results = null;
		QueryExecution qexec = null;
		String redirect = null;
		try {
			final com.hp.hpl.jena.query.Query cquery = QueryFactory.create(query);
			qexec = QueryExecutionFactory.create(cquery, model);
			results = qexec.execSelect();
		} catch (final QueryException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		} finally {
			if (results.hasNext()) {
				final QuerySolution sol = results.nextSolution();
				redirect = sol.getResource("label").getURI();
			}
		}
		return redirect;
	}
}
