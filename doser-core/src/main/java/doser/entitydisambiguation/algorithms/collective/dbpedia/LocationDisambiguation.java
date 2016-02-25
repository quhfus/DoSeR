package doser.entitydisambiguation.algorithms.collective.dbpedia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;

import doser.entitydisambiguation.algorithms.AbstractDisambiguationAlgorithm;
import doser.entitydisambiguation.algorithms.SurfaceForm;
import doser.entitydisambiguation.knowledgebases.EntityCentricKBDBpedia;
import doser.lucene.query.TermQuery;

class LocationDisambiguation {
	
	private static final float DOC2VECTHRESHOLD = 1.37f;
	private EntityCentricKBDBpedia eckb;

	public LocationDisambiguation(EntityCentricKBDBpedia eckb) {
		super();
		this.eckb = eckb;
	}

	void solve(List<SurfaceForm> reps) {
		for (SurfaceForm c : reps) {
			if (c.getCandidates().size() > 1 && !c.isMatchesInitial()) {
				disambiguate(c);
			}
		}
	}

	private void disambiguate(SurfaceForm c) {
		String context = AbstractDisambiguationAlgorithm.extractContext(
				c.getPosition(), c.getContext(),
				CollectiveAndContextDriver.PREPROCESSINGCONTEXTSIZE);

		List<String> candidates = c.getCandidates();
		String surfaceForm = c.getSurfaceForm();
		Set<Document> sfDocuments = queryLuceneLabel(surfaceForm, candidates);
		removeUnusedDocs(sfDocuments, candidates);
		Set<Document> nonLocations = checkForLocation(sfDocuments);

		// Dont care if no locations are available
		if (nonLocations.size() < sfDocuments.size()) {
			if (isLocation(nonLocations, c)) {
				String s = solveLocations(sfDocuments, candidates,
						c.getSurfaceForm(), context);
				if (s != null) {
					c.setDisambiguatedEntity(s);
				}
			}
		}
	}

	private String solveLocations(Set<Document> relevantEntities,
			List<String> allRelevantEntities, String surfaceForm, String context) {
		List<String> strList = new ArrayList<String>();
		// Preprocessing
		surfaceForm = surfaceForm.toLowerCase();

		for (Document d : relevantEntities) {
			String type = d.get("Type");
			if (type.equals("Location")) {
				String mainlink = d.get("Mainlink");
				String l = mainlink.toLowerCase().replaceAll(
						"http://dbpedia.org/resource/", "");
				String l_w = l.replaceAll("_", " ");
				if (l.contains(",_")) {
					String splitter[] = l.split(",_");
					String addition = splitter[1].toLowerCase().replaceAll("_",
							" ");
					String first = splitter[0].toLowerCase();
					int nrSpacesFirst = first.replaceAll("[^" + "_" + "]", "")
							.length();
					int nrSpacesSurfaceForm = surfaceForm.replaceAll(
							"[^" + " " + "]", "").length();

					if (!addition.equals(surfaceForm)
							&& !checkAdditionAbb(surfaceForm, addition, first)
							&& nrSpacesFirst == nrSpacesSurfaceForm) {
						strList.add(mainlink);
					}
				} else if (surfaceForm.equals(l_w)
						|| (surfaceForm.endsWith(".") && l_w
								.contains(surfaceForm.replaceAll("\\.", "")))
						|| checkFirstURLPart(surfaceForm, l_w)) {
					strList.add(mainlink);
				}
			}
		}
		return solveFinalCandidates(strList, surfaceForm, context);
	}

	private boolean checkFirstURLPart(String sf, String urlpart) {
		if (sf.contains(".")) {
			sf = sf.replaceAll("\\.", "");
			String[] splitter = urlpart.split(" ");
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < splitter.length; i++) {
				builder.append(splitter[i].substring(0, 1));
			}
			if (builder.toString().equals(sf)) {
				return true;
			}
		}
		return false;
	}

	private String solveFinalCandidates(List<String> candidates, String sf,
			String context) {
		String result = null;

		if (result == null) {
			for (String can : candidates) {
				String l = can.toLowerCase().replaceAll(
						"http://dbpedia.org/resource/", "");
				if (l.contains(",_")) {
					String splitter[] = l.split(",_");
					String addition = splitter[1].toLowerCase().replaceAll("_",
							" ");
					if (searchEvidenceInContext(context, addition, sf)) {
						result = can;
						break;
					}
				}
			}
		}
		if (result == null) {
			for (String can : candidates) {
				String l = can.toLowerCase().replaceAll(
						"http://dbpedia.org/resource/", "");
				if (!l.contains(",_")) {
					result = can;
					break;
				}
			}
		}
		return result;
	}

	private boolean checkAdditionAbb(String sf, String addition, String first) {
		if (!sf.endsWith(".")) {
			return false;
		}
		if (sf.endsWith(".") && addition.contains(sf.replaceAll("\\.", ""))) {
			return true;
		}
		if (sf.endsWith(".")) {
			sf = sf.replaceAll("\\.", "");
			if (first.contains("sf") && first.length() > 1) {
				return false;
			}
			String[] splitter = first.split(" ");
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < splitter.length; i++) {
				builder.append(splitter[i].substring(0, 1));
			}
			if (!builder.toString().equals(sf)) {
				return true;
			}
		}

		return false;
	}

	private boolean searchEvidenceInContext(String context, String word,
			String sf) {
		String conl = context.toLowerCase();
		if (sf.equals(word)) {
			return false;
		}
		String sfAbb = sf.replaceAll("[^\\w]", " ");
		String[] splitter = word.split(" ");
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < splitter.length; i++) {
			buffer.append(splitter[i].substring(0, 1));
			buffer.append(" ");
		}
		if (buffer.toString().equals(sfAbb)) {
			return false;
		}
		if (conl.contains(word)) {
			return true;
		}
		context = context.toLowerCase().trim().replaceAll(" +", " ");
		String[] words = context.toLowerCase().split(" ");
		for (int i = 0; i < words.length; i++) {
			String w = words[i].replaceAll("[^\\w\\s]", "");
			if (words[i].equals(w + ".")
					&& (word.startsWith(w) || word.endsWith(w))
					&& words[i].length() > 3) {
				System.out.println("Context adaptiert: " + words[i]);
				return true;
			}
		}
		return false;
	}

	private boolean isLocation(Set<Document> nonLocationSet, SurfaceForm sf) {
		for (Document doc : nonLocationSet) {
			String mainlink = doc.get("Mainlink");
			float docSim = this.eckb.getDoc2VecSimilarity(sf.getSurfaceForm(),
					sf.getContext(), mainlink);
			if (docSim > DOC2VECTHRESHOLD) {
				return false;
			}
		}
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

	private Set<Document> queryLuceneLabel(String surfaceForm,
			List<String> candidates) {
		Set<Document> documents = new HashSet<Document>();
		BooleanQuery query = new BooleanQuery();
		String[] splitter = surfaceForm.toLowerCase().split(" ");
		for (int i = 0; i < splitter.length; i++) {
			query.add(new TermQuery(new Term("Label", splitter[i])), Occur.MUST);
		}
		final IndexSearcher searcher = eckb.getSearcher();
		final IndexReader reader = searcher.getIndexReader();
		try {
			final TopDocs top = searcher.search(query, 25000);
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
}
