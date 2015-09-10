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

import experiments.collective.entdoccentric.LTR.LTRBooleanQuery;
import experiments.collective.entdoccentric.LTR.LearnToRankClause;
import experiments.collective.entdoccentric.LTR.LearnToRankFuzzyQuery;
import experiments.collective.entdoccentric.LTR.LearnToRankQuery;
import experiments.collective.entdoccentric.LTR.LearnToRankTermQuery;
import experiments.collective.entdoccentric.StandardQueryDataObject.EntityObject;
import experiments.collective.entdoccentric.dpo.EntityToDisambiguate;

/**
 * This class is responsible for feature setup when using the document centric
 * approach. The respective features must be specified in a single method.
 */

public class LearnToRankFeatureSetupDocumentCentric implements
		LearnToRankFeatureSetup {

	private List<LearnToRankClause> features;

	private LearnToRankQuery query;

	private Analyzer analyzer;

	public LearnToRankFeatureSetupDocumentCentric() {
		this.analyzer = new PositionalPorterStopAnalyzer(Version.LUCENE_41);
	}

	public void setMainQuery(LearnToRankQuery query) {
		this.features = new LinkedList<LearnToRankClause>();
		this.query = query;
	}

	public void setSubQueries(EntityObject dataObject) {
		features.add(this.query.add(createFeature1(dataObject), "Feature1",
				true));
		features.add(this.query.add(createFeature2(dataObject), "Feature2",
				true));
		features.add(this.query.add(createFeature3(dataObject), "Feature3",
				false));
		features.add(this.query.add(createFeature4(dataObject), "Feature4",
				false));
		// features.add(this.query.add(createFeature5(dataObject), "Feature5"));
		// features.add(this.query.add(createFeature6(dataObject), "Feature6"));
		// features.add(this.query.add(createFeature7(dataObject), "Feature7"));
		// features.add(this.query.add(createFeature8(dataObject), "Feature8"));

		features.get(0).setWeight(0.0056836f);
		features.get(1).setWeight(0.0305069f);
		features.get(2).setWeight(0.117543f);
		features.get(3).setWeight(0.365259f);
		
//		features.get(0).setWeight(0.018896f);
//		features.get(1).setWeight(0.0301477f);
//		features.get(2).setWeight(0.0951799f);
//		features.get(3).setWeight(0.285818f);
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
		LearnToRankTermQuery fq = new LearnToRankTermQuery(new Term("title",
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
		LearnToRankTermQuery fq = new LearnToRankTermQuery(new Term("abstract",
				keyword), defaultSim);
		return fq;
	}

	/**
	 * Feature 3: cos(Lucene-Score) * sim(t_d, q_c)
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
			LearnToRankTermQuery fq = new LearnToRankTermQuery(new Term(
					"abstract", split[i]), defaultSim);
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
				"abstract", keyword), bm25);
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
			LearnToRankFuzzyQuery fq = new LearnToRankFuzzyQuery(new Term(
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
			LearnToRankFuzzyQuery fq = new LearnToRankFuzzyQuery(new Term(
					"abstract", split[i]), bm25);
			bq.add(fq, Occur.SHOULD);
		}
		return bq;
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
