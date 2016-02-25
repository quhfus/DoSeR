package doser.entitydisambiguation.knowledgebases;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.search.similarities.Similarity;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import doser.word2vec.Word2VecJsonFormat;

public class EntityCentricKBGeneral extends EntityCentricKnowledgeBase {

	private static Cache<String, Float> w2vCache;

	static {
		w2vCache = CacheBuilder.newBuilder().maximumSize(5000).expireAfterWrite(60, TimeUnit.MINUTES)
				.build();
	}

	public EntityCentricKBGeneral(String uri, boolean dynamic) {
		super(uri, dynamic);
	}

	public EntityCentricKBGeneral(String uri, boolean dynamic, Similarity sim) {
		super(uri, dynamic, sim);
	}

	/**
	 * Takes a set of entities as well as a target entity and generates one
	 * string that fits into the word2vec query format used in this class. The
	 * source entities are concatenated and should be compared with the target
	 * entity.
	 *
	 * @param source
	 *            a set of source entities
	 * @param target
	 *            the target entity.
	 * @return String in appropriate word2vec query format
	 */
	public String generateWord2VecFormatString(String source, String target) {
		int c = source.compareToIgnoreCase(target);
		String res = "";
		if (c < 0) {
			res = source + "|" + target;
		} else if (c == 0) {
			res = source + "|" + target;
		} else {
			res = target + "|" + source;
		}
		return res;
	}

	/**
	 * Takes a set of entities as well as a target entity and generates one
	 * string that fits into the word2vec query format used in this class. The
	 * source entities are concatenated and should be compared wit the target
	 * entity.
	 *
	 * @param source
	 *            a set of source entities
	 * @param target
	 *            the target entity.
	 * @return String in appropriate word2vec query format
	 */
	public String generateWord2VecFormatString(List<String> source, String target) {
		StringBuilder builder = new StringBuilder();
		for (String s : source) {
			builder.append(s);
			builder.append("|");
		}
		String src = builder.toString();
		src = src.substring(0, src.length() - 1);
		return src + "|" + target;
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
			Float val = w2vCache.getIfPresent(s);
			if (val != null) {
				map.put(s, val);
			} else {
				neededSimilarities.add(s);
			}
		}
		if (neededSimilarities.size() > 0) {
			Map<String, Float> computedSimilarities = queryWord2VecSimilarities(neededSimilarities);
			w2vCache.putAll(computedSimilarities);
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
	private Map<String, Float> queryWord2VecSimilarities(Set<String> neededSimilarities) {
		Word2VecJsonFormat format = new Word2VecJsonFormat();
		format.setData(neededSimilarities);
		format.setDomain(generateDomainName());
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

	protected String generateDomainName() {
		return "General";
	}
}
