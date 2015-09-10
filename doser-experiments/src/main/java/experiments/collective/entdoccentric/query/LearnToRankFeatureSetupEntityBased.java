package experiments.collective.entdoccentric.query;

import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.util.Version;

import experiments.collective.entdoccentric.StandardQueryDataObject.EntityObject;
import experiments.collective.entdoccentric.LTR.LTRBooleanQuery;
import experiments.collective.entdoccentric.LTR.LearnToRankClause;
import experiments.collective.entdoccentric.LTR.LearnToRankFuzzyQuery;
import experiments.collective.entdoccentric.LTR.LearnToRankQuery;
import experiments.collective.entdoccentric.LTR.LearnToRankTermQuery;
import experiments.collective.entdoccentric.dpo.EntityToDisambiguate;

/**
 * This class is responsible for feature setup when using the entity based
 * approach. The respective features must be specified in a single method.
 */
public class LearnToRankFeatureSetupEntityBased implements
		LearnToRankFeatureSetup {

	private List<LearnToRankClause> features;

	private LearnToRankQuery query;

	private Analyzer analyzer;
	
	public LearnToRankFeatureSetupEntityBased() {
		this.analyzer = new PositionalPorterStopAnalyzer(Version.LUCENE_41);
	}
	
	public void setMainQuery(LearnToRankQuery query) {
		this.features = new LinkedList<LearnToRankClause>();
		this.query = query;
	}

	public void setSubQueries(EntityObject dataObject) {
		features.add(this.query.add(createFeature1(dataObject), "Feature1", false));
		features.add(this.query.add(createFeature2(dataObject), "Feature2", false));
		features.add(this.query.add(createFeature3(dataObject), "Feature3", false));
		features.add(this.query.add(createFeature4(dataObject), "Feature4", false));
		features.add(this.query.add(createFeature5(dataObject), "Feature5", false));
		features.add(this.query.add(createFeature6(dataObject), "Feature6", false));
		features.add(this.query.add(createFeature7(dataObject), "Feature7", false));
		features.add(this.query.add(createFeature8(dataObject), "Feature8", false));
		features.add(this.query.add(createFeature9(dataObject), "Feature9", false));
		features.add(this.query.add(createFeature10(dataObject), "Feature10", false));
		// features.add(this.query.add(createFeature7(dataObject), "Feature7"));
		// features.add(this.query.add(createFeature8(dataObject), "Feature8"));
		// features.add(this.query.add(createFeature9(dataObject), "Feature9"));
		// features.add(this.query.add(createFeature(dataObject), "Feature10"));


		features.get(0).setWeight(0.0375069f);
		features.get(1).setWeight(0.001f);
		features.get(2).setWeight(0.0238851f);
		features.get(3).setWeight(0.0858324f);
		features.get(4).setWeight(0.0375069f);
		features.get(5).setWeight(0.001f);
		features.get(6).setWeight(0.001f);
		features.get(7).setWeight(0.001f);
		features.get(8).setWeight(0.00513431f);
		features.get(9).setWeight(0.501216f);
		
//		features.get(0).setWeight(0.0915161f);
//		features.get(1).setWeight(0.01771f);
//		features.get(2).setWeight(0.0450872f);
//		features.get(3).setWeight(0.115529f);
//		features.get(4).setWeight(0.0915161f);
//		features.get(5).setWeight(0.01771f);
//		features.get(6).setWeight(-0.0468604f);
//		features.get(7).setWeight(-0.0947746f);
//		features.get(8).setWeight(0.321587f);
//		features.get(9).setWeight(0.379994f);

//		features.get(0).setWeight(1f);
//		features.get(1).setWeight(1f);
//		features.get(2).setWeight(1f);
//		features.get(3).setWeight(1f);
//		features.get(4).setWeight(1f);
//		features.get(5).setWeight(1f);
//		features.get(6).setWeight(1f);
//		features.get(7).setWeight(1f);
//		features.get(8).setWeight(1f);
//		features.get(9).setWeight(1f);
		
		
		// features.get(0).setWeight(0.421661f);
		// features.get(1).setWeight(0.239444f);
		// features.get(2).setWeight(0.0219451f);
		// features.get(3).setWeight(0.155427f);
		// features.get(4).setWeight(0.134472f);
		// features.get(5).setWeight(0.001f);
		// features.get(6).setWeight(0.0870064f);
		// features.get(7).setWeight(0.465076f);

		// features.get(6).setWeight(17.4425f);
		// features.get(7).setWeight(18.5569f);
	}

	/**
	 * Feature 1: cos(Lucene-Score) * sim(t_d, q)
	 * 
	 * @param keyword
	 * @return
	 */

	private Query createFeature1(EntityObject dataObject) {
		String keyword = dataObject.getText();
		DefaultSimilarity defaultSim = new DefaultSimilarity();
		LearnToRankFuzzyQuery fq = new LearnToRankFuzzyQuery(new Term("title",
				keyword), defaultSim);
		return fq;
	}

	/**
	 * Feature 2: cos(Lucene-Score) * sim(a_d, q)
	 * 
	 * @param dataObject
	 * @return
	 */
	private Query createFeature2(EntityObject dataObject) {
		String keyword = dataObject.getText();
		DefaultSimilarity defaultSim = new DefaultSimilarity();
		LearnToRankFuzzyQuery fq = new LearnToRankFuzzyQuery(new Term(
				"description", keyword), defaultSim);
		return fq;
	}

	/**
	 * Feature 3: cos(Lucene-Score) * sim(t_d, q_c)	private Query createFeature1(QueryDataObject dataObject) {
		String keyword = dataObject.getKeyword();
	 * 
	 * @param dataObject
	 * @return
	 */
	private Query createFeature3(EntityObject dataObject) {
		String sentence = dataObject.getContext();
		String[] split = sentence.split(" ");
		LTRBooleanQuery bq = new LTRBooleanQuery();
		DefaultSimilarity defaultSim = new DefaultSimilarity();
		for (int i = 0; i < split.length; i++) {
//			LearnToRankFuzzyQuery fq = new LearnToRankFuzzyQuery(new Term(
//					"title", split[i]), defaultSim);
			 LearnToRankTermQuery fq = new LearnToRankTermQuery(new Term(
			 "title", split[i]), defaultSim);
			bq.add(fq, Occur.SHOULD);
		}
		return bq;
	}

	/**
	 * Feature 4: cos(Lucene-Score) * sim(a_d, q_c)
	 * 
	 * @param dataObject
	 * @return
	 */
	private Query createFeature4(EntityObject dataObject) {
		String sentence = dataObject.getContext();
		String[] split = sentence.split(" ");
		LTRBooleanQuery bq = new LTRBooleanQuery();
		DefaultSimilarity defaultSim = new DefaultSimilarity();
		for (int i = 0; i < split.length; i++) {
//			LearnToRankFuzzyQuery fq = new LearnToRankFuzzyQuery(new Term(
//					"description", usePorterStemmer(split[i])), defaultSim);
			 LearnToRankTermQuery fq = new LearnToRankTermQuery(new Term(
			 "description", split[i]), defaultSim);
			bq.add(fq, Occur.SHOULD);
		}
		return bq;
	}

	/**
	 * Feature 5: cos(BM25) * sim(t_d, q)
	 * 
	 * @param keyword
	 * @return
	 */
	private Query createFeature5(EntityObject dataObject) {
		String keyword = dataObject.getText();
		BM25Similarity bm25 = new BM25Similarity();
		LearnToRankFuzzyQuery fq = new LearnToRankFuzzyQuery(new Term("title",
				keyword), bm25);
		return fq;
	}

	/**
	 * Feature 6: cos(Bm25) * sim(a_d, q)
	 * 
	 * @param dataObject
	 * @return
	 */
	private Query createFeature6(EntityObject dataObject) {
		String keyword = dataObject.getText();
		BM25Similarity bm25 = new BM25Similarity();
		LearnToRankFuzzyQuery fq = new LearnToRankFuzzyQuery(new Term(
				"description", keyword), bm25);
		return fq;
	}

	/**
	 * Feature 7: cos(BM25) * sim(t_d, q_c)
	 * 
	 * @param dataObject
	 * @return
	 */
	private Query createFeature7(EntityObject dataObject) {
		String sentence = dataObject.getContext();
		String[] split = sentence.split(" ");
		LTRBooleanQuery bq = new LTRBooleanQuery();
		BM25Similarity bm25 = new BM25Similarity();
		for (int i = 0; i < split.length; i++) {
//			LearnToRankFuzzyQuery fq = new LearnToRankFuzzyQuery(new Term(
//					"title", split[i]), bm25);
			 LearnToRankTermQuery fq = new LearnToRankTermQuery(new Term(
			 "title", split[i]), bm25);
			bq.add(fq, Occur.SHOULD);
		}
		return bq;
	}

	/**
	 * Feature 8: cos(BM25) * sim(a_d, q_c)
	 * 
	 * @param dataObject
	 * @return
	 */
	private Query createFeature8(EntityObject dataObject) {
		String sentence = dataObject.getContext();
		String[] split = sentence.split(" ");
		LTRBooleanQuery bq = new LTRBooleanQuery();
		BM25Similarity bm25 = new BM25Similarity();
		for (int i = 0; i < split.length; i++) {
//			LearnToRankFuzzyQuery fq = new LearnToRankFuzzyQuery(new Term(
//					"description", usePorterStemmer(split[i])), bm25);
			 LearnToRankTermQuery fq = new LearnToRankTermQuery(new Term(
			 "description", split[i]), bm25);
			bq.add(fq, Occur.SHOULD);
		}
		return bq;
	}

//	/**
//	 * Feature 1: cos(Lucene-Score) * sim(t_d, q)
//	 * 
//	 * @param keyword
//	 * @return
//	 */
//
//	private Query createFeature1(QueryDataObject dataObject) {
//		String keyword = dataObject.getKeyword();
//		DefaultSimilarity defaultSim = new DefaultSimilarity();
//		LearnToRankFuzzyQuery fq = new LearnToRankFuzzyQuery(new Term("Label",
//				keyword), defaultSim);
//		return fq;
//	}
//
//	/**
//	 * Feature 2: cos(Lucene-Score) * sim(a_d, q)
//	 * 
//	 * @param dataObject
//	 * @return
//	 */
//	private Query createFeature2(QueryDataObject dataObject) {
//		String keyword = dataObject.getKeyword();
//		DefaultSimilarity defaultSim = new DefaultSimilarity();
//		LearnToRankFuzzyQuery fq = new LearnToRankFuzzyQuery(new Term(
//				"Description", keyword), defaultSim);
//		return fq;
//	}
//
//	/**
//	 * Feature 3: cos(Lucene-Score) * sim(t_d, q_c)	private Query createFeature1(QueryDataObject dataObject) {
//		String keyword = dataObject.getKeyword();
//	 * 
//	 * @param dataObject
//	 * @return
//	 */
//	private Query createFeature3(QueryDataObject dataObject) {
//		String sentence = dataObject.getEntityContext();
//		String[] split = sentence.split(" ");
//		LTRBooleanQuery bq = new LTRBooleanQuery();
//		DefaultSimilarity defaultSim = new DefaultSimilarity();
//		for (int i = 0; i < split.length; i++) {
////			LearnToRankFuzzyQuery fq = new LearnToRankFuzzyQuery(new Term(
////					"title", split[i]), defaultSim);
//			 LearnToRankTermQuery fq = new LearnToRankTermQuery(new Term(
//			 "Label", split[i]), defaultSim);
//			bq.add(fq, Occur.SHOULD);
//		}
//		return bq;
//	}
//
//	/**
//	 * Feature 4: cos(Lucene-Score) * sim(a_d, q_c)
//	 * 
//	 * @param dataObject
//	 * @return
//	 */
//	private Query createFeature4(QueryDataObject dataObject) {
//		String sentence = dataObject.getEntityContext();
//		String[] split = sentence.split(" ");
//		LTRBooleanQuery bq = new LTRBooleanQuery();
//		DefaultSimilarity defaultSim = new DefaultSimilarity();
//		for (int i = 0; i < split.length; i++) {
////			LearnToRankFuzzyQuery fq = new LearnToRankFuzzyQuery(new Term(
////					"description", usePorterStemmer(split[i])), defaultSim);
//			 LearnToRankTermQuery fq = new LearnToRankTermQuery(new Term(
//			 "Description", split[i]), defaultSim);
//			bq.add(fq, Occur.SHOULD);
//		}
//		return bq;
//	}
//
//	/**
//	 * Feature 5: cos(BM25) * sim(t_d, q)
//	 * 
//	 * @param keyword
//	 * @return
//	 */
//	private Query createFeature5(QueryDataObject dataObject) {
//		String keyword = dataObject.getKeyword();
//		BM25Similarity bm25 = new BM25Similarity();
//		LearnToRankFuzzyQuery fq = new LearnToRankFuzzyQuery(new Term("Label",
//				keyword), bm25);
//		return fq;
//	}
//
//	/**
//	 * Feature 6: cos(Bm25) * sim(a_d, q)
//	 * 
//	 * @param dataObject
//	 * @return
//	 */
//	private Query createFeature6(QueryDataObject dataObject) {
//		String keyword = dataObject.getKeyword();
//		BM25Similarity bm25 = new BM25Similarity();
//		LearnToRankFuzzyQuery fq = new LearnToRankFuzzyQuery(new Term(
//				"Description", keyword), bm25);
//		return fq;
//	}
//
//	/**
//	 * Feature 7: cos(BM25) * sim(t_d, q_c)
//	 * 
//	 * @param dataObject
//	 * @return
//	 */
//	private Query createFeature7(QueryDataObject dataObject) {
//		String sentence = dataObject.getEntityContext();
//		String[] split = sentence.split(" ");
//		LTRBooleanQuery bq = new LTRBooleanQuery();
//		BM25Similarity bm25 = new BM25Similarity();
//		for (int i = 0; i < split.length; i++) {
////			LearnToRankFuzzyQuery fq = new LearnToRankFuzzyQuery(new Term(
////					"title", split[i]), bm25);
//			 LearnToRankTermQuery fq = new LearnToRankTermQuery(new Term(
//			 "Label", split[i]), bm25);
//			bq.add(fq, Occur.SHOULD);
//		}
//		return bq;
//	}
//
//	/**
//	 * Feature 8: cos(BM25) * sim(a_d, q_c)
//	 * 
//	 * @param dataObject
//	 * @return
//	 */
//	private Query createFeature8(QueryDataObject dataObject) {
//		String sentence = dataObject.getEntityContext();
//		String[] split = sentence.split(" ");
//		LTRBooleanQuery bq = new LTRBooleanQuery();
//		BM25Similarity bm25 = new BM25Similarity();
//		for (int i = 0; i < split.length; i++) {
////			LearnToRankFuzzyQuery fq = new LearnToRankFuzzyQuery(new Term(
////					"description", usePorterStemmer(split[i])), bm25);
//			 LearnToRankTermQuery fq = new LearnToRankTermQuery(new Term(
//			 "Description", split[i]), bm25);
//			bq.add(fq, Occur.SHOULD);
//		}
//		return bq;
//	}
	
	/**
	 * Feature 9: Prior
	 * 
	 * @param dataObject
	 * @return
	 */
	private Query createFeature9(EntityObject dataObject) {
		PriorQuery pq = new PriorQuery();
		return pq;
	}

	/**
	 * Feature 10: SensePrior
	 * 
	 * @param dataObject
	 * @return
	 */
	private Query createFeature10(EntityObject dataObject) {
		SensePriorQuery pq = new SensePriorQuery(dataObject.getText());
		return pq;
	}

	@Override
	public void setSubQueries(EntityToDisambiguate task) {
		// TODO Auto-generated method stub
		
	}
	
//	private String usePorterStemmer(String input) {
//		String nextToken = "";
//		try {
//			TokenStream source = analyzer.tokenStream(null, new StringReader(
//					input));
//			CharTermAttribute termAtt = source
//					.addAttribute(CharTermAttribute.class);
//			source.reset();
//			if (source.incrementToken()) {
//				nextToken = termAtt.toString();
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return nextToken;
//	}
//
//	public void reset() {
//		analyzer.close();
//	}
}
