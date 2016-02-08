package doser.entitydisambiguation.algorithms.rules;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import doser.entitydisambiguation.algorithms.SurfaceForm;
import doser.entitydisambiguation.knowledgebases.EntityCentricKBDBpedia;
import doser.lucene.query.TermQuery;

class CheckGeneralEntities extends AbstractRule {

	CheckGeneralEntities(EntityCentricKBDBpedia eckb) {
		super(eckb);
	}

	@Override
	public boolean applyRule(List<SurfaceForm> rep) {
		for (SurfaceForm c : rep) {
			String sf = c.getSurfaceForm().toLowerCase();
			List<String> candidates = c.getCandidates();
			String checked = null;
			// Surface Form - Candidate Match i.e. Saturday -
			// http://dbpedia.org/resource/Saturday
			for (String s : candidates) {
				String ent = s.replaceAll("http://dbpedia.org/resource/", "")
						.toLowerCase();
				if (sf.equalsIgnoreCase(ent)) {
					checked = s;
					break;
				}
			}

			if (checked != null && !checkSurfaceFormSubset(sf, rep)) {
				List<String> keepCandidates = new LinkedList<String>();
				for (String can : candidates) {
					String[] labels = null;
					IndexSearcher searcher = eckb.getSearcher();
					IndexReader reader = searcher.getIndexReader();
					TermQuery query = new TermQuery(new Term("Mainlink", can));
					try {
						final TopDocs top = searcher.search(query, 1);
						final ScoreDoc[] score = top.scoreDocs;
						final Document doc = reader.document(score[0].doc);
						labels = doc.getValues("Label");
					} catch (IOException e) {
						e.printStackTrace();
					}
					// Check whether the candidate has label of the original
					// surface form
					if (labels != null) {
						boolean isIn = false;
						for (int i = 0; i < labels.length; ++i) {
							if (labels[i].toLowerCase().equalsIgnoreCase(sf)) {
								isIn = true;
								break;
							}
						}
						// If IN, keep this candidate
						if (isIn) {
							keepCandidates.add(can);
						}
					}
				}
				if (!keepCandidates.isEmpty()) {
					c.setCandidates(keepCandidates);
					if(keepCandidates.size() == 1) {
						System.out.println("**********************************************************************");
						System.out.println(keepCandidates.toString());
						System.out.println("**********************************************************************");
					}
				}
			}
		}
		return false;
	}

	private boolean checkSurfaceFormSubset(String sf,
			List<SurfaceForm> reps) {
		boolean isIn = false;
		for (SurfaceForm c : reps) {
			String toCheck = c.getSurfaceForm().toLowerCase();
			if (!toCheck.equalsIgnoreCase(sf) && toCheck.contains(sf)) {
				isIn = true;
				break;
			}
		}
		return isIn;
	}
}
