package doser.entitydisambiguation.algorithms.collective.dbpedia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import doser.entitydisambiguation.algorithms.AbstractDisambiguationAlgorithm;
import doser.entitydisambiguation.algorithms.SurfaceForm;
import doser.entitydisambiguation.backend.AbstractDisambiguationTask;
import doser.entitydisambiguation.backend.DisambiguationTaskCollective;
import doser.entitydisambiguation.dpo.DisambiguatedEntity;
import doser.entitydisambiguation.dpo.EntityDisambiguationDPO;
import doser.entitydisambiguation.dpo.Response;
import doser.entitydisambiguation.knowledgebases.EntityCentricKBDBpedia;
import doser.entitydisambiguation.knowledgebases.AbstractKnowledgeBase;
import doser.lucene.query.TermQuery;

/**
 * Collective Disambiguation Approach by Stefan Zwicklbauer
 * 
 * @author quh
 * 
 */
public class CollectiveDisambiguationDBpediaEntities extends AbstractDisambiguationAlgorithm {

	private final static Logger logger = LoggerFactory.getLogger(CollectiveDisambiguationDBpediaEntities.class);
	
	private EntityCentricKBDBpedia eckb;

	private DisambiguationTaskCollective task;

	public CollectiveDisambiguationDBpediaEntities() {
		super();
	}

	@Override
	public boolean checkAndSetInputParameter(AbstractDisambiguationTask task) {
		AbstractKnowledgeBase kb = task.getKb();
		if (!(task instanceof DisambiguationTaskCollective)) {
			return false;
		} else if (!(kb instanceof EntityCentricKBDBpedia)) {
			return false;
		}
		this.eckb = (EntityCentricKBDBpedia) kb;
		this.task = (DisambiguationTaskCollective) task;
		return true;
	}

	@Override
	protected boolean preDisambiguation() {
		return true;
	}

	@Override
	public void processAlgorithm() {
		AdditionalCandidateQuery aq = new AdditionalCandidateQuery(eckb);
		List<EntityDisambiguationDPO> entityList = task.getEntityToDisambiguate();
		Response[] responseArray = new Response[entityList.size()];

		List<SurfaceForm> collectiveRep = new LinkedList<SurfaceForm>();
		System.out.println(
				"---------------------------------------------------------------------------------------------------------------------------");
		for (int i = 0; i < entityList.size(); i++) {
			EntityDisambiguationDPO dpo = entityList.get(i);
			// Dieser Fix sollte irgendwo anders passieren. TODO Auslagern
			dpo.setSelectedText(dpo.getSelectedText().replaceAll("’", "'"));
			Query query = createQuery(dpo.getSelectedText(), eckb);
			final IndexSearcher searcher = eckb.getSearcher();
			final IndexReader reader = searcher.getIndexReader();
			try {
				final TopDocs top = searcher.search(query, task.getReturnNr());
				final ScoreDoc[] score = top.scoreDocs;
				if (dpo.getSelectedText().equalsIgnoreCase("") || dpo.getSelectedText() == null) {
					ArrayList<String> l = new ArrayList<String>();
					l.add("");
					SurfaceForm col = new SurfaceForm(dpo.getSelectedText(), dpo.getContext(), l, i,
							dpo.getStartPosition());
					collectiveRep.add(col);
					System.out.println("Save Disambiguation: " + "" + "    " + dpo.getSelectedText());
				} else if (score.length == 1) {
					final Document doc = reader.document(score[0].doc);
					ArrayList<String> l = new ArrayList<String>();
					l.add(doc.get("Mainlink"));
					SurfaceForm col = new SurfaceForm(dpo.getSelectedText(), dpo.getContext(), l, i,
							dpo.getStartPosition());
					col.setInitial(true);
					collectiveRep.add(col);
					System.out.println("Save Disambiguation: " + doc.get("Mainlink") + "    " + dpo.getSelectedText());

				} else if (score.length > 1) {
					ArrayList<String> l = new ArrayList<String>();
					for (int j = 0; j < score.length; j++) {
						final Document doc = reader.document(score[j].doc);
						l.add(doc.get("Mainlink"));
					}
					SurfaceForm col = new SurfaceForm(dpo.getSelectedText(), dpo.getContext(), l, i,
							dpo.getStartPosition());
					collectiveRep.add(col);

				}
//				else {
//					SurfaceForm sf = aq.checkAdditionalSurfaceForms(dpo, i);
//					collectiveRep.add(sf);
//				}

			} catch (final IOException e) {
				logger.error("JsonException in "+CollectiveDisambiguationDBpediaEntities.class.getName(), e);
			}
		}

		for (SurfaceForm sf : collectiveRep) {
			if (sf.getSurfaceForm().equalsIgnoreCase("Sprint Communications Co")) {
				List<String> candidates = new ArrayList<String>();
				sf.setCandidates(candidates);
			}
		}

		// AlgorithmDriver solver = new CollectiveOnlyDriver(
		// responseArray, collectiveRep, eckb);
		CollectiveAndContextDriver solver = new CollectiveAndContextDriver(responseArray, collectiveRep, eckb, task.getMainTopic());
		solver.solve();

		solver.generateResult();
		List<Response> res = Arrays.asList(responseArray);
		task.setResponse(res);

		eckb.release();
	}

	void generateResult(Response[] responseArray, List<SurfaceForm> cols) {
		for (int i = 0; i < responseArray.length; i++) {
			SurfaceForm r = search(i, cols);
			if (responseArray[i] == null && r != null && r.getCandidates().size() == 1) {
				Response res = new Response();
				List<DisambiguatedEntity> entList = new LinkedList<DisambiguatedEntity>();
				DisambiguatedEntity ent = new DisambiguatedEntity();
				ent.setEntityUri(r.getCandidates().get(0));
				ent.setText("ToDoText");
				entList.add(ent);
				res.setDisEntities(entList);
				res.setStartPosition(-1);
				res.setSelectedText(r.getSurfaceForm());
				responseArray[i] = res;
			}
		}
	}

	private SurfaceForm search(int qryNr, List<SurfaceForm> rep) {
		for (SurfaceForm r : rep) {
			if (r.getQueryNr() == qryNr) {
				return r;
			}
		}
		return null;
	}

	private void sensePriorDisambiguation(SurfaceForm col) {
		if (col.getCandidates().size() > 1) {
			List<String> s = col.getCandidates();
			List<Candidate> canList = new LinkedList<Candidate>();
			for (String str : s) {
				canList.add(new Candidate(str, eckb.getFeatureDefinition().getOccurrences(col.getSurfaceForm(), str)));
			}

			Collections.sort(canList, Collections.reverseOrder());
			col.setDisambiguatedEntity(canList.get(0).getCandidate());
		}
	}

	protected class Candidate implements Comparable<Candidate> {

		private String candidate;
		private double score;

		protected Candidate(String candidate, double score) {
			super();
			this.candidate = candidate;
			this.score = score;
		}

		@Override
		public int compareTo(Candidate o) {
			if (this.score < o.score) {
				return -1;
			} else if (this.score > o.score) {
				return 1;
			} else {
				return 0;
			}
		}

		protected String getCandidate() {
			return candidate;
		}

		protected double getScore() {
			return score;
		}

	}

	private Query createQuery(String sf, EntityCentricKBDBpedia kb) {
		String surfaceform = sf.toLowerCase();
		TermQuery query = new TermQuery(new Term("UniqueLabel", surfaceform));

		return query;
	}

	public static void main(String args[]) {
		String s = "test . test ";
		Pattern regex = Pattern.compile(" ([,!?.])");
		Matcher regexMatcher = regex.matcher(s);
		StringBuffer buffer = new StringBuffer();
		while (regexMatcher.find())
			regexMatcher.appendReplacement(buffer, regexMatcher.group(1));
		regexMatcher.appendTail(buffer);
		System.out.println(buffer.toString());
	}
}
