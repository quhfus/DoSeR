package doser.entitydisambiguation.knowledgebases;

import java.util.ArrayList;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import doser.entitydisambiguation.algorithms.AbstractDisambiguationAlgorithm;
import doser.entitydisambiguation.algorithms.SurfaceForm;
import doser.word2vec.Data;
import doser.word2vec.Doc2VecJsonFormat;
import doser.word2vec.Word2VecJsonFormat;

public abstract class AbstractEntityCentricKBGeneral extends EntityCentricKnowledgeBase {

	private final static Logger logger = LoggerFactory.getLogger(AbstractEntityCentricKBGeneral.class);
	
	private static Cache<String, Float> w2vCache;
	private static Cache<String, Float> d2vCache;

	static {
		w2vCache = CacheBuilder.newBuilder().maximumSize(5000).expireAfterWrite(60, TimeUnit.MINUTES).build();
		d2vCache = CacheBuilder.newBuilder().maximumSize(5000).expireAfterWrite(60, TimeUnit.MINUTES).build();
	}

	public AbstractEntityCentricKBGeneral(String uri, boolean dynamic) {
		super(uri, dynamic);
	}

	public AbstractEntityCentricKBGeneral(String uri, boolean dynamic, Similarity sim) {
		super(uri, dynamic, sim);
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

	public float getDoc2VecSimilarity(String sf, String context, String entity) {
		String key = sf + context + entity;
		Float val = d2vCache.getIfPresent(key);
		if (val != null) {
			return val + 1.0f;
		} else {
			return 0;
		}
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
				logger.error("IOException in "+AbstractEntityCentricKBGeneral.class.getName(), e);
			}
		}
		return map;
	}

	public void precomputeDoc2VecSimilarities(List<SurfaceForm> rep, int contextSize) {
		Doc2VecJsonFormat format = new Doc2VecJsonFormat();
		for (SurfaceForm sf : rep) {
			String context = AbstractDisambiguationAlgorithm.extractContext(
					sf.getPosition(), sf.getContext(), contextSize);

			context = context.toLowerCase();
			context = context.replaceAll("[\\.\\,\\!\\? ]+", " ");

			Data doc = new Data();
			List<String> candidates = sf.getCandidates();
			List<String> toDoCandidates = new ArrayList<String>();
			for(String can : candidates) {
				if(!isInCache(sf.getSurfaceForm(), context, can)) {
					toDoCandidates.add(can);
				}
			}
//			if(!toDoCandidates.isEmpty()) {
				String[] cans = new String[toDoCandidates.size()];
				sf.getCandidates().toArray(cans);
				doc.setCandidates(cans);
				doc.setContext(context);
				doc.setSurfaceForm(sf.getSurfaceForm());
				doc.getQryNr();
				format.addData(doc);
//			}
		}
		JSONArray res = Word2VecJsonFormat.performquery(format, "d2vsim");

		// We obtain the same order of surface forms
		for (int i = 0; i < res.length(); i++) {
			SurfaceForm c = rep.get(i);
			try {
				JSONObject obj = res.getJSONObject(i);
				JSONArray simArray = obj.getJSONArray("sim");
				for (int j = 0; j < simArray.length(); j++) {
					float sim = (float) simArray.getDouble(j);
					String entity = c.getCandidates().get(j);
					d2vCache.put(c.getSurfaceForm() + c.getContext() + entity, sim);
				}
			} catch (JSONException e) {
				logger.error("JSONException in "+AbstractEntityCentricKBGeneral.class.getName(), e);
			}
		}
	}

	private boolean isInCache(String surfaceForm, String context, String entity) {
		String key = surfaceForm + context + entity;
		return d2vCache.getIfPresent(key) != null;
	}

	protected abstract String generateDomainName();
	
	public abstract String generateWord2VecFormatString(String source, String target);

	public abstract String generateWord2VecFormatString(List<String> source, String target);
	
}
