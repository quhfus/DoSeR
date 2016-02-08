package doser.entitydisambiguation.algorithms.rules;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import doser.entitydisambiguation.algorithms.SurfaceForm;
import doser.entitydisambiguation.knowledgebases.EntityCentricKBDBpedia;
import doser.lucene.query.TermQuery;

/**
 * Falls eine Surface Form eindeutig ist und weitere Surface Forms eine
 * Abkürzung darstellen, diese allerdings nicht eindeutig sind, wird dies sofort
 * aufgelöst.
 * 
 * Beispiel: 1 Surface Form: Burlington Industries Inc (eindeutig) 2 Surface
 * Form: Burlington (ambiguous) ...
 * 
 * 
 * @author quh
 *
 */

class UnambiguousToAmbiguousRule extends AbstractRule {

	UnambiguousToAmbiguousRule(EntityCentricKBDBpedia eckb) {
		super(eckb);
	}

	@Override
	public boolean applyRule(List<SurfaceForm> rep) {
		List<SurfaceForm> unambiguous = new LinkedList<SurfaceForm>();
		for (SurfaceForm c : rep) {
			if (c.getCandidates().size() == 1) {
				String candidate = c.getCandidates().get(0);
				String type = queryType(candidate);
				if (type.equalsIgnoreCase("Person") || type.equalsIgnoreCase("Organisation")) {
					unambiguous.add(c);
				}
			}
		}
		for (SurfaceForm c : rep) {
			if (c.getCandidates().size() > 1) {
				HashMap<String, Integer> map = new HashMap<String, Integer>();
				for (SurfaceForm un : unambiguous) {
					String type = queryType(un.getCandidates().get(0));
					if ((isSubString(un.getSurfaceForm(), c.getSurfaceForm())
							&& c.getCandidates().contains(un.getCandidates().get(0))
							&& un.getPosition() < c.getPosition())
							|| (type.equalsIgnoreCase("Person") && isSubString(un.getSurfaceForm(), c.getSurfaceForm())
									&& un.getPosition() < c.getPosition())) {
						map.put(un.getCandidates().get(0), c.getPosition() - un.getPosition());
						// c.setDisambiguatedEntity(un.getCandidates().get(0));
					}
				}
				if (!map.isEmpty()) {
					int distance = Integer.MAX_VALUE;
					String can = "";
					for (Map.Entry<String, Integer> entry : map.entrySet()) {
						if (entry.getValue() < distance) {
							distance = entry.getValue();
							can = entry.getKey();
						}
					}
					c.setDisambiguatedEntity(can);
				}
			}
		}
		return false;
	}

	private boolean isSubString(String s1, String s2) {
		if (s1.toLowerCase().contains(s2.toLowerCase())) {
			return true;
		} else
			return false;
	}

	private String queryType(String url) {
		String type = "";
		IndexSearcher searcher = eckb.getSearcher();
		Query q = new TermQuery(new Term("Mainlink", url));
		try {
			TopDocs docs = searcher.search(q, 1);
			ScoreDoc[] scoredocs = docs.scoreDocs;
			if(scoredocs.length == 0) {
				type = "Misc";
			} else {
				int nr = scoredocs[0].doc;
				Document doc = searcher.getIndexReader().document(nr);
				type = doc.get("Type");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return type;
	}

}
