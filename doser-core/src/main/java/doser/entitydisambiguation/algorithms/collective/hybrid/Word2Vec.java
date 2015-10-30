package doser.entitydisambiguation.algorithms.collective.hybrid;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import doser.word2vec.Word2VecJsonFormat;

public class Word2Vec {

	private Map<String, Float> word2vecsimilarities;

	/**
	 * Constructor
	 */
	public Word2Vec() {
		super();
		this.word2vecsimilarities = new HashMap<String, Float>();
	}

	/**
	 * Takes a set of dbpedia entities as well as a target entity and generates
	 * one string that fits into the word2vec query format used in this class.
	 * The source entities are concatenated and should be compared wit the
	 * target entity.
	 *
	 * @param source
	 *            a set of source entities
	 * @param target
	 *            the target entity.
	 * @return String in appropriate word2vec query format
	 */
	public String generateWord2VecFormatString(String source, String target) {
		String s = source.replaceAll("http://dbpedia.org/resource/", "");
		String t = target.replaceAll("http://dbpedia.org/resource/", "");
		int c = s.compareToIgnoreCase(target);
		String res = "";
		if (c < 0) {
			res = s + "|" + t;
		} else if (c == 0) {
			res = s + "|" + t;
		} else {
			res = t + "|" + s;
		}
		return res;
	}

	/**
	 * Given a set of word2vec queries, this methods retrieves the corresponding
	 * word2vec similarities. If the similarities of a query is not cashed, we
	 * query the word2vec server and compute the similarities from scratch.
	 *
	 * @param set
	 *            A set of query strings
	 * @return Returns a map containing the word2vec similarities of the given
	 *         queries
	 */
	public Map<String, Float> getWord2VecSimilarities(Set<String> set) {
		Map<String, Float> map = new HashMap<String, Float>();
		Set<String> neededSimilarities = new HashSet<String>();
		for (String s : set) {
			if (this.word2vecsimilarities.containsKey(s)) {
				map.put(s, this.word2vecsimilarities.get(s));
			} else {
				neededSimilarities.add(s);
			}
		}
		if (neededSimilarities.size() > 0) {
			Map<String, Float> computedSimilarities = queryWord2VecSimilarities(neededSimilarities);
			this.word2vecsimilarities.putAll(computedSimilarities);
			map.putAll(computedSimilarities);
		}
		return map;
	}

	/**
	 * Retrieves the word2vec similarities of a set of entity pairs
	 *
	 *
	 * @param neededSimilarities
	 *            A set of entity pairs whose word2vec similarities are needed.
	 *            The strings are already in the appropriate format
	 * @return a map that contains the query strings as keys and the
	 *         similarities as values
	 */
	private Map<String, Float> queryWord2VecSimilarities(
			Set<String> neededSimilarities) {
		Word2VecJsonFormat format = new Word2VecJsonFormat();
		format.setData(neededSimilarities);
		JSONArray res = Word2VecJsonFormat.performquery(format, "w2vsim");
		Map<String, Float> map = new HashMap<String, Float>();
		for (int i = 0; i < res.length(); i++) {
			try {
				JSONObject obj = res.getJSONObject(i);
				String ents = obj.getString("ents");
				float sim = (float) obj.getDouble("sim") + 1;
				map.put(ents, sim);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return map;
	}
}
