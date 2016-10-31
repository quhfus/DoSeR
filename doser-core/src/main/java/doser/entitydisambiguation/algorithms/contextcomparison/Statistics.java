package doser.entitydisambiguation.algorithms.contextcomparison;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
import doser.lucene.query.TermQuery;

public class Statistics extends AbstractDisambiguationAlgorithm {

	private final static Logger logger = LoggerFactory.getLogger(Statistics.class);

	public static long contexts = 0;

	public static long candidates = 0;
	public static long surfacef = 0;

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
					System.out.println("Kandidatenlänge: "+l.size());

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

		List<Response> res = Arrays.asList(disambiguate(collectiveRep, responseArray));
		task.setResponse(res);
		eckb.release();
	}

	private Response[] disambiguate(List<SurfaceForm> collectiveRep, Response[] resultList) {
		for (int i = 0; i < collectiveRep.size(); i++) {
			SurfaceForm sf = collectiveRep.get(i);
			Statistics.contexts += sf.getContext().split(" ").length;
			Statistics.candidates += sf.getCandidates().size();
			Response re = new Response();
			List<DisambiguatedEntity> entList = new LinkedList<DisambiguatedEntity>();
			DisambiguatedEntity ent = new DisambiguatedEntity();
			ent.setEntityUri("");
			ent.setText("ToDoText");
			entList.add(ent);
			re.setDisEntities(entList);
			re.setStartPosition(-1);
			re.setSelectedText(sf.getSurfaceForm());
			resultList[i] = re;
		}
		Statistics.surfacef += collectiveRep.size();
		System.out.println("Average Context: " + ((double) Statistics.contexts / (double) Statistics.surfacef));
		System.out.println("Average Candidates: " + ((double) Statistics.candidates / (double) Statistics.surfacef));
		return resultList;
	}

	private Query createQuery(String sf, EntityCentricKBDBpedia kb) {
		String surfaceform = sf.toLowerCase();
		TermQuery query = new TermQuery(new Term("UniqueLabel", surfaceform));

		return query;
	}

	@Override
	protected boolean preDisambiguation() {
		return true;
	}

}
