package experiments.collective.entdoccentric.LTR;

import java.util.HashMap;
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

	private String name;

	private float weight;

	private Query query;

	private HashMap<Integer, Float> featureValues;

	private boolean mustOccur;
	
	private Weight w;

	public LearnToRankClause(Query query, String name, boolean mustOccur) {
		this.query = query;
		this.name = name;
		this.weight = 1.0f;
		this.mustOccur = mustOccur;
		featureValues = new HashMap<Integer, Float>();
	}

	public void setQuery(Query query) {
		this.query = query;
	}

	public Query getQuery() {
		return query;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public String getName() {
		return name;
	}
	
	public boolean isMustOccur() {
		return mustOccur;
	}

	public void clear() {
		featureValues.clear();
	}

	public void addFeatureValue(int docBase, int docNr, float value) {
		featureValues.put((docBase + docNr), value);
	}
	

	public Weight getW() {
		return w;
	}

	public void setW(Weight w) {
		this.w = w;
	}

	public double getFeatureValue(int docId) {
		double val = 0f;
		try {
			val = featureValues.get(docId);
		} catch (NullPointerException e) {
			val = 0f;
		}
		return val;
	}

	class Pair {

		private int featureNr;

		private float featureValue;

		Pair(int docNr, float featureValue) {
			this.featureNr = docNr;
			this.featureValue = featureValue;
		}

		public int getDocNr() {
			return featureNr;
		}

		public float getFeatureValue() {
			return featureValue;
		}

	}
}
