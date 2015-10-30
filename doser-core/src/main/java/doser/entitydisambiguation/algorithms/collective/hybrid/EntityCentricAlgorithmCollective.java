package doser.entitydisambiguation.algorithms.collective.hybrid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
		List<EntityDisambiguationDPO> entityList = task
				.getEntityToDisambiguate();
		Response[] responseArray = new Response[entityList.size()];

		List<SurfaceForm> collectiveRep = new LinkedList<SurfaceForm>();
		System.out
				.println("---------------------------------------------------------------------------------------------------------------------------");
		for (int i = 0; i < entityList.size(); i++) {
			EntityDisambiguationDPO dpo = entityList.get(i);
			Query query = createQuery(dpo, eckb);
			final IndexSearcher searcher = eckb.getSearcher();
			final IndexReader reader = searcher.getIndexReader();
			try {
				final TopDocs top = searcher.search(query, task.getReturnNr());
				final ScoreDoc[] score = top.scoreDocs;
				if (score.length == 1) {
					final Document doc = reader.document(score[0].doc);
					ArrayList<String> l = new ArrayList<String>();
					l.add(doc.get("Mainlink"));
					SurfaceForm col = new SurfaceForm(dpo.getSelectedText(),
							dpo.getContext(), l, i, dpo.getStartPosition());
					collectiveRep.add(col);
					System.out.println("Save Disambiguation: "
							+ doc.get("Mainlink") + "    "+dpo.getSelectedText());
				} else if (score.length > 1) {
					ArrayList<String> l = new ArrayList<String>();
					for (int j = 0; j < score.length; j++) {
						final Document doc = reader.document(score[j].doc);
						l.add(doc.get("Mainlink"));
					}
					SurfaceForm col = new SurfaceForm(dpo.getSelectedText(),
							dpo.getContext(), l, i, dpo.getStartPosition());
					collectiveRep.add(col);

				} else {
					ArrayList<String> l = new ArrayList<String>();
					SurfaceForm col = new SurfaceForm(dpo.getSelectedText(),
							dpo.getContext(), l, i, dpo.getStartPosition());
					collectiveRep.add(col);
				}

			} catch (final IOException e) {
				Logger.getRootLogger().error("Lucene Searcher Error: ", e);
				e.printStackTrace();
			}
		}

		// AlgorithmDriver solver = new CollectiveOnlyDriver(
		// responseArray, collectiveRep, eckb);
		AlgorithmDriver solver = new CollectiveAndContextDriver(responseArray,
				collectiveRep, eckb);
		solver.solve();

		solver.generateResult();
		List<Response> res = Arrays.asList(responseArray);
		task.setResponse(res);

		eckb.release();
	}

	public void generateResult(Response[] responseArray, List<SurfaceForm> cols) {
		for (int i = 0; i < responseArray.length; i++) {
			SurfaceForm r = search(i, cols);
			if (responseArray[i] == null && r != null
					&& r.getCandidates().size() == 1) {
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

	// protected void sensePriorDisambiguation(CollectiveSFRepresentation col) {
	// if (col.getCandidates().size() > 1) {
	// List<String> s = col.getCandidates();
	// List<Candidate> canList = new LinkedList<Candidate>();
	// for (String str : s) {
	// canList.add(new Candidate(str, eckb.getFeatureDefinition()
	// .getOccurrences(col.getSurfaceForm(), str)));
	// }
	//
	// Collections.sort(canList, Collections.reverseOrder());
	// col.setDisambiguatedEntity(canList.get(0).getCandidate());
	// }
	// }

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

	private Query createQuery(EntityDisambiguationDPO dpo,
			EntityCentricKnowledgeBaseDefault kb) {
		String sf = dpo.getSelectedText().toLowerCase();
		TermQuery query = new TermQuery(new Term("UniqueLabel", sf));

		return query;
	}

	// private Query createPhraseQuery(EntityDisambiguationDPO dpo,
	// EntityCentricKnowledgeBaseDefault kb) {
	// LearnToRankQuery query = new LearnToRankQuery();
	// List<LearnToRankClause> features = new LinkedList<LearnToRankClause>();
	//
	// DefaultSimilarity defaultSim = new DefaultSimilarity();
	// LTRBooleanQuery bq = new LTRBooleanQuery();
	// bq.add(LuceneFeatures.queryLabelTerm(dpo.getSelectedText(),
	// "UniqueLabelString", defaultSim), Occur.SHOULD);
	// bq.add(LuceneFeatures.queryLabelTerm(dpo.getSelectedText(), "Label",
	// defaultSim), Occur.SHOULD);
	//
	// // Feature 1
	// features.add(query.add(bq, "Feature1", true));
	// // Feature 2
	// features.add(query.add(
	// LuceneFeatures.querySensePrior(dpo.getSelectedText(),
	// kb.getFeatureDefinition()), "Feature2", false));
	//
	// features.get(0).setWeight(1f);
	// features.get(1).setWeight(1f);
	// return query;
	// }

	// private Query createFuzzyQuery(EntityDisambiguationDPO dpo,
	// EntityCentricKnowledgeBaseDefault kb) {
	// LearnToRankQuery query = new LearnToRankQuery();
	// List<LearnToRankClause> features = new LinkedList<LearnToRankClause>();
	// DefaultSimilarity defaultSim = new DefaultSimilarity();
	//
	// // Feature 1
	// features.add(query.add(LuceneFeatures.queryStringTerm(
	// dpo.getSelectedText(), "Label", defaultSim, Occur.SHOULD,
	// EntityDisambiguation.MAXCLAUSECOUNT), "Feature1", true));
	// // Feature 2
	// features.add(query.add(
	// LuceneFeatures.querySensePrior(dpo.getSelectedText(),
	// kb.getFeatureDefinition()), "Feature2", false));
	// features.get(0).setWeight(0.0915161f);
	// features.get(1).setWeight(0.350994f);
	// return query;
	// }

	// public void testGraphExpansionAndHits() {
	// CurrentEntity obamastr = new CurrentEntity(
	// "http://dbpedia.org/resource/Barack_Obama,_Sr.");
	// obamastr.setEntityQuery(0);
	// obamastr.setCandidate(true);
	// CurrentEntity obama = new CurrentEntity(
	// "http://dbpedia.org/resource/Barack_Obama");
	// obama.setEntityQuery(0);
	// obama.setCandidate(true);
	// CurrentEntity washingtondcnovel = new CurrentEntity(
	// "http://dbpedia.org/resource/Washington,_D.C._%28novel%29");
	// washingtondcnovel.setEntityQuery(1);
	// washingtondcnovel.setCandidate(true);
	// CurrentEntity washingtondc = new CurrentEntity(
	// "http://dbpedia.org/resource/Washington,_D.C.");
	// washingtondc.setEntityQuery(1);
	// washingtondc.setCandidate(true);
	//
	// DirectedSparseGraph<CurrentEntity, CurrentEdge> graph = new
	// DirectedSparseGraph<CurrentEntity, CurrentEdge>();
	//
	// graph.addVertex(obamastr);
	// graph.addVertex(obama);
	// graph.addVertex(washingtondcnovel);
	// graph.addVertex(washingtondc);
	// // Perform graph expansion
	// int iterations = 0;
	// long time = System.currentTimeMillis();
	// while (iterations < POPERATORITERATIONS) {
	// pOperator(graph);
	// iterations++;
	// }
	// System.out.println(System.currentTimeMillis() - time);
	//
	// // Apply HITS Algorithm
	// HITS<CurrentEntity, CurrentEdge> hitsAlgorithm = new HITS<CurrentEntity,
	// CurrentEdge>(
	// graph);
	// hitsAlgorithm.initialize();
	// hitsAlgorithm.setTolerance(0.000001);
	// hitsAlgorithm.setMaxIterations(200);
	// hitsAlgorithm.evaluate();
	// for (CurrentEntity ent : graph.getVertices()) {
	// System.out.println(ent.getUri() + "  \th:"
	// + hitsAlgorithm.getVertexScore(ent).hub + "\ta:"
	// + hitsAlgorithm.getVertexScore(ent).authority);
	// ent.setAuthorityValue(hitsAlgorithm.getVertexScore(ent).authority);
	// }
	//
	// System.out
	// .println("-----------------------------------------------------------------------------------");
	//
	// Collection<CurrentEntity> ents = graph.getVertices();
	// List<CurrentEntity> list = new LinkedList<CurrentEntity>(ents);
	// Collections.sort(list);
	//
	// BitSet bitset = new BitSet(2);
	// for (CurrentEntity ent : list) {
	// int pos = ent.getEntityQuery();
	// if (ent.isCandidate() && !bitset.get(pos)) {
	// System.out.println(ent.getEntityQuery() + "   " + ent.getUri()
	// + "    " + ent.getAuthorityValue());
	// bitset.set(ent.getEntityQuery());
	// }
	// }
	// }

	// public void test() {
	// CurrentEntity universityHawaii = new CurrentEntity(
	// "http://dbpedia.org/resource/University_of_Hawaii");
	// CurrentEntity hawaii = new CurrentEntity(
	// "http://dbpedia.org/resource/Hawaii");
	// CurrentEntity anndunham = new CurrentEntity(
	// "http://dbpedia.org/resource/Ann_Dunham");
	// CurrentEntity obamastr = new CurrentEntity(
	// "http://dbpedia.org/resource/Barack_Obama_Str");
	// CurrentEntity obama = new CurrentEntity(
	// "http://dbpedia.org/resource/Barack_Obama");
	// CurrentEntity elizabeth = new CurrentEntity(
	// "http://dbpedia.org/resource/ElizabethII");
	// CurrentEntity london = new CurrentEntity(
	// "http://dbpedia.org/resource/London");
	// CurrentEntity whiteHouse = new CurrentEntity(
	// "http://dbpedia.org/resource/Whitehouse");
	// CurrentEntity unitedkingdom = new CurrentEntity(
	// "http://dbpedia.org/resource/UnitedKingdom");
	// CurrentEntity federaldistrict = new CurrentEntity(
	// "http://dbpedia.org/resource/FederalDistrict");
	// CurrentEntity washingtondcnovel = new CurrentEntity(
	// "http://dbpedia.org/resource/WashingtonDCNovel");
	// CurrentEntity washingtondc = new CurrentEntity(
	// "http://dbpedia.org/resource/WashingtonDC");
	// CurrentEntity gorefidal = new CurrentEntity(
	// "http://dbpedia.org/resource/GoreFidal");
	// CurrentEntity newyork = new CurrentEntity(
	// "http://dbpedia.org/resource/NewYork");
	//
	// DirectedSparseGraph<CurrentEntity, CurrentEdge> graph = new
	// DirectedSparseGraph<CurrentEntity, CurrentEdge>();
	// graph.addVertex(universityHawaii);
	// graph.addVertex(hawaii);
	// graph.addVertex(anndunham);
	// graph.addVertex(obamastr);
	// graph.addVertex(obama);
	// graph.addVertex(elizabeth);
	// graph.addVertex(london);
	// graph.addVertex(whiteHouse);
	// graph.addVertex(unitedkingdom);
	// graph.addVertex(federaldistrict);
	// graph.addVertex(washingtondcnovel);
	// graph.addVertex(washingtondc);
	// graph.addVertex(gorefidal);
	// graph.addVertex(newyork);
	//
	// graph.addEdge(new CurrentEdge(), universityHawaii, hawaii);
	// graph.addEdge(new CurrentEdge(), universityHawaii, obamastr);
	// graph.addEdge(new CurrentEdge(), obamastr, universityHawaii);
	// graph.addEdge(new CurrentEdge(), obamastr, anndunham);
	// graph.addEdge(new CurrentEdge(), obamastr, obama);
	// graph.addEdge(new CurrentEdge(), hawaii, obama);
	// graph.addEdge(new CurrentEdge(), anndunham, universityHawaii);
	// graph.addEdge(new CurrentEdge(), anndunham, obamastr);
	// graph.addEdge(new CurrentEdge(), anndunham, obama);
	// graph.addEdge(new CurrentEdge(), obama, hawaii);
	// graph.addEdge(new CurrentEdge(), obama, whiteHouse);
	// graph.addEdge(new CurrentEdge(), whiteHouse, obama);
	// graph.addEdge(new CurrentEdge(), whiteHouse, washingtondc);
	// graph.addEdge(new CurrentEdge(), washingtondc, whiteHouse);
	// graph.addEdge(new CurrentEdge(), washingtondc, federaldistrict);
	// graph.addEdge(new CurrentEdge(), federaldistrict, washingtondc);
	// graph.addEdge(new CurrentEdge(), unitedkingdom, elizabeth);
	// graph.addEdge(new CurrentEdge(), unitedkingdom, london);
	// graph.addEdge(new CurrentEdge(), london, unitedkingdom);
	// graph.addEdge(new CurrentEdge(), washingtondcnovel, unitedkingdom);
	// graph.addEdge(new CurrentEdge(), washingtondcnovel, gorefidal);
	// graph.addEdge(new CurrentEdge(), gorefidal, washingtondcnovel);
	// graph.addEdge(new CurrentEdge(), gorefidal, newyork);
	//
	// // Apply HITS Algorithm
	// HITS<CurrentEntity, CurrentEdge> hitsAlgorithm = new HITS<>(graph);
	// hitsAlgorithm.initialize();
	// hitsAlgorithm.setTolerance(0.000001);
	// hitsAlgorithm.setMaxIterations(20);
	// hitsAlgorithm.evaluate();
	// for (CurrentEntity ent : graph.getVertices()) {
	// System.out.println(ent.getUri() + "  \th:"
	// + hitsAlgorithm.getVertexScore(ent).hub + "\ta:"
	// + hitsAlgorithm.getVertexScore(ent).authority);
	// }
	// }
	//
	// public static void main(String args[]) {
	// EntityCentricAlgorithmCollective collective = new
	// EntityCentricAlgorithmCollective();
	// collective.testGraphExpansionAndHits();
	// }
}
