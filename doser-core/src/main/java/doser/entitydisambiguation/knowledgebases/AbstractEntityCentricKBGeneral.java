package doser.entitydisambiguation.knowledgebases;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
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
import doser.lda.LDAJsonFormat;
import doser.lucene.query.TermQuery;
import doser.word2vec.Data;
import doser.word2vec.Doc2VecJsonFormat;
import doser.word2vec.Word2VecJsonFormat;

public abstract class AbstractEntityCentricKBGeneral extends EntityCentricKnowledgeBase {

	private final static Logger logger = LoggerFactory.getLogger(AbstractEntityCentricKBGeneral.class);

	private static Cache<String, Float> w2vCache;
	private static Cache<String, Float> d2vCache;
	private static Cache<String, Float> ldaCache;

	static {
		w2vCache = CacheBuilder.newBuilder().maximumSize(5000).expireAfterWrite(60, TimeUnit.MINUTES).build();
		d2vCache = CacheBuilder.newBuilder().maximumSize(5000).expireAfterWrite(60, TimeUnit.MINUTES).build();
		ldaCache = CacheBuilder.newBuilder().maximumSize(5000).expireAfterWrite(60, TimeUnit.MINUTES).build();
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

	public float getLDASimilarity(String sf, String context, String entity) {
		String key = sf + context + entity;
		Float val = ldaCache.getIfPresent(key);
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
				logger.error("IOException in " + AbstractEntityCentricKBGeneral.class.getName(), e);
			}
		}
		return map;
	}
	
	public void precomputeDoc2VecSimilarities(List<SurfaceForm> rep, int contextSize) {
		Doc2VecJsonFormat format = new Doc2VecJsonFormat();
		for (SurfaceForm sf : rep) {
			String context = AbstractDisambiguationAlgorithm.extractContext(
					sf.getPosition(), sf.getContext(), contextSize);

//			context = context.toLowerCase();
//			context = context.replaceAll("[\\.\\,\\!\\? ]+", " ");
			context = analyze(context);

			Data doc = new Data();
			List<String> candidates = sf.getCandidates();
			List<String> toDoCandidates = new ArrayList<String>();
			for(String can : candidates) {
				if(!isInCache(sf.getSurfaceForm(), context, can)) {
					toDoCandidates.add(can.replaceAll("http://dbpedia.org/resource/", ""));
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
		JSONArray res = Word2VecJsonFormat.performquery(format, "d2vsim_exp");

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
	

//	public void precomputeDoc2VecSimilarities(List<SurfaceForm> rep, int contextSize) {
//		for (SurfaceForm sf : rep) {
//			Doc2VecJsonFormat format = new Doc2VecJsonFormat();
//			String context = AbstractDisambiguationAlgorithm.extractContext(sf.getPosition(), sf.getContext(),
//					contextSize);
//
//			context = context.toLowerCase();
//			context = context.replaceAll("[\\.\\,\\!\\? ]+", " ");
//
//			Data doc = new Data();
//			List<String> candidates = sf.getCandidates();
//			List<String> toDoCandidates = new ArrayList<String>();
//			HashMap<Integer, String> positionmap = new HashMap<Integer, String>();
//			String[] str = generateWikiText(candidates);
//			for (int i = 0; i < candidates.size(); i++) {
//				String can = candidates.get(i);
//				int counter = 0;
//				if (!isInCache(sf.getSurfaceForm(), context, can)) {
//					positionmap.put(counter, can);
//					toDoCandidates.add(str[i]);
//					counter++;
//				}
//			}
//			String[] cans = new String[toDoCandidates.size()];
//			toDoCandidates.toArray(cans);
//			doc.setCandidates(cans);
//			doc.setContext(context);
//			doc.setSurfaceForm(sf.getSurfaceForm());
//			doc.getQryNr();
//			format.addData(doc);
//			if (candidates.size() > 0) {
//				JSONArray res = Word2VecJsonFormat.performquery(format, "d2vsimtest");
//
//				try {
//					JSONObject obj = res.getJSONObject(0);
//					JSONArray simArray = obj.getJSONArray("sim");
//					for (int j = 0; j < simArray.length(); j++) {
//						float sim = (float) simArray.getDouble(j);
//						String entity = positionmap.get(j);
//						d2vCache.put(sf.getSurfaceForm() + sf.getContext() + entity, sim);
//					}
//				} catch (JSONException e) {
//					logger.error("JSONException in " + AbstractEntityCentricKBGeneral.class.getName(), e);
//				}
//			}
//		}
//	}

	public void precomputeLDASimilarities(List<SurfaceForm> rep, int contextSize) {
		LDAJsonFormat format = new LDAJsonFormat();
		for (SurfaceForm sf : rep) {
			String context = AbstractDisambiguationAlgorithm.extractContext(sf.getPosition(), sf.getContext(),
					contextSize);

			context = context.toLowerCase();
			context = context.replaceAll("[\\.\\,\\!\\? ]+", " ");

			List<String> candidates = sf.getCandidates();
			List<String> toDoCandidates = new ArrayList<String>();

			for (String can : candidates) {
				if (!isInCache(sf.getSurfaceForm(), context, can)) {
					toDoCandidates.add(can);
				}
			}
			String[] cans = generateWikiText(toDoCandidates);
			format.setQuery(context);
			format.setDocuments(cans);

			JSONArray res = Word2VecJsonFormat.performquery(format, "ldasim");
			// System.out.println(res.toString());
			for (int i = 0; i < res.length(); i++) {
				try {
					float sim = (float) res.getDouble(i);

					String entity = toDoCandidates.get(i);
					ldaCache.put(sf.getSurfaceForm() + sf.getContext() + entity, sim);
				} catch (JSONException e) {
					logger.error("JSONException in " + AbstractEntityCentricKBGeneral.class.getName(), e);
				}
			}
		}
	}

	private String[] generateWikiText(List<String> candidates) {
		String[] arr = new String[candidates.size()];
		for (int i = 0; i < arr.length; i++) {
			String uri = candidates.get(i);
			try {
				Term term = new Term("Mainlink", uri);
				TermQuery q = new TermQuery(term);
				TopDocs topdocs = super.getSearcher().search(q, 1);
				ScoreDoc[] scoredocs = topdocs.scoreDocs;
				String text = "";
				if (scoredocs.length > 0) {
					Document doc = super.getSearcher().getIndexReader().document(scoredocs[0].doc);
					text = doc.get("Wikitext");
				}
				if (text != null) {
					arr[i] = text;
				} else {
					arr[i] = "";
				}
				// System.out.println(arr[i]);
			} catch (IOException e) {
				logger.error("IOException in " + AbstractEntityCentricKBGeneral.class.getName(), e);
			}
		}
		return arr;
	}

	private boolean isInCache(String surfaceForm, String context, String entity) {
		String key = surfaceForm + context + entity;
		return d2vCache.getIfPresent(key) != null;
	}
	
	private String analyze(String text) {
		Analyzer ana = new StandardAnalyzer();
		StringBuilder builder = new StringBuilder();
		try {
			TokenStream stream = ana.tokenStream("", new StringReader(text));

			stream.reset();
			while (stream.incrementToken()) {
				builder.append(stream.getAttribute(CharTermAttribute.class).toString()+" ");
//				result.add(stream.getAttribute(CharTermAttribute.class).toString());
			}
		} catch (IOException e) {
			// not thrown b/c we're using a string reader...
		}
		ana.close();
		return builder.toString().trim();
	}

	protected abstract String generateDomainName();

	public abstract String generateWord2VecFormatString(String source, String target);

	public abstract String generateWord2VecFormatString(List<String> source, String target);

}
