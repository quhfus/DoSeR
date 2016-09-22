package doser.entitydisambiguation.algorithms.contextcomparison;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;
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

public class SingleTFIDF extends AbstractDisambiguationAlgorithm {

	private final static Logger logger = LoggerFactory.getLogger(SingleTFIDF.class);
	
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
		disambiguate(collectiveRep, responseArray);
		List<Response> res = Arrays.asList(responseArray);
		task.setResponse(res);

		eckb.release();
	}
	
	private void disambiguate(List<SurfaceForm> collectiveRep, Response[] resultList) {
		for (SurfaceForm sf: collectiveRep) {
			String context = extractContext(sf.getPosition(), sf.getContext(), 300);
			String res = queryContext(context, sf.getCandidates());
			for (int i = 0; i < resultList.length; i++) {
				SurfaceForm r = search(i, collectiveRep);
				if (resultList[i] == null && r != null && r.getCandidates().size() == 1) {
					Response re = new Response();
					List<DisambiguatedEntity> entList = new LinkedList<DisambiguatedEntity>();
					DisambiguatedEntity ent = new DisambiguatedEntity();
					ent.setEntityUri(res);
					ent.setText("ToDoText");
					entList.add(ent);
					re.setDisEntities(entList);
					re.setStartPosition(-1);
					re.setSelectedText(r.getSurfaceForm());
					resultList[i] = re;
				}
			}
		}
	}
	
	private String queryContext(String context, List<String> candidates) {
		String topEntity = null;
		BooleanQuery bq = new BooleanQuery();
		for(String s : candidates) {
			TermQuery tq = new TermQuery(new Term("Mainlink", s));
			bq.add(tq, Occur.SHOULD);
		}
		Filter candidateFilter = new QueryWrapperFilter(bq);
		
		BooleanQuery termbq = new BooleanQuery();
		final String[] split = context.split(" ");
		for (final String element : split) {
			TermQuery tq = new TermQuery(new Term("Wikitext", element));
			termbq.add(tq, Occur.SHOULD);
		}
		Query q = new FilteredQuery(termbq, candidateFilter);
		IndexSearcher s = eckb.getSearcher();
		s.setSimilarity(new DefaultSimilarity());
		try {
			TopDocs top = s.search(q, 10);
			ScoreDoc[] sd = top.scoreDocs;
			if(sd.length > 0) {
				topEntity = s.getIndexReader().document(sd[0].doc).get("Mainlink");
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		return topEntity;
	}
	
	private Query createQuery(String sf, EntityCentricKBDBpedia kb) {
		String surfaceform = sf.toLowerCase();
		TermQuery query = new TermQuery(new Term("UniqueLabel", surfaceform));

		return query;
	}
	
	private SurfaceForm search(int qryNr, List<SurfaceForm> rep) {
		for (SurfaceForm r : rep) {
			if (r.getQueryNr() == qryNr) {
				return r;
			}
		}
		return null;
	}

	@Override
	protected boolean preDisambiguation() {
		return true;
	}

}
