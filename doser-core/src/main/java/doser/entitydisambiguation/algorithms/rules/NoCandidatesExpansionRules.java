package doser.entitydisambiguation.algorithms.rules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DefaultSimilarity;

import doser.entitydisambiguation.algorithms.SurfaceForm;
import doser.entitydisambiguation.knowledgebases.AbstractKnowledgeBase;
import doser.lucene.features.LuceneFeatures;
import doser.lucene.query.LearnToRankClause;
import doser.lucene.query.LearnToRankQuery;
import doser.tools.Inflector;

/**
 * Falls eine Surface Form keine Kandidaten hat, allerdings aus mindestens 3
 * Wörtern besteht, werden alle Wörter mit kleinergleich 3 Buchstaben entfernt
 * und erneut angefragt. Dies geschieht ebenfalls nach der Entfernung von
 * Sonderzeichen. Entsprechend werden die Kandidaten gesetzt.
 * 
 * @author quh
 */

class NoCandidatesExpansionRules extends AbstractRule {

	NoCandidatesExpansionRules(AbstractKnowledgeBase eckb) {
		super(eckb);
	}

	@Override
	public boolean applyRule(List<SurfaceForm> rep) {
		for (SurfaceForm c : rep) {
			if (c.getCandidates().size() == 0) {
				c.setCandidates(queryCandidates(c.getSurfaceForm()));
			}
		}
		return false;
	}

	private ArrayList<String> queryCandidates(String surfaceForm) {
		ArrayList<String> lst = new ArrayList<String>();
		String[] splitter = surfaceForm.split(" ");
		if (splitter.length > 2) {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < splitter.length; i++) {
				if (splitter[i].length() > 3) {
					builder.append(splitter[i] + " ");

				}
			}
			String builderstring = builder.toString();
			if (builderstring.length() > 0) {
				String newSf = builderstring.substring(0,
						builderstring.length() - 1);
				lst = queryLucene(surfaceForm);
				if (lst.size() == 0) {
					// Try again without special chars
					newSf = newSf.replaceAll("[^a-zA-Z ]", "");
					lst = queryLucene(newSf);
					// If size is 0 anyway, still check Plural to singular
					if (lst.size() == 0) {
						String singular = Inflector.getInstance().singularize(
								newSf);
						if (!newSf.equalsIgnoreCase(singular)) {
							// Try singular search
							lst = queryCandidates(singular);
						}
					}
				}
			}
		}
		return lst;
	}

	private ArrayList<String> queryLucene(String surfaceForm) {
		ArrayList<String> list = new ArrayList<String>();
		final IndexSearcher searcher = eckb.getSearcher();
		final IndexReader reader = searcher.getIndexReader();
		LearnToRankQuery query = new LearnToRankQuery();
		List<LearnToRankClause> features = new LinkedList<LearnToRankClause>();
		DefaultSimilarity defaultSim = new DefaultSimilarity();
		features.add(query.add(LuceneFeatures.queryLabelTerm(surfaceForm,
				"UniqueLabel", defaultSim), "Feature1", true));
		try {
			final TopDocs top = searcher.search(query, 150);
			final ScoreDoc[] score = top.scoreDocs;
			if (score.length <= 5) {
				for (int i = 0; i < score.length; ++i) {
					final Document doc = reader.document(score[i].doc);
					list.add(doc.get("Mainlink"));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}
}
