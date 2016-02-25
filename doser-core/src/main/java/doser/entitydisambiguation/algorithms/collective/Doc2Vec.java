package doser.entitydisambiguation.algorithms.collective;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import doser.entitydisambiguation.algorithms.AbstractDisambiguationAlgorithm;
import doser.entitydisambiguation.algorithms.SurfaceForm;
import doser.word2vec.Data;
import doser.word2vec.Doc2VecJsonFormat;
import doser.word2vec.Word2VecJsonFormat;

/**
 * Intelligent implementation of doc2vec similarities. Reduces the
 * amount of word2vec queries by storing similarities in a hashmap.
 * 
 * @author quh
 *
 */
public class Doc2Vec {

	private Map<String, Float> doc2vecsimilarities;

	private int contextSize;

	public Doc2Vec(List<SurfaceForm> rep, int contextSize) {
		super();
		this.contextSize = contextSize;
		this.computeLocalContextCompatibility(rep);
	}

	public float getDoc2VecSimilarity(String sf, String context, String entity) {
		String key = sf + context + entity;
		if (this.doc2vecsimilarities.containsKey(key)) {
			return this.doc2vecsimilarities.get(key) + 1.0f;
		} else {
			return 0;
		}
	}

	private void computeLocalContextCompatibility(List<SurfaceForm> rep) {
		this.doc2vecsimilarities = new HashMap<String, Float>();
		Doc2VecJsonFormat format = new Doc2VecJsonFormat();
		for (SurfaceForm sf : rep) {
			String context = AbstractDisambiguationAlgorithm.extractContext(
					sf.getPosition(), sf.getContext(), this.contextSize);

			context = context.toLowerCase();
			context = context.replaceAll("[\\.\\,\\!\\? ]+", " ");

			Data doc = new Data();
			String[] candidates = new String[sf.getCandidates().size()];
			sf.getCandidates().toArray(candidates);
			doc.setCandidates(candidates);
			doc.setContext(context);
			doc.setSurfaceForm(sf.getSurfaceForm());
			doc.getQryNr();
			format.addData(doc);
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
					this.doc2vecsimilarities.put(
							c.getSurfaceForm() + c.getContext() + entity, sim);
					// c.setCandidateCompatibility(entity, sim);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}