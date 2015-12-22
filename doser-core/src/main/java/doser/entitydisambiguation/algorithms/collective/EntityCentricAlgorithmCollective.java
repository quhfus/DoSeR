package doser.entitydisambiguation.algorithms.collective;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.aksw.agdistis.datatypes.DocumentText;
import org.aksw.agdistis.datatypes.NamedEntitiesInText;
import org.aksw.agdistis.datatypes.NamedEntityInText;
import org.apache.log4j.Logger;
import org.apache.lucene.search.IndexSearcher;

import doser.entitydisambiguation.algorithms.DisambiguationAlgorithm;
import doser.entitydisambiguation.algorithms.collective.rules.RuleAdapation;
import doser.entitydisambiguation.backend.DisambiguationTask;
import doser.entitydisambiguation.backend.DisambiguationTaskCollective;
import doser.entitydisambiguation.dpo.DisambiguatedEntity;
import doser.entitydisambiguation.dpo.EntityDisambiguationDPO;
import doser.entitydisambiguation.dpo.Response;
import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBaseDefault;
import doser.entitydisambiguation.knowledgebases.KnowledgeBase;
import doser.lucene.features.LuceneFeatures;
import doser.lucene.query.LearnToRankClause;
import doser.lucene.query.LearnToRankQuery;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

import org.aksw.agdistis.CandidateUtil;
import org.aksw.agdistis.Node;
import org.aksw.agdistis.datatypes.Document;

/**
 * Collective Disambiguation Approach by Stefan Zwicklbauer
 * 
 * @author quh
 * 
 */
public class EntityCentricAlgorithmCollective extends DisambiguationAlgorithm {

	public static final int NUMBEROFQUERYANWERS = 10000;
	
	public static final String[] SPECIALWORDS = { "corp", "ltd", "inc", "co", "inc." };
	public static final String[] SPECIALWORDS2 = { "la" };

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

		List<CollectiveSFRepresentation> collectiveRep = new LinkedList<CollectiveSFRepresentation>();
		System.out
				.println("---------------------------------------------------------------------------------------------------------------------------");
		for (int i = 0; i < entityList.size(); i++) {
			final IndexSearcher searcher = eckb.getSearcher();
			searcher.toString();
			EntityDisambiguationDPO dpo = entityList.get(i);
			
			Document doc = new Document();
			doc.setDocumentId(0);
			String sf = dpo.getSelectedText();
			doc.addTest(new DocumentText(sf));
			NamedEntitiesInText intext = new NamedEntitiesInText();
			NamedEntityInText in = new NamedEntityInText(0, sf.length(), null);
			intext.addNamedEntity(in);
			doc.addNamedEntitiesInText(intext);

			DirectedSparseGraph<Node, String> graph = new DirectedSparseGraph<Node, String>();

			// 0) insert candidates into Text
			List<String> sfs = new LinkedList<String>();
			try {
				CandidateUtil util = CandidateUtil.getInstance();
				util.insertCandidatesIntoText(graph, doc, 0.82, false);
				Collection<Node> nodes = graph.getVertices();
				for (Node node : nodes) {
					sfs.add(node.getCandidateURI());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (sfs.size() == 1) {
				ArrayList<String> l = new ArrayList<String>();
				l.add(sfs.get(0));
				CollectiveSFRepresentation col = new CollectiveSFRepresentation(
						dpo.getSelectedText(), dpo.getContext(), l, i);
				collectiveRep.add(col);
			} else if (sfs.size() > 1) {
				ArrayList<String> l = new ArrayList<String>();
				for (int j = 0; j < sfs.size(); j++) {
					l.add(sfs.get(j));
				}
				CollectiveSFRepresentation col = new CollectiveSFRepresentation(
						dpo.getSelectedText(), dpo.getContext(), l, i);
				collectiveRep.add(col);

			} else { ArrayList<String> l = new ArrayList<String>();
				CollectiveSFRepresentation col = new CollectiveSFRepresentation(
						dpo.getSelectedText(), dpo.getContext(), l, i);
				collectiveRep.add(col);
		}}
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
//			Query query = createQuery(dpo, eckb);
//			final IndexSearcher searcher = eckb.getSearcher();
//			final IndexReader reader = searcher.getIndexReader();
//			try {
//				final TopDocs top = searcher.search(query, NUMBEROFQUERYANWERS);
//				final ScoreDoc[] score = top.scoreDocs;
//				if (score.length == 1) {
//					final Document doc = reader.document(score[0].doc);
//					ArrayList<String> l = new ArrayList<String>();
//					l.add(doc.get("Mainlink"));
//					CollectiveSFRepresentation col = new CollectiveSFRepresentation(
//							dpo.getSelectedText(), dpo.getContext(), l, i);
//					collectiveRep.add(col);
//					System.out.println("Save Disambiguation: "
//							+ doc.get("Mainlink"));
//				} else if (score.length > 1) {
//					ArrayList<String> l = new ArrayList<String>();
//					for (int j = 0; j < score.length; j++) {
//						final Document doc = reader.document(score[j].doc);
//						l.add(doc.get("Mainlink"));
//					}
//					CollectiveSFRepresentation col = new CollectiveSFRepresentation(
//							dpo.getSelectedText(), dpo.getContext(), l, i);
//					collectiveRep.add(col);
//
//				} else {
//					// Try Another Query
//					// Todo vllt geht noch was
////					Query query = createQuery(dpo, eckb);
//
//					ArrayList<String> l = new ArrayList<String>();
//					CollectiveSFRepresentation col = new CollectiveSFRepresentation(
//							dpo.getSelectedText(), dpo.getContext(), l, i);
//					collectiveRep.add(col);
//				}
//
//			} catch (final IOException e) {
//				Logger.getRootLogger().error("Lucene Searcher Error: ", e);
//				e.printStackTrace();
//			}
//		}

		RuleAdapation rules = new RuleAdapation(eckb);
		rules.performRuleChainBeforeCandidateSelection(collectiveRep);
		
		CandidatePruning pruner = new CandidatePruning(eckb);
		pruner.prune(collectiveRep);
		
		EntityCentricAlgorithmCollectiveSolver solver = new EntityCentricAlgorithmCollectiveSolver(
				responseArray, collectiveRep, eckb);
		solver.solve();

		solver.generateResult();
		List<Response> res = Arrays.asList(responseArray);
		task.setResponse(res);

		eckb.release();
	}

	public void generateResult(Response[] responseArray,
			List<CollectiveSFRepresentation> cols) {
		for (int i = 0; i < responseArray.length; i++) {
			CollectiveSFRepresentation r = search(i, cols);
			if (responseArray[i] == null && r != null
					&& r.getCandidates().size() == 1) {
				Response res = new Response();
				List<DisambiguatedEntity> entList = new LinkedList<DisambiguatedEntity>();
				DisambiguatedEntity ent = new DisambiguatedEntity();
				ent.setEntityUri(r.getCandidates().get(0));
				ent.setText("ToDoText");
				entList.add(ent);
				res.setDisEntities(entList);
				res.setPosition(null);
				res.setSelectedText(r.getSurfaceForm());
				responseArray[i] = res;
			}
		}
	}

	private CollectiveSFRepresentation search(int qryNr,
			List<CollectiveSFRepresentation> rep) {
		for (CollectiveSFRepresentation r : rep) {
			if (r.getQueryNr() == qryNr) {
				return r;
			}
		}
		return null;
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

//	private Query createQuery(EntityDisambiguationDPO dpo,
//			EntityCentricKnowledgeBaseDefault kb) {
//		LearnToRankQuery query = new LearnToRankQuery();
//		List<LearnToRankClause> features = new LinkedList<LearnToRankClause>();
//		DefaultSimilarity defaultSim = new DefaultSimilarity();
//
//		// String Transformation
//		String sf = dpo.getSelectedText().toLowerCase();
//		if (sf.endsWith("'s") || sf.endsWith("s'")) {
//			sf = sf.substring(0, sf.length() - 2);
//		}
//
////		sf = Inflector.getInstance().singularize(sf);
//		for (int i = 0; i < SPECIALWORDS.length; i++) {
//			if (sf.startsWith(SPECIALWORDS[i]) || sf.endsWith(SPECIALWORDS[i])) {
//				sf.replaceAll(SPECIALWORDS[i], "");
//				break;
//			}
//		}
//
//		// Check Number
//		boolean isNumber = true;
//		try {
//			Integer.parseInt(sf);
//		} catch (NumberFormatException e) {
//			isNumber = false;
//		}
//		if (!isNumber) {
//			sf = sf.replaceAll("[0-9]", "");
//		}
//		sf = sf.trim();
//
//		// Feature 1
//		features.add(query.add(LuceneFeatures.queryLabelTerm(sf,
//				"DBpediaUniqueLabel", defaultSim), "Feature1", true));
//
//		features.get(0).setWeight(1f);
//		return query;
//	}

//	private Query createSecondQuery(EntityDisambiguationDPO dpo,
//			EntityCentricKnowledgeBaseDefault kb) {
//		LearnToRankQuery query = new LearnToRankQuery();
//		List<LearnToRankClause> features = new LinkedList<LearnToRankClause>();
//		DefaultSimilarity defaultSim = new DefaultSimilarity();
//
//		// String Transformation
//		String sf = dpo.getSelectedText().toLowerCase();
//		if (sf.endsWith("'s") || sf.endsWith("s'")) {
//			sf = sf.substring(0, sf.length() - 2);
//		}
//
//		sf = Inflector.getInstance().singularize(sf);
//		for (int i = 0; i < SPECIALWORDS.length; i++) {
//			if (sf.startsWith(SPECIALWORDS[i]) || sf.endsWith(SPECIALWORDS[i])) {
//				sf = sf.replaceAll(SPECIALWORDS[i], "");
//				break;
//			}
//		}
//		sf = sf.trim();
//
//		// Feature 1
//		features.add(query.add(LuceneFeatures.queryLabelTerm(sf,
//				"DBpediaUniqueLabel", defaultSim), "Feature1", true));
//
//		features.get(0).setWeight(1f);
//		return query;
//	}
	
	public static void main(String[] args) {
		String sf = "CBS";
		if (sf.endsWith("'s") || sf.endsWith("s'")) {
			sf = sf.substring(0, sf.length() - 2);
		}
		sf = Inflector.getInstance().singularize(sf);
		for (int i = 0; i < SPECIALWORDS.length; i++) {
			if (sf.startsWith(SPECIALWORDS[i]) || sf.endsWith(SPECIALWORDS[i])) {
				sf = sf.replaceAll(SPECIALWORDS[i], "");
				break;
			}
		}

		// Check Number
		boolean isNumber = true;
		try {
			Integer.parseInt(sf);
		} catch (NumberFormatException e) {
			isNumber = false;
		}
		if (!isNumber) {
			sf = sf.replaceAll("[0-9]", "");
		}
		sf = sf.trim();
		System.out.println(sf);
	}
}
