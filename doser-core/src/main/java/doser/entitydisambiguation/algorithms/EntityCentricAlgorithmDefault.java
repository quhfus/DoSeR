package doser.entitydisambiguation.algorithms;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;

import doser.entitydisambiguation.backend.DisambiguationMainService;
import doser.entitydisambiguation.backend.AbstractDisambiguationTask;
import doser.entitydisambiguation.backend.DisambiguationTaskSingle;
import doser.entitydisambiguation.dpo.DisambiguatedEntity;
import doser.entitydisambiguation.dpo.EntityDisambiguationDPO;
import doser.entitydisambiguation.dpo.Response;
import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBase;
import doser.entitydisambiguation.knowledgebases.AbstractKnowledgeBase;
import doser.lucene.features.LuceneFeatures;
import doser.lucene.query.FuzzyLabelSimilarity;
import doser.lucene.query.LearnToRankClause;
import doser.lucene.query.LearnToRankQuery;

public class EntityCentricAlgorithmDefault extends AbstractDisambiguationAlgorithm {

	private EntityCentricKnowledgeBase eckb;
	private DisambiguationTaskSingle task;

	EntityCentricAlgorithmDefault() {
		super();
	}

	@Override
	public boolean checkAndSetInputParameter(AbstractDisambiguationTask task) {
		AbstractKnowledgeBase kb = task.getKb();
		if (!(task instanceof DisambiguationTaskSingle)) {
			return false;
		} else if (!(kb instanceof EntityCentricKnowledgeBase)) {
			return false;
		}
		this.eckb = (EntityCentricKnowledgeBase) kb;
		this.task = (DisambiguationTaskSingle) task;
		return true;
	}

	@Override
	protected boolean preDisambiguation() {
		EntityDisambiguationDPO toDis = task.getEntityToDisambiguate();
		boolean res = true;
		final Pattern pattern = Pattern.compile("^\\d*[.,]?\\d*$");
		final String surfaceForms = toDis.getSelectedText();

		final String str = surfaceForms;
		final Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			res = false;
		}
		if (!res) {
			final List<DisambiguatedEntity> disEntityList = new LinkedList<DisambiguatedEntity>();
			final DisambiguatedEntity disEntity = new DisambiguatedEntity();
			disEntity.setEntityUri("http://dbpedia.org/resource/Number");
			disEntity.setText("Number");
			disEntity
					.setDescription("A number is a mathematical object used to count, label, and measure. In mathematics, the definition of number has been extended over the years to include such numbers "
							+ "as zero, negative numbers, rational numbers, irrational numbers, and complex numbers. Mathematical operations are certain procedures that take one or more numbers as input and"
							+ " produce a number as output. Unary operations take a single input number and produce a single output number. For example, the successor operation adds one to an integer, thus "
							+ "the successor of 4 is 5. Binary operations take two input numbers and produce a single output number. Examples of binary operations include addition, subtraction, "
							+ "multiplication, division, and exponentiation. The study of numerical operations is called arithmetic. A notational symbol that represents a number is called a numeral. "
							+ "In addition to their use in counting and measuring, numerals are often used for labels, for ordering, and for codes. In common usage, the word number can mean the abstract "
							+ "object, the symbol, or the word for the number.");
			disEntity.setConfidence(1);
			disEntityList.add(disEntity);
			Response response = new Response();
			response.setSelectedText(toDis.getSelectedText());
			response.setStartPosition(toDis.getStartPosition());
			response.setDisEntities(disEntityList);
			List<Response> resList = new LinkedList<Response>();
			resList.add(response);
			task.setResponse(resList);
		}
		return res;
	}

	@Override
	public void processAlgorithm() {
		final Query query = createQuery(task.getEntityToDisambiguate(), eckb);
		final IndexSearcher searcher = eckb.getSearcher();
		final IndexReader reader = searcher.getIndexReader();
		EntityDisambiguationDPO dpo = task.getEntityToDisambiguate();
		try {
			final TopDocs top = searcher.search(query, task.getReturnNr());
			final ScoreDoc[] score = top.scoreDocs;
			final List<DisambiguatedEntity> disList = new LinkedList<DisambiguatedEntity>();

			final String[] entityMainLinks = new String[score.length];

			for (int j = 0; j < score.length; j++) {
				final DisambiguatedEntity entity = new DisambiguatedEntity();
				entity.setConfidence(score[j].score);
				final Document doc = reader.document(score[j].doc);
				final String mainLink = doc.get("Mainlink");
				entity.setEntityUri(mainLink);
				entityMainLinks[j] = mainLink;
				entity.setText(doc.get("Label"));
				entity.setDescription(doc.get("Description"));
//				if (task.isRetrieveDocClasses()) {
//					entity.setDoc(doc);
//				}
				disList.add(entity);
			}

//			if (Properties.getInstance().getHBaseStorage()) {
//
//				task.getOutput().storeQuery(dpo.getDocumentId(),
//						dpo.getSelectedText(), dpo.getStartPosition(),
//						entityMainLinks, dpo.getContext());
//			}
			Response response = new Response();
			response.setSelectedText(dpo.getSelectedText());
			response.setStartPosition(dpo.getStartPosition());
			response.setDisEntities(disList);
			List<Response> resList = new LinkedList<Response>();
			resList.add(response);
			task.setResponse(resList);
		} catch (final IOException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
		eckb.release();
	}

	private Query createQuery(EntityDisambiguationDPO dpo,
			EntityCentricKnowledgeBase kb) {
		LearnToRankQuery query = new LearnToRankQuery();
		List<LearnToRankClause> features = new LinkedList<LearnToRankClause>();
		FuzzyLabelSimilarity fuzzyLabelSim = new FuzzyLabelSimilarity();
		DefaultSimilarity defaultSim = new DefaultSimilarity();
		BM25Similarity bm25 = new BM25Similarity();

		// Feature 1
		features.add(query.add(LuceneFeatures.queryStringFuzzy(
				dpo.getSelectedText(), "Label", fuzzyLabelSim, Occur.MUST,
				DisambiguationMainService.MAXCLAUSECOUNT), "Feature1", true));
		// Feature 2
		features.add(query.add(LuceneFeatures.queryStringTerm(
				dpo.getSelectedText(), "Description", defaultSim,
				Occur.SHOULD, DisambiguationMainService.MAXCLAUSECOUNT), "Feature2",
				false));
		// Feature 3
		features.add(query.add(LuceneFeatures.queryStringTerm(dpo.getContext(),
				"Label", defaultSim, Occur.SHOULD,
				DisambiguationMainService.MAXCLAUSECOUNT), "Feature3", false));
		// Feature 4
		features.add(query.add(LuceneFeatures.queryStringTerm(dpo.getContext(),
				"Description", defaultSim, Occur.SHOULD,
				DisambiguationMainService.MAXCLAUSECOUNT), "Feature4", false));
		// Feature 5
		features.add(query.add(LuceneFeatures.queryLabelFuzzy(
				dpo.getSelectedText(), "Label", bm25), "Feature5", false));
		// Feature 6
		features.add(query.add(LuceneFeatures.queryStringTerm(dpo.getContext(),
				"Label", bm25, Occur.SHOULD,
				DisambiguationMainService.MAXCLAUSECOUNT), "Feature6", false));
		// Feature 7
		features.add(query.add(LuceneFeatures.queryStringTerm(dpo.getContext(),
				"Description", bm25, Occur.SHOULD,
				DisambiguationMainService.MAXCLAUSECOUNT), "Feature7", false));
		// Feature 8
		features.add(query.add(
				LuceneFeatures.queryPrior(kb.getFeatureDefinition()),
				"Feature8", false));
		// Feature 9
		features.add(query.add(
				LuceneFeatures.querySensePrior(dpo.getSelectedText(),
						kb.getFeatureDefinition()), "Feature9", false));

		features.get(0).setWeight(0.0524974f);
		features.get(1).setWeight(0.01771f);
		features.get(2).setWeight(0.0615202f);
		features.get(3).setWeight(0.0933433f);
		features.get(4).setWeight(0.0915161f);
		features.get(5).setWeight(-0.0468604f);
		features.get(6).setWeight(-0.0947746f);
		features.get(7).setWeight(0.0423863f);
		features.get(8).setWeight(0.465053f);
		return query;
	}
}
