package doser.lucene.features;

import java.util.Locale;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.similarities.Similarity;

import doser.lucene.query.LTRBooleanQuery;
import doser.lucene.query.LearnToRankFuzzyQuery;
import doser.lucene.query.LearnToRankTermQuery;
import doser.lucene.query.PriorQuery;
import doser.lucene.query.SensePriorQuery;

public class LuceneFeatures {

	public static Query queryLabelTerm(String keyword, String field,
			Similarity sim) {
		final LearnToRankTermQuery q = new LearnToRankTermQuery(new Term(field,
				keyword.toLowerCase(Locale.US)), sim);
		return q;
	}

	public static Query queryLabelFuzzy(String keyword, String field,
			Similarity sim) {
		final LearnToRankFuzzyQuery q = new LearnToRankFuzzyQuery(new Term(
				field, keyword.toLowerCase(Locale.US)), sim);
		return q;
	}


	public static Query queryStringTerm(String str, String field,
			Similarity sim, Occur occ, int maxclause) {

		final String[] split = str.split(" ");
		final LTRBooleanQuery bquery = new LTRBooleanQuery();
		for (final String element : split) {
			final LearnToRankTermQuery tquery = new LearnToRankTermQuery(
					new Term(field, element.toLowerCase(Locale.US)), sim);
			bquery.add(tquery, occ);
		}
		return bquery;
	}

	public static Query queryStringFuzzy(String str, String field,
			Similarity sim, Occur occ, int maxclause) {

		final String[] split = str.split(" ");
		final LTRBooleanQuery bquery = new LTRBooleanQuery();
		for (final String element : split) {
			final LearnToRankFuzzyQuery tquery = new LearnToRankFuzzyQuery(
					new Term(field, element.toLowerCase(Locale.US)), sim);
			bquery.add(tquery, occ);

		}
		return bquery;
	}


	public static Query queryPrior(IEntityCentricExtFeatures kb) {
		return new PriorQuery(kb);
	}

	public static Query querySensePrior(String str, IEntityCentricExtFeatures kb) {
		return new SensePriorQuery(str, kb);
	}
}
