package doser.lucene.query;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.Weight;

/**
 * LearnToRank clause representing an arbitrary feature query. Additional
 * criterias may be defined later but are not necessary so far.
 * 
 * HashMap featuresValues contains all calculated featuresValues. The HashMap
 * key stores the document number. The Pair integer stores the featureNumber.
 * 
 * The HashMap has to be resetted after each query.
 * 
 */
public class LearnToRankClause {

	class Pair {

		private final int featureNr;

		private final float featureValue;

		Pair(final int docNr, final float featureValue) {
			featureNr = docNr;
			this.featureValue = featureValue;
		}

		public int getDocNr() {
			return featureNr;
		}

		public float getFeatureValue() {
			return featureValue;
		}

	}

	private Weight cweight;

	private final Map<Integer, Float> featureValues;

	private final boolean mustOccur;

	private final String name;

	private Query query;

	private float weight;

	public LearnToRankClause(final Query query, final String name,
			final boolean mustOccur) {
		this.query = query;
		this.name = name;
		weight = 1.0f;
		this.mustOccur = mustOccur;
		featureValues = new HashMap<Integer, Float>();
	}

	public void addFeatureValue(final int docBase, final int docNr,
			final float value) {
		featureValues.put((docBase + docNr), value);
	}

	public void clear() {
		featureValues.clear();
	}

	public double getFeatureValue(final int docId) {
		double val = 0f;
		try {
			val = featureValues.get(docId);
		} catch (final NullPointerException e) {
			val = 0f;
		}
		return val;
	}

	public String getName() {
		return name;
	}

	public Query getQuery() {
		return query;
	}

	public Weight getW() {
		return cweight;
	}

	public float getWeight() {
		return weight;
	}

	public boolean isMustOccur() {
		return mustOccur;
	}

	public void setQuery(final Query query) {
		this.query = query;
	}

	public void setW(final Weight cweight) {
		this.cweight = cweight;
	}

	public void setWeight(final float weight) {
		this.weight = weight;
	}
}
