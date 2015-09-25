package doser.entitydisambiguation.algorithms.collective.hybrid;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import doser.entitydisambiguation.algorithms.collective.CollectiveSFRepresentation;
import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBaseDefault;
import doser.lucene.query.TermQuery;

class LocationDisambiguation {

	private EntityCentricKnowledgeBaseDefault eckb;

	public LocationDisambiguation(EntityCentricKnowledgeBaseDefault eckb) {
		super();
		this.eckb = eckb;
	}

	void solve(List<CollectiveSFRepresentation> reps) {
		for (CollectiveSFRepresentation c : reps) {
			if (c.getCandidates().size() > 1) {
				disambiguate(c);
			}
		}
	}

	private void disambiguate(CollectiveSFRepresentation c) {
		List<String> candidates = c.getCandidates();
		String surfaceForm = c.getSurfaceForm();
		Set<Document> sfDocuments = queryLuceneLabel(surfaceForm);
		removeUnusedDocs(sfDocuments, candidates);
		Set<Document> nonLocations = checkForLocation(sfDocuments);
		if (isLocation(nonLocations)) {
			c.setDisambiguatedEntity(selectLocationWithSensePrior(sfDocuments,
					candidates, c.getSurfaceForm()));
		}
	}

	private String selectLocationWithSensePrior(Set<Document> relevantEntities,
			List<String> allRelevantEntities, String surfaceForm) {
		Set<String> relString = new HashSet<String>();
		for (Document d : relevantEntities) {
			String type = d.get("Type");
			if (type.equals("Location")) {
				relString.add(d.get("Mainlink"));
			}
		}
		// Integrate other Location Check e.g. Tennessee, Tennessee,_Illinois; Maybe we have indicators for Tennesse,_Illinois. 
		return sensePriorDisambiguation(new LinkedList<String>(relString),
				surfaceForm);
	}

	private boolean isLocation(Set<Document> nonLocationSet) {
		return true;
	}

	private void removeUnusedDocs(Set<Document> set, List<String> candidates) {
		for (Iterator<Document> iterator = set.iterator(); iterator.hasNext();) {
			Document d = (Document) iterator.next();
			String mainLink = d.get("Mainlink");
			if (!candidates.contains(mainLink)) {
				iterator.remove();
			}
		}
	}

	private Set<Document> checkForLocation(Set<Document> set) {
		Set<Document> nonLocations = new HashSet<Document>();
		for (Document d : set) {
			String type = d.get("Type");
			if (!type.equals("Location")) {
				nonLocations.add(d);
			}
		}
		return nonLocations;
	}

	private Set<Document> queryLuceneLabel(String surfaceForm) {
		Set<Document> documents = new HashSet<Document>();
		Query query = new TermQuery(new Term("Label", surfaceForm));
		final IndexSearcher searcher = eckb.getSearcher();
		final IndexReader reader = searcher.getIndexReader();
		try {
			final TopDocs top = searcher.search(query, 250);
			final ScoreDoc[] score = top.scoreDocs;
			for (int i = 0; i < score.length; i++) {
				final Document doc = reader.document(score[i].doc);
				documents.add(doc);
			}
		} catch (IOException e) {
			Logger.getRootLogger().error("Lucene Searcher Error: ", e);
		}
		return documents;
	}

	private String sensePriorDisambiguation(List<String> entities, String sf) {
		List<Candidate> canList = new LinkedList<Candidate>();
		for (String str : entities) {
			canList.add(new Candidate(str, eckb.getFeatureDefinition()
					.getOccurrences(sf, str)));
		}
		Collections.sort(canList, Collections.reverseOrder());
		return canList.get(0).getCandidate();
	}
}
