package doser.entitydisambiguation.algorithms;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import doser.entitydisambiguation.backend.DisambiguationTask;
import doser.entitydisambiguation.backend.DisambiguationTaskSingle;
import doser.entitydisambiguation.dpo.DisambiguatedEntity;
import doser.entitydisambiguation.dpo.EntityDisambiguationDPO;
import doser.entitydisambiguation.dpo.Response;
import doser.entitydisambiguation.knowledgebases.EnCenKBCStable;
import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBaseDefault;
import doser.entitydisambiguation.knowledgebases.KnowledgeBase;
import doser.lucene.features.LuceneFeatures;
import doser.lucene.query.LearnToRankClause;
import doser.lucene.query.LearnToRankQuery;

/**
 * Simple class which only uses sense prior für computer science tables End of
 * code Project to disambiguate 20 percent of table contents
 * 
 * @author Quhfus
 * 
 */
public class EntityCentricAlgorithmCSTable extends DisambiguationAlgorithm {

	private EnCenKBCStable eckb;
	private DisambiguationTaskSingle task;
	
	@Override
	public boolean checkAndSetInputParameter(DisambiguationTask task) {
		KnowledgeBase kb = task.getKb();
		if (!(task instanceof DisambiguationTaskSingle)) {
			return false;
		} else if (!(kb instanceof EnCenKBCStable)) {
			return false;
		}
		this.eckb = (EnCenKBCStable) kb;
		this.task = (DisambiguationTaskSingle) task;
		return true;
	}
	
	@Override
	protected boolean preDisambiguation() {
		return true;
	}

	@Override
	public void processAlgorithm()
			throws IllegalDisambiguationAlgorithmInputException {
		final Query query = createQuery(task.getEntityToDisambiguate(), eckb);
		final IndexSearcher searcher = eckb.getSearcher();
		final IndexReader reader = searcher.getIndexReader();
		EntityDisambiguationDPO dpo = task.getEntityToDisambiguate();
		try {
			final TopDocs top = searcher.search(query, task.getReturnNr());
			final ScoreDoc[] score = top.scoreDocs;

			final List<DisambiguatedEntity> disList = new LinkedList<DisambiguatedEntity>();
			for (int i = 0; i < score.length; i++) {
				final DisambiguatedEntity entity = new DisambiguatedEntity();
				entity.setConfidence(score[i].score);
				final Document doc = reader.document(score[i].doc);
				final String mainLink = doc.get("mainlink");
				if (score[i].score == 0.0f) {
					entity.setEntityUri(null);
				} else {
					entity.setEntityUri(mainLink);
				}
				entity.setText(doc.get("label"));
				entity.setDescription(doc.get("description"));
				disList.add(entity);
				Response response = new Response();
				response.setSelectedText(dpo.getSelectedText());
				response.setPosition(dpo.getPosition());
				response.setDisEntities(disList);
				List<Response> resList = new LinkedList<Response>();
				resList.add(response);
				task.setResponse(resList);
//				if (task.isRetrieveDocClasses()) {
//					entity.setDoc(doc);
//				}
			}

		} catch (IOException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
		eckb.release();
	}

	private Query createQuery(EntityDisambiguationDPO dpo,
			EntityCentricKnowledgeBaseDefault kb) {
		LearnToRankQuery query = new LearnToRankQuery();
		List<LearnToRankClause> features = new LinkedList<LearnToRankClause>();

		// Feature 1
		features.add(query.add(
				LuceneFeatures.querySensePrior(dpo.getSelectedText(),
						kb.getFeatureDefinition()), "Feature1", false));

		features.get(0).setWeight(1f);
		return query;
	}
}
