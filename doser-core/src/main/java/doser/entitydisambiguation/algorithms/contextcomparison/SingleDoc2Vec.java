package doser.entitydisambiguation.algorithms.contextcomparison;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import doser.entitydisambiguation.algorithms.IllegalDisambiguationAlgorithmInputException;
import doser.entitydisambiguation.algorithms.SurfaceForm;
import doser.entitydisambiguation.algorithms.collective.dbpedia.CollectiveDisambiguationDBpediaEntities;
import doser.entitydisambiguation.backend.AbstractDisambiguationTask;
import doser.entitydisambiguation.backend.DisambiguationTaskCollective;
import doser.entitydisambiguation.dpo.DisambiguatedEntity;
import doser.entitydisambiguation.dpo.EntityDisambiguationDPO;
import doser.entitydisambiguation.dpo.Response;
import doser.entitydisambiguation.knowledgebases.AbstractKnowledgeBase;
import doser.entitydisambiguation.knowledgebases.EntityCentricKBDBpedia;
import doser.general.HelpfulMethods;
import doser.lucene.query.TermQuery;

public class SingleDoc2Vec extends AbstractDisambiguationAlgorithm {

	private final static Logger logger = LoggerFactory.getLogger(SingleDoc2Vec.class);

	private EntityCentricKBDBpedia eckb;

	private DisambiguationTaskCollective task;

	@Override
	protected boolean checkAndSetInputParameter(AbstractDisambiguationTask task) {
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
	protected void processAlgorithm() throws IllegalDisambiguationAlgorithmInputException {
		List<EntityDisambiguationDPO> entityList = task.getEntityToDisambiguate();
		Response[] responseArray = new Response[entityList.size()];

		List<SurfaceForm> collectiveRep = new LinkedList<SurfaceForm>();
		for (int i = 0; i < entityList.size(); i++) {
			EntityDisambiguationDPO dpo = entityList.get(i);
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
				} else if (score.length == 1) {
					final Document doc = reader.document(score[0].doc);
					ArrayList<String> l = new ArrayList<String>();
					l.add(doc.get("Mainlink"));
					SurfaceForm col = new SurfaceForm(dpo.getSelectedText(), dpo.getContext(), l, i,
							dpo.getStartPosition());
					col.setInitial(true);
					collectiveRep.add(col);

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
					ArrayList<String> l = new ArrayList<String>();
					SurfaceForm col = new SurfaceForm(dpo.getSelectedText(), dpo.getContext(), l, i,
							dpo.getStartPosition());
					collectiveRep.add(col);
				}

			} catch (final IOException e) {
				logger.error("JsonException in " + CollectiveDisambiguationDBpediaEntities.class.getName(), e);
			}
		}

//		prune(collectiveRep);

		List<Response> res = Arrays.asList(disambiguate(collectiveRep, responseArray));
		task.setResponse(res);
		List<Response> reslist = task.getResponse();
		// for (Response r : reslist) {
		// List<DisambiguatedEntity> entities = r.getDisEntities();
		// System.out.println(r.getSelectedText());
		// for(DisambiguatedEntity ent : entities) {
		// System.out.println("DisambiguatedEntity: "+ent.getEntityUri());
		// }
		// }
		eckb.release();
	}

	private Response[] disambiguate(List<SurfaceForm> collectiveRep, Response[] resultList) {
		StandardAnalyzer ana = new StandardAnalyzer();
		this.eckb.precomputeDoc2VecSimilarities(collectiveRep, 240);
		// this.eckb.precomputeLDASimilarities(collectiveRep, 3000);
		for (int i = 0; i < collectiveRep.size(); i++) {
			SurfaceForm sf = collectiveRep.get(i);
			if (sf.getCandidates().size() > 0) {
				// String context = extractContext(sf.getPosition(),
				// sf.getContext(), 300);
				String res = queryContext(sf.getSurfaceForm(), sf.getContext(), sf.getCandidates());
				Response re = new Response();
				List<DisambiguatedEntity> entList = new LinkedList<DisambiguatedEntity>();
				DisambiguatedEntity ent = new DisambiguatedEntity();
				ent.setEntityUri(res);
				ent.setText("ToDoText");
				entList.add(ent);
				re.setDisEntities(entList);
				re.setStartPosition(-1);
				re.setSelectedText(sf.getSurfaceForm());
				resultList[i] = re;
			}
		}
		return resultList;
	}

	private String queryContext(String surfaceForm, String context, List<String> candidates) {
		HashMap<String, Float> map = new HashMap<String, Float>();
		for (String can : candidates) {
			float val = this.eckb.getDoc2VecSimilarity(surfaceForm, context, can);
			// float val = this.eckb.getLDASimilarity(surfaceForm, context,
			// can);
			map.put(can, val);
		}
		Map<String, Float> sortedmap = sortByValue(map);
//		for (Map.Entry<String, Float> m : sortedmap.entrySet()) {
//			System.out.println("Candidate: " + m.getKey() + " Score: " + m.getValue());
//		}
		if (sortedmap.size() > 0) {
			return sortedmap.entrySet().iterator().next().getKey();
		} else {
			return "";
		}
	}

	private Query createQuery(String sf, EntityCentricKBDBpedia kb) {
		String surfaceform = sf.toLowerCase();
		TermQuery query = new TermQuery(new Term("UniqueLabel", surfaceform));

		return query;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return -(o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	private void prune(List<SurfaceForm> rep) {
		for (SurfaceForm c : rep) {
			List<String> candidates = c.getCandidates();
			if (candidates.size() > 10) {
				Set<String> prunedCandidates = new HashSet<String>();

				// Sense Prior
				Map<String, Integer> map = new HashMap<String, Integer>();
				for (String candidate : candidates) {
					map.put(candidate, eckb.getFeatureDefinition().getOccurrences(c.getSurfaceForm(), candidate));
				}
				@SuppressWarnings("deprecation")
				List<Map.Entry<String, Integer>> l = HelpfulMethods.sortByValue(map);
				for (int i = 0; i < 10; ++i) {
					prunedCandidates.add(l.get(i).getKey());
				}
				c.setCandidates(new ArrayList<String>(prunedCandidates));
			}
		}
	}

	@Override
	protected boolean preDisambiguation() {
		return true;
	}

}
