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
 * Überprüft ob eine surface form im plural angegeben ist und falls ja überprüfe
 * den singular
 * 
 * @author stefan
 *
 */
class NoCandidatesCheckPlural extends AbstractRule {

	NoCandidatesCheckPlural(AbstractKnowledgeBase eckb) {
		super(eckb);
	}

	@Override
	public boolean applyRule(List<SurfaceForm> rep) {
		for (SurfaceForm r : rep) {
			if (r.getCandidates().size() == 0) {
				String sf = r.getSurfaceForm();
				String singular = Inflector.getInstance().singularize(sf);
				if (!sf.equalsIgnoreCase(singular)) {
					// Try singular search
					ArrayList<String> lst = queryLucene(singular);
					if (lst.size() != 0) {
						r.setCandidates(lst);
					}
				}
			}
		}
		return false;
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
