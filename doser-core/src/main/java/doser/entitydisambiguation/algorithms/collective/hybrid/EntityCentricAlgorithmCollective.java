package doser.entitydisambiguation.algorithms.collective.hybrid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import doser.entitydisambiguation.algorithms.DisambiguationAlgorithm;
import doser.entitydisambiguation.backend.DisambiguationTask;
import doser.entitydisambiguation.backend.DisambiguationTaskCollective;
import doser.entitydisambiguation.dpo.DisambiguatedEntity;
import doser.entitydisambiguation.dpo.EntityDisambiguationDPO;
import doser.entitydisambiguation.dpo.Response;
import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBaseDefault;
import doser.entitydisambiguation.knowledgebases.KnowledgeBase;
import doser.lucene.query.TermQuery;

/**
 * Collective Disambiguation Approach by Stefan Zwicklbauer
 * 
 * @author quh
 * 
 */
public class EntityCentricAlgorithmCollective extends DisambiguationAlgorithm {

	private static final int PREPROCESSINGCONTEXTSIZE = 500;

	private EntityCentricKnowledgeBaseDefault eckb;

	private DisambiguationTaskCollective task;

	public EntityCentricAlgorithmCollective() {
		super();
	}

	@Override
	public boolean checkAndSetInputParameter(DisambiguationTask task) {
		KnowledgeBase kb = task.getKb();
		if (!(task instanceof DisambiguationTaskCollective)) {
			return false;
		} else if (!(kb instanceof EntityCentricKnowledgeBaseDefault)) {
			return false;
		}
		this.eckb = (EntityCentricKnowledgeBaseDefault) kb;
		this.task = (DisambiguationTaskCollective) task;
		return true;
	}

	@Override
	protected boolean preDisambiguation() {
		return true;
	}

	@Override
	public void processAlgorithm() {
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

				} else {
					System.out.println("ICH FINE ABSOLUT KEINE SURFACE FORM: " + dpo.getSelectedText());
					System.out.println("Ich versuchs nochmal und zwar mit ");
					String s = dpo.getSelectedText();
					Pattern regex = Pattern.compile(" ([,!?.])");
					Matcher regexMatcher = regex.matcher(s);
					StringBuffer buffer = new StringBuffer();
					while (regexMatcher.find()) {
						regexMatcher.appendReplacement(buffer, regexMatcher.group(1));
					}
					regexMatcher.appendTail(buffer);
					if (!dpo.getSelectedText().equalsIgnoreCase(buffer.toString())) {
						System.out.println("Ich führe einen neuen SearchRun durch!");
						anotherSearchRun(buffer.toString(), searcher, collectiveRep, i, dpo);
					} else {
						ArrayList<String> l = new ArrayList<String>();
						SurfaceForm col = new SurfaceForm(dpo.getSelectedText(), dpo.getContext(), l, i,
								dpo.getStartPosition());
						collectiveRep.add(col);
					}
				}

			} catch (final IOException e) {
				Logger.getRootLogger().error("Lucene Searcher Error: ", e);
				e.printStackTrace();
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
		AlgorithmDriver solver = new CollectiveAndContextDriver(responseArray, collectiveRep, eckb);
		solver.solve();

		solver.generateResult();
		List<Response> res = Arrays.asList(responseArray);
		task.setResponse(res);

		eckb.release();
	}

	private void anotherSearchRun(String newSf, IndexSearcher searcher, List<SurfaceForm> colRep, int iteration,
			EntityDisambiguationDPO dpo) {
		IndexReader reader = searcher.getIndexReader();
		Query query = createQuery(newSf, eckb);
		try {
			final TopDocs top = searcher.search(query, task.getReturnNr());
			final ScoreDoc[] score = top.scoreDocs;
			if (score.length == 1) {
				final Document doc = reader.document(score[0].doc);
				ArrayList<String> l = new ArrayList<String>();
				l.add(doc.get("Mainlink"));
				SurfaceForm col = new SurfaceForm(newSf, dpo.getContext(), l, iteration, dpo.getStartPosition());
				col.setInitial(true);
				colRep.add(col);

			} else if (score.length > 1) {
				ArrayList<String> l = new ArrayList<String>();
				for (int j = 0; j < score.length; j++) {
					final Document doc = reader.document(score[j].doc);
					l.add(doc.get("Mainlink"));
				}
				SurfaceForm col = new SurfaceForm(dpo.getSelectedText(), dpo.getContext(), l, iteration,
						dpo.getStartPosition());
				colRep.add(col);

			} else {
				ArrayList<String> l = new ArrayList<String>();
				SurfaceForm col = new SurfaceForm(dpo.getSelectedText(), dpo.getContext(), l, iteration,
						dpo.getStartPosition());
				colRep.add(col);
			}

		} catch (final IOException e) {
			Logger.getRootLogger().error("Lucene Searcher Error: ", e);
			e.printStackTrace();
		}
	}

	public void generateResult(Response[] responseArray, List<SurfaceForm> cols) {
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

	private Query createQuery(String sf, EntityCentricKnowledgeBaseDefault kb) {
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
