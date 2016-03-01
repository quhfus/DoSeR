package doser.entitydisambiguation.algorithms.collective.general;

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
import doser.entitydisambiguation.backend.AbstractDisambiguationTask;
import doser.entitydisambiguation.backend.DisambiguationTaskCollective;
import doser.entitydisambiguation.dpo.EntityDisambiguationDPO;
import doser.entitydisambiguation.dpo.Response;
import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBase;
import doser.lucene.query.TermQuery;
import doser.entitydisambiguation.knowledgebases.AbstractKnowledgeBase;
import doser.entitydisambiguation.knowledgebases.AbstractEntityCentricKBGeneral;

public class CollectiveDisambiguationGeneralEntities extends AbstractDisambiguationAlgorithm {

	private final static Logger logger = LoggerFactory.getLogger(CollectiveDisambiguationGeneralEntities.class);
	
	private AbstractEntityCentricKBGeneral eckb;
	
	private DisambiguationTaskCollective task;
	
	@Override
	protected boolean checkAndSetInputParameter(AbstractDisambiguationTask task) {
		AbstractKnowledgeBase kb = task.getKb();
		if (!(task instanceof DisambiguationTaskCollective)) {
			return false;
		}
		
		this.eckb = (AbstractEntityCentricKBGeneral) kb;
		this.task = (DisambiguationTaskCollective) task;
		return true;
	}

	@Override
	protected void processAlgorithm() throws IllegalDisambiguationAlgorithmInputException {
//		AdditionalCandidateQuery aq = new AdditionalCandidateQuery(eckb);
		List<EntityDisambiguationDPO> entityList = task.getEntityToDisambiguate();
		Response[] responseArray = new Response[entityList.size()];

		List<SurfaceForm> collectiveRep = new LinkedList<SurfaceForm>();
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
//					SurfaceForm sf = aq.checkAdditionalSurfaceForms(dpo, i);
//					collectiveRep.add(sf);
				}

			} catch (final IOException e) {
				logger.error("JsonException in "+CollectiveDisambiguationGeneralEntities.class.getName(), e);
			}
		}

		CollectiveContextDriverGeneral solver = new CollectiveContextDriverGeneral(responseArray, collectiveRep, eckb);
		solver.solve();

		solver.generateResult();
		List<Response> res = Arrays.asList(responseArray);
		task.setResponse(res);

		eckb.release();
	}

	@Override
	protected boolean preDisambiguation() {
		return true;
	}
	
	private Query createQuery(String sf, EntityCentricKnowledgeBase kb) {
		String surfaceform = sf.toLowerCase();
		TermQuery query = new TermQuery(new Term("UniqueLabel", surfaceform));

		return query;
	}
}
